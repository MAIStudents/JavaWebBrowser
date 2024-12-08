package ru.mai.lessons.rpks;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import ru.mai.lessons.rpks.Controllers.WindowController;

public class Main extends Application {
    private WindowController windowController;

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/mai/lessons/rpks/fxml/MainPage.fxml"));

        Parent root = loader.load();
        windowController = loader.getController();
        Scene scene = new Scene(root);

        stage.setTitle("made by yashelter");
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            event.consume();
            handleWindowClose(stage);
        });

        stage.show();
    }

    private void handleWindowClose(Stage stage) {
        windowController.saveState();
        Platform.exit();
    }


    public static void main(String[] args) {
        launch(args);
    }
}