package ru.mai.lessons.rpks.javawebbrowser;

import org.apache.log4j.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import org.w3c.dom.Document;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ru.mai.lessons.rpks.javawebbrowser.history.HistoryLine;
import ru.mai.lessons.rpks.javawebbrowser.pageStorage.PageStorageFromAndToJSON;
import ru.mai.lessons.rpks.javawebbrowser.history.History;
import ru.mai.lessons.rpks.javawebbrowser.history.HistoryFromAndToJSON;


public class WebBrowserImplementation implements Initializable {

    public static final Logger log = Logger.getLogger(WebBrowserImplementation.class.getName());
    @FXML
    private TabPane tabPane;
    @FXML
    private WebView currentWebView;
    private List<WebView> webViewList;
    @FXML
    private TextField textField;
    private String homePage;
    private WebHistory history;
    @FXML
    private Button backButton;
    @FXML
    private Button forwardButton;
    @FXML
    private Button reloadButton;
    @FXML
    private Button findButton;

    private boolean isCurrentPageBlockedHistory;
    private boolean isHistoryEnabled;

    private PageStorageFromAndToJSON favorites;
    private PageStorageFromAndToJSON blockedHistory;

    private HTMLEditorImplementation htmlEditorImplementation;

    private History historyStorage;

    private final String FAVORITES_PAGES_JSON = "JSONs/favorites.json";
    private final String BLOCKED_HISTORY_JSON = "JSONs/blocked.json";
    private final String DOWNLOADS_FOLDER = "downloads/";

    private final String SEARCH_QUERY = "https://www.google.com/search?q=";
    private final String HOME_PAGE = "https://google.com";
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        toolTips();
        historyStorage = new History();

        favorites = new PageStorageFromAndToJSON(jsonToString(FAVORITES_PAGES_JSON));
        blockedHistory = new PageStorageFromAndToJSON(jsonToString(BLOCKED_HISTORY_JSON));

        webViewList = new ArrayList<>();
        webViewList.add(currentWebView);
        isHistoryEnabled = true;

        htmlEditorImplementation = new HTMLEditorImplementation();
        homePage = HOME_PAGE;

