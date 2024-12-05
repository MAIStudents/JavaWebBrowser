package ru.mai.lessons.rpks;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URI;
import java.net.URL;

import java.nio.file.Paths;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


public class Controller {

    @FXML
    private AnchorPane globalAnchor;

    @FXML
    private TabPane tabPane;

    @FXML
    private TextField textField;

    @FXML
    private TableView<HistoryEntry> historyTable;

    @FXML
    private TableColumn<HistoryEntry, String> urlColumn;

    @FXML
    private TableColumn<HistoryEntry, LocalDateTime> visitTimeColumn;

    @FXML
    private TableColumn<HistoryEntry, Long> timeSpentColumn;

    @FXML
    private TableView<LikedEntry> tableLiked;

    @FXML
    private TableColumn<LikedEntry, String> urlColumnLiked;

    @FXML
    private AnchorPane PaneForImage;

    @FXML
    private ImageView backgroundImage;

    @FXML
    private Button newTabButton;

    @FXML
    private Button buttonAllLiked;

    @FXML
    private Button buttonHistory;

    @FXML
    private CheckBox checkBoxLike;

    @FXML
    private Button clearButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button saveZipButton;

    @FXML
    private Button editButton;

    @FXML
    private AnchorPane anchorForEdit;

    @FXML
    public TextArea htmlTextArea;
    @FXML
    public Button buttonCloseEditor;
    @FXML
    public Button buttonLoad;

    @FXML
    public CheckBox IncognitoCheckBox;

    @FXML
    public CheckBox privateCheckBox;

    @FXML
    public TableView<PrivateEntry> tablePrivate;
    @FXML
    public TableColumn<PrivateEntry, String> urlColumnPrivate;
    @FXML
    public Button buttonAllPrivate;



    private final HistoryManager historyManager;
    private final LikeManager likeManager;
    private final PrivateManager privateManager;
    private static final String HISTORY_FILE_PATH = Paths.get("src/main/resources/ru/mai/lessons/rpks/history.json").toString();
    private static final String LIKED_FILE_PATH = Paths.get("src/main/resources/ru/mai/lessons/rpks/liked.json").toString();
    private boolean isIncognito = false;



    public void clickIncognito() {
        isIncognito = !isIncognito;
        if (isIncognito) {
            globalAnchor.setStyle("-fx-background-color: #dd8989;");
        } else {
            globalAnchor.setStyle("-fx-background-color: #2b2b2b;");
        }

        newTabButton.setDisable(isIncognito);
        buttonAllLiked.setDisable(isIncognito);
        checkBoxLike.setDisable(isIncognito);
        saveButton.setDisable(isIncognito);
        saveZipButton.setDisable(isIncognito);
        editButton.setDisable(isIncognito);
        privateCheckBox.setDisable(isIncognito);
        buttonAllPrivate.setDisable(isIncognito);

        ObservableList<Tab> tabs = tabPane.getTabs();

        for (int i = tabs.size() - 1; i >= 0; i--) {
            Tab tab = tabs.get(i);

            if (tab instanceof BrowserTab) {
                ((BrowserTab) tab).setIsIncognito(isIncognito);
            }

        }
    }


    public Controller() {
        historyManager = new HistoryManager();
        likeManager = new LikeManager();
        privateManager = new PrivateManager();
    }

    public void saveHistory() {
        try {
            historyManager.saveHistoryToFile(HISTORY_FILE_PATH);
            System.out.println("History saved");
        } catch (IOException e) {
            System.err.println("Error during saving history to" + HISTORY_FILE_PATH + e.getMessage());
        }
    }


    public void loadHistory() {
        try {
            File historyFile = new File(HISTORY_FILE_PATH);
            if (!historyFile.exists()) {
                System.out.println("History file does not exist at: " + HISTORY_FILE_PATH);
                return;
            }
            if (historyFile.length() == 0) {
                System.out.println("History file is empty.");
                return;
            }
            historyManager.loadHistoryFromFile(HISTORY_FILE_PATH);
            if (historyManager.getHistory() == null || historyManager.getHistory().isEmpty()) {
                return;
            }
            historyTable.setItems(historyManager.getHistory());
            historyTable.refresh();
            System.out.println("History success loaded!");
        } catch (IOException e) {
            System.err.println("Problem during loading: " + HISTORY_FILE_PATH + e.getMessage());
        } catch (NullPointerException e) {
            System.err.println("NullPointerException occurred: " + e.getMessage());
        }
    }

