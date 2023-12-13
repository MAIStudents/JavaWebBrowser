package ru.mai.lessons.rpks.controler;

import com.google.gson.Gson;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import ru.mai.lessons.rpks.BrowserHistoryEntry;
import ru.mai.lessons.rpks.WebViewExample;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainController implements Initializable {
    private final HashMap<Tab, WebView> webViews = new HashMap<>();
    private final HashMap<String, String> disableHistorySites = new HashMap<>();
    @FXML
    private TextField textField;

    @FXML
    private TabPane tabPane;

    @FXML
    private AnchorPane anchorPane;
    private Tab selectedTab;
    private WebHistory history;
    private WebEngine engine;
    private String homePage;
    private double webZoom;
    private final static String PATH_DOWNLOADS = WebViewExample.class.getResource("").getPath() + "downloads/";
    private final static String EDIT_HTML_FILE = PATH_DOWNLOADS + "index.html";

    private boolean isEnableHistory = true;

    private final List<BrowserHistoryEntry> historyEntries = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        homePage = "www.google.com";
        textField.setText(homePage);
        createTab();
        webZoom = 1;
        loadPage();

    }

    public void loadPage() {
        selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof WebView) {
            webViews.get(selectedTab).getEngine().load("https://" + textField.getText());
            selectedTab.setText(textField.getText());
            WebHistory.Entry lastEntry = getCurrentWebView().getEngine().getHistory().getEntries().stream().reduce((a, b) -> b).orElse(null);
            if (lastEntry != null) {
                if (!disableHistorySites.containsKey(lastEntry.getUrl())) {
                    BrowserHistoryEntry entry = new BrowserHistoryEntry(lastEntry.getUrl(), getCurrentWebsiteName(), lastEntry.getLastVisitedDate());
                    historyEntries.add(entry);
                }
            }
        } else {
            engine.load("https://" + textField.getText());
        }
    }


    public void loadContent(String html) {
        getCurrentWebView().getEngine().loadContent(html);
    }

    public void refreshPage() {
        selectedTab = tabPane.getSelectionModel().getSelectedItem();
        webViews.get(selectedTab).getEngine().reload();
    }

    public void zoomIn() {
        webZoom += 0.25;
        webViews.get(selectedTab).setZoom(webZoom);
    }

    public void zoomOut() {
        webZoom -= 0.25;
        webViews.get(selectedTab).setZoom(webZoom);
    }

    public void displayHistory() {

        if (isEnableHistory == false) return;

        String filePath = WebViewExample.class.getResource("/webHistory.json").getPath(); // Specify the file path here
        Gson gson = new Gson();
        String json = gson.toJson(historyEntries);
        try (FileWriter fileWriter = new FileWriter(filePath)) {
            fileWriter.write(json);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        showHistory(historyEntries);
    }

    public void disableHistoryForSite() {
        disableHistorySites.put(getCurrentWebView().getEngine().getLocation(), getCurrentWebsiteName());
    }

    public void disableHistory() {
        isEnableHistory = false;
        anchorPane.setStyle("-fx-background-color: #000;");
    }

    public void enableHistory() {
        isEnableHistory = true;
        anchorPane.setStyle("-fx-background-color: #fff;");
    }

    public void showHistory(List<BrowserHistoryEntry> historyEntries) {
        Stage historyStage = new Stage();
        VBox historyBox = new VBox();
        ListView<String> listView = new ListView<>();
        listView.getItems().setAll(historyEntries.stream().map(entry -> entry.getUrl()).collect(Collectors.toList()));
        historyBox.getChildren().addAll(new Label("History"), listView);

        Scene historyScene = new Scene(historyBox, 300, 200);
        historyStage.setScene(historyScene);
        historyStage.setTitle("History");
        historyStage.show();
    }


    public void back() {
        history = getCurrentWebView().getEngine().getHistory();
        ObservableList<WebHistory.Entry> entries = history.getEntries();
        history.go(-1);
        textField.setText(entries.get(history.getCurrentIndex()).getUrl());
        selectedTab.setText(textField.getText());
    }

    public void forward() {
        history = getCurrentWebView().getEngine().getHistory();
        ObservableList<WebHistory.Entry> entries = history.getEntries();
        history.go(1);
        textField.setText(entries.get(history.getCurrentIndex()).getUrl());
        selectedTab.setText(textField.getText());
    }


    private void addCloseButtonToTab(Tab tab) {
        Label closeButton = new Label("x");
        closeButton.setOnMouseClicked(event -> tabPane.getTabs().remove(tab));
        tab.setGraphic(closeButton);
        tab.setClosable(false);
    }

    public void createTab() {
        Tab tab = new Tab();
        tab.setText(homePage);
        WebView newWebView = new WebView();
        webViews.put(tab, newWebView);
        engine = newWebView.getEngine();
        engine.load("https://" + homePage);
        tab.setContent(newWebView);
        tabPane.getTabs().add(tab);
        addCloseButtonToTab(tab);
    }

    private WebView getCurrentWebView() {
        selectedTab = tabPane.getSelectionModel().getSelectedItem();
        return webViews.get(selectedTab);
    }

    private String getCurrentWebsiteName() {
        selectedTab = tabPane.getSelectionModel().getSelectedItem();
        return selectedTab.getText();
    }

    private final List<String> favoritesSite = new ArrayList<>();

    public void addFavoritesSite() {
        String currentSite = getCurrentWebsiteName();
        Alert alert;
        if (currentSite != null) {
            favoritesSite.add(currentSite);
            alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Add to favorites");
            alert.setHeaderText(null);
            alert.setContentText("The current site has added to the favorites list");
            alert.showAndWait();
        } else {
            alert = new Alert(Alert.AlertType.ERROR);
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
        listView.getItems().setAll(favoritesSite);
        favoritesBox.getChildren().addAll(new Label("Favorite sites"), listView);

        Scene favoritesScene = new Scene(favoritesBox, 300, 200);
        favoritesStage.setScene(favoritesScene);
        favoritesStage.setTitle("Favorite sites");
        favoritesStage.show();
    }


    public void saveZip() throws IOException {
        WebView currentWebView = getCurrentWebView();
        String currentWebsite = getCurrentWebsiteName();

        if (currentWebsite != null) {
            String filename = getSafeFilenameFromURL(currentWebView.getEngine().getLocation());
            File file = new File(PATH_DOWNLOADS + filename);
            FileUtils.copyURLToFile(new URL(currentWebView.getEngine().getLocation()), file);

            File zipOutFile = new File(PATH_DOWNLOADS + filename + ".zip");

            try (FileOutputStream outputStreamZip = new FileOutputStream(zipOutFile);
                 FileInputStream inputStream = new FileInputStream(file);
                 ZipOutputStream zipOut = new ZipOutputStream(outputStreamZip)) {
                ZipEntry zipEntry = new ZipEntry(filename);
                zipOut.putNextEntry(zipEntry);

                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    zipOut.write(buffer, 0, len);
                }

                zipOut.closeEntry();
            }

            file.delete();
        }
    }

    private String getSafeFilenameFromURL(String url) {
        String safeChars = "[^a-zA-Z0-9.-]";
        String basename = new File(url).getName();
        return basename.replaceAll(safeChars, "_");
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
        FXMLLoader fxmlLoader = new FXMLLoader(WebViewExample.class.getResource("/edit-html.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 590);
        HTMLEditorController controllerHTMLEditor = fxmlLoader.getController();
        controllerHTMLEditor.setControllerApplication(this);
        htmlStage.setResizable(false);
        htmlStage.setTitle("HTML editor");
        htmlStage.setScene(scene);
        htmlStage.show();
    }

    public void createHTML() throws IOException {
        Stage htmlStage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(WebViewExample.class.getResource("/create-html.fxml"));
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
                System.out.println(e.getMessage());
            }
        }
    }


}