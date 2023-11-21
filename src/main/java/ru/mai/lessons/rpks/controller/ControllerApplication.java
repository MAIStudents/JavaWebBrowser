package ru.mai.lessons.rpks.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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

import ru.mai.lessons.rpks.Application;
import ru.mai.lessons.rpks.factory.TabFactory;

public class ControllerApplication implements Initializable {
    @FXML
    private TabPane tabPane;
    private boolean isEnableHistory;
    private List<String> untrackedSites;
    private List<String> favoriteSites;
    private final static String PATH_DOWNLOADS = "/home/alexandr/MAI/5-semestr/RPKS/JavaLabs/JavaWebBrowser/src/main/resources/ru/mai/lessons/rpks/downloads/";
    private final static String EDIT_HTML_FILE = PATH_DOWNLOADS + "editHTML.html";
    private final static String PATH_JSON_HISTORY = "/home/alexandr/MAI/5-semestr/RPKS/JavaLabs/JavaWebBrowser/src/main/resources/ru/mai/lessons/rpks/history.json";
    private final ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        untrackedSites = new ArrayList<>();
        favoriteSites = new ArrayList<>();
        isEnableHistory = true;
        Tab mainTab = TabFactory.create(
                actionEvent -> backOnHistory(),
                actionEvent -> forwardOnHistory(),
                actionEvent -> reloadPage(),
                actionEvent -> loadPage(),
                actionEvent -> onCloseTab()
        );
        Tab toAddTab = new Tab("+");

        toAddTab.setOnSelectionChanged(event -> {
            if (toAddTab.isSelected()) {
                addTab();
            }
        });

