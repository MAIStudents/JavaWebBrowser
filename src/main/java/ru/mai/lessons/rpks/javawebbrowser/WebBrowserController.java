package ru.mai.lessons.rpks.javawebbrowser;

import org.apache.log4j.Logger;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.geometry.Insets;
import org.w3c.dom.Document;
import ru.mai.lessons.rpks.javawebbrowser.history.HistoryEntry;
import ru.mai.lessons.rpks.javawebbrowser.pageStorage.PageStorageJSON;
import ru.mai.lessons.rpks.javawebbrowser.history.BrowserHistory;
import ru.mai.lessons.rpks.javawebbrowser.history.HistoryJSON;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class WebBrowserController implements Initializable {

    public static final Logger log = Logger.getLogger(WebBrowserController.class.getName());
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
    private Button starButton;
    @FXML
    private Button blockedHistoryButton;
    @FXML
    private Button findButton;
    @FXML
    private Button newPageButton;
    @FXML
    private Button emptyPageButton;
    @FXML
    private Button editButton;
    @FXML
    private Button downloadButton;
    @FXML
    private Button zipButton;
    @FXML
    private Button historyButton;
    @FXML
    private Button disableHistoryButton;

    private boolean isCurrentPageFavorite;
    private boolean isCurrentPageBlockedHistory;
    private boolean isHistoryEnabled;

    private PageStorageJSON favorites;
    private PageStorageJSON blockedHistory;

    private HTMLEditor htmlEditor;

    private BrowserHistory historyStorage;

    private final int TAB_WIDTH = 150;
    private final String FAVORITES_PAGES_JSON = "JSONs/favorites.json";
    private final String HISTORY_JSON = "JSONs/history.json";
    private final String BLOCKED_HISTORY_JSON = "JSONs/blocked.json";
    private final String DOWNLOADS_FOLDER = "downloads/";

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        setToolTips();
        historyStorage = new BrowserHistory();

        favorites = new PageStorageJSON(jsonToString(FAVORITES_PAGES_JSON));
        blockedHistory = new PageStorageJSON(jsonToString(BLOCKED_HISTORY_JSON));

        webViewList = new ArrayList<>();

        webViewList.add(currentWebView);
        isHistoryEnabled = true;

        htmlEditor = new HTMLEditor();

        homePage = "https://duckduckgo.com/";

        newPage();
    }

    private void setToolTips() {
        backButton.setTooltip(new Tooltip("Page back"));
        forwardButton.setTooltip(new Tooltip("Page forward"));
        reloadButton.setTooltip(new Tooltip("Reload page"));
        starButton.setTooltip(new Tooltip("Add/Remove favorite page"));
        blockedHistoryButton.setTooltip(new Tooltip("Disable/Enable collecting history for this page"));
        findButton.setTooltip(new Tooltip("Find"));
        newPageButton.setTooltip(new Tooltip("New page"));
        emptyPageButton.setTooltip(new Tooltip("Create new html page"));
        editButton.setTooltip(new Tooltip("Edit html"));
        downloadButton.setTooltip(new Tooltip("Download page"));
        zipButton.setTooltip(new Tooltip("Download page compressed to zip"));
        historyButton.setTooltip(new Tooltip("Save history"));
        disableHistoryButton.setTooltip(new Tooltip("Disable/Enable collecting history"));
    }

    private void initNewPage() {

        Tab newTab = new Tab("New Tab");
        tabPane.getTabs().add(newTab);
        textField.setText(homePage);


        newTab.setOnSelectionChanged((val) -> onTabSelected());
        newTab.setOnCloseRequest((e) -> onTabClosed(e, newTab));

        HBox box = new HBox();

        box.prefHeightProperty().bind(tabPane.heightProperty());
        box.prefWidthProperty().bind(tabPane.widthProperty());

        box.setPadding(new Insets(40, 4, 4, 4));
        newTab.setContent(box);


        currentWebView = new WebView();

        currentWebView.setOnKeyPressed((ae) -> onEnter(ae));
        currentWebView.setOnMouseClicked((me) -> onMouseClicked(me));

        webViewList.add(currentWebView);

        currentWebView.prefHeightProperty().bind(box.heightProperty());
        currentWebView.prefWidthProperty().bind(box.widthProperty());

        box.getChildren().add(currentWebView);
    }
    private void loadStartPage() {
        currentWebView.getEngine().load(homePage);
    }
    public void loadPageFromTextField() {

        String text = textField.getText();
        String query = text;

        if (!text.startsWith("https://") && !(text.startsWith("http://"))) {
            query = "https://" + text;
        }

        String regex = "^(http:\\/\\/|https:\\/\\/)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z]{3}\\.([a-z]+)?$";

        if (!Pattern.compile(regex).matcher(query).matches()) {
            query = "https://duckduckgo.com/?q=" + String.join("+", text.split(" "));
        }

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

        if (htmlEditor.isEnabled()) {
            htmlEditor.setDisabled();
        }

        HBox tabBox = (HBox) tab.getContent();
        if (tabBox.getChildren().size() > 1) {
            tabBox.getChildren().remove(1);
        }

        currentWebView = (WebView) (tabBox).getChildren().get(0);

        refreshTextField();
        changeStarButton();
        changeBlockedHistoryButton();
        refreshTabText();
    }
    private void onTabClosed(Event e, Tab tab) {

        ObservableList<WebHistory.Entry> historyEntries =
                ((WebView) ((HBox) tab.getContent()).getChildren().get(0)).getEngine().getHistory().getEntries();

        if (historyEntries.size() > 1) {
            historyStorage.addHistoryEntries(historyEntries);
        }

        if (tabPane.getTabs().size() <= 1) {
            newPage();
        }
    }
    private void changeTabPaneWidth() {

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
    private void changeStarButton() {
        isCurrentPageFavorite = favorites.isPagePresent(textField.getText());
        starButtonSwitch(isCurrentPageFavorite);
    }
    private void changeBlockedHistoryButton() {
        isCurrentPageBlockedHistory = blockedHistory.isPagePresent(
                getSiteFromURLString(textField.getText()));
        log.info(getSiteFromURLString(textField.getText()));
        log.info("site blocked in history " + isCurrentPageBlockedHistory);
        blockedHistoryButtonSwitch(isCurrentPageBlockedHistory);
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
                    changeStarButton();
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
        List<HistoryEntry> historyEntries = historyStorage.getHistoryEntriesList();

        for (HistoryEntry entry : historyEntries) {
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

            historyStorage.addHistoryEntries(newEntries);
        }
        displayHistory();
    }
    private void writeHistoryToJSON() {

        HistoryJSON historyJSON = new HistoryJSON();
        String json = jsonToString(HISTORY_JSON);

        try (Writer writer = new FileWriter(new File(
                getClass().getResource(HISTORY_JSON).toURI()))) {

            if (json.length() != 0) {
                historyStorage.addHistoryList(
                        historyJSON.deserializeToListHistoryEntry(json)
                );
            }

            historyJSON.serialize(historyStorage.getHistoryEntriesList(), writer);
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

        isCurrentPageFavorite = favorites.isPagePresent(textField.getText());

        starButtonSwitch(!isCurrentPageFavorite);

        try (Writer writer = new FileWriter(new File((
                getClass().getResource(FAVORITES_PAGES_JSON).toURI())))) {


            if (isCurrentPageFavorite) {
                favorites.removeFrom(getCurrentPageAddress());
            } else {
                favorites.addPage(getCurrentPageAddress());
            }
            isCurrentPageFavorite = !isCurrentPageFavorite;

            favorites.serialize(writer);

        } catch (IOException | URISyntaxException e) {
            log.error("Error while writing favorite page", e);
        }
    }
    public void blockedHistoryButtonAction() {

        isCurrentPageBlockedHistory = blockedHistory.isPagePresent(
                getSiteFromURLString(textField.getText()));

        blockedHistoryButtonSwitch(!isCurrentPageBlockedHistory);

        try (Writer writer = new FileWriter(new File((
                getClass().getResource(BLOCKED_HISTORY_JSON).toURI())))) {

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
        currentWebView.getEngine().loadContent("<html>hello, world</html>", "text/html");

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

        if (htmlEditor.isEnabled()) {
            tabBox.getChildren().remove(1);
            htmlEditor.setDisabled();
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

        htmlEditor.setTextArea(textArea);

        try {
            htmlEditor.setText(documentToString(currentWebView.getEngine().getDocument()));
        } catch (TransformerException e) {
            log.error("Error transform page to document", e);
        }

        htmlEditor.setEnabled();
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
        switchHistoryWrite(isHistoryEnabled);
    }
    private void applyHTMLAction() {
        currentWebView.getEngine().loadContent(htmlEditor.getText(), "text/html");
        loadEnvironment();
    }

    private void switchHistoryWrite(boolean enabled) {
        disableHistoryButton.setText(enabled ? "⦸" : "✔");
    }
    private void blockedHistoryButtonSwitch(boolean enabled) {
        blockedHistoryButton.setText(enabled ? "✔" : "⦸");
    }
    private void starButtonSwitch(boolean enabled) {
        starButton.setText(enabled ? "★" : "☆");
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
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

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