package ru.mai.lessons.rpks.create;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.mai.lessons.rpks.application.Application;


public class CreateNotification {
    public static Stage create(String title, String text, double width, double height) {
        var notifyStage = new Stage();

        var notifyText = new Label(text);
        notifyText.getStyleClass().add("notificationText");
        HBox.setMargin(notifyText, new Insets(10.0, 10.0, 5.0, 10.0));

        var notifyButton = new Button("ok");
        notifyButton.setOnAction(actionEvent -> notifyStage.close());
        notifyButton.getStyleClass().add("notificationButton");
        HBox.setMargin(notifyButton, new Insets(5.0, 10.0, 5.0, 5.0));

        var notifyBox = new HBox();
        notifyBox.getChildren().add(notifyText);
        notifyBox.getChildren().add(notifyButton);
        notifyBox.getStyleClass().add("notificationBox");

        var notifyScene = new Scene(notifyBox, width, height);
        notifyScene.getStylesheets().add(String.valueOf(Application.class.getResource("notification.css")));

        notifyStage.initModality(Modality.APPLICATION_MODAL);
        notifyStage.setResizable(false);
        notifyStage.setTitle(title);
        notifyStage.setScene(notifyScene);

        return notifyStage;
    }
}
