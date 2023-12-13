package ru.mai.lessons.rpks.create;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.lang3.tuple.Pair;
import ru.mai.lessons.rpks.application.Application;

public class CreateEditor {
    public static Stage create(double height, double width,
                               String currentHtml, EventHandler<ActionEvent> clickedSave) {
        var editorStage = new Stage();

        var editorTextArea = new TextArea();
        editorTextArea.getStyleClass().add("editorTextArea");
        editorTextArea.setPrefHeight(1000);
        editorTextArea.setPrefWidth(1500);
        VBox.setMargin(editorTextArea, new Insets(10.0, 10.0, 5.0, 10.0));
        if (currentHtml != null) {
            editorTextArea.setText(currentHtml);
        }

        var editorFilename = new TextField();
        editorFilename.getStyleClass().add("editorFilename");
        HBox.setMargin(editorFilename, new Insets(5.0, 5.0, 10.0, 10.0));
        editorFilename.setPromptText("enter filename...");
        editorFilename.setPrefWidth(250);
        editorFilename.setPrefHeight(40);

        var editorButton = new Button("save");
        editorButton.setOnAction(actionEvent -> {
            clickedSave.handle(new ActionEvent(Pair.of(editorTextArea.getText(), editorFilename.getText()),
                    null));
            editorStage.close();
        });
        editorButton.getStyleClass().add("editorButton");
        HBox.setMargin(editorButton, new Insets(5.0, 10.0, 10.0, 5.0));
        editorButton.setPrefWidth(100);
        editorButton.setPrefHeight(30);

        var editorHBox = new HBox();
        editorHBox.setAlignment(Pos.CENTER);
        editorHBox.getChildren().add(editorFilename);
        editorHBox.getChildren().add(editorButton);
        editorHBox.getStyleClass().add("editorHBox");

        var editorVBox = new VBox();
        editorVBox.setAlignment(Pos.CENTER);
        editorVBox.getChildren().add(editorTextArea);
        editorVBox.getChildren().add(editorHBox);
        editorVBox.getStyleClass().add("editorVBox");

        var editorScene = new Scene(editorVBox);
        editorScene.getStylesheets().add(String.valueOf(Application.class.getResource("editor.css")));

        editorStage.initModality(Modality.APPLICATION_MODAL);
        editorStage.setResizable(false);
        editorStage.setTitle("Editor");
        editorStage.setScene(editorScene);
        editorStage.setHeight(height);
        editorStage.setWidth(width);

        return editorStage;
    }
}
