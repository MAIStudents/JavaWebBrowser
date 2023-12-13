package ru.mai.lessons.rpks;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WebViewExample extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(WebViewExample.class.getResource("/web-browser.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 861.0, 857.0);
        primaryStage.setResizable(false);
        primaryStage.setTitle("Browser");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}