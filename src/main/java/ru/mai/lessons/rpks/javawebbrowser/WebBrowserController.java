package ru.mai.lessons.rpks.javawebbrowser;

import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.geometry.Insets;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class WebBrowserController implements Initializable {

    @FXML
    private TabPane tabPane;
    @FXML
    private WebView currentWebView;
    private List<WebView> webViewList;
    private int currentWebViewIndex;
    @FXML
    private TextField textField;
    private String homePage;
    private WebHistory history;

    private final int TAB_WIDTH = 150;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        webViewList = new ArrayList<>();
        currentWebViewIndex = -1;

        ++currentWebViewIndex;
        webViewList.add(currentWebView);

        homePage = "www.duckduckgo.com";

        changeTabPaneWidth();
        initNewPage();
        loadPage();
    }

    private void initNewPage() {

        Tab newTab = new Tab("New Tab");
        tabPane.getTabs().add(newTab);

        newTab.setOnSelectionChanged((val) -> onTabSelected());


        HBox box = new HBox();

        box.prefHeightProperty().bind(tabPane.heightProperty());
        box.prefWidthProperty().bind(tabPane.widthProperty());

        box.setPadding(new Insets(40, 4, 4, 4));
        newTab.setContent(box);


        currentWebView = new WebView();

        currentWebView.setOnKeyPressed((ae) -> onEnter(ae));
        currentWebView.setOnMouseClicked((me) -> onMouseClicked(me));

        ++currentWebViewIndex;
        webViewList.add(currentWebView);

        currentWebView.prefHeightProperty().bind(box.heightProperty());
        currentWebView.prefWidthProperty().bind(box.widthProperty());

        box.getChildren().add(currentWebView);

        textField.setText(homePage);
    }

    public void loadPage() {

        currentWebView.getEngine().load("http://" + homePage);
        System.out.println("loading");
        history = currentWebView.getEngine().getHistory();
        ObservableList<WebHistory.Entry> historyEntries = history.getEntries();
        if (historyEntries.size() == 0) {
            return;
        }
        textField.setText(historyEntries.get(history.getCurrentIndex()).getUrl());
    }

    public void reloadPage() {
        currentWebView.getEngine().reload();
    }

    public void displayHistory() {

        history = currentWebView.getEngine().getHistory();
        ObservableList<WebHistory.Entry> historyEntries = history.getEntries();

        for (WebHistory.Entry entry : historyEntries) {
            System.out.println(entry.getUrl() + " " + entry.getLastVisitedDate());
        }
    }

    public void pageBack() {

        history = currentWebView.getEngine().getHistory();

        if (history.getCurrentIndex() == 0) {
            return;
        }

        history.go(-1);
        ObservableList<WebHistory.Entry> historyEntries = history.getEntries();
        textField.setText(historyEntries.get(history.getCurrentIndex()).getUrl());
    }

    public void pageForward() {

        history = currentWebView.getEngine().getHistory();
        ObservableList<WebHistory.Entry> historyEntries = history.getEntries();

        if (history.getCurrentIndex() + 1 >= historyEntries.size()) {
            return;
        }

        history.go(1);
        textField.setText(historyEntries.get(history.getCurrentIndex()).getUrl());
    }

    @FXML
    public void onEnter(KeyEvent ae) {
        if (ae.getCode() == KeyCode.ENTER) {
            System.out.println("Key enter pressed");
            waitAndRefreshTextField();
        }
    }

    @FXML
    public void onMouseClicked(MouseEvent me) {
        if (me.isPrimaryButtonDown()) {
            System.out.println("Mouse clicked");
            waitAndRefreshTextField();
        }
    }

    private void waitAndRefreshTextField() {
        System.out.println("waiting loading");
        currentWebView.getEngine().getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends Worker.State> observable,
                 Worker.State oldValue,
                 Worker.State newValue) -> {
                    if (newValue != Worker.State.SUCCEEDED) {
                        return;
                    }

                    refreshTextField();
                });
    }

    private void refreshTextField() {
        System.out.println("refreshing textField");
        history = currentWebView.getEngine().getHistory();
        ObservableList<WebHistory.Entry> historyEntries = history.getEntries();
        if (historyEntries.size() != 0) {
            textField.setText(historyEntries.get(history.getCurrentIndex()).getUrl());
        }
    }

    public void newPage() {
        System.out.println("New page");
        initNewPage();
        loadPage();
        changeTabPaneWidth();
    }

    private void onTabSelected() {
        currentWebView = (WebView) ((HBox) tabPane.getSelectionModel().getSelectedItem()
                .getContent()).getChildren().get(0);

        refreshTextField();

        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        tab.setText(currentWebView.getEngine().getTitle());
    }

    private void changeTabPaneWidth() {

        if (tabPane.getTabs().isEmpty()) {
            tabPane.tabMaxWidthProperty().set(TAB_WIDTH);
        } else {
            tabPane.tabMaxWidthProperty().set(
                Math.min(tabPane.getWidth() / tabPane.getTabs().size(), TAB_WIDTH)
            );
        }

        tabPane.tabMinWidthProperty().set(tabPane.getTabMaxWidth());
    }
}