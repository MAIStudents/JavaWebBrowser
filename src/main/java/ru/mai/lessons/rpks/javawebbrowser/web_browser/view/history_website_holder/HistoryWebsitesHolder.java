package ru.mai.lessons.rpks.javawebbrowser.web_browser.view.history_website_holder;

import javafx.geometry.Pos;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.web.WebView;
import ru.mai.lessons.rpks.javawebbrowser.Main;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.controller.Controller;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.model.web_module.WebModule;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.view.websites_holder.WebsitesHolder;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.view.websites_holder.state.WebsiteHolderStorageState;

import java.text.MessageFormat;

public class HistoryWebsitesHolder extends WebsitesHolder {

    public HistoryWebsitesHolder(TabPane tabs, String holderTitle, Controller controller) {
        super(tabs, holderTitle, controller);
    }

    private final WebModule webModule = new WebModule();

    @Override
    protected void onItemsExist(WebsiteHolderStorageState state) {
        if (!(state instanceof WebsiteHolderStorageState.ForHistory websites)) {
            throw new RuntimeException("onItemsExist function of WebsitesHolder class requires ForHistory state!");
        }

        VBox websitesStorage = (VBox) scroller.getContent();
        websitesStorage.getChildren().clear();
        websites.websites().forEach(website -> {

            Hyperlink link = new Hyperlink(MessageFormat.format(Main.BUNDLE.getString("commons.websitesInWebsiteHistoryHolderText"), website.getKey(), website.getValue().title(), website.getValue().location())) {{
                setAlignment(Pos.BASELINE_LEFT);
                setFont(Font.font("Arial", 25));
            }};

            link.setOnMouseClicked((e) -> {
                Tab siteTab = new Tab(website.getValue().title());

                WebView web = new WebView();
                web.getEngine().load(website.getValue().location());

                siteTab.setContent(web);
                siteTab.setText(website.getValue().title());

                tabs.getTabs().add(siteTab);
                tabs.getSelectionModel().selectLast();

                web.getEngine().documentProperty().addListener((observable, oldValue, newValue) -> {
                    controller.onLoadProgressActionPicker(newValue, siteTab, web.getEngine());
                });

                e.consume();
            });

            websitesStorage.getChildren().add(link);
        });
    }

}
