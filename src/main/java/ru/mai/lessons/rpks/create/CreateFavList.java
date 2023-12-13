package ru.mai.lessons.rpks.create;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.mai.lessons.rpks.application.Application;

import java.util.List;


public class CreateFavList {

    public static Stage create(String title, List<String> favSites,
                               TextField currentTextField,
                               EventHandler<ActionEvent> clickedAction) {
        var favListStage = new Stage();

        var favTitle = new Label(title);
        favTitle.getStyleClass().add("favListTitle");
        VBox.setMargin(favTitle, new Insets(25.0, 15.0, 15.0, 10.0));
        favTitle.setAlignment(Pos.CENTER);
        favTitle.setPrefWidth(600);

        var favListing = new ListView<String>();
        favListing.getStyleClass().add("favListing");
        favListing.getItems().setAll(favSites);
        VBox.setMargin(favListing, new Insets(10.0, 10.0, 10.0, 10.0));
        favListing.setPrefHeight(600);
        favListing.setOnMouseClicked(mouseEvent -> {
            var site = favListing.getSelectionModel().getSelectedItem();
            if (site != null) {
                currentTextField.setText(site);
                clickedAction.handle(new ActionEvent());
                favListStage.close();
            }
        });

        var favListButton = new Button("ok");
        favListButton.setOnAction(actionEvent -> favListStage.close());
        favListButton.getStyleClass().add("favListButton");
        VBox.setMargin(favListButton, new Insets(10.0, 10.0, 10.0, 10.0));
        favListButton.setPrefWidth(600);
        favListButton.setPrefHeight(40);

        var favListBox = new VBox();
        favListBox.getChildren().add(favTitle);
        favListBox.getChildren().add(favListing);
        favListBox.getChildren().add(favListButton);
        favListBox.getStyleClass().add("favListBox");

        var notifyScene = new Scene(favListBox);
        notifyScene.getStylesheets().add(String.valueOf(Application.class.getResource("favList.css")));

        favListStage.initModality(Modality.APPLICATION_MODAL);
        favListStage.setResizable(false);
        favListStage.setTitle(title);
        favListStage.setScene(notifyScene);
        favListStage.setHeight(600);
        favListStage.setWidth(350);

        return favListStage;
    }
}
