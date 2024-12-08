package ru.mai.lessons.rpks.Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;

import java.util.List;
import java.util.function.Consumer;

public class LinksViewController {

    @FXML
    private ListView<String> linksListView;

    private Consumer<String> onLinkClicked;


    public void setLinks(List<String> links) {
        linksListView.getItems().setAll(links);
    }


    public void setOnLinkClicked(Consumer<String> onLinkClicked) {
        this.onLinkClicked = onLinkClicked;
        linksListView.setOnMouseClicked(event -> {
            String selectedLink = linksListView.getSelectionModel().getSelectedItem();
            if (selectedLink != null && onLinkClicked != null) {
                onLinkClicked.accept(selectedLink);
            }
        });
    }
}
