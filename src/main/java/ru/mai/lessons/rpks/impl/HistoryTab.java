package ru.mai.lessons.rpks.impl;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // initialize list view, get on item clicked with observable list
        // initialize table view, get on item clicked
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