        mainTab.setText("JavaWebBrowser");
        tabPane.getTabs().addAll(mainTab, toAddTab);
    }

    private WebView getCurrentWebView() {
        return (WebView) getCurrentTab().getContent().lookup("#webView");
    }

    private String getCurrentWebsite() {
        String website = getCurrentWebView().getEngine().getLocation();

        if (website == null) {
            return null;
        }

        return website.substring(0, website.length() - 1);
    }

    private Tab getCurrentTab() {
        return tabPane.getSelectionModel().getSelectedItem();
    }

    private TextField getCurrentTextField() {
        return (TextField) getCurrentTab().getContent().lookup("#textField");
    }

    public void loadPage() {
        String requestText = ((TextField) getCurrentTab().getContent().lookup("#textField")).getText();
        String requestTextWithHTTPS = "";

        if (!requestText.contains("https://")) {
            requestTextWithHTTPS = "https://" + requestText;
        } else {
            requestTextWithHTTPS = requestText;
        }

        tabPane.getSelectionModel().getSelectedItem().setText(requestTextWithHTTPS);
        String finalRequestTextWithHTTPS = requestTextWithHTTPS;
        service.submit(() -> Platform.runLater(() -> getCurrentWebView().getEngine().load(finalRequestTextWithHTTPS)));

        if (isEnableHistory && !untrackedSites.contains(requestTextWithHTTPS)) {
            addNoteHistory(getCurrentTab().getText(), requestTextWithHTTPS, java.time.LocalDateTime.now());
        }
    }

    public void reloadPage() {
        getCurrentWebView().getEngine().reload();
    }

    public void addTab() {
        Tab newTab = TabFactory.create(
                actionEvent -> backOnHistory(),
                actionEvent -> forwardOnHistory(),
                actionEvent -> reloadPage(),
                actionEvent -> loadPage(),
                actionEvent -> onCloseTab()
        );
        tabPane.getTabs().add(tabPane.getTabs().size() - 1, newTab);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2);
    }

    public void backOnHistory() {
        if (isEnableHistory) {
            WebHistory history = getCurrentWebView().getEngine().getHistory();
            ObservableList<WebHistory.Entry> entries = history.getEntries();

            if (history.getCurrentIndex() > 0) {
                history.go(-1);
                getCurrentTextField().setText(entries.get(history.getCurrentIndex()).getUrl());
                getCurrentTab().setText(entries.get(history.getCurrentIndex()).getUrl());
            }
        }
    }

    public void forwardOnHistory() {
        if (isEnableHistory) {
            WebHistory history = getCurrentWebView().getEngine().getHistory();
            ObservableList<WebHistory.Entry> entries = history.getEntries();

            if (history.getCurrentIndex() < entries.size() - 1) {
                history.go(1);
                getCurrentTextField().setText(entries.get(history.getCurrentIndex()).getUrl());
                getCurrentTab().setText(entries.get(history.getCurrentIndex()).getUrl());
            }
        }
    }

    public void disableHistoryForSite() {
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Disable history for site");
        dialog.setHeaderText("Enter the site for which you want to disable history");
        Optional<String> optional = dialog.showAndWait();
        String website = "";

        if (optional.isPresent()) {
            String websiteTemp = optional.get();

            if (!websiteTemp.contains("https://")) {
                website = "https://" + websiteTemp;
            } else {
                website = websiteTemp;
            }

            untrackedSites.add(website);
        }

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;

        try {
            jsonObject = (JSONObject) parser.parse(new FileReader(PATH_JSON_HISTORY));
        } catch (ParseException e) {
            System.err.println("ParseException");
            System.err.println(Arrays.toString(e.getStackTrace()));
        } catch (IOException e) {
            System.err.println("IOException");
            System.err.println(Arrays.toString(e.getStackTrace()));
        }

        JSONArray jsonHistory = (JSONArray) jsonObject.get("history");
        JSONArray newJsonHistory = new JSONArray();

        for (var historyItem : jsonHistory) {
            if (!untrackedSites.contains((String) ((JSONObject) historyItem).get("website"))) {
                newJsonHistory.add(historyItem);
            }
        }

        jsonObject.put("history", newJsonHistory);
        JsonElement jsonString = new JsonParser().parse(jsonObject.toJSONString());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(PATH_JSON_HISTORY);
             JsonWriter jsonWriter = new JsonWriter(writer)) {
            jsonWriter.setIndent("    ");
            gson.toJson(jsonString, jsonWriter);
        } catch (IOException e) {
            System.err.println("IOException");
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    public void enableHistoryForSite() {
        Dialog<String> dialog = new TextInputDialog();
        dialog.setTitle("Enable history for site");
        dialog.setHeaderText("Enter the site for which you want to enable history");
        Optional<String> optional = dialog.showAndWait();

        String website = "";

        if (optional.isPresent()) {
            String websiteTemp = optional.get();

            if (!websiteTemp.contains("https://")) {
                website = "https://" + websiteTemp;
            } else {
                website = websiteTemp;
            }

            if (untrackedSites.contains(website)) {
                untrackedSites.remove(website);
            }
        }
    }

    public void disableHistory() {
        isEnableHistory = false;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Disable history");

        alert.setHeaderText(null);
        alert.setContentText("History for all sites successfully disabled");

        alert.showAndWait();

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;

        try {
            jsonObject = (JSONObject) parser.parse(new FileReader(PATH_JSON_HISTORY));
        } catch (ParseException e) {
            System.err.println("ParseException");
            System.err.println(Arrays.toString(e.getStackTrace()));
        } catch (IOException e) {
            System.err.println("IOException");
            System.err.println(Arrays.toString(e.getStackTrace()));
        }

        JSONArray jsonHistory = (JSONArray) jsonObject.get("history");
        jsonHistory.clear();
        jsonObject.put("history", jsonHistory);

        JsonElement jsonString = new JsonParser().parse(jsonObject.toJSONString());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(PATH_JSON_HISTORY);
             JsonWriter jsonWriter = new JsonWriter(writer)) {
            jsonWriter.setIndent("    ");
            gson.toJson(jsonString, jsonWriter);
        } catch (IOException e) {
            System.err.println("IOException");
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    public void enableHistory() {
        isEnableHistory = true;
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Enable history");

        alert.setHeaderText(null);
        alert.setContentText("History for all sites successfully enable");

        alert.showAndWait();
    }

    public void addToFavorites() {
        String currentSite = getCurrentWebsite();

        if (currentSite != null) {
            favoriteSites.add(getCurrentWebsite());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Add to favorites");

            alert.setHeaderText(null);
            alert.setContentText("The current site has added to the favorites list");

            alert.showAndWait();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Add to favorites");

            alert.setHeaderText(null);
            alert.setContentText("Can't add empty site to the favorites list");

            alert.showAndWait();
        }
    }

    public void showFavorites() {
        Stage favoritesStage = new Stage();
        VBox favoritesBox = new VBox();
        ListView<String> listView = new ListView<>();
        listView.getItems().setAll(favoriteSites);
        favoritesBox.getChildren().addAll(new Label("Favorite sites"), listView);

        Scene favoritesScene = new Scene(favoritesBox, 300, 200);
        favoritesStage.setScene(favoritesScene);
        favoritesStage.setTitle("Favorite sites");
        favoritesStage.show();
    }

    public void savePage() throws IOException {
        WebView currentWebView = getCurrentWebView();
        String currentWebsite = getCurrentWebsite();

        if (currentWebsite != null) {
            String filename = currentWebsite.substring("https://".length());
            File file = new File(PATH_DOWNLOADS + filename + ".html");
            URL url = new URL(currentWebView.getEngine().getLocation());
            FileUtils.copyURLToFile(url, file);
        }
    }

    public void saveZipPage() throws IOException {
        WebView currentWebView = getCurrentWebView();
        String currentWebsite = getCurrentWebsite();

        if (currentWebsite != null) {
            String filename = currentWebsite.substring("https://".length());
            File file = new File(PATH_DOWNLOADS + filename);
            URL url = new URL(currentWebView.getEngine().getLocation());
            FileUtils.copyURLToFile(url, file);

            File zipOutFile = new File(PATH_DOWNLOADS + filename + ".zip");
            zipOutFile.createNewFile();

            try (FileOutputStream outputStreamZip = new FileOutputStream(PATH_DOWNLOADS + filename + ".zip");
                 FileInputStream inputStream = new FileInputStream(file);
                 ZipOutputStream zipOut = new ZipOutputStream(outputStreamZip)) {
                ZipEntry zipEntry = new ZipEntry(file.getName());
                zipOut.putNextEntry(zipEntry);

                zipOut.write(inputStream.readAllBytes());
            }

            file.delete();
        }
    }

    private void addNoteHistory(String oldWebsite, String newWebsite, LocalDateTime visitTime) {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;

        try {
            jsonObject = (JSONObject) parser.parse(new FileReader(PATH_JSON_HISTORY));
        } catch (ParseException e) {
            System.err.println("ParseException");
            System.err.println(Arrays.toString(e.getStackTrace()));
        } catch (IOException e) {
            System.err.println("IOException");
            System.err.println(Arrays.toString(e.getStackTrace()));
        }

        JSONArray jsonHistory = (JSONArray) jsonObject.get("history");
        JSONObject newJsonObject = new JSONObject();

        if (oldWebsite != null) {
            for (int i = 0; i < jsonHistory.size(); i++) {
                if (((JSONObject) jsonHistory.get(i)).get("tab name").equals(getCurrentTab().getId()) &&
                        ((JSONObject) jsonHistory.get(i)).get("website").equals(oldWebsite) &&
                        ((JSONObject) jsonHistory.get(i)).get("time end").equals("")) {
                    ((JSONObject) jsonHistory.get(i)).put("time end", visitTime.toString());
                    LocalDateTime dateBegin = LocalDateTime.parse((String) ((JSONObject) jsonHistory.get(i)).get("time begin"));
                    LocalDateTime dateEnd = LocalDateTime.parse((String) ((JSONObject) jsonHistory.get(i)).get("time end"));
                    ((JSONObject) jsonHistory.get(i)).put("time spent on website", ChronoUnit.SECONDS.between(dateBegin, dateEnd) + " sec");
                }
            }
        }

        newJsonObject.put("tab name", getCurrentTab().getId());
        newJsonObject.put("website", newWebsite);
        newJsonObject.put("time begin", visitTime.toString());
        newJsonObject.put("time end", "");
        newJsonObject.put("time spent on website", "");

        jsonHistory.add(newJsonObject);
        jsonObject.put("history", jsonHistory);

        JsonElement jsonString = new JsonParser().parse(jsonObject.toJSONString());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(PATH_JSON_HISTORY);
             JsonWriter jsonWriter = new JsonWriter(writer)) {
            jsonWriter.setIndent("    ");
            gson.toJson(jsonString, jsonWriter);
        } catch (IOException e) {
            System.err.println("IOException");
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    private void onCloseTab() {
        if (isEnableHistory) {
            String currentWebSite = getCurrentWebsite();

            if (currentWebSite != null && !untrackedSites.contains(currentWebSite)) {
                LocalDateTime currentTime = java.time.LocalDateTime.now();
                JSONParser parser = new JSONParser();
                JSONObject jsonObject = null;

                try {
                    jsonObject = (JSONObject) parser.parse(new FileReader(PATH_JSON_HISTORY));
                } catch (ParseException e) {
                    System.err.println("ParseException");
                    System.err.println(Arrays.toString(e.getStackTrace()));
                } catch (IOException e) {
                    System.err.println("IOException");
                    System.err.println(Arrays.toString(e.getStackTrace()));
                }

                JSONArray jsonHistory = (JSONArray) jsonObject.get("history");

                for (int i = 0; i < jsonHistory.size(); i++) {
                    if (((JSONObject) jsonHistory.get(i)).get("tab name").equals(getCurrentTab().getId()) &&
                            ((JSONObject) jsonHistory.get(i)).get("website").equals(currentWebSite) &&
                            ((JSONObject) jsonHistory.get(i)).get("time end").equals("")) {
                        ((JSONObject) jsonHistory.get(i)).put("time end", currentTime.toString());
                        LocalDateTime dateBegin = LocalDateTime.parse((String) ((JSONObject) jsonHistory.get(i)).get("time begin"));
                        LocalDateTime dateEnd = LocalDateTime.parse((String) ((JSONObject) jsonHistory.get(i)).get("time end"));
                        ((JSONObject) jsonHistory.get(i)).put("time spent on website", ChronoUnit.SECONDS.between(dateBegin, dateEnd) + " sec");
                    }
                }

                jsonObject.put("history", jsonHistory);

                JsonElement jsonString = new JsonParser().parse(jsonObject.toJSONString());
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                try (FileWriter writer = new FileWriter(PATH_JSON_HISTORY);
                     JsonWriter jsonWriter = new JsonWriter(writer)) {
                    jsonWriter.setIndent("    ");
                    gson.toJson(jsonString, jsonWriter);
                } catch (IOException e) {
                    System.err.println("IOException");
                    System.err.println(Arrays.toString(e.getStackTrace()));
                }
            }
        }
    }

    public void createHTML() throws IOException {
        Stage htmlStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("html-creator.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 590);
        htmlStage.setResizable(false);
        htmlStage.setTitle("HTML creator");
        htmlStage.setScene(scene);
        htmlStage.show();
    }

    public void loadHTML() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select HTML File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                String htmlContent = Files.readString(Path.of(selectedFile.getPath()));
                getCurrentWebView().getEngine().loadContent(htmlContent);
            } catch (IOException e) {
                System.out.println("IOException");
                System.out.println(Arrays.toString(e.getStackTrace()));
            }
        }
    }

    public void editHTML() throws IOException {
        String html = "";

        if (getCurrentWebView().getEngine().getLoadWorker().getState().equals(Worker.State.RUNNING)) {
            WebEngine webEngine = getCurrentWebView().getEngine();
            html = new org.jsoup.helper.W3CDom().asString(webEngine.getDocument());
        }

        File file = new File(EDIT_HTML_FILE);
        file.createNewFile();

        try (FileWriter editHTMLFile = new FileWriter(file)) {
            editHTMLFile.write(html);
        }

        Stage htmlStage = new Stage();
        htmlStage.setOnCloseRequest(event -> file.delete());
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("html-editor.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 590);
        ControllerHTMLEditor controllerHTMLEditor = fxmlLoader.getController();
        controllerHTMLEditor.setControllerApplication(this);
        htmlStage.setResizable(false);
        htmlStage.setTitle("HTML editor");
        htmlStage.setScene(scene);
        htmlStage.show();
    }

    public void loadContent(String html) {
        getCurrentWebView().getEngine().loadContent(html);
    }

    public void onClose() {
        service.shutdown();

        try {
            if (!service.awaitTermination(1000, TimeUnit.MILLISECONDS)) {
                service.shutdownNow();
            }
        } catch (InterruptedException e) {
            service.shutdownNow();
        }

        JSONParser parser = new JSONParser();
        JSONObject jsonObject = null;

        try {
            jsonObject = (JSONObject) parser.parse(new FileReader(PATH_JSON_HISTORY));
        } catch (ParseException e) {
            System.err.println("ParseException");
            System.err.println(Arrays.toString(e.getStackTrace()));
        } catch (IOException e) {
            System.err.println("IOException");
            System.err.println(Arrays.toString(e.getStackTrace()));
        }

        JSONArray jsonHistory = (JSONArray) jsonObject.get("history");
        jsonHistory.clear();
        jsonObject.put("history", jsonHistory);

        JsonElement jsonString = new JsonParser().parse(jsonObject.toJSONString());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(PATH_JSON_HISTORY);
             JsonWriter jsonWriter = new JsonWriter(writer)) {
            jsonWriter.setIndent("    ");
            gson.toJson(jsonString, jsonWriter);
        } catch (IOException e) {
            System.err.println("IOException");
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }
}