    public void saveLiked() {
        try {
            likeManager.saveLikedToFile(LIKED_FILE_PATH);
            System.out.println("Liked saved");
        } catch (IOException e) {
            System.err.println("Error during saving likes to" + LIKED_FILE_PATH + e.getMessage());
        }
    }


    public void loadLiked() {
        try {
            File likeFile = new File(LIKED_FILE_PATH);
            if (!likeFile.exists()) {
                System.out.println("Liked file does not exist at: " + LIKED_FILE_PATH);
                return;
            }
            if (likeFile.length() == 0) {
                System.out.println("Liked file is empty.");
                return;
            }
            likeManager.loadLikedFromFile(LIKED_FILE_PATH);
            if (likeManager.getLiked() == null || likeManager.getLiked().isEmpty()) {
                return;
            }
            tableLiked.setItems(likeManager.getLiked());
            tableLiked.refresh();
            System.out.println("Liked success loaded!");
        } catch (IOException e) {
            System.err.println("Problem during loading: " + LIKED_FILE_PATH + e.getMessage());
        } catch (NullPointerException e) {
            System.err.println("NullPointerException occurred: " + e.getMessage());
        }
    }

    @FXML
    public void initialize() {
        backgroundImage.fitWidthProperty().bind(PaneForImage.widthProperty());
        backgroundImage.fitHeightProperty().bind(PaneForImage.heightProperty());

        setupHistoryTable();
        loadHistory();
        setupLikedTable();
        loadLiked();
        setupLPrivateTable();
        createNewTab();

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> updateCheckBoxState());


