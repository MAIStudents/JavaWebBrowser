package ru.mai.lessons.rpks;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import ru.mai.lessons.rpks.logger.Logger;

public class Main extends Application {
    public static final String PATH_TO_FXML_FILE = "main_window.fxml";
    public static final String PATH_TO_LOGO = "i.png";
    private static final Logger log = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(PATH_TO_FXML_FILE));
        Parent root = loader.load();
        Scene scene = new Scene(root);

        stage.setTitle("DesBrowser");
        stage.setScene(scene);
        stage.getIcons().add(new Image(getClass().getResourceAsStream(PATH_TO_LOGO)));
        stage.show();
    }
}