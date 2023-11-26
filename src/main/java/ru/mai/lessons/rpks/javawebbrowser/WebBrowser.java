package ru.mai.lessons.rpks.javawebbrowser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;

public class WebBrowser extends Application {

    private static Stage stage;
    private static Scene scn;
    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(WebBrowser.class.getResource("webbrowser-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 720);

        scn = scene;
        stage = primaryStage;
        stage.setTitle("WebBrowser");
        stage.setScene(scene);
        stage.show();
    }

    public static void showStage(VBox box, WebView webView) {
//        try {

            FXMLLoader fxmlLoader = new FXMLLoader(WebBrowser.class.getResource("webbrowser-view.fxml"));

//            Scene scene = new Scene(fxmlLoader.load(), 1080, 720);
//            webView.getEngine().load("www.google.com");


//            stage.setScene(scn);
//            stage.show();

//        }
    }

    public static void main(String[] args) {
        launch();
    }
}