package ru.mai.lessons.rpks;

import javafx.scene.control.Tab;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

public class BrowserTab extends Tab {

    private WebView webView;

    private WebEngine webEngine;

    private WebHistory webHistory;

    public BrowserTab(String title) {
        super(title);
        webView = new WebView();
        webEngine = webView.getEngine();
        webHistory = webEngine.getHistory();

        setContent(webView);

        webEngine.locationProperty().addListener((obs, oldLocation, newLocation) -> {
            if (newLocation != null) {
                setText(newLocation);
            }
        });
    }

    public WebEngine getWebEngine() {
        return webEngine;
    }

    public WebHistory getWebHistory() {
        return webHistory;
    }

    public void back() {
        if (webHistory.getCurrentIndex() > 0) {
            webHistory.go(-1);
        }
    }

    public void forward() {
        if (webHistory.getCurrentIndex() < webHistory.getEntries().size() - 1) {
            webHistory.go(1);
        }
    }
}
