package ru.mai.lessons.rpks.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.mai.lessons.rpks.application.Application;
import ru.mai.lessons.rpks.create.CreateNotification;
import ru.mai.lessons.rpks.create.CreateTab;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


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

    private List<String> historyDisableSites;

    private boolean historySwitch;

    private final ExecutorService executorService = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors());


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
            CurrentTab().setText("Empty url :(");
        }
    }

    private void addToHistory(String prevSite, String newSite, LocalDateTime actionDate) {
        JSONParser parser = new JSONParser();
        JSONObject oldJson = null;
        try {
            oldJson = (JSONObject) parser.parse(new FileReader(Objects.requireNonNull(
                    Application.class.getResource("history.json")).getPath()));
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

        if (prevSite != null && historyJson != null) {
            if (((JSONObject) historyJson.getLast()).get("url").equals(prevSite) &&
                    ((JSONObject) historyJson.getLast()).get("date end").equals("")) {
                ((JSONObject) historyJson.getLast()).put("time end", actionDate.toString());
                LocalDateTime dateBegin = LocalDateTime.parse((String) ((JSONObject) historyJson.getLast()).get("date begin"));
                LocalDateTime dateEnd = LocalDateTime.parse((String) ((JSONObject) historyJson.getLast()).get("date end"));
                ((JSONObject) historyJson.getLast()).put("time spent", ChronoUnit.SECONDS.between(dateBegin, dateEnd) + " sec");
            }
        }

        try {
            newJson.put("url", newSite);
            newJson.put("date begin", actionDate.toString());
            newJson.put("date end", "");
            newJson.put("time spent", "");
            historyJson.addLast(newJson);
            oldJson.put("history", historyJson);
        } catch (NullPointerException ex) {
            System.out.println("NullPointerException caught: newJson is null! Ex.message: " + ex.getMessage());
        }

        JsonElement jsonString = new JsonParser().parse(oldJson.toJSONString());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter tmp = new FileWriter(Application.class.getResource("history.json").getPath());
             JsonWriter jsonWriter = new JsonWriter(tmp)) {
            jsonWriter.setIndent("    ");
            gson.toJson(jsonString, jsonWriter);
        } catch (IOException ex) {
            System.out.println("IOException caught: invalid json writing! Ex.message: " + ex.getMessage());
        }
    }


    @FXML
    void EditorCreate(ActionEvent event) {

    }

    @FXML
    void EditorEdit(ActionEvent event) {

    }

    @FXML
    void FavAdd(ActionEvent event) {

    }

    @FXML
    void FavDelete(ActionEvent event) {

    }

    @FXML
    void FavShow(ActionEvent event) {

    }

    @FXML
    void HTMLLoad(ActionEvent event) {

    }

    @FXML
    void HTMLSave(ActionEvent event) {

    }

    @FXML
    void HTMLZip(ActionEvent event) {

    }

    @FXML
    void HistoryDisable(ActionEvent event) {

    }

    @FXML
    void HistoryDisableForSite(ActionEvent event) {

    }

    @FXML
    void HistoryEnable() {
        historySwitch = true;

        double notifyWidth = 415.0;
        double notifyHeight = 50.0;
        try {
            double mainX = getWindowX();
            double mainY = getWindowY();
            double mainWidth = getWindowWidth();

            var notifyStage = CreateNotification.create("History Switch", "History successfully turned ON!",
                    notifyWidth, notifyHeight);
            notifyStage.setX(mainX + mainWidth - notifyWidth - 25);
            notifyStage.setY(mainY + 40);
            notifyStage.showAndWait();
        }
        catch (RuntimeException ex) {
            System.out.println("RuntimeException caught: " + ex.getMessage());
        }
    }

    @FXML
    void HistoryEnableForSite(ActionEvent event) {

    }

    @FXML
    void addNewTab() {
        var newTab = CreateTab.create(
                actionEvent -> goBackTab(),
                actionEvent -> goForwardTab(),
                actionEvent -> reloadTab(),
                actionEvent -> closeTab()
        );
        tabPane.getTabs().add(tabPane.getTabs().size() - 1, newTab);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2);
    }

    @FXML
    void closeTab() {

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
        }
        else {
            setTabTitle(requestWebsiteHTTPS);
            executorService.submit(() ->
                    Platform.runLater(() ->
                            CurrentWebView().getEngine().load(requestWebsiteHTTPS)));

            if (historySwitch && !historyDisableSites.contains(requestWebsiteHTTPS)) {
                addToHistory(CurrentWebsite(), requestWebsiteHTTPS, java.time.LocalDateTime.now());
            }
        }
    }

    @FXML
    void initialize() {
        historySwitch = true;
        historyDisableSites = new ArrayList<>();
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
    }
}
