package ru.mai.lessons.rpks.Controllers;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class WindowController implements Initializable {
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

        Tab newTab = new Tab(getDomainName(url)); // Устанавливаем начальное имя вкладки

        // Обновляем название вкладки при изменении адреса
        webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> newTab.setText(getDomainName(newValue)));
        });

        newTab.setContent(webView);
        String finalUrl1 = url;
        newTab.setOnClosed(event -> System.out.println("Вкладка закрыта: " + finalUrl1));

        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }

    /**
     * Получает короткое доменное имя из URL.
     * Например, "https://www.example.com/path" -> "example.com"
     */
    private String getDomainName(String url) {
        try {
            java.net.URL netUrl = new java.net.URL(url);
            String host = netUrl.getHost();
            // Убираем префикс "www." если он есть
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            return host;
        } catch (Exception e) {
            System.out.println("Ошибка получения доменного имени: " + e.getMessage());
            return url; // Если URL некорректен, возвращаем его как есть
        }
    }

}
