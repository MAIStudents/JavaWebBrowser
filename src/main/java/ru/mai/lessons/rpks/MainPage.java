package ru.mai.lessons.rpks;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class MainPage implements Initializable {
    @FXML
    public TabPane tabPane;
    @FXML
    private WebView webCore;
    @FXML
    public TextField webPath;

    @FXML
    public Button goButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void openNewTab() {
        String url = webPath.getText();

        // Проверяем, что URL не пустой
        if (url == null || url.isBlank()) {
            System.out.println("URL не указан!");
            return;
        }

        try {
            // Проверяем, что URL имеет правильный формат
            new java.net.URL(url).toURI();
        } catch (Exception e) {
            // Если URL некорректен, создаем поисковой запрос
            System.out.println("Некорректный URL, выполняем поиск в Google.");
            url = "https://www.google.com/search?q=" + java.net.URLEncoder.encode(webPath.getText(), StandardCharsets.UTF_8);
        }

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        // Устанавливаем обработчик для проверки загрузки страницы
        String finalUrl = url;
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.FAILED) {
                // Если загрузка не удалась, открываем поисковый запрос
                System.out.println("Не удалось загрузить страницу, выполняем поиск.");
                webEngine.load("https://www.google.com/search?q=" + java.net.URLEncoder.encode(finalUrl, StandardCharsets.UTF_8));
            }
        });

        webEngine.load(url);

        Tab newTab = new Tab(url);
        newTab.setContent(webView);
        String finalUrl1 = url;
        newTab.setOnClosed(event -> System.out.println("Вкладка закрыта: " + finalUrl1));

        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }

}
