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
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.tuple.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private TabPane tabPane;
    private HashMap<Tab, WebView> tabs;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Pair<Tab, WebView> mainTab = createTab();
        Tab toAddTab = new Tab("+");

        toAddTab.setOnSelectionChanged(event -> {
            if (toAddTab.isSelected()) {
                addTab();
            }
        });

        mainTab.getKey().setText("JavaWebBrowser");

        tabPane.getTabs().add(mainTab.getKey());
        tabPane.getTabs().add(toAddTab);

        tabs = new HashMap<>();
        tabs.put(mainTab.getKey(), mainTab.getValue());
    }

    private WebView getCurrentWebView(Tab currentTab) {
        return tabs.get(currentTab);
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

        try {
            addNoteHistory(requestTextWithHTTPS, java.time.LocalDateTime.now());
        } catch (Exception e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
        }

        getCurrentWebView(getCurrentTab()).getEngine().load(requestTextWithHTTPS);
    }

    public void reloadPage() {
        getCurrentWebView(getCurrentTab()).getEngine().reload();
    }

    public void addTab() {
        Pair<Tab, WebView> newTab = createTab();
        tabs.put(newTab.getKey(), newTab.getValue());

        tabPane.getTabs().add(tabPane.getTabs().size() - 1, newTab.getKey());
        tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2);
    }

    public void backOnHistory() {
        WebHistory history = getCurrentWebView(getCurrentTab()).getEngine().getHistory();
        ObservableList<WebHistory.Entry> entries = history.getEntries();

        if (history.getCurrentIndex() > 0) {
            history.go(-1);
            getCurrentTextField().setText(entries.get(history.getCurrentIndex()).getUrl());
            getCurrentTab().setText(entries.get(history.getCurrentIndex()).getUrl());
        }
    }

    public void forwardOnHistory() {
        WebHistory history = getCurrentWebView(getCurrentTab()).getEngine().getHistory();
        ObservableList<WebHistory.Entry> entries = history.getEntries();

        if (history.getCurrentIndex() < entries.size() - 1) {
            history.go(1);
            getCurrentTextField().setText(entries.get(history.getCurrentIndex()).getUrl());
            getCurrentTab().setText(entries.get(history.getCurrentIndex()).getUrl());
        }
    }

    private void addNoteHistory(String website, LocalDateTime visitTime) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(getClass().getResource("history.json").getPath()));
        JSONArray jsonHistory = (JSONArray) jsonObject.get("history");
        JSONObject newJsonObject = new JSONObject();

        newJsonObject.put("website", website);
        newJsonObject.put("time begin", visitTime.toString());
        newJsonObject.put("time end", "");

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

    private Pair<Tab, WebView> createTab() {
        AnchorPane newAnchorPane = createAnchorPane();
        WebView mewWebView = createWebView();
        VBox newVBox = createVBox();
        HBox newHBoxFirst = createHBoxFirst();
        HBox newHBoxSecond = createHBoxSecond();

        newHBoxFirst.getChildren().add(createButtonBackOnHistory());
        newHBoxFirst.getChildren().add(createButtonForwardOnHistory());
        newHBoxFirst.getChildren().add(createButtonReloadPage());
        newHBoxFirst.getChildren().add(createTextField());
        newHBoxFirst.getChildren().add(createButtonLoadPage());

        newVBox.getChildren().add(newHBoxFirst);
        newVBox.getChildren().add(newHBoxSecond);

        newAnchorPane.getChildren().add(mewWebView);
        newAnchorPane.getChildren().add(newVBox);

        Tab newTab = new Tab("New Tab");
        newTab.setContent(newAnchorPane);

        return Pair.of(newTab, mewWebView);
    }

    private AnchorPane createAnchorPane() {
        AnchorPane anchorPane = new AnchorPane();

        anchorPane.prefWidth(900);
        anchorPane.prefHeight(590);

        return anchorPane;
    }

    private VBox createVBox() {
        VBox vBox = new VBox();

        vBox.prefWidth(900);
        vBox.prefWidth(90);
        AnchorPane.setTopAnchor(vBox, 0.0);
        AnchorPane.setRightAnchor(vBox, 0.0);
        AnchorPane.setBottomAnchor(vBox, 500.0);
        AnchorPane.setLeftAnchor(vBox, 0.0);

        return vBox;
    }

    private HBox createHBoxFirst() {
        HBox hBox = new HBox();

        hBox.prefWidth(200);
        hBox.prefHeight(100);
        VBox.setMargin(hBox, new Insets(0, 0, 0, 0));

        return hBox;
    }

    private HBox createHBoxSecond() {
        HBox hBox = new HBox();

        hBox.prefWidth(200);
        hBox.prefHeight(100);

        return hBox;
    }

    private WebView createWebView() {
        WebView webView = new WebView();

        webView.prefWidth(900);
        webView.prefHeight(500);
        AnchorPane.setTopAnchor(webView, 88.0);
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