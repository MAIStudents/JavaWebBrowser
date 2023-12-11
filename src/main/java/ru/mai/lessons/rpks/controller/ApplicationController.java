package ru.mai.lessons.rpks.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import ru.mai.lessons.rpks.tab.CreateTab;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class ApplicationController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private AnchorPane mainPain;

    @FXML
    private MenuBar menuBar;

    @FXML
    private TabPane tabPane;

    private boolean historySwitch;

    private final ExecutorService executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());


    private Tab CurrentTab() {
        return tabPane.getSelectionModel().getSelectedItem();
    }

    private WebView CurrentWebView() {
        return (WebView) CurrentTab().getContent().lookup("#webView");
    }

    private TextField CurrentTextField() {
        return (TextField) CurrentTab().getContent().lookup("#urlField");
    }

    private String CurrentWebsite() {
        String website = CurrentWebView().getEngine().getLocation();
        if (website == null || website.isEmpty()) {
            return null;
        }
        return website.substring(0, website.length() - 1);
    }


    @FXML
    void EditorCreate(ActionEvent event) {

    }

    @FXML
    void EditorEdit(ActionEvent event) {

    }

    @FXML
    void FavAdd(ActionEvent event) {

    }

    @FXML
    void FavDelete(ActionEvent event) {

    }

    @FXML
    void FavShow(ActionEvent event) {

    }

    @FXML
    void HTMLLoad(ActionEvent event) {

    }

    @FXML
    void HTMLSave(ActionEvent event) {

    }

    @FXML
    void HTMLZip(ActionEvent event) {

    }

    @FXML
    void HistoryDisable(ActionEvent event) {

    }

    @FXML
    void HistoryDisableForSite(ActionEvent event) {

    }

    @FXML
    void HistoryEnable(ActionEvent event) {

    }

    @FXML
    void HistoryEnableForSite(ActionEvent event) {

    }

    @FXML
    void addNewTab() {
        var newTab = CreateTab.create(
                actionEvent -> goBackTab(),
                actionEvent -> goForwardTab(),
                actionEvent -> reloadTab(),
                actionEvent -> closeTab()
        );
        tabPane.getTabs().add(tabPane.getTabs().size() - 1, newTab);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2);
    }

    @FXML
    void closeTab() {

    }

    @FXML
    void goBackTab() {
        if (historySwitch) {
            var history = CurrentWebView().getEngine().getHistory();
            if (history.getCurrentIndex() > 0) {
                history.go(-1);
                var currentUrl = history.getEntries().get(history.getCurrentIndex()).getUrl();
                CurrentTextField().setText(currentUrl);
                setTabTitle(currentUrl);
            }
        }
    }

    @FXML
    void goForwardTab() {
        if (historySwitch) {
            var history = CurrentWebView().getEngine().getHistory();
            if (history.getCurrentIndex() < history.getEntries().size() - 1) {
                history.go(1);
                var currentUrl = history.getEntries().get(history.getCurrentIndex()).getUrl();
                CurrentTextField().setText(currentUrl);
                setTabTitle(currentUrl);
            }
        }
    }

    private void setTabTitle(String currentUrl) {
        String TabTitle;
        if (!currentUrl.isEmpty()) {
            if (currentUrl.length() > 20) {
                TabTitle = currentUrl.substring(0, 12) + "...";
            } else {
                TabTitle = currentUrl;
            }
            CurrentTab().setText(TabTitle);
        } else {
            CurrentTab().setText("Empty url :(");
        }
    }

    @FXML
    void reloadTab() {
        String requestWebsite = CurrentTextField().getText();
        String requestWebsiteHTTPS;
        if (!requestWebsite.contains("https://") && !requestWebsite.isEmpty()) {
            requestWebsiteHTTPS = "https://" + requestWebsite;
        } else {
            requestWebsiteHTTPS = requestWebsite;
        }

        if (CurrentWebsite() != null && Objects.equals(CurrentWebsite(), requestWebsiteHTTPS)) {
            CurrentWebView().getEngine().reload();
        }
        else {
            setTabTitle(requestWebsiteHTTPS);
            executorService.submit(() ->
                    Platform.runLater(() ->
                            CurrentWebView().getEngine().load(requestWebsiteHTTPS)));

        }
    }

    @FXML
    void initialize() {
        historySwitch = true;
    }

    public void onClose() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
