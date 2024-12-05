package ru.mai.lessons.rpks;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/mai/lessons/rpks/fxml/MainPage.fxml"));

        Parent root = loader.load();
       // MainPage window = loader.getController();
        Scene scene = new Scene(root);

        stage.setTitle("made by yashelter");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}