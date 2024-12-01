package ru.mai.lessons.rpks;

import javafx.application.Platform;
import javafx.scene.control.Tab;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

public class BrowserTab extends Tab {

    private WebView webView;

    private WebEngine webEngine;

    private final HistoryManager historyManager;

    private LocalDateTime startTime;

    private WebHistory webHistory;

    public BrowserTab(String title, HistoryManager manager) {
        super(title);
        webView = new WebView();
        webEngine = webView.getEngine();
        webHistory = webEngine.getHistory();
        historyManager = manager;
        setContent(webView);
        setUpListeners();
    }

    private void setUpListeners() {
        webEngine.locationProperty().addListener((observable, oldLocation, newLocation) -> {
            long timeSpent = System.currentTimeMillis() / 1000 - startTime.getSecond();
            boolean isValid = validateUrl(oldLocation);
            historyManager.addHistoryEntry(new HistoryEntry(oldLocation, startTime, timeSpent, isValid));
        });
        startTime = LocalDateTime.now();

        webEngine.locationProperty().addListener((obs, oldLocation, newLocation) -> {
            if (newLocation != null) {
                setText(newLocation);
            }
        });
    }

    public boolean validateUrl(String url) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(2000);
            connection.connect();
//            connection.disconnect();
//            return connection.getResponseCode() < 400 ||  connection.getResponseCode() == 418;
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void loadPage(String url) {
        if (validateUrl(url)) {
            Platform.runLater(() -> webEngine.load(url));
        } else {
            Platform.runLater(() -> setText("Invalid URL: " + url));
        }
    }

    public void refreshPage() {
        webEngine.reload();
    }



    public WebEngine getWebEngine() {
        return webEngine;
    }

    public WebHistory getWebHistory() {
        return webHistory;
    }

    public void back() {
        Platform.runLater(() -> {
            if (webHistory.getCurrentIndex() > 0) {
                webEngine.getHistory().go(-1);
            }
        });
    }

    public void forward() {
        Platform.runLater(() -> {
            if (webHistory.getCurrentIndex() < webHistory.getEntries().size() - 1) {
                webEngine.getHistory().go(1);
            }
        });
    }

//    public void back() {
//        if (webHistory.getCurrentIndex() > 0) {
//            webHistory.go(-1);
//        }
//    }
//
//    public void forward() {
//        if (webHistory.getCurrentIndex() < webHistory.getEntries().size() - 1) {
//            webHistory.go(1);
//        }
//    }
}
