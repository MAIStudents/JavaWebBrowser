package ru.mai.lessons.rpks;

import javafx.application.Platform;

import javafx.collections.ObservableList;
import javafx.scene.control.Tab;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;


import java.time.Duration;
import java.time.LocalDateTime;


public class BrowserTab extends Tab {

    private final WebView webView;

    private final WebEngine webEngine;

    private final HistoryManager historyManager;

    private LocalDateTime startTime;

    private final WebHistory webHistory;

    private final Controller controller;

    private boolean isIncognito;

    private ObservableList<PrivateEntry> listOfPrivates;



    public BrowserTab(String title, HistoryManager manager, Controller cntrllr, boolean isInco, ObservableList<PrivateEntry> list ) {
        setText(title);
        webView = new WebView();
        webEngine = webView.getEngine();
        webHistory = webEngine.getHistory();
        historyManager = manager;
        controller = cntrllr;
        isIncognito = isInco;
        listOfPrivates = list;
        setContent(webView);
        setUpListeners();
        setOnClosed(event -> updateTimeSpent());
    }

    public void setListOfPrivates(ObservableList<PrivateEntry> listOfPrivates) {
        this.listOfPrivates = listOfPrivates;
    }

    public void setIsIncognito(boolean isIncognito) {
        this.isIncognito = isIncognito;
    }

    public void updateTimeSpent() {
        diffTime();
    }

    private void diffTime() {
        if (startTime != null) {
            Duration timeSpent = Duration.between(startTime, LocalDateTime.now());
            long millis = timeSpent.toMillis();

            String currentUrl = webEngine.getLocation();
            if (currentUrl != null) {
                HistoryEntry currentEntry = historyManager.getHistoryEntryByUrl(currentUrl, startTime);
                if (currentEntry != null) {

                    currentEntry.setTimeSpent(millis);

                    historyManager.updateHistoryEntry(currentEntry);
                }
            }
        }
    }

    private Controller getController() {
        return controller;
    }

    private void setUpListeners() {
        webEngine.titleProperty().addListener((observable, oldTitle, newTitle) -> {
            if (newTitle != null && !newTitle.isEmpty()) {
                setText(newTitle.length() > 20 ? newTitle.substring(0, 20) + "..." : newTitle);
            }
        });

        webEngine.locationProperty().addListener((observable, oldLocation, newLocation) -> {
            if (newLocation != null) {
                if (startTime != null && oldLocation != null) {
                    Duration timeSpent = Duration.between(startTime, LocalDateTime.now());
                    long millis = timeSpent.toMillis();

                    HistoryEntry oldEntry = historyManager.getHistoryEntryByUrl(oldLocation, startTime);
                    if (oldEntry != null ) {
                        oldEntry.setTimeSpent(millis);
                        historyManager.updateHistoryEntry(oldEntry);
                    }
                }
                startTime = LocalDateTime.now();
                updateTabTitle(newLocation);

                if (!isIncognito && !listOfPrivates.contains(new PrivateEntry(newLocation))) {
                    historyManager.addHistoryEntry(new HistoryEntry(newLocation, startTime, 0));
                }


                if (controller != null) {
                    controller.updateCheckBoxState();
                }
            }
        });
    }

    private void updateTabTitle(String url) {
        if (url != null) {
            String truncatedTitle = url.length() > 20 ? url.substring(0, 20) + "..." : url;
            setText(truncatedTitle);
        }
    }

    public void loadPage(String url) {
        Platform.runLater(() -> {
            try {
                diffTime();

                startTime = LocalDateTime.now();
                webEngine.load(url);

            } catch (Exception e) {
                webEngine.load("https://www.google.com/search?q=" + url.replace(" ", "+"));
            }
        });
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

    public String getLocation() {
        return webEngine.getLocation();
    }

}
