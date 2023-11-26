package ru.mai.lessons.rpks.javawebbrowser;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class WebBrowserController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}