package ru.mai.lessons.rpks.controler;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;

public class MainController implements Initializable {
    @FXML
    private WebView webView;

    @FXML
    private TextField textField;

    private WebHistory history;
    private WebEngine engine;
    private String homePage;

    private double webZoom;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        engine = webView.getEngine();
        homePage = "www.google.com";
        textField.setText(homePage);
        webZoom = 1;
        loadPage();
    }

    public void loadPage() {
        engine.load("http://" + textField.getText());
    }

    public void refreshPage() {
        engine.reload();
    }

    public void zoomIn() {
        webZoom += 0.25;
        webView.setZoom(webZoom);
    }
    public void zoomOut() {
        webZoom -=0.25;
        webView.setZoom(webZoom);
    }

    public void displayHistory() {
        history = engine.getHistory();
        ObservableList<WebHistory.Entry> entries = history.getEntries();

        for(WebHistory.Entry entry : entries) {
            System.out.println(entry);
        }
    }


}