        tabPane.getTabs().addListener((ListChangeListener.Change<?> change) -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    updateBackgroundVisibility();

                    if (tabPane.getTabs().isEmpty()) {
                        checkBoxLike.setSelected(false);
                        privateCheckBox.setSelected(false);
                    }
                }
            }

        });

        Platform.runLater(() -> tabPane.getScene().getWindow().setOnCloseRequest(event -> {
            closeAllTabs();
            saveHistory();
            saveLiked();
            System.out.println("History saved on exit");
        }));
    }

    private void closeAllTabs() {
        ObservableList<Tab> tabs = tabPane.getTabs();

        for (int i = tabs.size() - 1; i >= 0; i--) {
            Tab tab = tabs.get(i);

            if (tab instanceof BrowserTab) {
                ((BrowserTab) tab).updateTimeSpent();
            }

            tabPane.getTabs().remove(tab);
        }
    }

    private void updateBackgroundVisibility() {
        PaneForImage.setVisible(tabPane.getTabs().isEmpty());
    }

    private void setupHistoryTable() {
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        urlColumn.setCellFactory(param -> {
            TableCell<HistoryEntry, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String url, boolean empty) {
                    super.updateItem(url, empty);
                    if (empty || url == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        Hyperlink hyperlink = new Hyperlink(url);
                        hyperlink.setOnAction(event -> {
                            openUrlInNewTab(url);
                            displayHistory();
                        });
                        setGraphic(hyperlink);

                    }
                }
            };
            return cell;
        });

        visitTimeColumn.setCellValueFactory(new PropertyValueFactory<>("visitTime"));
        visitTimeColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatVisitTime(item));
                }
            }
        });

        timeSpentColumn.setCellValueFactory(new PropertyValueFactory<>("timeSpent"));
        timeSpentColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(formatTimeSpent(item));
                }
            }
        });

        historyTable.setItems(historyManager.getHistory());
    }


    private void setupLikedTable() {

        urlColumnLiked.setCellValueFactory(new PropertyValueFactory<>("url"));
        urlColumnLiked.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Hyperlink hyperlink = new Hyperlink(url);
                    hyperlink.setOnAction(event -> {
                        openUrlInNewTab(url);
                        displayLiked();
                    });
                    setGraphic(hyperlink);
                    setText(null);
                }
            }
        });

        tableLiked.setItems(likeManager.getLiked());
    }

    private void setupLPrivateTable() {

        urlColumnPrivate.setCellValueFactory(new PropertyValueFactory<>("url"));
        urlColumnPrivate.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(String url, boolean empty) {
                super.updateItem(url, empty);
                if (empty || url == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Hyperlink hyperlink = new Hyperlink(url);
                    hyperlink.setOnAction(event -> {
                        openUrlInNewTab(url);
                        displayPrivate();
                    });
                    setGraphic(hyperlink);
                    setText(null);
                }
            }
        });

        tablePrivate.setItems(privateManager.getPrivates());
    }


    private void openUrlInNewTab(String url) {
        BrowserTab newTab = new BrowserTab(url, historyManager, this, isIncognito, privateManager.getPrivates());
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
        newTab.loadPage(url);
    }

    private String formatVisitTime(LocalDateTime visitTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm:ss");
        return visitTime.format(formatter);
    }


    private String formatTimeSpent(long milliseconds) {
        long hours = milliseconds / 3600000;
        long minutes = (milliseconds % 3600000) / 60000;
        long seconds = (milliseconds % 60000) / 1000;
        long millis = milliseconds % 1000;
        return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
    }


    public void createNewTab() {
        BrowserTab newTab = new BrowserTab("New Tab", historyManager, this, isIncognito, privateManager.getPrivates());
        tabPane.getTabs().add(newTab);
        tabPane.getSelectionModel().select(newTab);
    }

    public void displayHistory() {
        if (isIncognito) {
            historyTable.setVisible(!historyTable.isVisible());
            return;
        }
        boolean isVisible = historyTable.isVisible();
        historyTable.setVisible(!isVisible);
        newTabButton.setDisable(!isVisible);
        buttonAllLiked.setDisable(!isVisible);
        checkBoxLike.setDisable(!isVisible);
        clearButton.setDisable(!isVisible);
        saveButton.setDisable(!isVisible);
        saveZipButton.setDisable(!isVisible);
        editButton.setDisable(!isVisible);
        IncognitoCheckBox.setDisable(!isVisible);
        privateCheckBox.setDisable(!isVisible);
        buttonAllPrivate.setDisable(!isVisible);
    }


    public void back() {
        BrowserTab currentTab = (BrowserTab) tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            currentTab.back();
        }
    }

    public void forward() {
        BrowserTab currentTab = (BrowserTab) tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            currentTab.forward();
        }
    }

    public void refreshPage() {
        BrowserTab currentTab = (BrowserTab) tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            currentTab.refreshPage();
            updateCheckBoxState();
        }
    }

    public void loadPage() {
        BrowserTab currentTab = (BrowserTab) tabPane.getSelectionModel().getSelectedItem();
        String input = textField.getText().trim();

        if (currentTab != null) {
            String url = handleInvalidInput(input);
            currentTab.loadPage(url);
            updateCheckBoxState();
        }
    }

    private String handleInvalidInput(String input) {
        if (!isValidUrl(input)) {
            return "https://www.google.com/search?q=" + input.replace(" ", "+");
        }
        return normalizeUrl(input);
    }

    private boolean isValidUrl(String input) {
        try {
            if (!input.startsWith("http://") && !input.startsWith("https://")) {
                input = "https://" + input;
            }
            URI uri = new URL(input).toURI();
            String host = uri.getHost();
            return host != null && host.contains(".");
        } catch (Exception e) {
            return false;
        }
    }

    private String normalizeUrl(String input) {
        if (!input.startsWith("http://") && !input.startsWith("https://")) {
            return "https://" + input;
        }
        return input;
    }




    public void displayLiked() {
        boolean isVisible = tableLiked.isVisible();

        tableLiked.setVisible(!isVisible);

        newTabButton.setDisable(!isVisible);

        buttonHistory.setDisable(!isVisible);

        checkBoxLike.setDisable(!isVisible);

        clearButton.setDisable(!isVisible);

        saveButton.setDisable(!isVisible);

        saveZipButton.setDisable(!isVisible);

        editButton.setDisable(!isVisible);

        IncognitoCheckBox.setDisable(!isVisible);

        privateCheckBox.setDisable(!isVisible);

        buttonAllPrivate.setDisable(!isVisible);
    }

    public void likePage() {

        BrowserTab currentTab = (BrowserTab) tabPane.getSelectionModel().getSelectedItem();

        if (currentTab != null) {
            String currentUrl = currentTab.getLocation();


            if (checkBoxLike.isSelected()) {
                likeManager.addLikedEntry(new LikedEntry(currentUrl));
            } else {
                likeManager.removeLikedEntry(currentUrl);
            }

            tableLiked.refresh();
        }
    }

    public void privatePage() {

        BrowserTab currentTab = (BrowserTab) tabPane.getSelectionModel().getSelectedItem();

        if (currentTab != null) {
            String currentUrl = currentTab.getLocation();


            if (privateCheckBox.isSelected()) {
                privateManager.add(new PrivateEntry(currentUrl));
            } else {
                privateManager.remove(currentUrl);
            }

            tablePrivate.refresh();

            ObservableList<Tab> tabs = tabPane.getTabs();

            for (int i = tabs.size() - 1; i >= 0; i--) {
                Tab tab = tabs.get(i);

                if (tab instanceof BrowserTab) {
                    ((BrowserTab) tab).setListOfPrivates(privateManager.getPrivates());
                }

            }
        }
    }

    public void updateCheckBoxState() {
        BrowserTab currentTab = (BrowserTab) tabPane.getSelectionModel().getSelectedItem();

        if (currentTab != null) {
            String currentUrl = currentTab.getLocation();
            checkBoxLike.setSelected(likeManager.getLiked().stream().anyMatch(entry -> entry.getUrl().equals(currentUrl)));
            privateCheckBox.setSelected(privateManager.getPrivates().stream().anyMatch(entry -> entry.getUrl() != null && entry.getUrl().equals(currentUrl)));
        }
    }

    public void clearHistory() {
        if (historyManager.getHistory().isEmpty()) {
            return;
        }
        historyManager.clearHistory();
        historyTable.setItems(historyManager.getHistory());
        historyTable.refresh();
    }

    public void displayPrivate() {
        boolean isVisible = tablePrivate.isVisible();

        tablePrivate.setVisible(!isVisible);

        newTabButton.setDisable(!isVisible);

        buttonHistory.setDisable(!isVisible);

        checkBoxLike.setDisable(!isVisible);

        clearButton.setDisable(!isVisible);

        saveButton.setDisable(!isVisible);

        saveZipButton.setDisable(!isVisible);

        editButton.setDisable(!isVisible);

        IncognitoCheckBox.setDisable(!isVisible);

        privateCheckBox.setDisable(!isVisible);

        buttonAllLiked.setDisable(!isVisible);

        clearButton.setDisable(!isVisible);
    }

    public void savePage() {

        BrowserTab currentTab = (BrowserTab) tabPane.getSelectionModel().getSelectedItem();

        if (currentTab == null) {
            return;
        }

        WebEngine webEngine = currentTab.getWebEngine();



        String htmlContent = (String) webEngine.executeScript("document.documentElement.outerHTML");

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML files", "*.html"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(htmlContent);
                System.out.println("Page saved to " + file.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error saving page: " + e.getMessage());
            }
        }
    }

    public void savePageAsZip() {
        BrowserTab currentTab = (BrowserTab) tabPane.getSelectionModel().getSelectedItem();
        if (currentTab == null) {
            return;
        }
        WebEngine webEngine = currentTab.getWebEngine();

        String htmlContent = (String) webEngine.executeScript("document.documentElement.outerHTML");

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP files", "*.zip"));
        File zipFile = fileChooser.showSaveDialog(null);

        if (zipFile != null) {
            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile))) {
                ZipEntry zipEntry = new ZipEntry("index.html");
                zipOut.putNextEntry(zipEntry);
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(zipOut))) {
                    writer.write(htmlContent);
                }
                System.out.println("Page saved to " + zipFile.getAbsolutePath());

            } catch (IOException e) {
                System.err.println("Error saving page as ZIP: " + e.getMessage());
            }
        }
    }

    public void htmlEditor() {
        BrowserTab currentTab = (BrowserTab) tabPane.getSelectionModel().getSelectedItem();
        if (currentTab == null) {
            return;
        }
        WebEngine webEngine = currentTab.getWebEngine();


        try {
            String htmlContentOriginal = (String) webEngine.executeScript("document.documentElement.outerHTML");
            if (htmlContentOriginal != null) {
                htmlTextArea.setText(htmlContentOriginal);
                anchorForEdit.setVisible(true);
            } else {
                System.err.println("Не удалось загрузить HTML-содержимое.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Ошибка при выполнении JavaScript: " + e.getMessage());
        }


    }

    public void loadHtmlChanges() {
        BrowserTab currentTab = (BrowserTab) tabPane.getSelectionModel().getSelectedItem();
        WebEngine webEngine = currentTab.getWebEngine();

        String updatedHtmlContent = htmlTextArea.getText();

        if (!updatedHtmlContent.trim().startsWith("<!DOCTYPE html>")) {
            updatedHtmlContent = "<!DOCTYPE html>\n" + updatedHtmlContent;
        }
        if (!updatedHtmlContent.trim().endsWith("</html>")) {
            updatedHtmlContent += "\n</html>";
        }

        updatedHtmlContent = updatedHtmlContent.replace("\n", "").replace("\r", "").replace("\"", "\\\"");

        try {
            webEngine.loadContent(updatedHtmlContent);
            System.out.println("HTML content loaded successfully");
        } catch (Exception e) {
            System.out.println("cant load html content" + e.getMessage());
        }

        closeEditor();
    }

    public void closeEditor() {
        anchorForEdit.setVisible(false);
    }



}
