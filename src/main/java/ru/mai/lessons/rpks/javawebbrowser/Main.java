package ru.mai.lessons.rpks.javawebbrowser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("webbrowser-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);

        stage.setTitle("WebBrowser");
        stage.setScene(scene);
        stage.show();

        WebBrowserImplementation controller = fxmlLoader.getController();
        stage.setOnHidden((e) -> controller.shutdown());

    }

    public static void main(String[] args) {
        launch();
    }
}