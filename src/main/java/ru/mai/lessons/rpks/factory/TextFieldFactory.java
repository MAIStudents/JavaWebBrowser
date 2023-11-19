package ru.mai.lessons.rpks.factory;

import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class TextFieldFactory {
    public static TextField create(double width, double height, Insets insets, String idTextField) {
        TextField textField = new TextField();

        textField.setMinWidth(width);
        textField.setMaxWidth(width);
        textField.setMinHeight(height);
        textField.setMaxHeight(height);
        textField.setId(idTextField);
        HBox.setMargin(textField, insets);

        return textField;
    }
}
