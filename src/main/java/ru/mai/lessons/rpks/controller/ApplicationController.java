package ru.mai.lessons.rpks.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.mai.lessons.rpks.application.Application;
import ru.mai.lessons.rpks.create.CreateFavList;
import ru.mai.lessons.rpks.create.CreateNotification;
import ru.mai.lessons.rpks.create.CreateTab;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class ApplicationController {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private MenuBar menuBar;

    @FXML
    private AnchorPane mainPain;

    @FXML
    private TabPane tabPane;

    private Tab previousSelectedTab;

    private List<String> historyDisableSites;

    private ObservableList<String> favSites;

    private boolean historySwitch;

    private final ExecutorService executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());

    private final static String DOWNLOADS = (String.valueOf(Application.class.getResource("")))
            .replace("file:/", "")
            .replace("%20", " ") + "downloads/";

    private final static String EMPTY_URL_MESSAGE = "Empty url :(";


    private Tab CurrentTab() {
        return tabPane.getSelectionModel().getSelectedItem();
    }

    private WebView CurrentWebView() {
        return (WebView) CurrentTab().getContent().lookup("#webView");
    }

    private TextField CurrentTextField() {
        return (TextField) CurrentTab().getContent().lookup("#urlField");
    }

    private String CurrentWebsite() {
        String website = CurrentWebView().getEngine().getLocation();
        if (website == null || website.isEmpty()) {
            return null;
        }
        return website.substring(0, website.length() - 1);
    }

    private double getWindowX() {
        for (Window window : Stage.getWindows()) {
            if (window.isShowing()) {
                return window.getX();
            }
        }
        throw new RuntimeException("Window is not found!");
    }

    private double getWindowY() {
        for (Window window : Stage.getWindows()) {
            if (window.isShowing()) {
                return window.getY();
            }
        }
        throw new RuntimeException("Window is not found!");
    }

    private double getWindowWidth() {
        for (Window window : Stage.getWindows()) {
            if (window.isShowing()) {
                return window.getWidth();
            }
        }
        throw new RuntimeException("Window is not found!");
    }

    private double getWindowHeight() {
        for (Window window : Stage.getWindows()) {
            if (window.isShowing()) {
                return window.getHeight();
            }
        }
        throw new RuntimeException("Window is not found!");
    }

    private void setTabTitle(String currentUrl) {
        String TabTitle;
        if (!currentUrl.isEmpty()) {
            if (currentUrl.length() > 30) {
                TabTitle = currentUrl.substring(0, 12) + "...";
            } else {
                TabTitle = currentUrl;
            }
            CurrentTab().setText(TabTitle);
        } else {
            CurrentTab().setText(EMPTY_URL_MESSAGE);
        }
    }

    private void addToHistory(String prevSite, String newSite, LocalDateTime actionDate, boolean closing) {
        JSONParser parser = new JSONParser();
        JSONObject oldJson = null;
        try {
            oldJson = (JSONObject) parser.parse(new FileReader(
                    (String.valueOf(Application.class.getResource("history/history.json")))
                            .replace("file:/", "")
                            .replace("%20", " ")));
        } catch (ParseException ex) {
            System.out.println("ParseException caught: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("IOException caught: " + ex.getMessage());
        } catch (NullPointerException ex) {
            System.out.println("NullPointerException caught: history.json not found! Ex.message: " + ex.getMessage());
        }
        JSONArray historyJson = null;
        try {
            historyJson = (JSONArray) Objects.requireNonNull(oldJson).get("history");
        } catch (NullPointerException ex) {
            System.out.println("NullPointerException caught: broken history.json! Ex.message: " + ex.getMessage());
        }
        JSONObject newJson = new JSONObject();

        if (prevSite != null || tabPane.getTabs().size() > 2) {
            try {
                if (((JSONObject) historyJson.getLast()).get("date end").equals("")) {
                    ((JSONObject) historyJson.getLast()).put("date end", actionDate.toString());
                    LocalDateTime dateBegin = LocalDateTime.parse((String) ((JSONObject) historyJson.getLast()).get("date begin"));
                    LocalDateTime dateEnd = LocalDateTime.parse((String) ((JSONObject) historyJson.getLast()).get("date end"));
                    ((JSONObject) historyJson.getLast()).put("time spent", ChronoUnit.SECONDS.between(dateBegin, dateEnd) + " sec");
                }
            } catch (NoSuchElementException ex) {
                System.out.println("No Such Element! Ex.message: " + ex.getMessage());
            }
        }

        try {
            if (!closing) {
                newJson.put("time spent", "");
                newJson.put("date end", "");
                newJson.put("date begin", actionDate.toString());
                newJson.put("url", newSite);
                historyJson.addLast(newJson);
            }
            oldJson.put("history", historyJson);
        } catch (NullPointerException ex) {
            System.out.println("NullPointerException caught: newJson is null! Ex.message: " + ex.getMessage());
        }

        JsonElement jsonString = new JsonParser().parse(oldJson.toJSONString());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter tmp = new FileWriter(
                (String.valueOf(Application.class.getResource("history/history.json")))
                        .replace("file:/", "")
                        .replace("%20", " ")
        );
             JsonWriter jsonWriter = new JsonWriter(tmp)) {
            jsonWriter.setIndent("    ");
            gson.toJson(jsonString, jsonWriter);
        } catch (IOException ex) {
            System.out.println("IOException caught: invalid json writing! Ex.message: " + ex.getMessage());
        }
    }

    @FXML
    void EditorCreate() {

    }

    @FXML
    void EditorEdit() {

    }

    @FXML
    void FavAdd() {
        String text;
        double notifyWidth;
        double notifyHeight;

        if (CurrentWebsite() == null) {
            text = EMPTY_URL_MESSAGE;
            notifyWidth = 220.0;
            notifyHeight = 50.0;
        } else if (favSites.contains(CurrentWebsite())) {
            text = String.format("Website %s\nis already added to Favourites!", CurrentWebsite().substring(8));
            notifyWidth = 400.0;
            notifyHeight = 70.0;
        } else {
            favSites.add(CurrentWebsite());
            text = String.format("Website %s\nsuccessfully added to Favourites!", CurrentWebsite().substring(8));
            notifyWidth = 430.0;
            notifyHeight = 70.0;
        }

        try {
            double mainX = getWindowX();
            double mainY = getWindowY();
            double mainWidth = getWindowWidth();

            var notifyStage = CreateNotification.create("Favourites", text,
                    notifyWidth, notifyHeight);
            notifyStage.setX(mainX + mainWidth - notifyWidth - 25);
            notifyStage.setY(mainY + 40);
            notifyStage.showAndWait();
        } catch (RuntimeException ex) {
            System.out.println("RuntimeException caught: " + ex.getMessage());
        }
    }

    @FXML
    void FavDelete() {
        String text;
        double notifyWidth;
        double notifyHeight;
        if (CurrentWebsite() == null) {
            text = EMPTY_URL_MESSAGE;
            notifyWidth = 220.0;
            notifyHeight = 50.0;
        } else if (!favSites.contains(CurrentWebsite())) {
            text = String.format("Website %s\nis already deleted from Favourites!", CurrentWebsite().substring(8));
            notifyWidth = 440.0;
            notifyHeight = 70.0;
        } else {
            favSites.remove(CurrentWebsite());
            text = String.format("Website %s\nsuccessfully deleted from Favourites!", CurrentWebsite().substring(8));
            notifyWidth = 470.0;
            notifyHeight = 70.0;
        }

        try {
            double mainX = getWindowX();
            double mainY = getWindowY();
            double mainWidth = getWindowWidth();

            var notifyStage = CreateNotification.create("Favourites", text,
                    notifyWidth, notifyHeight);
            notifyStage.setX(mainX + mainWidth - notifyWidth - 25);
            notifyStage.setY(mainY + 40);
            notifyStage.showAndWait();
        } catch (RuntimeException ex) {
            System.out.println("RuntimeException caught: " + ex.getMessage());
        }
    }

    @FXML
    void FavShow() {
        double mainX = getWindowX();
        double mainY = getWindowY();
        double mainWidth = getWindowWidth();

        var favListStage = CreateFavList.create("Favourites", favSites,
                CurrentTextField(), actionEvent -> reloadTab());
        double favListWidth = favListStage.getWidth();
        favListStage.setX(mainX + mainWidth - favListWidth - 15);
        favListStage.setY(mainY + 45);
        favListStage.showAndWait();
    }

    @FXML
    void HTMLLoad() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select HTML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                String htmlContent = Files.readString(Path.of(selectedFile.getPath()));
                CurrentWebView().getEngine().loadContent(htmlContent);
                CurrentTextField().setText(selectedFile.getName().replace(".html", ""));
            } catch (IOException ex) {
                System.out.println("IOException caught in HTMLLoad! Ex.message" + ex.getMessage());
            }
        }
    }

    @FXML
    void HTMLSave() throws IOException {
        String text;
        double notifyWidth = 220.0;
        double notifyHeight = 50.0;
        if (CurrentWebsite() != null) {
            String filename = CurrentWebsite().substring("https://".length());
            File file = new File(DOWNLOADS + filename + ".html");
            URL url = new URL(CurrentWebsite());
            FileUtils.copyURLToFile(url, file);
            text = "HTML Saved!";
        } else {
            text = EMPTY_URL_MESSAGE;
        }

        double mainX = getWindowX();
        double mainY = getWindowY();
        double mainWidth = getWindowWidth();
        var notifyStage = CreateNotification.create("HTML", text,
                notifyWidth, notifyHeight);
        notifyStage.setX(mainX + mainWidth - notifyWidth - 25);
        notifyStage.setY(mainY + 40);
        notifyStage.showAndWait();
    }

    @FXML
    void HTMLZip() throws IOException {
        String text;
        double notifyWidth = 220.0;
        double notifyHeight = 50.0;
        if (CurrentWebsite() != null) {
            String filename = CurrentWebsite().substring("https://".length());
            File file = new File(DOWNLOADS + filename + ".html");
            URL url = new URL(CurrentWebsite());
            FileUtils.copyURLToFile(url, file);
            File zipOutFile = new File(DOWNLOADS + filename + ".zip");
            zipOutFile.createNewFile();
            try (FileOutputStream outputStreamZip = new FileOutputStream(DOWNLOADS + filename + ".zip");
                 FileInputStream inputStream = new FileInputStream(file);
                 ZipOutputStream zipOut = new ZipOutputStream(outputStreamZip)) {
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zipOut.putNextEntry(zipEntry);
                zipOut.write(inputStream.readAllBytes());
            }
            file.delete();
            text = "HTML Saved!";
        } else {
            text = EMPTY_URL_MESSAGE;
        }

        double mainX = getWindowX();
        double mainY = getWindowY();
        double mainWidth = getWindowWidth();
        var notifyStage = CreateNotification.create("HTML", text,
                notifyWidth, notifyHeight);
        notifyStage.setX(mainX + mainWidth - notifyWidth - 25);
        notifyStage.setY(mainY + 40);
        notifyStage.showAndWait();
    }

    @FXML
    void HistoryDisable() {
        String text;
        double notifyWidth;
        double notifyHeight;
        if (!historySwitch) {
            text = "History is already turned OFF!";
            notifyWidth = 400.0;
            notifyHeight = 50.0;
        } else {
            historySwitch = false;
            text = "History successfully turned OFF!";
            notifyWidth = 430.0;
            notifyHeight = 50.0;
            if (!historyDisableSites.contains(CurrentWebsite())) {
                addToHistory(CurrentWebsite(), CurrentWebsite(), java.time.LocalDateTime.now(), true);
            }
        }

        try {
            double mainX = getWindowX();
            double mainY = getWindowY();
            double mainWidth = getWindowWidth();

            var notifyStage = CreateNotification.create("History Switch", text,
                    notifyWidth, notifyHeight);
            notifyStage.setX(mainX + mainWidth - notifyWidth - 25);
            notifyStage.setY(mainY + 40);
            notifyStage.showAndWait();
        } catch (RuntimeException ex) {
            System.out.println("RuntimeException caught: " + ex.getMessage());
        }
    }

    @FXML
    void HistoryDisableForSite() {
        String text;
        double notifyWidth;
        double notifyHeight;
        if (CurrentWebsite() == null) {
            text = EMPTY_URL_MESSAGE;
            notifyWidth = 220.0;
            notifyHeight = 50.0;
        } else if (historyDisableSites.contains(CurrentWebsite())) {
            text = String.format("History for %s\nalready turned OFF!", CurrentWebsite().substring(8));
            notifyWidth = 390.0;
            notifyHeight = 70.0;
        } else {
            historyDisableSites.add(CurrentWebsite());
            text = String.format("History for %s\nsuccessfully turned OFF!", CurrentWebsite().substring(8));
            notifyWidth = 390.0;
            notifyHeight = 70.0;
        }

        try {
            double mainX = getWindowX();
            double mainY = getWindowY();
            double mainWidth = getWindowWidth();

            var notifyStage = CreateNotification.create("History Switch for Website", text,
                    notifyWidth, notifyHeight);
            notifyStage.setX(mainX + mainWidth - notifyWidth - 25);
            notifyStage.setY(mainY + 40);
            notifyStage.showAndWait();
        } catch (RuntimeException ex) {
            System.out.println("RuntimeException caught: " + ex.getMessage());
        }
    }

    @FXML
    void HistoryEnable() {
        String text;
        double notifyWidth;
        double notifyHeight;
        if (historySwitch) {
            text = "History is already turned ON!";
            notifyWidth = 390.0;
            notifyHeight = 50.0;
        } else {
            historySwitch = true;
            text = "History successfully turned ON!";
            notifyWidth = 415.0;
            notifyHeight = 50.0;
        }

        try {
            double mainX = getWindowX();
            double mainY = getWindowY();
            double mainWidth = getWindowWidth();

            var notifyStage = CreateNotification.create("History Switch", text,
                    notifyWidth, notifyHeight);
            notifyStage.setX(mainX + mainWidth - notifyWidth - 25);
            notifyStage.setY(mainY + 40);
            notifyStage.showAndWait();
        } catch (RuntimeException ex) {
            System.out.println("RuntimeException caught: " + ex.getMessage());
        }
    }

    @FXML
    void HistoryEnableForSite() {
        String text;
        double notifyWidth;
        double notifyHeight;
        if (CurrentWebsite() == null) {
            text = EMPTY_URL_MESSAGE;
            notifyWidth = 220.0;
            notifyHeight = 50.0;
        } else if (!historyDisableSites.contains(CurrentWebsite())) {
            text = String.format("History for %s\nalready turned ON!", CurrentWebsite().substring(8));
            notifyWidth = 390.0;
            notifyHeight = 70.0;
        } else {
            historyDisableSites.remove(CurrentWebsite());
            text = String.format("History for %s\nsuccessfully turned ON!", CurrentWebsite().substring(8));
            notifyWidth = 390.0;
            notifyHeight = 70.0;
        }

        try {
            double mainX = getWindowX();
            double mainY = getWindowY();
            double mainWidth = getWindowWidth();

            var notifyStage = CreateNotification.create("History Switch for Website", text,
                    notifyWidth, notifyHeight);
            notifyStage.setX(mainX + mainWidth - notifyWidth - 25);
            notifyStage.setY(mainY + 40);
            notifyStage.showAndWait();
        } catch (RuntimeException ex) {
            System.out.println("RuntimeException caught: " + ex.getMessage());
        }
    }

    @FXML
    void addNewTab() {
        var newTab = CreateTab.create(
                actionEvent -> goBackTab(),
                actionEvent -> goForwardTab(),
                actionEvent -> reloadTab(),
                this::changedSelectionTab
        );
        tabPane.getTabs().add(tabPane.getTabs().size() - 1, newTab);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2);
    }

    @FXML
    public void changedSelectionTab(Event event) {
        if (event.getSource() != previousSelectedTab) {
            if (previousSelectedTab != null) {
                var site = ((WebView) previousSelectedTab.getContent()
                        .lookup("#webView")).getEngine().getLocation();
                if (historySwitch && !historyDisableSites.contains(site)) {
                    addToHistory(site, ((WebView) ((Tab) event.getSource()).getContent().lookup("#webView"))
                            .getEngine().getLocation(), java.time.LocalDateTime.now(), false);
                }
            }
            previousSelectedTab = CurrentTab();
        }
    }

    @FXML
    void goBackTab() {
        if (historySwitch) {
            var history = CurrentWebView().getEngine().getHistory();
            if (history.getCurrentIndex() > 0) {
                history.go(-1);
                var currentUrl = history.getEntries().get(history.getCurrentIndex()).getUrl();
                CurrentTextField().setText(currentUrl);
                setTabTitle(currentUrl);
            }
        }
    }

    @FXML
    void goForwardTab() {
        if (historySwitch) {
            var history = CurrentWebView().getEngine().getHistory();
            if (history.getCurrentIndex() < history.getEntries().size() - 1) {
                history.go(1);
                var currentUrl = history.getEntries().get(history.getCurrentIndex()).getUrl();
                CurrentTextField().setText(currentUrl);
                setTabTitle(currentUrl);
            }
        }
    }

    @FXML
    void reloadTab() {
        String requestWebsite = CurrentTextField().getText();
        String requestWebsiteHTTPS;
        if (!requestWebsite.contains("https://") && !requestWebsite.isEmpty()) {
            requestWebsiteHTTPS = "https://" + requestWebsite;
        } else {
            requestWebsiteHTTPS = requestWebsite;
        }

        if (CurrentWebsite() != null && Objects.equals(CurrentWebsite(), requestWebsiteHTTPS)) {
            CurrentWebView().getEngine().reload();
        } else {
            setTabTitle(requestWebsiteHTTPS);
            executorService.submit(() ->
                    Platform.runLater(() ->
                            CurrentWebView().getEngine().load(requestWebsiteHTTPS)));
            if (historySwitch && !historyDisableSites.contains(requestWebsiteHTTPS)) {
                addToHistory(CurrentWebsite(), requestWebsiteHTTPS, java.time.LocalDateTime.now(), false);
            }
        }
    }

    @FXML
    void initialize() {
        historySwitch = true;
        historyDisableSites = new ArrayList<>();
        favSites = FXCollections.observableArrayList();
        previousSelectedTab = CurrentTab();

        String path = null;
        String directoryPath = null;
        try {
            directoryPath = (Application.class.getResource("") + "history/")
                    .replace("file:/", "")
                    .replace("%20", " ");
            path = directoryPath + "history.json";
        } catch (NullPointerException ex) {
            System.out.println("Initialize error: invalid resource path!");
        }

        if (directoryPath != null) {
            File directoryFile = new File(directoryPath);
            File historyFile = new File(path);
            try {
                if (!directoryFile.exists()) {
                    directoryFile.mkdir();
                }
                if (!historyFile.createNewFile()) {
                    if (!historyFile.delete()) {
                        System.out.println("Initialize error: invalid deletion of previous history file!");
                    } else {
                        System.out.println("Note: previous history file successfully removed!");
                    }
                } else {
                    System.out.println("Note: history.json file did not exist while initializing, creating new one...");
                }
            } catch (IOException ex) {
                System.out.println("Initialize error: could not create a history.json file!");
            }
        }

        var jsonString = new JsonParser().parse("{history:[]}");
        var gson = new GsonBuilder().setPrettyPrinting().create();
        try (var tmp = new FileWriter(
                ((Application.class.getResource("")) + "history/history.json")
                        .replace("file:/", "")
                        .replace("%20", " "));
             var jsonWriter = new JsonWriter(tmp)) {
            jsonWriter.setIndent("    ");
            gson.toJson(jsonString, jsonWriter);
        } catch (IOException ex) {
            System.out.println("Initialize error: could not refresh a history.json file!");
        }
    }

    public void onClose() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }

        if (historySwitch && !historyDisableSites.contains(CurrentWebsite())) {
            addToHistory(CurrentWebsite(), CurrentWebsite(), java.time.LocalDateTime.now(), true);
        }
    }
}