        newPage();
    }

    private void toolTips() {
        backButton.setTooltip(new Tooltip("Page back"));
        forwardButton.setTooltip(new Tooltip("Page forward"));
        reloadButton.setTooltip(new Tooltip("Reload page"));
        findButton.setTooltip(new Tooltip("Find"));
    }

    private void initNewPage() {
        Tab newTab = new Tab("New Tab");
        tabPane.getTabs().add(newTab);
        textField.setText(homePage);

        configureNewTab(newTab);

        HBox box = setupHBoxForTabContent();
        setupWebView(box);

        box.getChildren().add(currentWebView);
    }

    private void configureNewTab(Tab newTab) {
        newTab.setOnSelectionChanged((val) -> onTabSelected());
        newTab.setOnCloseRequest((e) -> onTabClosed(newTab));
    }

    private HBox setupHBoxForTabContent() {
        HBox box = new HBox();
        box.prefHeightProperty().bind(tabPane.heightProperty());
        box.prefWidthProperty().bind(tabPane.widthProperty());
        box.setPadding(new Insets(40, 4, 4, 4));

        Tab currentTab = tabPane.getTabs().get(tabPane.getTabs().size() - 1);
        currentTab.setContent(box);
        return box;
    }

    private void setupWebView(HBox box) {
        currentWebView = new WebView();
        currentWebView.setOnKeyPressed(this::onEnter);
        currentWebView.setOnMouseClicked(this::onMouseClicked);
        webViewList.add(currentWebView);

        currentWebView.prefHeightProperty().bind(box.heightProperty());
        currentWebView.prefWidthProperty().bind(box.widthProperty());
    }

    private void loadStartPage() {
        currentWebView.getEngine().load(homePage);
    }

    public void loadPageFromTextField() {
        String text = textField.getText().trim();
        String query = text.startsWith("https://") || text.startsWith("http://") ? text : "https://" + text;

        String urlRegex = "^(http:\\/\\/|https:\\/\\/)?(www\\.)?([a-zA-Z0-9]+)\\.[a-zA-Z0-9]*\\.[a-z]{2,3}\\.([a-z]+)?$";
        query = Pattern.matches(urlRegex, query) ? query : SEARCH_QUERY + text.replace(" ", "+");

        try {
            currentWebView.getEngine().load(query);
        } catch (IllegalArgumentException e) {
            log.info("Wrong address");
            query = "";
        }

        textField.setText(query);
        loadEnvironment();
    }


    public void newPage() {
        log.info("New page");
        changeTabPaneWidth();
        initNewPage();
        loadStartPage();
        loadEnvironment();
        tabPane.getSelectionModel().selectLast();
    }

    private void onTabSelected() {

        Tab tab = tabPane.getSelectionModel().getSelectedItem();

        if (htmlEditorImplementation.isEnabled()) {
            htmlEditorImplementation.setDisabled();
        }

        HBox tabBox = (HBox) tab.getContent();
        if (tabBox.getChildren().size() > 1) {
            tabBox.getChildren().remove(1);
        }

        currentWebView = (WebView) (tabBox).getChildren().get(0);

        refreshTextField();
        changeBlockedHistoryButton();
        refreshTabText();
    }

    private void onTabClosed(Tab tab) {

        ObservableList<WebHistory.Entry> historyEntries =
                ((WebView) ((HBox) tab.getContent()).getChildren().get(0)).getEngine().getHistory().getEntries();

        if (historyEntries.size() > 1) {
            historyStorage.addHistoryLine(historyEntries);
        }

        if (tabPane.getTabs().size() <= 1) {
            newPage();
        }
    }

    private void changeTabPaneWidth() {

        int TAB_WIDTH = 150;
        if (tabPane.getTabs().isEmpty()) {
            tabPane.tabMaxWidthProperty().set(TAB_WIDTH);
        } else {
            tabPane.tabMaxWidthProperty().set(
                    Math.min(tabPane.getWidth() / tabPane.getTabs().size() + 30, TAB_WIDTH)
            );
        }

        tabPane.tabMinWidthProperty().set(tabPane.getTabMaxWidth());
    }

    private void loadEnvironment() {
        waitAndRefreshTextField();
    }


    private void changeBlockedHistoryButton() {
        isCurrentPageBlockedHistory = blockedHistory.isPagePresent(
                getSiteFromURLString(textField.getText()));
        log.info(getSiteFromURLString(textField.getText()));
        log.info("site blocked in history " + isCurrentPageBlockedHistory);
    }

    private void waitAndRefreshTextField() {
        currentWebView.getEngine().getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends Worker.State> observable,
                 Worker.State oldValue,
                 Worker.State newValue) -> {
                    if (newValue != Worker.State.SUCCEEDED) {
                        return;
                    }

                    refreshTextField();
                    changeBlockedHistoryButton();

                    refreshTabText();
                });
    }

    private void refreshTextField() {
        history = currentWebView.getEngine().getHistory();
        ObservableList<WebHistory.Entry> historyEntries = history.getEntries();

        for (WebHistory.Entry entry : historyEntries) {
            log.info(entry);
        }

        if (historyEntries.size() != 0) {
            textField.setText(historyEntries.get(history.getCurrentIndex()).getUrl());
        }
    }

    private void refreshTabText() {
        String title = currentWebView.getEngine().getTitle();
        if (title != null && !title.equals("")) {
            tabPane.getSelectionModel().getSelectedItem().setText(title);
        }
    }

    public void displayHistory() {

        history = currentWebView.getEngine().getHistory();
        List<HistoryLine> historyEntries = historyStorage.getHistoryLinesList();

        for (HistoryLine entry : historyEntries) {
            log.info(entry);
        }
    }

    private void refreshHistoryInStorage() {

        ObservableList<Tab> tabs = tabPane.getTabs();

        for (Tab tab : tabs) {
            List<WebHistory.Entry> entries =
                    ((WebView) ((HBox) tab.getContent()).getChildren().get(0))
                            .getEngine().getHistory().getEntries();

            List<WebHistory.Entry> newEntries = new ArrayList<>();

            for (WebHistory.Entry entry : entries) {
                if (!blockedHistory.isPagePresent(getSiteFromURLString(entry.getUrl()))) {
                    log.info("added page " + entry);
                    newEntries.add(entry);
                }
            }

            historyStorage.addHistoryLine(newEntries);
        }
        displayHistory();
    }

    private void writeHistoryToJSON() {

        HistoryFromAndToJSON historyFromAndToJSON = new HistoryFromAndToJSON();
        String HISTORY_JSON = "JSONs/history.json";
        String json = jsonToString(HISTORY_JSON);

        try (Writer writer = new FileWriter(new File(
                Objects.requireNonNull(getClass().getResource(HISTORY_JSON)).toURI()))) {

            if (json.length() != 0) {
                historyStorage.addHistory(
                        historyFromAndToJSON.deserializeToListHistoryLines(json)
                );
            }

            historyFromAndToJSON.serialize(historyStorage.getHistoryLinesList(), writer);
            historyStorage.clear();

        } catch (IOException | URISyntaxException e) {
            log.error("Error while writing history to json", e);
        }
    }


    public void pageBackButtonAction() {

        history = currentWebView.getEngine().getHistory();

        if (history.getCurrentIndex() == 0) {
            return;
        }

        history.go(-1);
        ObservableList<WebHistory.Entry> historyEntries = history.getEntries();
        textField.setText(historyEntries.get(history.getCurrentIndex()).getUrl());
        loadEnvironment();
    }

    public void pageForwardButtonAction() {

        history = currentWebView.getEngine().getHistory();
        ObservableList<WebHistory.Entry> historyEntries = history.getEntries();

        if (history.getCurrentIndex() + 1 >= historyEntries.size()) {
            return;
        }

        history.go(1);
        textField.setText(historyEntries.get(history.getCurrentIndex()).getUrl());
        loadEnvironment();
    }

    public void reloadPageButtonAction() {
        currentWebView.getEngine().reload();
    }

    public void favoriteButtonAction() {

        boolean isCurrentPageFavorite = favorites.isPagePresent(textField.getText());


        try (Writer writer = new FileWriter(new File((
                Objects.requireNonNull(getClass().getResource(FAVORITES_PAGES_JSON)).toURI())))) {


            if (isCurrentPageFavorite) {
                favorites.removeFrom(getCurrentPageAddress());
            } else {
                favorites.addPage(getCurrentPageAddress());
            }

            favorites.serialize(writer);

        } catch (IOException | URISyntaxException e) {
            log.error("Error while writing favorite page", e);
        }
    }

    public void blockedHistoryButtonAction() {

        isCurrentPageBlockedHistory = blockedHistory.isPagePresent(
                getSiteFromURLString(textField.getText()));


        try (Writer writer = new FileWriter(new File((
                Objects.requireNonNull(getClass().getResource(BLOCKED_HISTORY_JSON)).toURI())))) {

            if (isCurrentPageBlockedHistory) {
                blockedHistory.removeFrom(getSiteFromURLString(getCurrentPageAddress()));
            } else {
                blockedHistory.addPage(getSiteFromURLString(getCurrentPageAddress()));
            }
            isCurrentPageBlockedHistory = !isCurrentPageBlockedHistory;

            blockedHistory.serialize(writer);

        } catch (IOException | URISyntaxException e) {
            log.error("Error while writing blocked site", e);
        }
    }

    public void newEmptyPageButtonAction() {
        changeTabPaneWidth();
        initNewPage();
        tabPane.getSelectionModel().selectLast();
        textField.setText("");
        currentWebView.getEngine().loadContent("<html>HELLO! This is your NEW HTML PAGE. Have fun!</html>", "text/html");

        currentWebView.getEngine().getLoadWorker().stateProperty().addListener(
                (ObservableValue<? extends Worker.State> observable,
                 Worker.State oldValue,
                 Worker.State newValue) -> {
                    if (newValue != Worker.State.SUCCEEDED) {
                        return;
                    }
                    editButtonAction();
                });
    }

    public void editButtonAction() {

        Tab tab = tabPane.getSelectionModel().getSelectedItem();
        HBox tabBox = (HBox) tab.getContent();

        if (htmlEditorImplementation.isEnabled()) {
            tabBox.getChildren().remove(1);
            htmlEditorImplementation.setDisabled();
            return;
        }


        VBox editBox = new VBox();

        tabBox.getChildren().add(editBox);

        editBox.prefHeightProperty().bind(tabBox.heightProperty());
        editBox.prefWidthProperty().bind(tabBox.widthProperty());


        TextArea textArea = new TextArea();
        Button applyHTML = new Button("Apply");

        editBox.getChildren().add(textArea);
        editBox.getChildren().add(applyHTML);

        textArea.prefHeightProperty().bind(editBox.heightProperty());
        textArea.prefWidthProperty().bind(editBox.widthProperty());
        applyHTML.prefWidthProperty().bind(editBox.widthProperty());


        applyHTML.setOnAction((val) -> applyHTMLAction());

        htmlEditorImplementation.setTextArea(textArea);

        try {
            htmlEditorImplementation.setText(documentToString(currentWebView.getEngine().getDocument()));
        } catch (TransformerException e) {
            log.error("Error transform page to document", e);
        }

        htmlEditorImplementation.setEnabled();
    }

    public void downloadButtonAction() {

        try {

            String html = documentToString(currentWebView.getEngine().getDocument());

            File dir = new File(DOWNLOADS_FOLDER);
            if (!dir.exists()) {
                dir.mkdir();
            }

            File file = new File(getDownloadFileName("html"));

            Writer writer = new FileWriter(file);
            writer.write(html);
            writer.close();

        } catch (TransformerException | IOException e) {
            log.error("Error while working with file " + getDownloadFileName("html"));
        }
    }

    public void zipButtonAction() {

        downloadButtonAction();

        String sourceFile = getDownloadFileName("html");

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(getDownloadFileName("zip"));
        } catch (FileNotFoundException e) {
            log.error("File " + getDownloadFileName("zip") + " cannot be created", e);
            return;
        }
        ZipOutputStream zipOut = new ZipOutputStream(fos);

        File fileToZip = new File(sourceFile);

        FileInputStream fis;
        try {
            fis = new FileInputStream(fileToZip);
        } catch (FileNotFoundException e) {
            log.error("File " + fileToZip + " does not exist");
            return;
        }


        try {

            ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }

            zipOut.close();
            fis.close();
            fos.close();
        } catch (IOException e) {
            log.error("Error while working with file " + fileToZip.getName(), e);
        }

        (new File(sourceFile)).delete();
    }

    public void historyButtonAction() {
        if (!isHistoryEnabled) {
            return;
        }
        refreshHistoryInStorage();
        writeHistoryToJSON();
    }

    public void switchHistoryWritingButtonAction() {
        if (isHistoryEnabled) {
            historyButtonAction();
        }
        isHistoryEnabled = !isHistoryEnabled;

    }

    private void applyHTMLAction() {
        currentWebView.getEngine().loadContent(htmlEditorImplementation.getText(), "text/html");
        loadEnvironment();
    }

    @FXML
    public void onEnter(KeyEvent ae) {
        if (ae.getCode() == KeyCode.ENTER) {
            log.info("Key enter pressed");
            loadEnvironment();
        }
    }

    @FXML
    public void onMouseClicked(MouseEvent me) {
        log.info("Mouse clicked");
        loadEnvironment();
    }

    private String documentToString(Document doc) throws TransformerException {

        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{https://xml.apache.org/xslt}indent-amount", "4");

        StringWriter stringWriter = new StringWriter();
        StreamResult streamResult = new StreamResult(stringWriter);

        transformer.transform(new DOMSource(doc), streamResult);
        return stringWriter.getBuffer().toString();
    }

    private String getCurrentPageAddress() {
        return textField.getText();
    }

    private String getSiteFromURLString(String url) {

        String site;

        if (url.contains("://")) {
            int startInd = url.indexOf("://") + "://".length();
            int endInd = url.indexOf("/", startInd);
            if (endInd == -1) {
                site = url.substring(startInd);
            } else {
                site = url.substring(startInd, endInd);
            }

        } else {
            int endInd = url.indexOf("/");
            if (endInd == -1) {
                site = url;
            } else {
                site = url.substring(0, endInd);
            }
        }

        return site;
    }

    private String jsonToString(String jsonFileName) {

        StringBuilder json = new StringBuilder();

        if (getClass().getResource(jsonFileName) == null) {
            throw new RuntimeException("JSON file " + jsonFileName + " does not exist!");
        }

        try (Reader reader = new FileReader(new File(
                getClass().getResource(jsonFileName).toURI()))) {

            int ch;
            while ((ch = reader.read()) != -1) {
                json.append((char) ch);
            }

        } catch (IOException | URISyntaxException e) {
            log.error("Error parse json file " + jsonFileName + " to string", e);
        }

        return json.toString();
    }

    private String getDownloadFileName(String extension) {
        String title = currentWebView.getEngine().getTitle();
        if (title == null) {
            title = "myPage";
        }
        return DOWNLOADS_FOLDER + title + "." + extension;
    }


    public void shutdown() {
        if (!isHistoryEnabled) {
            return;
        }
        refreshHistoryInStorage();
        writeHistoryToJSON();
    }
}