package ru.mai.lessons.rpks.factory;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

public class ButtonFactory {
    public static Button create(double width, double height, Insets insets, EventHandler<ActionEvent> onButton, String textButton, String idButton) {
        Button button = new Button();

        button.setText(textButton);
        button.setMinWidth(width);
        button.setMaxWidth(width);
        button.setMinHeight(height);
        button.setMaxHeight(height);
        button.setId(idButton);
        HBox.setMargin(button, insets);

        button.setOnAction(onButton);

        return button;
    }
}
