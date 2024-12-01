package ru.mai.lessons.rpks;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;


import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.web.WebHistory;


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

    private List<String> globalHistory;

    public Controller() {
        globalHistory = new ArrayList<>();
    }

    @FXML
    public void initialize() {
        createNewTab();
    }

    public void createNewTab() {
        BrowserTab newTab = new BrowserTab("New Tab");

        newTab.getWebHistory().getEntries().addListener((ListChangeListener<WebHistory.Entry>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (WebHistory.Entry entry : change.getAddedSubList()) {
                        globalHistory.add(entry.getUrl());
                        System.out.println("Global History: " + entry.getUrl());
                    }
                }
            }
        });

        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);

        newTab.setOnClosed(event -> {
            System.out.println("Tab closed: " + newTab.getText());

        });

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
            currentTab.getWebEngine().reload();
        }
    }


    public void loadPage() {
        BrowserTab currentTab = (BrowserTab) tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            currentTab.getWebEngine().load("http://" + textField.getText());
        }
    }

    public List<String> getGlobalHistory() {
        return globalHistory;
    }

    public void displayHistory() {


        for (String url : globalHistory) {
            System.out.println(url);
        }
    }






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
