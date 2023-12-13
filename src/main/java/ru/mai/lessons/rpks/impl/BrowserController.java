package ru.mai.lessons.rpks.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BrowserController extends StackPane implements Initializable {
    private final List<String> favourites = new ArrayList<>();

    public List<String> getFavouritesList() {
        return favourites;
    }

    @FXML
    private TabPane tabPane;
    private final Logger logger = Logger.getLogger(getClass().getName());

    private final String favouritesJsonPath = "ru/mai/lessons/rpks/impl/json/favourites.json";

    public void addToFavouritesList(String url) {
        if (favourites.contains(url)) {
            favourites.remove(url);
            return;
        }
        favourites.add(url);
    }

    public void loadFavouritesList() {
        try {
            Gson gson = new Gson();
            FileReader reader = new FileReader(getClass().getClassLoader().getResource(favouritesJsonPath).getPath());
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> fromGson = gson.fromJson(reader, listType);
            reader.close();
            if (fromGson != null) {
                favourites.addAll(fromGson);
            }
        } catch (FileNotFoundException e) {
            // todo: log
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveFavouritesToJson() {
        try {
            Gson gson = new Gson();
            FileWriter writer = new FileWriter(getClass().getClassLoader().getResource(favouritesJsonPath).getPath());
            Type listType = new TypeToken<List<String>>(){}.getType();
            gson.toJson(favourites, listType, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // todo: log
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tabPane.getTabs().clear();
        loadFavouritesList();
        tabPane.getTabs().add(newTabButton(tabPane));
    }

    private Tab newTabButton(TabPane tabPane) {
        Tab addTab = new Tab("Create Tab"); // You can replace the text with an icon
        addTab.setClosable(false);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == addTab) {
                createAndAddNewTab(getNewTabIndex(),"https://www.google.ru");
                tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2); // Selecting the tab before the button, which is the newly created one
            }
        });
        return addTab;
    }

    public void createTabAndSelect(String url) {
        tabPane.getSelectionModel().select(createAndAddNewTab(getNewTabIndex(), url).getTab());
    }

    public BrowserTabController createAndAddNewTab(int index, String... webSite) {

        //Create
        BrowserTabController webBrowserTab = createNewTab(webSite);

        //Add the tab
        tabPane.getTabs().add(index, webBrowserTab.getTab());

        return webBrowserTab;
    }

    public void createNewFavouritesTab() {
        Tab tab = new Tab("Favourites");
        FavouritesTabController favouritesTab = new FavouritesTabController(this, tab);
        tabPane.getTabs().add(getNewTabIndex(), favouritesTab.getTab());
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2); // Selecting the tab before the button, which is the newly created one
    }

    public BrowserTabController createNewTab(String... webSite) {
        //Create
        Tab tab = new Tab("");
        tab.setClosable(true);
        BrowserTabController webBrowserTab = new BrowserTabController(this, tab, webSite.length == 0 ? null : webSite[0]);

        return webBrowserTab;
    }

    public void closeTabsToTheRight(Tab givenTab) {
        //Return if size <= 1
        if (tabPane.getTabs().size() <= 1)
            return;

        //The start
        int start = tabPane.getTabs().indexOf(givenTab);

        //Remove the appropriate items
        tabPane.getTabs().stream()
                //filter
                .filter(tab -> tabPane.getTabs().indexOf(tab) > start)
                //Collect the all to a list
                .collect(Collectors.toList()).forEach(this::removeTab);

    }

    public void closeTabsToTheLeft(Tab givenTab) {
        //Return if size <= 1
        if (tabPane.getTabs().size() <= 1)
            return;

        //The start
        int start = tabPane.getTabs().indexOf(givenTab);

        //Remove the appropriate items
        tabPane.getTabs().stream()
                //filter
                .filter(tab -> tabPane.getTabs().indexOf(tab) < start)
                //Collect the all to a list
                .collect(Collectors.toList()).forEach(this::removeTab);

    }

    public void removeTab(Tab tab) {
        tabPane.getTabs().remove(tab);
        tab.getOnClosed().handle(null);
    }

    public TabPane getTabPane() {
        return tabPane;
    }

    public int getNewTabIndex() {
        if (tabPane.getTabs().isEmpty()) {
            return 0;
        }
        return tabPane.getTabs().size() - 1;
    }
}
