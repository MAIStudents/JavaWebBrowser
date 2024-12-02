package ru.mai.lessons.rpks.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import ru.mai.lessons.rpks.events.Event;
import ru.mai.lessons.rpks.managers.FavSitesManager;
import ru.mai.lessons.rpks.managers.HistoryManager;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Browser {
    @FXML
    public Button history;
    @FXML
    public Button editHtml;
    @FXML
    public Button excludedSites;
    @FXML
    public Button historySwitch;
    @FXML
    public Button favSites;
    @FXML
    public Button createHTML;
    @FXML
    private TabPane tabPane;

    public static boolean isHistoryEnabled = true;
    private static final Logger logger = Logger.getLogger(Browser.class.getName());
    private final String TAB_CONTENT_FILE = "fxml/tabContent.fxml";
    private final String HTML_EDITOR_FILE = "fxml/htmlEditor.fxml";
    private final String WEB_VIEW = "#webView";
    private final String UNNAMED_TAB = "Tab";
    private final String DELETE_BUTTON_TEXT = "-";
    public static final String HISTORY_TAB_NAME = "History";
    public static final String EXCLUDED_SITES_TAB_NAME = "Excluded sites";
    public static final String FAVORITES_TAB_NAME = "Favorites";
    private final String CREATING_TAB_ERROR = "Error occurred while creating new tab";
    private final String OPENING_HTML_EDITOR_ERROR = "Error occurred while opening HTML Editor";
    public static final String LOADING_ERROR = "Error occurred while loading ";
    public static final String SAVING_ERROR = "Error occurred while saving ";

    @FXML
    public void initialize() {
        appendTabToTabPane(createTab());
        Tab addTab = createAddTab();
        tabPane.getTabs().add(addTab);
    }

    public void createHistoryTab() {
        List<HistoryManager.HistoryEntry> entries = HistoryManager.getHistory();
        Collections.reverse(entries);
        ListView<HBox> historyListView = createHistoryListView(entries);

        Button clearHistoryButton = createButton("Clear history", Event.clearHistory(historyListView));

        VBox.setMargin(clearHistoryButton, new Insets(10, 0, 0, 0));
        VBox container = createVBoxContainer(clearHistoryButton, historyListView);

        Tab historyTab = createTab(HISTORY_TAB_NAME, container);

        appendTabToTabPane(historyTab);
    }

    private VBox createVBoxContainer(Button button, ListView<HBox> listView) {
        VBox container = new VBox();
        container.setSpacing(10);
        VBox.setVgrow(listView, Priority.ALWAYS);
        container.getChildren().addAll(button, listView);
        return container;
    }

    private ListView<HBox> createHistoryListView(List<HistoryManager.HistoryEntry> entries) {
        ListView<HBox> historyListView = new ListView<>();
        historyListView.setPrefHeight(Region.USE_COMPUTED_SIZE);
        for (HistoryManager.HistoryEntry entry : entries) {
            historyListView.getItems().add(createHistoryEntry(historyListView, entry));
        }
        return historyListView;
    }

    private HBox createHistoryEntry(ListView<HBox> historyListView, HistoryManager.HistoryEntry entry) {
        HBox hBox = new HBox();
        hBox.setSpacing(10);

        Label urlLabel = createUrlLabel(entry.title(), entry.host(), entry.url());
        Label timeSpentLabel = createTimeSpentLabel(entry.timeSpent());
        Label dateLabel = createDateLabel(entry.timestamp());

        Button excludeButton = createButton("Exclude", Event.excludeSiteFromHistory(entry.host(), historyListView, hBox));
        Button deleteButton = createButton(DELETE_BUTTON_TEXT, Event.removeHistoryEntry(historyListView, hBox, entry));

        hBox.getChildren().addAll(deleteButton, urlLabel, dateLabel, timeSpentLabel, excludeButton);
        return hBox;
    }

    public void openExcludedSites(ActionEvent ignoredActionEvent) {
        ListView<HBox> excludedSitesListView = createExcludedSitesListView(HistoryManager.getExcludedSites());
        Tab tab = createTab(EXCLUDED_SITES_TAB_NAME, excludedSitesListView);

        appendTabToTabPane(tab);
    }

    private ListView<HBox> createExcludedSitesListView(List<String> sites) {
        ListView<HBox> excludedSitesListView = new ListView<>();
        for (String url : sites) {
            HBox hBox = new HBox();
            hBox.setSpacing(10);
            Label urlLabel = new Label(url);
            urlLabel.setTextFill(Color.BLACK);
            Button deleteButton = createButton(DELETE_BUTTON_TEXT, Event.includeSiteToHistory(url, excludedSitesListView, hBox));
            hBox.getChildren().addAll(deleteButton, urlLabel);
            excludedSitesListView.getItems().add(hBox);
        }
        return excludedSitesListView;
    }

    public void openFavSites(ActionEvent ignoredActionEvent) {
        ListView<HBox> sitesListView = createFavSitesListView(FavSitesManager.getSites());
        Tab tab = createTab(FAVORITES_TAB_NAME, sitesListView);
        appendTabToTabPane(tab);
    }

    public ListView<HBox> createFavSitesListView(ArrayList<FavSitesManager.SiteEntry> sites) {
        ListView<HBox> sitesListView = new ListView<>();
        for (FavSitesManager.SiteEntry site : sites) {
            sitesListView.getItems().add(createFavSitesEntry(sitesListView, site));
        }
        return sitesListView;
    }

    public HBox createFavSitesEntry(ListView<HBox> sitesListView, FavSitesManager.SiteEntry site) {
        HBox hBox = new HBox();
        hBox.setSpacing(10);
        Label urlLabel = new Label(site.host() + " - " + site.title());
        urlLabel.setTextFill(Color.BLACK);
        Button excludeButton = createButton(DELETE_BUTTON_TEXT, Event.removeFavSiteEntry(sitesListView, hBox, site));
        hBox.getChildren().addAll(excludeButton, urlLabel);
        return hBox;
    }

    public void switchHistoryStatus(ActionEvent ignoredActionEvent) {
        if (isHistoryEnabled) {
            historySwitch.setText("Enable " + HISTORY_TAB_NAME);
        } else {
            historySwitch.setText("Disable " + HISTORY_TAB_NAME);
        }
        isHistoryEnabled = !isHistoryEnabled;
    }

    public void openHtmlEditor(ActionEvent ignoredActionEvent) {
        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        if (!Objects.equals(tab.getText(), HISTORY_TAB_NAME) && !Objects.equals(tab.getText(), FAVORITES_TAB_NAME) && !Objects.equals(tab.getText(), EXCLUDED_SITES_TAB_NAME)) {
            String html = getHTMLCode(tab.getContent());

            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(HTML_EDITOR_FILE));
            try {
                htmlEditor.open(loader, html, tab, false);
            } catch (IOException e) {
                logger.log(Level.SEVERE, OPENING_HTML_EDITOR_ERROR, e);
            }
        } else {
            logger.log(Level.WARNING, "Trying to open HTML Editor on invalid page");
        }
    }

    private String getHTMLCode(Node tabContent) {
        WebView webView = (WebView) tabContent.lookup(WEB_VIEW);
        WebEngine webEngine = webView.getEngine();
        return (String) webEngine.executeScript("document.documentElement.outerHTML");
    }

    public void createHtmlEditor(ActionEvent ignoredActionEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(HTML_EDITOR_FILE));
        try {
            htmlEditor.open(loader, null, null, true);
        } catch (IOException e) {
            logger.log(Level.SEVERE, OPENING_HTML_EDITOR_ERROR, e);
        }
    }

    private Tab createTab(String title, Node content) {
        Tab tab = new Tab(title, content);
        tab.setClosable(true);
        return tab;
    }

    private Tab createTab(String url) {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(TAB_CONTENT_FILE));
        AnchorPane tabContent = null;
        try {
            tabContent = loader.load();
        } catch (IOException e) {
            logger.log(Level.SEVERE, CREATING_TAB_ERROR, e);
        }
        TabController tabController = loader.getController();

        Tab tab = createTab(UNNAMED_TAB, tabContent);
        tabController.tab = tab;

        if (tabContent != null) {
            WebView webView = (WebView) tabContent.lookup(WEB_VIEW);
            loadWebEngine(webView, url);
        }
        return tab;
    }

    private void loadWebEngine(WebView webView, String url) {
        WebEngine webEngine = webView.getEngine();
        webEngine.load(url);
    }

    private Tab createTab() {
        FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource(TAB_CONTENT_FILE));
        AnchorPane tabContent = null;
        try {
            tabContent = loader.load();
        } catch (IOException e) {
            logger.log(Level.SEVERE, CREATING_TAB_ERROR, e);
        }
        TabController tabController = loader.getController();

        Tab tab = createTab(UNNAMED_TAB, tabContent);
        tabController.tab = tab;
        return tab;
    }

    private void appendTabToTabPane(Tab tab) {
        int addTabIndex = Math.max(0, tabPane.getTabs().size() - 1);
        tabPane.getTabs().add(addTabIndex, tab);
        tabPane.getSelectionModel().select(tab);
    }

    private Button createButton(String text, EventHandler<ActionEvent> event) {
        Button button = new Button(text);
        button.setOnAction(event);
        return button;
    }

    private Label createUrlLabel(String title, String host, String url) {
        Label urlLabel = new Label(title + " - " + host);
        urlLabel.setTextFill(Color.BLACK);
        urlLabel.setStyle("-fx-cursor: hand;");
        urlLabel.setOnMouseClicked(e -> appendTabToTabPane(createTab(url)));
        return urlLabel;
    }

    private Label createTimeSpentLabel(long timeSpent) {
        Label timeSpentLabel = new Label("Time spent: " + formatTimeSpent(timeSpent));
        timeSpentLabel.setTextFill(Color.GRAY);
        return timeSpentLabel;
    }

    private Label createDateLabel(LocalDateTime timeStamp) {
        Label dateLabel = new Label("Date: " + timeStamp.toLocalDate().toString());
        dateLabel.setTextFill(Color.GRAY);
        return dateLabel;
    }

    private String formatTimeSpent(long seconds) {
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private Tab createAddTab() {
        Tab addTab = new Tab("+");
        addTab.setClosable(false);
        addTab.setOnSelectionChanged(Event.createNewTab(addTab, tabPane, this::appendTabToTabPane, this::createTab));
        return addTab;
    }
}
