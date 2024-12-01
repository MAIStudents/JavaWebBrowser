package ru.mai.lessons.rpks;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;


import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.web.WebHistory;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


public class Controller {

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField textField;

    @FXML
    private TableView<HistoryEntry> historyTable;

    @FXML
    private TableColumn<HistoryEntry, String> urlColumn;

    @FXML
    private TableColumn<HistoryEntry, LocalDateTime> visitTimeColumn;

    @FXML
    private TableColumn<HistoryEntry, Long> timeSpentColumn;

    @FXML
    private TableColumn<HistoryEntry, Boolean> validColumn;

    private final HistoryManager historyManager;

    public Controller() {
        historyManager = new HistoryManager();
    }

    @FXML
    public void initialize() {
        setupHistoryTable();
        createNewTab();
    }

    private void setupHistoryTable() {
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        visitTimeColumn.setCellValueFactory(new PropertyValueFactory<>("visitTime"));
        timeSpentColumn.setCellValueFactory(new PropertyValueFactory<>("timeSpent"));
        validColumn.setCellValueFactory(new PropertyValueFactory<>("valid"));
        historyTable.setItems(historyManager.getHistory());
    }

    public void createNewTab() {
        BrowserTab newTab = new BrowserTab("New Tab", historyManager);
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }


    public void back() {
        BrowserTab currentTab = (BrowserTab) tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            currentTab.back();
            textField.setText(currentTab.getWebHistory().getEntries().get(currentTab.getWebHistory().getCurrentIndex()).getUrl());
        }

    }


    public void forward() {
        BrowserTab currentTab = (BrowserTab) tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            currentTab.forward();
            textField.setText(currentTab.getWebHistory().getEntries().get(currentTab.getWebHistory().getCurrentIndex()).getUrl());
        }
    }


    public void refreshPage() {
        BrowserTab currentTab = (BrowserTab) tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            currentTab.refreshPage();
        }
    }




    public void loadPage() {
        BrowserTab currentTab = (BrowserTab) tabPane.getSelectionModel().getSelectedItem();
        String url = textField.getText();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }
        if (currentTab != null) {
            currentTab.loadPage(url);
        }
    }



    public void displayHistory() {
        boolean isVisible = historyTable.isVisible();
        historyTable.setVisible(!isVisible);
    }





    //    public String encodeUrl(String url) {
//        try {
//            return URLEncoder.encode(url, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//            return url;
//        }
//    }

//    public List<String> getGlobalHistory() {
//        return globalHistory;
//    }

//    private WebEngine engine;
//
//    private String homePage;
//
//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//        engine = webView.getEngine();
//        homePage = "www.yandex.ru";
//        textField.setText(homePage);
//        loadPage();
//    }
//
//    public void loadPage() {
//        engine.load("http://" + textField.getText());
//    }
//
//    public void refreshPage() {
//        engine.reload();
//    }
//

//
//    public void back() {
//        history = engine.getHistory();
//
//        ObservableList<WebHistory.Entry> entries = history.getEntries();
//
//        history.go(-1);
//        textField.setText(entries.get(history.getCurrentIndex()).getUrl());
//    }
//
//    public void forward() {
//        history = engine.getHistory();
//
//        ObservableList<WebHistory.Entry> entries = history.getEntries();
//
//        history.go(1);
//
//        textField.setText(entries.get(history.getCurrentIndex()).getUrl());
//
//    }
//
//
//
//



}
