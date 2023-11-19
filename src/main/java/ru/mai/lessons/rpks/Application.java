package ru.mai.lessons.rpks;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {
    ControllerApplication controllerApplication;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("web-browser.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 590);
        controllerApplication = fxmlLoader.getController();
        stage.setResizable(false);
        stage.setTitle("Browser");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        controllerApplication.onClose();
    }

    public static void main(String[] args) {
        launch();
    }
}