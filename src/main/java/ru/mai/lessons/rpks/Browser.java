package ru.mai.lessons.rpks;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

/**
 * Главный класс браузера.
 * Этот класс отвечает за инициализацию и запуск графического интерфейса клиента.
 */
public final class Browser extends Application {
    private double xOffset = 0;
    private double yOffset = 0;

    /**
     * Метод, который запускает браузер. Он загружает FXML-файл,
     * устанавливает контроллер и настраивает сцену.
     *
     * @param stage основной этап приложения
     * @throws IOException если возникает ошибка при загрузке FXML-файла
     */
    @Override
    public void start(final Stage stage) throws IOException {
        FXMLLoader fxmlLoader
            = new FXMLLoader(getClass().getResource("browser.fxml"));

        AnchorPane root = fxmlLoader.load();

        Scene scene = new Scene(root, 1280, 768);

        stage.setTitle("Browser");
        stage.initStyle(StageStyle.TRANSPARENT);

        root.setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        root.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Точка входа в браузер.
     * Запускает браузер.
     *
     * @param args аргументы командной строки
     */
    public static void main(final String[] args) {
        launch(args);
    }
}