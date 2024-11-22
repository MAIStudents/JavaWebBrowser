package ru.mai.lessons.rpks;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static final String PATH_TO_FXML_FILE = "main_view.fxml";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(PATH_TO_FXML_FILE));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        stage.setTitle("MPokeZWebBrowser");
        stage.setScene(scene);
        stage.show();

    }
}