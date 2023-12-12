package ru.mai.lessons.rpks.javawebbrowser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WebBrowser extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(WebBrowser.class.getResource("webbrowser-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);

        stage.setTitle("WebBrowser");
        stage.setScene(scene);
        stage.show();

        WebBrowserController controller = fxmlLoader.getController();
        stage.setOnHidden((e) -> controller.shutdown());

    }

    @Override
    public void stop() {
        System.out.println("Stage is closing");
    }

    public static void main(String[] args) {
        launch();
    }
}