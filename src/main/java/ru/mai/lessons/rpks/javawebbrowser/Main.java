package ru.mai.lessons.rpks.javawebbrowser;

import com.google.gson.Gson;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.controller.Controller;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.model.save_module.SaveBlacklistModel;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Locale;
import java.util.ResourceBundle;

public class Main extends javafx.application.Application {

    private static final Locale LOCALE_RU = Locale.forLanguageTag("ru_RU");

    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("Messages_ru_RU", LOCALE_RU);

    private static Controller controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("web-browser-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        controller = fxmlLoader.getController();

        stage.setMinWidth(1050);
        stage.setMinHeight(640);

        stage.setTitle(BUNDLE.getString("root.appTitle"));
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        try (Writer favouritesWriter = new FileWriter((Main.class.getResource("favourites_save.json") + "").substring(6));
             Writer historyWriter = new FileWriter((Main.class.getResource("history_save.json") + "").substring(6));
             Writer blacklistWriter = new FileWriter((Main.class.getResource("blacklist_save.json") + "").substring(6))) {
            Gson gson = new Gson();

            gson.toJson(controller.getFavouriteWebsites(), favouritesWriter);
            gson.toJson(controller.getHistoryWebsites(), historyWriter);

            SaveBlacklistModel blacklistModel = new SaveBlacklistModel(controller.areAllSitesDisabled(), controller.getBlacklistedSites());
            gson.toJson(blacklistModel, blacklistWriter);

            favouritesWriter.flush();
            historyWriter.flush();
            blacklistWriter.flush();
        } catch (IOException error) {
            error.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }

}