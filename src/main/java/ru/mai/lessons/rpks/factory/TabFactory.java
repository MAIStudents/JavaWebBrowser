package ru.mai.lessons.rpks.factory;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;

public class TabFactory {
    private static int idTab;

    public static Tab create(EventHandler<ActionEvent> backOnHistory,
                             EventHandler<ActionEvent> forwardOnHistory,
                             EventHandler<ActionEvent> reloadPage,
                             EventHandler<ActionEvent> loadPage,
                             EventHandler<Event> onCloseTab) {
        AnchorPane newAnchorPane = new AnchorPane();

        newAnchorPane.prefWidth(900);
        newAnchorPane.prefHeight(600);

        WebView mewWebView = WebViewFactory.create(900, 550, 45, 0, 0, 0, "webView");
        HBox newHBox = HBoxFactory.create(900, 50, new Insets(0, 0, 0, 0), "hBox");

        newHBox.getChildren().add(ButtonFactory.create(52, 24, new Insets(10, 0, 10, 20), backOnHistory, "back", "buttonBackOnHistory"));
        newHBox.getChildren().add(ButtonFactory.create(68, 24, new Insets(10, 10, 10, 10), forwardOnHistory, "forward", "buttonForwardOnHistory"));
        newHBox.getChildren().add(ButtonFactory.create(77.6, 24, new Insets(10, 10, 10, 0), reloadPage, "reload", "buttonReloadPage"));
        newHBox.getChildren().add(TextFieldFactory.create(559.2, 24, new Insets(10, 10, 10, 0), "textField"));
        newHBox.getChildren().add(ButtonFactory.create(71.2, 24, new Insets(10, 10, 10, 0), loadPage, "load", "buttonLoadPage"));

        newAnchorPane.getChildren().add(mewWebView);
        newAnchorPane.getChildren().add(newHBox);

        Tab newTab = new Tab("New Tab");
        newTab.setContent(newAnchorPane);
        newTab.setOnCloseRequest(onCloseTab);
        newTab.setId("tab #" + idTab++);

        return newTab;
    }
}
