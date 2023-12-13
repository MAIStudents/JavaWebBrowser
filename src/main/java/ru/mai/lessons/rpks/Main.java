package ru.mai.lessons.rpks;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import ru.mai.lessons.rpks.impl.BrowserController;

import java.io.IOException;

public class Main extends Application {
    public static Stage window;
    public static BorderPane root;

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("impl/fxml/Browser.fxml"));
        BrowserController browserController = new BrowserController();
        fxmlLoader.setController(browserController);
        Scene scene;
        try {
            scene = new Scene(fxmlLoader.load());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        primaryStage.setTitle("Browser");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.setOnCloseRequest(cl -> {
            browserController.saveFavouritesToJson();
            System.exit(0);
        });
        primaryStage.show();
    }
}