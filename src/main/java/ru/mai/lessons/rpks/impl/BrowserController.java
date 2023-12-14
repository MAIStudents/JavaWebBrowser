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
import ru.mai.lessons.rpks.helpClasses.HistoryTableViewDataProvider;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            FileReader reader = new FileReader(Objects.requireNonNull(getClass().getClassLoader().getResource(favouritesJsonPath)).getPath());
            Type listType = new TypeToken<ArrayList<String>>() {
            }.getType();
            List<String> fromGson = gsonFavourites.fromJson(reader, listType);
            reader.close();
            if (fromGson != null) {
                favourites.addAll(fromGson);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    public void saveFavouritesToJson() {
        try {
            Gson gsonFavourites = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(Objects.requireNonNull(getClass().getClassLoader().getResource(favouritesJsonPath)).getPath());
            Type listType = new TypeToken<List<String>>() {
            }.getType();
            gsonFavourites.toJson(favourites, listType, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }
    // endregion

    // region History
    boolean trackHistory = true;

    void setTrackHistory(boolean trackHistory) {
        this.trackHistory = trackHistory;
    }

    private final String historyJsonPath = "ru/mai/lessons/rpks/impl/json/history.json";
    private final List<HistoryTableViewDataProvider> history = new ArrayList<>();

    public List<HistoryTableViewDataProvider> getHistory() {
        return history;
    }

    public void addToHistoryList(HistoryTableViewDataProvider data) {
        history.add(data);
    }

    static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d.MMM.uuuu HH:mm:ss");

        @Override
        public JsonElement serialize(LocalDateTime localDateTime, Type srcType, JsonSerializationContext context) {
            return new JsonPrimitive(formatter.format(localDateTime));
        }
    }

    static class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(),
                    DateTimeFormatter.ofPattern("d.MMM.uuuu HH:mm:ss").withLocale(Locale.ENGLISH));
        }
    }

    static class LocalTimeSerializer implements JsonSerializer<LocalTime> {
        private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        @Override
        public JsonElement serialize(LocalTime localTime, Type type, JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(formatter.format(localTime));
        }
    }

    static class LocalTimeDeserializer implements JsonDeserializer<LocalTime> {
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
            FileReader reader = new FileReader(Objects.requireNonNull(getClass().getClassLoader().getResource(historyJsonPath)).getPath());
            Type listType = new TypeToken<ArrayList<HistoryTableViewDataProvider>>() {
            }.getType();
            List<HistoryTableViewDataProvider> readFromJson = gson.fromJson(reader, listType);
            if (readFromJson != null) {
                history.addAll(readFromJson);
            }
            reader.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    public void saveHistory() {
        try {
            java.io.FileWriter writer = new java.io.FileWriter(Objects.requireNonNull(getClass().getClassLoader().getResource(historyJsonPath)).getPath());
            Type listType = new TypeToken<List<HistoryTableViewDataProvider>>() {
            }.getType();
            gson.toJson(history, listType, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
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
            FileReader reader = new FileReader(Objects.requireNonNull(getClass().getClassLoader().getResource(ignoredJsonPath)).getPath());
            Type listType = new TypeToken<ArrayList<String>>() {
            }.getType();
            List<String> fromGson = gsonIgnored.fromJson(reader, listType);
            reader.close();
            if (fromGson != null) {
                ignored.addAll(fromGson);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    public void saveIgnored() {
        try {
            Gson gsonIgnored = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(Objects.requireNonNull(getClass().getClassLoader().getResource(ignoredJsonPath)).getPath());
            Type listType = new TypeToken<List<String>>() {
            }.getType();
            gsonIgnored.toJson(ignored, listType, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
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

    public void createAndAddNewTab(int index, String... webSite) {

        //Create
        BrowserTabController webBrowserTab = createNewTab(webSite);

        //Add the tab
        tabPane.getTabs().add(index, webBrowserTab.getTab());

    }

    public void createNewFavouritesTab() {
        Tab tab = new Tab("Favourites");
        FavouritesTabController favouritesTab = new FavouritesTabController(this, tab);
        tabPane.getTabs().add(getNewTabIndex(), favouritesTab.getTab());
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2); // Selecting the tab before the button, which is the newly created one
    }

    public void createNewHistoryTab() {
        Tab tab = new Tab("History");
        HistoryTabController historyTabController = new HistoryTabController(this, tab);
        tabPane.getTabs().add(getNewTabIndex(), historyTabController.getTab());
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2); // Selecting the tab before the button, which is the newly created one
    }

    public void createNewCreateHTMLPageTab() {
        Tab tab = new Tab("Create HTML page");
        CreateHTMLTabController createHTMLTabController = new CreateHTMLTabController(this, tab);
        tabPane.getTabs().add(getNewTabIndex(), createHTMLTabController.tab);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2);
    }

    public void createNewEditHTMLTab(String address, boolean isEditable) {
        Tab tab = new Tab(isEditable ? "Edit HTML page" : "View HTML page");
        EditHTMLTabController editHTMLTabController = new EditHTMLTabController(this, tab, address, isEditable);
        tabPane.getTabs().add(getNewTabIndex(), editHTMLTabController.tab);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2);
    }

    public BrowserTabController createNewTab(String... webSite) {
        //Create
        Tab tab = new Tab("");
        tab.setClosable(true);

        return new BrowserTabController(this, tab, webSite.length == 0 ? null : webSite[0]);
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
