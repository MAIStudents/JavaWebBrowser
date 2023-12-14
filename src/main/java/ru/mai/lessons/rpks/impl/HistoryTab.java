package ru.mai.lessons.rpks.impl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import ru.mai.lessons.rpks.helpClasses.HistoryTableViewDataProvider;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HistoryTab extends StackPane implements Initializable {
    private final Logger logger = Logger.getLogger(getClass().getName());

    @FXML
    private TableView<HistoryTableViewDataProvider> historyTableview;

    @FXML
    private TableColumn<HistoryTableViewDataProvider, String> dateCol;

    @FXML
    private TableColumn<HistoryTableViewDataProvider, String> timeCol;

    @FXML
    private TableColumn<HistoryTableViewDataProvider, String> webAddressCol;

    @FXML
    private ListView<String> turnOffHistoryListview;

    BrowserController browserController;
    Tab tab;

    private void historyDisplayData() {
        ObservableList<HistoryTableViewDataProvider> historyList = FXCollections.observableArrayList(browserController.getHistory());

        dateCol.setCellValueFactory(new PropertyValueFactory<>("Date"));
        timeCol.setCellValueFactory(new PropertyValueFactory<>("Time"));
        webAddressCol.setCellValueFactory(new PropertyValueFactory<>("Address"));

        historyTableview.setItems(historyList);
        historyTableview.setOnMouseClicked(event -> {
            browserController.createAndAddNewTab(browserController.getNewTabIndex(), historyTableview.getSelectionModel().getSelectedItem().getAddress());
            browserController.getTabPane().getSelectionModel().select(browserController.getTabPane().getTabs().size() - 2);
        });
    }

    private void ignoredDisplayData() {
        ObservableList<String> toAssign = FXCollections.observableArrayList(browserController.getIgnored());
        turnOffHistoryListview.setItems(toAssign);
        turnOffHistoryListview.setOnMouseClicked(event -> {
            browserController.createAndAddNewTab(browserController.getNewTabIndex(), turnOffHistoryListview.getSelectionModel().getSelectedItem());
            browserController.getTabPane().getSelectionModel().select(browserController.getTabPane().getTabs().size() - 2);
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        historyDisplayData();
        ignoredDisplayData();
    }

    public HistoryTab(BrowserController browserController, Tab tab) {
        this.browserController = browserController;
        this.tab = tab;
        this.tab.setContent(this);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/HistoryTab.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "", ex);
        }
    }

    public Tab getTab() {
        return tab;
    }

}
