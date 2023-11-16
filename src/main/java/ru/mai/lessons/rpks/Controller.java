package ru.mai.lessons.rpks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class Controller {
    @FXML
    private AnchorPane mainPain;
    @FXML
    private WebView webView;
    @FXML
    private TextField textField;
    private WebHistory history;

    public void loadPage() throws IOException, ParseException {
        String requestText = textField.getText();
        String requestTextWithHTTPS = "";

        if (!requestText.contains("https://")) {
            requestTextWithHTTPS = "https://" + requestText;
        } else {
            requestTextWithHTTPS = requestText;
        }

        getTabPane().getSelectionModel().getSelectedItem().setText(requestTextWithHTTPS);
        addNoteHistory(requestTextWithHTTPS, java.time.LocalDateTime.now());
        webView.getEngine().load(requestTextWithHTTPS);
    }

    public void reloadPage() {
        webView.getEngine().reload();
    }

    public void addTab() throws IOException {
        TabPane tabPane = getTabPane();

        Tab newTab = new Tab("New Tab");
        newTab.setContent(FXMLLoader.load(getClass().getResource("tab-template.fxml")));
        tabPane.getTabs().add(tabPane.getTabs().size() - 1, newTab);
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2);
    }

    public void backOnHistory() {
        history = webView.getEngine().getHistory();
        ObservableList<WebHistory.Entry> entries = history.getEntries();

        if (history.getCurrentIndex() > 0) {
            history.go(-1);
            textField.setText(entries.get(history.getCurrentIndex()).getUrl());
        }
    }

    public void forwardOnHistory() {
        history = webView.getEngine().getHistory();
        ObservableList<WebHistory.Entry> entries = history.getEntries();

        if (history.getCurrentIndex() < entries.size() - 1) {
            history.go(1);
            textField.setText(entries.get(history.getCurrentIndex()).getUrl());
        }
    }

    private TabPane getTabPane() {
        return (TabPane) mainPain.getScene().lookup("#tabPane");
    }

    private void addNoteHistory(String website, LocalDateTime visitTime) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(getClass().getResource("history.json").getPath()));
        JSONArray jsonHistory = (JSONArray) jsonObject.get("history");
        JSONObject newJsonObject = new JSONObject();

        newJsonObject.put("website", website);
        newJsonObject.put("visitTime", visitTime.toString());

        jsonHistory.add(newJsonObject);
        jsonObject.put("history", jsonHistory);

        JsonElement jsonString = new JsonParser().parse(jsonObject.toJSONString());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        FileWriter writer = new FileWriter(getClass().getResource("history.json").getPath());
        JsonWriter jsonWriter = new JsonWriter(writer);

        jsonWriter.setIndent("    ");
        gson.toJson(jsonString, jsonWriter);

        jsonWriter.close();
    }
}