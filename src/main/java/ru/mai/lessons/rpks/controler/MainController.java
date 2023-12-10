package ru.mai.lessons.rpks.controler;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private WebView webView;

    @FXML
    private TextField textField;

    private WebEngine engine;
    private String homePage;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        engine = webView.getEngine();
        homePage = "www.google.com";
        textField.setText(homePage);
        loadPage();
    }

    public void loadPage() {
        engine.load("http://" + textField.getText());
    }
}