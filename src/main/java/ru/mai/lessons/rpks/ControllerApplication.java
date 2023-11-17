package ru.mai.lessons.rpks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ControllerApplication implements Initializable {
    @FXML
    private TabPane tabPane;
    private List<Tab> tabs;
    private boolean isEnableHistory;
    private List<String> untrackedSites;
    private long idTab;
    private final static String PATH_DOWNLOADS = "/home/alexandr/MAI/5-semestr/RPKS/JavaLabs/JavaWebBrowser/src/main/resources/ru/mai/lessons/rpks/downloads/";
    private final static String PATH_JSON_HISTORY = "/home/alexandr/MAI/5-semestr/RPKS/JavaLabs/JavaWebBrowser/src/main/resources/ru/mai/lessons/rpks/history.json";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        untrackedSites = new ArrayList<>();
        tabs = new ArrayList<>();
        isEnableHistory = true;
        Tab mainTab = createTab();
        Tab toAddTab = new Tab("+");

        toAddTab.setOnSelectionChanged(event -> {
            if (toAddTab.isSelected()) {
                addTab();
            }
        });

        mainTab.setText("JavaWebBrowser");

        tabPane.getTabs().add(mainTab);
        tabPane.getTabs().add(toAddTab);

        tabs.add(mainTab);
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

        String beforeWebsite = getCurrentWebsite();
        tabPane.getSelectionModel().getSelectedItem().setText(requestTextWithHTTPS);
        getCurrentWebView().getEngine().load(requestTextWithHTTPS);
        String afterWebsite = getCurrentWebsite();

        if (isEnableHistory && !untrackedSites.contains(requestTextWithHTTPS)) {
            addNoteHistory(beforeWebsite, afterWebsite, java.time.LocalDateTime.now());
        }
    }

    public void reloadPage() {
        getCurrentWebView().getEngine().reload();
    }

    public void addTab() {
        Tab newTab = createTab();
        tabs.add(newTab);

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
    }

    public void addToFavorites() {

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

        tabs.remove(getCurrentTab());
    }

    private Tab createTab() {
        AnchorPane newAnchorPane = createAnchorPane();
        WebView mewWebView = createWebView();
        HBox newHBox = createHBox();

        newHBox.getChildren().add(createButtonBackOnHistory());
        newHBox.getChildren().add(createButtonForwardOnHistory());
        newHBox.getChildren().add(createButtonReloadPage());
        newHBox.getChildren().add(createTextField());
        newHBox.getChildren().add(createButtonLoadPage());

        newAnchorPane.getChildren().add(mewWebView);
        newAnchorPane.getChildren().add(newHBox);

        Tab newTab = new Tab("New Tab");
        newTab.setId("tab #" + idTab++);
        newTab.setContent(newAnchorPane);
        newTab.setOnCloseRequest(event -> onCloseTab());

        return newTab;
    }

    private AnchorPane createAnchorPane() {
        AnchorPane anchorPane = new AnchorPane();

        anchorPane.prefWidth(900);
        anchorPane.prefHeight(600);

        return anchorPane;
    }

    private HBox createHBox() {
        HBox hBox = new HBox();

        hBox.prefWidth(900);
        hBox.prefHeight(50);
        VBox.setMargin(hBox, new Insets(0, 0, 0, 0));

        return hBox;
    }

    private WebView createWebView() {
        WebView webView = new WebView();

        webView.prefWidth(900);
        webView.prefHeight(550);
        AnchorPane.setTopAnchor(webView, 45.0);
        AnchorPane.setRightAnchor(webView, 0.0);
        AnchorPane.setBottomAnchor(webView, 0.0);
        AnchorPane.setLeftAnchor(webView, 0.0);
        webView.setId("webView");

        return webView;
    }

    private TextField createTextField() {
        TextField newTextField = new TextField();

        newTextField.setMinWidth(559.2);
        newTextField.setMaxWidth(559.2);
        newTextField.setMinHeight(24);
        newTextField.setMaxHeight(24);
        newTextField.setId("textField");
        HBox.setMargin(newTextField, new Insets(10, 10, 10, 0));

        return newTextField;
    }

    private Button createButtonBackOnHistory() {
        Button buttonBackOnHistory = new Button();

        buttonBackOnHistory.setText("back");
        buttonBackOnHistory.setMinWidth(52);
        buttonBackOnHistory.setMaxWidth(52);
        buttonBackOnHistory.setMinHeight(24);
        buttonBackOnHistory.setMaxHeight(24);
        buttonBackOnHistory.setId("buttonBackOnHistory");
        HBox.setMargin(buttonBackOnHistory, new Insets(10, 0, 10, 20));

        buttonBackOnHistory.setOnAction(actionEvent -> backOnHistory());

        return buttonBackOnHistory;
    }

    private Button createButtonForwardOnHistory() {
        Button buttonForwardOnHistory = new Button();

        buttonForwardOnHistory.setText("forward");
        buttonForwardOnHistory.setMinWidth(68);
        buttonForwardOnHistory.setMaxWidth(68);
        buttonForwardOnHistory.setMinHeight(24);
        buttonForwardOnHistory.setMaxHeight(24);
        buttonForwardOnHistory.setId("buttonForwardOnHistory");
        HBox.setMargin(buttonForwardOnHistory, new Insets(10, 10, 10, 10));

        buttonForwardOnHistory.setOnAction(actionEvent -> forwardOnHistory());

        return buttonForwardOnHistory;
    }

    private Button createButtonReloadPage() {
        Button buttonReloadPage = new Button();

        buttonReloadPage.setText("reload");
        buttonReloadPage.setMinWidth(77.6);
        buttonReloadPage.setMaxWidth(77.6);
        buttonReloadPage.setMinHeight(24);
        buttonReloadPage.setMaxHeight(24);
        buttonReloadPage.setId("buttonReloadPage");
        HBox.setMargin(buttonReloadPage, new Insets(10, 10, 10, 0));

        buttonReloadPage.setOnAction(actionEvent -> reloadPage());

        return buttonReloadPage;
    }

    private Button createButtonLoadPage() {
        Button buttonLoadPage = new Button();

        buttonLoadPage.setText("load");
        buttonLoadPage.setMinWidth(71.2);
        buttonLoadPage.setMaxWidth(71.2);
        buttonLoadPage.setMinHeight(24);
        buttonLoadPage.setMaxHeight(24);
        buttonLoadPage.setId("buttonLoadPage");
        HBox.setMargin(buttonLoadPage, new Insets(10, 10, 10, 0));

        buttonLoadPage.setOnAction(actionEvent -> loadPage());

        return buttonLoadPage;
    }
}