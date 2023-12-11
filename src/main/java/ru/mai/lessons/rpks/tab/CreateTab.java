package ru.mai.lessons.rpks.tab;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;


public class CreateTab {

    private static Button createButton(double width, String text, Insets insets, EventHandler<ActionEvent> onAction) {
        var button = new Button();
        button.setText(text);
        button.setMinHeight(40.0);
        button.setMinWidth(width);
        button.setPrefHeight(40.0);
        button.setPrefWidth(width);
        button.setMnemonicParsing(false);
        button.setWrapText(true);
        button.setTextAlignment(TextAlignment.CENTER);
        button.setFont(Font.font("Cooper Black", 22.0));
        HBox.setMargin(button, insets);
        button.setOnAction(onAction);
        button.getStyleClass().add("Button");
        return button;
    }

    private static TextField createUrlField() {
        var urlField = new TextField();
        urlField.setMinHeight(40.0);
        urlField.setMinWidth(800.0);
        urlField.setPrefHeight(40.0);
        urlField.setPrefWidth(800.0);
        urlField.setId("urlField");
        urlField.setPromptText("Start typing...");
        urlField.setFont(Font.font("Cooper Black", 20.0));
        HBox.setMargin(urlField, new Insets(10.0, 10.0, 10.0, 20.0));
        urlField.getStyleClass().add("TextURL");
        return urlField;
    }

    public static Tab create(EventHandler<ActionEvent> goBackTabAction,
                             EventHandler<ActionEvent> goForwardTabAction,
                             EventHandler<ActionEvent> reloadTabAction,
                             EventHandler<Event> onCloseTabAction) {
        var goBackTabButton = createButton(80.0, "<",
                new Insets(10.0, 5.0, 10.0, 10.0), goBackTabAction);
        var goForwardTabButton = createButton(80.0, ">",
                new Insets(10.0, 5.0, 10.0, 5.0), goForwardTabAction);
        var reloadButton = createButton(180.0, "reload",
                new Insets(10.0, 0.0, 10.0, 5.0), reloadTabAction);
        var urlField = createUrlField();
        var hBox = new HBox();
        hBox.setMinHeight(60.0);
        hBox.setMinWidth(1200.0);
        hBox.setPrefHeight(60.0);
        hBox.setPrefWidth(1200.0);
        hBox.getChildren().add(goBackTabButton);
        hBox.getChildren().add(goForwardTabButton);
        hBox.getChildren().add(reloadButton);
        hBox.getChildren().add(urlField);

        var webView = new WebView();
        webView.setMinHeight(605.0);
        webView.setMinWidth(1200.0);
        webView.setPrefHeight(605.0);
        webView.setPrefWidth(1200.0);
        webView.setId("webView");
        webView.setLayoutY(70.0);

        var anchorPane = new AnchorPane();
        anchorPane.setMinHeight(675.0);
        anchorPane.setMinWidth(1200.0);
        anchorPane.setPrefHeight(675.0);
        anchorPane.setPrefWidth(1200.0);
        anchorPane.getChildren().add(webView);
        anchorPane.getChildren().add(hBox);

        var tab = new Tab();
        tab.setText("Oh, hello!");
        tab.setOnCloseRequest(onCloseTabAction);
        tab.setContent(anchorPane);
        tab.getStyleClass().add("Tab");

        return tab;
    }
}
