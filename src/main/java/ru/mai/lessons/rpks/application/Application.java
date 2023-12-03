package ru.mai.lessons.rpks.application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.mai.lessons.rpks.controller.ApplicationController;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;


public class Application extends javafx.application.Application {
    ApplicationController ApplicationController;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(Application.class.getResource("browser.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 750);

        ApplicationController = loader.getController();
        stage.setResizable(false);
        stage.setTitle("Web Browser");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        ApplicationController.onClose();
    }

    public static void main(String[] args) {
        launch();
    }
}
