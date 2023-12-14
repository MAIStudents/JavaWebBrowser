package ru.mai.lessons.rpks.impl;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import ru.mai.lessons.rpks.helpClasses.HistoryTableViewDataProvider;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BrowserController extends StackPane implements Initializable {
    @FXML
    private TabPane tabPane;
    private final Logger logger = Logger.getLogger(getClass().getName());

    // region Favourites
    private final String favouritesJsonPath = "ru/mai/lessons/rpks/impl/json/favourites.json";

    private final List<String> favourites = new ArrayList<>();

    public List<String> getFavouritesList() {
        return favourites;
    }

    public void addToFavouritesList(String url) {
        if (favourites.contains(url)) {
            favourites.remove(url);
            return;
        }
        favourites.add(url);
    }

    private void loadFavouritesList() {
        try {
            Gson gsonFavourites = new Gson();
            FileReader reader = new FileReader(getClass().getClassLoader().getResource(favouritesJsonPath).getPath());
            Type listType = new TypeToken<ArrayList<String>>() {
            }.getType();
            List<String> fromGson = gsonFavourites.fromJson(reader, listType);
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
            Gson gsonFavourites = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(getClass().getClassLoader().getResource(favouritesJsonPath).getPath());
            Type listType = new TypeToken<List<String>>() {
            }.getType();
            gsonFavourites.toJson(favourites, listType, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // todo: log
            throw new RuntimeException(e);
        }
    }
    // endregion

    // region History
    private final String historyJsonPath = "ru/mai/lessons/rpks/impl/json/history.json";
    private final List<HistoryTableViewDataProvider> history = new ArrayList<>();

    public List<HistoryTableViewDataProvider> getHistory() {
        return history;
    }

    public void addToHistoryList(HistoryTableViewDataProvider data) {
        history.add(data);
    }

    class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.MMM.uuuu HH:mm:ss");

        @Override
        public JsonElement serialize(LocalDateTime localDateTime, Type srcType, JsonSerializationContext context) {
            return new JsonPrimitive(formatter.format(localDateTime));
        }
    }

    class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(),
                    DateTimeFormatter.ofPattern("d.MMM.uuuu HH:mm:ss").withLocale(Locale.ENGLISH));
        }
    }

    class LocalTimeSerializer implements JsonSerializer<LocalTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        @Override
        public JsonElement serialize(LocalTime localTime, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(formatter.format(localTime));
        }
    }

    class LocalTimeDeserializer implements JsonDeserializer<LocalTime> {
        @Override
        public LocalTime deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            return LocalTime.parse(jsonElement.getAsString(),
                    DateTimeFormatter.ofPattern("HH:mm:ss").withLocale(Locale.ENGLISH));
        }
    }

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
            .registerTypeAdapter(LocalTime.class, new LocalTimeSerializer())
            .registerTypeAdapter(LocalTime.class, new LocalTimeDeserializer())
            .setPrettyPrinting().create();

    private void loadHistory() {
        try {
            FileReader reader = new FileReader(getClass().getClassLoader().getResource(historyJsonPath).getPath());
            Type listType = new TypeToken<ArrayList<HistoryTableViewDataProvider>>() {
            }.getType();
            List<HistoryTableViewDataProvider> readFromJson = gson.fromJson(reader, listType);
            if (readFromJson != null) {
                history.addAll(readFromJson);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveHistory() {
        try {
            java.io.FileWriter writer = new java.io.FileWriter(getClass().getClassLoader().getResource(historyJsonPath).getPath());
            Type listType = new TypeToken<List<HistoryTableViewDataProvider>>() {
            }.getType();
            gson.toJson(history, listType, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // todo: log
            throw new RuntimeException(e);
        }
    }
    // endregion

    // region Ignored
    private final String ignoredJsonPath = "ru/mai/lessons/rpks/impl/json/ignored.json";
    private final List<String> ignored = new ArrayList<>();

    public List<String> getIgnored() {
        return ignored;
    }

    public void addToIgnoredList(String url) {
        if (ignored.contains(url)) {
            ignored.remove(url);
            return;
        }
        ignored.add(url);
        history.removeIf(data -> Objects.equals(data.getAddress(), url));
    }

    private void loadIgnored() {
        try {
            Gson gsonIgnored = new Gson();
            FileReader reader = new FileReader(getClass().getClassLoader().getResource(ignoredJsonPath).getPath());
            Type listType = new TypeToken<ArrayList<String>>() {
            }.getType();
            List<String> fromGson = gsonIgnored.fromJson(reader, listType);
            reader.close();
            if (fromGson != null) {
                ignored.addAll(fromGson);
            }
        } catch (FileNotFoundException e) {
            // todo: log
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveIgnored() {
        try {
            Gson gsonIgnored = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(getClass().getClassLoader().getResource(ignoredJsonPath).getPath());
            Type listType = new TypeToken<List<String>>() {
            }.getType();
            gsonIgnored.toJson(ignored, listType, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            // todo: log
            throw new RuntimeException(e);
        }
    }
    // endregion

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tabPane.getTabs().clear();
        tabPane.setTabMinWidth(100);
        tabPane.setTabMaxWidth(150);
        loadFavouritesList();
        loadIgnored();
        loadHistory();
        tabPane.getTabs().add(newTabButton(tabPane));
    }

    private Tab addTab;

    private Tab newTabButton(TabPane tabPane) {
        addTab = new Tab(" + ");
        addTab.setClosable(false);
        tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
            if (newTab == addTab) {
                createAndAddNewTab(getNewTabIndex(), "https://www.google.ru");
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

    public void createNewHistoryTab() {
        Tab tab = new Tab("History");
        HistoryTab historyTab = new HistoryTab(this, tab);
        tabPane.getTabs().add(getNewTabIndex(), historyTab.getTab());
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

    public void removeAllTabs() {
        tabPane.getTabs().remove(addTab);
        for (Tab tab : tabPane.getTabs()) {
            ActionEvent.fireEvent(tab, new Event(Tab.CLOSED_EVENT));
        }
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
