package ru.mai.lessons.rpks.javawebbrowser.web_browser.view.websites_holder;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import ru.mai.lessons.rpks.javawebbrowser.Main;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.controller.Controller;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.view.websites_holder.state.WebsiteHolderRenderState;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.view.websites_holder.state.WebsiteHolderStorageState;

import java.text.MessageFormat;

public class WebsitesHolder extends VBox {

    private final String holderTitle;

    protected final ScrollPane scroller = new ScrollPane();
    protected final TabPane tabs;
    protected final Controller controller;

    public WebsitesHolder(TabPane tabs, String holderTitle, Controller controller) {
        super();

        this.controller = controller;
        this.tabs = tabs;
        this.holderTitle = holderTitle;

        getChildren().add(new Label(holderTitle) {{
            setPadding(new Insets(40));
            setFont(Font.font("Arial", FontWeight.BOLD, 40));
        }});

        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroller.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroller.setMaxHeight(1e20);

        VBox historyBox = new VBox();
        scroller.setContent(historyBox);

        getChildren().add(scroller);

        setAlignment(Pos.TOP_CENTER);
    }

    protected void onItemsExist(WebsiteHolderStorageState state) {
        if (!(state instanceof WebsiteHolderStorageState.ForFavourites websites)) {
            throw new RuntimeException("onItemsExist function of WebsitesHolder class requires ForFavourites state!");
        }

        VBox websitesStorage = (VBox) scroller.getContent();
        websitesStorage.getChildren().clear();
        websites.websites().forEach(website -> {
            Hyperlink link = new Hyperlink(MessageFormat.format(Main.BUNDLE.getString("commons.websitesInWebsiteHolderText"), website.title(), website.location())) {{
                setAlignment(Pos.BASELINE_LEFT);
                setFont(Font.font("Arial", 25));
            }};

            link.setOnMouseClicked((e) -> {
                Tab siteTab = new Tab(website.title());

                WebView web = new WebView();
                web.getEngine().load(website.location());

                siteTab.setContent(web);
                siteTab.setText(website.title());

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

    private void onNoItems(String noItemsText) {
        VBox websitesStorage = (VBox) scroller.getContent();
        websitesStorage.getChildren().clear();
        websitesStorage.getChildren().add(new Label(noItemsText) {{
            setPadding(new Insets(40));
            setFont(Font.font("Arial", FontWeight.BOLD, 25));
        }});
    }

    public void render(WebsiteHolderRenderState state) {
        switch (state) {
            case WebsiteHolderRenderState.ITEMS_EXIST exist -> onItemsExist(exist.websites());
            case WebsiteHolderRenderState.NO_ITEMS notExists -> onNoItems(notExists.noItemsText());
        }
    }

}
