package ru.mai.lessons.rpks.Controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.Setter;
import org.tinylog.Logger;
import ru.mai.lessons.rpks.Management.HistoryItem;
import javafx.util.Callback;
import java.net.URL;
import java.util.ResourceBundle;

public class HistoryController implements Initializable {
    @Setter
    private WindowController windowController;

    @FXML
    private TableView<HistoryItem> historyTable;

    @FXML
    private TableColumn<HistoryItem, String> urlColumn;

    @FXML
    private TableColumn<HistoryItem, String> entryTimeColumn;

    @FXML
    private TableColumn<HistoryItem, String> exitTimeColumn;

    private final ObservableList<HistoryItem> historyList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        entryTimeColumn.setCellValueFactory(new PropertyValueFactory<>("entryTime"));
        exitTimeColumn.setCellValueFactory(new PropertyValueFactory<>("exitTime"));

        historyTable.setItems(historyList);

        urlColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<HistoryItem, String> call(TableColumn<HistoryItem, String> param) {
                return new TableCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle(null);
                        } else {
                            setText(item);
                            setStyle("-fx-cursor: pointer; -fx-text-fill: blue; -fx-underline: true;");
                            setOnMouseClicked(event -> onUrlClicked(item));
                        }
                    }
                };
            }
        });
    }


    public void addHistoryItem(HistoryItem item) {
        historyList.add(item);
    }


    private void onUrlClicked(String url) {
       Logger.info("URL clicked: " + url);
       windowController.openTabByUrl(url);
    }
}
