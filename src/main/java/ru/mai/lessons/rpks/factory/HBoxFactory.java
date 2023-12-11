package ru.mai.lessons.rpks.factory;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HBoxFactory {
    public static HBox create(double width, double height, Insets insets, String idHBox) {
        HBox hBox = new HBox();

        hBox.prefWidth(width);
        hBox.prefHeight(height);
        VBox.setMargin(hBox, insets);
        hBox.setId(idHBox);

        return hBox;
    }
}
