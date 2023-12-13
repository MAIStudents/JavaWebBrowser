package ru.mai.lessons.rpks.impl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FavouritesTabController extends StackPane implements Initializable {
    @FXML
    private ListView<String> listView;

    @FXML
    private Tab tab;

    private BrowserController browserController;

    private final Logger logger = Logger.getLogger(getClass().getName());
    public FavouritesTabController(BrowserController browserController, Tab tab) {
        this.browserController = browserController;
        this.tab = tab;
        this.tab.setContent(this);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/FavouritesTab.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "", ex);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> toAssign = FXCollections.observableArrayList(browserController.getFavouritesList());
        listView.setItems(toAssign);
        listView.setOnMouseClicked(event -> {
            browserController.createAndAddNewTab(browserController.getNewTabIndex(), listView.getSelectionModel().getSelectedItem());
            browserController.getTabPane().getSelectionModel().select(browserController.getTabPane().getTabs().size() - 2);
        });
    }

    public Tab getTab() {
        return tab;
    }
}
