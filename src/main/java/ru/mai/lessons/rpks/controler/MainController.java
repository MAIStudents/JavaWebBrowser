package ru.mai.lessons.rpks.controler;

import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FileUtils;
import ru.mai.lessons.rpks.WebViewExample;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.lang.Integer.parseInt;

public class MainController implements Initializable {


    private WebView webView;

    private final HashMap<Tab, WebView> webViews = new HashMap<>();

    @FXML
    private TextField textField;

    @FXML
    private TabPane tabPane;

    private Tab selectedTab;
    private WebHistory history;
    private WebEngine engine;
    private String homePage;
    private double webZoom;
    private final static String PATH_DOWNLOADS = WebViewExample.class.getResource("").getPath() + "downloads/";
    private final static String EDIT_HTML_FILE = PATH_DOWNLOADS + "index.html";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        homePage = "www.google.com";
        textField.setText(homePage);
        createTab();
        webZoom = 1;
        loadPage();

        System.out.println(PATH_DOWNLOADS);
    }

    public void loadPage() {
        selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof WebView) {
            webViews.get(selectedTab).getEngine().load("https://" + textField.getText());
            selectedTab.setText(textField.getText());
        } else {
            engine.load("https://" + textField.getText());
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
        FXMLLoader fxmlLoader = new FXMLLoader(WebViewExample.class.getResource("/edit-html.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 590);
        HTMLEditorController controllerHTMLEditor = fxmlLoader.getController();
        controllerHTMLEditor.setControllerApplication(this);
        htmlStage.setResizable(false);
        htmlStage.setTitle("HTML editor");
        htmlStage.setScene(scene);
        htmlStage.show();
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
        selectedTab = tabPane.getSelectionModel().getSelectedItem();
        history = webViews.get(selectedTab).getEngine().getHistory();
        ObservableList<WebHistory.Entry> entries = history.getEntries();
        for (WebHistory.Entry entry : entries) {
            System.out.println(entry.getUrl() + " " + entry.getLastVisitedDate());
        }
    }

    public void back() {
        selectedTab = tabPane.getSelectionModel().getSelectedItem();
        history = webViews.get(selectedTab).getEngine().getHistory();
        ObservableList<WebHistory.Entry> entries = history.getEntries();
        history.go(-1);
        textField.setText(entries.get(history.getCurrentIndex()).getUrl());
        selectedTab.setText(textField.getText());
    }

    public void forward() {
        selectedTab = tabPane.getSelectionModel().getSelectedItem();
        history = webViews.get(selectedTab).getEngine().getHistory();
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