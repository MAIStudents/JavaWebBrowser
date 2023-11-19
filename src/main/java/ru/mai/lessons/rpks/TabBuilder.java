package ru.mai.lessons.rpks;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;

public class TabBuilder {
    private final EventHandler<ActionEvent> backOnHistory;
    private final EventHandler<ActionEvent> forwardOnHistory;
    private final EventHandler<ActionEvent> reloadPage;
    private final EventHandler<ActionEvent> loadPage;
    private final EventHandler<Event> onCloseTab;
    private int idTab;

    public TabBuilder(EventHandler<ActionEvent> backOnHistory,
                      EventHandler<ActionEvent> forwardOnHistory,
                      EventHandler<ActionEvent> reloadPage,
                      EventHandler<ActionEvent> loadPage,
                      EventHandler<Event> onCloseTab) {
        this.backOnHistory = backOnHistory;
        this.forwardOnHistory = forwardOnHistory;
        this.reloadPage = reloadPage;
        this.loadPage = loadPage;
        this.onCloseTab = onCloseTab;
        idTab = 0;
    }

    public Tab build() {
        AnchorPane newAnchorPane = new AnchorPane();

        newAnchorPane.prefWidth(900);
        newAnchorPane.prefHeight(600);

        WebView mewWebView = createWebView();
        HBox newHBox = createHBox();

        newHBox.getChildren().add(createButtonBackOnHistory(backOnHistory));
        newHBox.getChildren().add(createButtonForwardOnHistory(forwardOnHistory));
        newHBox.getChildren().add(createButtonReloadPage(reloadPage));
        newHBox.getChildren().add(createTextField());
        newHBox.getChildren().add(createButtonLoadPage(loadPage));

        newAnchorPane.getChildren().add(mewWebView);
        newAnchorPane.getChildren().add(newHBox);

        Tab newTab = new Tab("New Tab");
        newTab.setContent(newAnchorPane);
        newTab.setOnCloseRequest(onCloseTab);
        newTab.setId("tab #" + idTab++);

        return newTab;
    }

    private HBox createHBox() {
        HBox hBox = new HBox();

        hBox.prefWidth(900);
        hBox.prefHeight(50);
        VBox.setMargin(hBox, new Insets(0, 0, 0, 0));

        return hBox;
    }

    private WebView createWebView() {
        WebView webView = new WebView();

        webView.prefWidth(900);
        webView.prefHeight(550);
        AnchorPane.setTopAnchor(webView, 45.0);
        AnchorPane.setRightAnchor(webView, 0.0);
        AnchorPane.setBottomAnchor(webView, 0.0);
        AnchorPane.setLeftAnchor(webView, 0.0);
        webView.setId("webView");

        return webView;
    }

    private TextField createTextField() {
        TextField newTextField = new TextField();

        newTextField.setMinWidth(559.2);
        newTextField.setMaxWidth(559.2);
        newTextField.setMinHeight(24);
        newTextField.setMaxHeight(24);
        newTextField.setId("textField");
        HBox.setMargin(newTextField, new Insets(10, 10, 10, 0));

        return newTextField;
    }

    private Button createButtonBackOnHistory(EventHandler<ActionEvent> onButton) {
        Button buttonBackOnHistory = new Button();

        buttonBackOnHistory.setText("back");
        buttonBackOnHistory.setMinWidth(52);
        buttonBackOnHistory.setMaxWidth(52);
        buttonBackOnHistory.setMinHeight(24);
        buttonBackOnHistory.setMaxHeight(24);
        buttonBackOnHistory.setId("buttonBackOnHistory");
        HBox.setMargin(buttonBackOnHistory, new Insets(10, 0, 10, 20));

        buttonBackOnHistory.setOnAction(onButton);

        return buttonBackOnHistory;
    }

    private Button createButtonForwardOnHistory(EventHandler<ActionEvent> onButton) {
        Button buttonForwardOnHistory = new Button();

        buttonForwardOnHistory.setText("forward");
        buttonForwardOnHistory.setMinWidth(68);
        buttonForwardOnHistory.setMaxWidth(68);
        buttonForwardOnHistory.setMinHeight(24);
        buttonForwardOnHistory.setMaxHeight(24);
        buttonForwardOnHistory.setId("buttonForwardOnHistory");
        HBox.setMargin(buttonForwardOnHistory, new Insets(10, 10, 10, 10));

        buttonForwardOnHistory.setOnAction(onButton);

        return buttonForwardOnHistory;
    }

    private Button createButtonReloadPage(EventHandler<ActionEvent> onButton) {
        Button buttonReloadPage = new Button();

        buttonReloadPage.setText("reload");
        buttonReloadPage.setMinWidth(77.6);
        buttonReloadPage.setMaxWidth(77.6);
        buttonReloadPage.setMinHeight(24);
        buttonReloadPage.setMaxHeight(24);
        buttonReloadPage.setId("buttonReloadPage");
        HBox.setMargin(buttonReloadPage, new Insets(10, 10, 10, 0));

        buttonReloadPage.setOnAction(onButton);

        return buttonReloadPage;
    }

    private Button createButtonLoadPage(EventHandler<ActionEvent> onButton) {
        Button buttonLoadPage = new Button();

        buttonLoadPage.setText("load");
        buttonLoadPage.setMinWidth(71.2);
        buttonLoadPage.setMaxWidth(71.2);
        buttonLoadPage.setMinHeight(24);
        buttonLoadPage.setMaxHeight(24);
        buttonLoadPage.setId("buttonLoadPage");
        HBox.setMargin(buttonLoadPage, new Insets(10, 10, 10, 0));

        buttonLoadPage.setOnAction(onButton);

        return buttonLoadPage;
    }
}
