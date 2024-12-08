package ru.mai.lessons.rpks.Holders;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import lombok.Getter;
import org.tinylog.Logger;
import ru.mai.lessons.rpks.Controllers.WindowController;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;

public class TabHolder {
    private Long timeStart;

    private final Deque<String> visitedUrls = new ArrayDeque<>();
    private final Deque<String> topStack = new ArrayDeque<>();

    private final WindowController parent;

    @Getter
    private WebView webView;
    private WebEngine webEngine;

    @Getter
    private Tab tab;

    private final AtomicBoolean onForward = new AtomicBoolean(false);

    public TabHolder(WindowController parent, String url) {
        this.parent = parent;
        loadNewTab(url);
        parent.leftArrow.setDisable(true);
        parent.rightArrow.setDisable(true);
    }

    private void loadNewTab(final String url) {
        Logger.info("Try loading tab with url :" + url);

        if (url == null || url.isBlank()) {
            Logger.info("No url provided");
            return;
        }

        webView = new WebView();
        webEngine = webView.getEngine();


        final ChangeListener<Worker.State> loadStateListener = (obs, oldState, newState) -> {
            if (newState == Worker.State.FAILED) {
                Logger.warn("Searching for a page");
                webEngine.load("https://www.google.com/search?q=" + java.net.URLEncoder.encode(url, StandardCharsets.UTF_8));
            }
        };

        webEngine.getLoadWorker().stateProperty().addListener(loadStateListener);

        webEngine.load(url);
        tab = new Tab(getDomainName(url));
        timeStart =  System.currentTimeMillis();

        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || oldValue.equals(newValue) || newValue.isEmpty() || newValue.equals("no protocol")) {
                return;
            } else  {
                Long currentTime = System.currentTimeMillis();
                parent.addToHistory(oldValue, timeStart, currentTime - timeStart);
                timeStart = currentTime;
            }
            if (!oldValue.equals(topStack.peek()) && !onForward.get()) {
                parent.leftArrow.setDisable(false);
                clearForward();
                visitedUrls.push(oldValue);
            }
            if (onForward.get()) {
                visitedUrls.push(oldValue);
                parent.leftArrow.setDisable(false);
                onForward.set(false);
            }
            Platform.runLater(() -> tab.setText(getDomainName(newValue)));
            parent.colorPrivateButton();
            parent.colorLikeButton();
            Logger.info("Set new name of tab " + getDomainName(newValue));
        });

        tab.setContent(webView);
        tab.setOnClosed(event -> {
                    Long currentTime = System.currentTimeMillis();
                    parent.addToHistory(webEngine.getLocation(),  timeStart, currentTime - timeStart);
                    timeStart = currentTime;
                    parent.closeTab(tab);
                    Logger.info("Вкладка закрыта: " + url);
        });

        parent.getTabPane().getTabs().add(tab);
        parent.getTabPane().getSelectionModel().select(tab);
        Logger.info("New tab loaded " + tab.getText());
    }
    private void updatePage(final String newUrl) {
        if (newUrl == null || newUrl.isBlank()) {
            Logger.warn("Попытка загрузить пустой или недопустимый URL.");
            return;
        }

        Logger.info("Обновление страницы по новому URL: " + newUrl);
        webEngine.load(newUrl);
    }


    public void reloadPage() {
        if (tab == null) {
            Logger.error("Cannot reload: tab is null.");
        }
        if (webEngine.getLocation() != null && !webEngine.getLocation().isBlank()) {
            Logger.info("Reloading page: " + webEngine.getLocation());
            webEngine.reload();
        } else {
            Logger.warn("Cannot reload: no valid URL found.");
        }
    }


    private static String getDomainName(String url) {
        try {
            java.net.URL netUrl = new java.net.URL(url);
            String host = netUrl.getHost();
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            return host;
        } catch (Exception e) {
            Logger.error("Cannot get short domain name: " + e.getMessage());
            return url;
        }
    }

    private void clearForward() {
        while (topStack.peek() != null) {
            visitedUrls.push(topStack.pop());
        }
        parent.rightArrow.setDisable(true);
    }


    public void goBack() {
        topStack.push(webEngine.getLocation());
        parent.rightArrow.setDisable(false);

        updatePage(visitedUrls.pop());

        if (visitedUrls.isEmpty()) {
            parent.leftArrow.setDisable(true);
        }

    }


    public void goForward() {
        onForward.set(true);
        updatePage(topStack.pop());

        if (topStack.isEmpty()) {
            parent.rightArrow.setDisable(true);
        }
    }

    public String getUrl() {
        return webEngine.getLocation();
    }

}
