package ru.mai.lessons.rpks.controllers;

import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import ru.mai.lessons.rpks.managers.FavSitesManager;
import ru.mai.lessons.rpks.managers.HistoryManager;
import ru.mai.lessons.rpks.managers.SavingManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.*;
import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TabController {
    @FXML
    public Button forwardButton;
    @FXML
    public Button reloadButton;
    @FXML
    public Button backButton;
    @FXML
    public WebView webView;
    @FXML
    public Button favButton;
    @FXML
    public Button saveButton;
    @FXML
    private TextField UrlField;

    private long startTime = Instant.now().toEpochMilli();
    private String oldURL = "";
    Tab tab;
    private final String DOCUMENT_TITLE = "document.title";
    private static final Logger logger = Logger.getLogger(TabController.class.getName());

    @FXML
    public void backAction() {
        webView.getEngine().executeScript("history.back()");
    }

    @FXML
    public void forwardAction() {
        webView.getEngine().executeScript("history.forward()");
    }

    @FXML
    public void reloadAction() {
        webView.getEngine().reload();
    }

    @FXML
    public void UrlFieldEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            String url = UrlField.getText();
            url = formatUrl(url);
            webView.getEngine().load(url);
        }
    }

    private String formatUrl(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        return url;
    }

    @FXML
    public void initialize() {
        WebEngine engine = webView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == javafx.concurrent.Worker.State.RUNNING || newValue == Worker.State.SUCCEEDED) {
                updatePage(engine);
            }
        });
    }

    private void updatePage(WebEngine engine) {
        String newUrl = engine.getLocation();
        UrlField.setText(newUrl);
        String title = (String) engine.executeScript(DOCUMENT_TITLE);

        updateHistory(oldURL, title);
        oldURL = newUrl;
        startTime = Instant.now().toEpochMilli();

        if (tab != null) {
            updateTab(title);
        }
    }

    private void updateTab(String title) {
        if (title != null && !title.isEmpty()) {
            tab.setText(title);
        } else {
            tab.setText("Untitled");
        }
    }

    private void updateHistory(String url, String title) {
        long endTime = Instant.now().toEpochMilli();
        long timeSpent = (endTime - startTime) / 1000;
        if (!url.isEmpty()) {
            String host;
            try {
                host = new URL(url).getHost();
                HistoryManager.addEntry(url, title, host, timeSpent);
            } catch (MalformedURLException e) {
                logger.log(Level.SEVERE, "Can't connect to url: " + url, e);
            }
        }
    }

    public void favSite(MouseEvent ignoredMouseEvent) {
        WebEngine engine = webView.getEngine();
        String url = engine.getLocation();
        String title = (String) engine.executeScript(DOCUMENT_TITLE);
        String host;
        try {
            host = new URL(url).getHost();
            FavSitesManager.addEntry(url, title, host);
        } catch (MalformedURLException e) {
            logger.log(Level.SEVERE, "Can't connect to url: " + oldURL, e);
        }
    }

    public void saveAndCompressPage(Stage stage) {
        WebEngine engine = webView.getEngine();
        String htmlContent = (String) engine.executeScript("document.documentElement.outerHTML");
        FileChooser fileChooser = SavingManager.createFileChooser();
        File selectedFile = fileChooser.showSaveDialog(stage);
        if (selectedFile == null) {
            logger.log(Level.INFO, "Saving was canceled");
            return;
        }
        SavingManager.createZip(selectedFile, htmlContent);
        logger.log(Level.INFO, "Page successfully saved here: " + selectedFile.getAbsolutePath());
    }

    public void saveSite(MouseEvent ignoredMouseEvent) {
        Stage stage = (Stage) webView.getScene().getWindow();
        saveAndCompressPage(stage);
    }
}
