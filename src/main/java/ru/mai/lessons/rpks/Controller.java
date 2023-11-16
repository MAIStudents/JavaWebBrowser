package ru.mai.lessons.rpks;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller {
    @FXML
    private WebView webView;
    @FXML
    private TextField textField;

    public void loadPage() {
        String requestText = textField.getText();

        if (!requestText.contains("https://")) {
            webView.getEngine().load("https://" + requestText);
        } else {
            webView.getEngine().load(requestText);
        }
    }

    public void reloadPage() {
        webView.getEngine().reload();
    }
}