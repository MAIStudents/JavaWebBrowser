package ru.mai.lessons.rpks.javawebbrowser.web_browser.view.settings;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.mai.lessons.rpks.javawebbrowser.Main;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.controller.Controller;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.model.single_tab.SingleTab;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.view.history_website_holder.HistoryWebsitesHolder;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.view.websites_holder.WebsitesHolder;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class SettingsView extends VBox {

    private final Label settingsTitle;
    private final Button history;
    private final Button favourites;
    private final Button createWebPage;
    private final Label disableHistoryTitle;
    private final CheckBox disableAllSites;
    private final TextArea sitesToDisable;

    private WebsitesHolder favouritesHolder = null;
    private WebsitesHolder historyHolder = null;

    private boolean isHistoryDisabled = false;

    public SettingsView(TabPane tabs, SingleTab singleTab, Controller controller) {
        super();

        this.settingsTitle = new Label(Main.BUNDLE.getString("settings.title")) {{
            setPadding(new Insets(20));
            setFont(Font.font("Arial", FontWeight.BOLD, 40));
        }};

        history = new Button(Main.BUNDLE.getString("settings.history")) {{
            setPrefWidth(300);
            setFont(Font.font("Arial", 24));
        }};

        history.setOnMouseClicked((e) -> {
            int historyTabId;
            if ((historyTabId = singleTab.singleTabExists(Main.BUNDLE.getString("settings.history"))) == SingleTab.NOT_OPENED) {
                Tab historyTab = new Tab(Main.BUNDLE.getString("settings.history"));

                historyHolder = new HistoryWebsitesHolder(tabs, Main.BUNDLE.getString("settings.history"), controller);

                historyTab.setContent(historyHolder);
                tabs.getTabs().add(historyTab);
                tabs.getSelectionModel().selectLast();
            } else {
                tabs.getSelectionModel().select(historyTabId);
            }
        });

        favourites = new Button(Main.BUNDLE.getString("settings.favourites")) {{
            setPrefWidth(300);
            setFont(Font.font("Arial", 24));
        }};

        favourites.setOnMouseClicked((e) -> {
            int favouritesTabId;
            if ((favouritesTabId = singleTab.singleTabExists(Main.BUNDLE.getString("settings.favourites"))) == SingleTab.NOT_OPENED) {
                Tab favouritesTab = new Tab(Main.BUNDLE.getString("settings.favourites"));

                favouritesHolder = new WebsitesHolder(tabs, Main.BUNDLE.getString("favourites.favourites"), controller);

                favouritesTab.setContent(favouritesHolder);
                tabs.getTabs().add(favouritesTab);
                tabs.getSelectionModel().selectLast();
            } else {
                tabs.getSelectionModel().select(favouritesTabId);
            }
        });

        createWebPage = new Button(Main.BUNDLE.getString("settings.createWebPage")) {{
            setPrefWidth(300);
            setFont(Font.font("Arial", 24));
        }};

        createWebPage.setOnMouseClicked((e) -> {
            try {
                Parent root = FXMLLoader.load(Main.class.getResource("html-page-creator.fxml"));
                Stage stage = new Stage();

                stage.initModality(Modality.WINDOW_MODAL);
                stage.initOwner(getScene().getWindow());
                stage.setScene(new Scene(root));

                stage.setMinWidth(1050);
                stage.setMinHeight(640);

                stage.setTitle(Main.BUNDLE.getString("creator.title"));
                stage.show();
            } catch (IOException error) {
                error.printStackTrace();
            }
        });

        disableHistoryTitle = new Label(Main.BUNDLE.getString("settings.historyTitle")) {{
            setPadding(new Insets(40, 0, 0, 0));
            setFont(Font.font("Arial", FontWeight.BOLD, 40));
        }};

        disableAllSites = new CheckBox(Main.BUNDLE.getString("settings.disableAllSites")) {{
            setFont(Font.font("Arial", 14));
            setPadding(new Insets(20));
            setSelected(isHistoryDisabled);
        }};

        sitesToDisable = new TextArea() {{
            setPromptText(Main.BUNDLE.getString("settings.sitesToDisable"));
            setMaxWidth(500);
            setMaxHeight(170);
            setFont(Font.font("Arial", 14));
            setDisable(isHistoryDisabled);
        }};

        setAlignment(Pos.TOP_CENTER);
        getChildren().addAll(
                settingsTitle,
                history,
                favourites,
                createWebPage,
                disableHistoryTitle,
                disableAllSites,
                sitesToDisable
        );

        disableAllSites.setOnMouseClicked(mouseEvent -> {
            if (disableAllSites.isSelected()) {
                sitesToDisable.setDisable(true);
                isHistoryDisabled = true;
            } else {
                sitesToDisable.setDisable(false);
                isHistoryDisabled = false;
            }
        });
    }

    public LinkedList<String> getSitesToDisable() {
        return Arrays.stream(sitesToDisable.getText().split("\\n"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .collect(Collectors.toCollection(LinkedList::new));
    }

    public CheckBox getDisabledHistoryCheckBox() {
        return disableAllSites;
    }

    public TextArea getSitesToDisableArea() {
        return sitesToDisable;
    }

    public WebsitesHolder getFavouritesHolder() {
        return favouritesHolder;
    }

    public WebsitesHolder getHistoryHolder() {
        return historyHolder;
    }

}
