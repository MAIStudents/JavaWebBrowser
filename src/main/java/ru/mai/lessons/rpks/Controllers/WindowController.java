package ru.mai.lessons.rpks.Controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.tinylog.Logger;
import ru.mai.lessons.rpks.Holders.TabHolder;
import ru.mai.lessons.rpks.Management.HistoryItem;

import lombok.Getter;
import ru.mai.lessons.rpks.Management.SiteSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WindowController implements Initializable  {
    private final String SETTINGS_SAVE_PATH = "settings.json";
    private final String HISTORY_SAVE_PATH = "history.json";
    private final String STYLESSHEET_FXML_PATH = "/ru/mai/lessons/rpks/fxml/styles.css";
    private final String CODE_EDITOR_FXML_PATH = "/ru/mai/lessons/rpks/fxml/CodeEditor.fxml";
    private final String HISTORY_FXML_PATH = "/ru/mai/lessons/rpks/fxml/History.fxml";
    private final String DISPLAY_LINKS_FXML_PATH = "/ru/mai/lessons/rpks/fxml/DisplaySites.fxml";
    private final HashMap<Tab, TabHolder> openedTabs = new HashMap<>();
    private final List<HistoryItem> history = new ArrayList<>();
    private final HashMap<String, SiteSettings> sitesSettings = new HashMap<>();

    private boolean globalPrivateModeEnabled = false;

    @Getter
    @FXML
    public TabPane tabPane;

    @FXML
    public TextField webPath;
    @FXML
    public Button goButton;

    @FXML
    public Button privateModeButton;

    @FXML
    public Button likeButton;

    @FXML
    public Button leftArrow;

    @FXML
    public Button rightArrow;

    public void closeTab(Tab tab) {
        openedTabs.remove(tab);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadState();
        TabHolder newTab = new TabHolder(this, "https://www.google.com/");
        tabPane.getTabs().addListener((ListChangeListener<Tab>) change -> {
            while (change.next()) {
                if (tabPane.getTabs().isEmpty()) {
                    saveState();
                    Logger.info("Closing browser");
                    Platform.exit();
                }
            }
        });
        openedTabs.put(newTab.getTab(), newTab);

        tabPane.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldTab, newOpened) -> {
            if (newOpened != null) {
                Platform.runLater(() -> {
                    colorPrivateButton();
                    colorLikeButton();
                });
            }
        });
        loadHistoryFromJson(new File(HISTORY_SAVE_PATH));
    }

    @FXML
    public void openNewTab() {
        openTabByUrl(webPath.getText());
    }

    @FXML
    public void toggleGlobalPrivateMode() {
        globalPrivateModeEnabled = !globalPrivateModeEnabled;
    }

    public void openTabByUrl(String url) {
        TabHolder tab = new TabHolder(this, url);
        openedTabs.put(tab.getTab(), tab);
        colorPrivateButton();
        colorLikeButton();
    }

    @FXML
    public void reloadTab() {
        Tab activeTab = tabPane.getSelectionModel().getSelectedItem();
        TabHolder selectedTab = openedTabs.get(activeTab);
        if (activeTab != null) {
            selectedTab.reloadPage();
        }
    }

    private boolean isPrivateModeEnabled() {
        String url =  openedTabs.get(tabPane.getSelectionModel().getSelectedItem()).getUrl();

        if (sitesSettings.containsKey(url)) {
            return sitesSettings.get(url).isPrivate();
        }
        return false;
    }

    public void colorPrivateButton() {
        if (isPrivateModeEnabled()) {
            privateModeButton.setStyle("-fx-background-color: black; -fx-text-fill: white;");
        } else {
            privateModeButton.setStyle("");
        }
    }
    @FXML
    public void togglePrivateSite() {
        String url =  openedTabs.get(tabPane.getSelectionModel().getSelectedItem()).getUrl();
        SiteSettings currentSettings;

        if (sitesSettings.containsKey(url)) {
            currentSettings = sitesSettings.get(url);
        } else {
            currentSettings = new SiteSettings(url);
        }

        currentSettings.setPrivate(!currentSettings.isPrivate());
        sitesSettings.put(url, currentSettings);
        colorPrivateButton();
    }


    @FXML
    public void goBackHistory() {
        Tab activeTab = tabPane.getSelectionModel().getSelectedItem();
        TabHolder selectedTab = openedTabs.get(activeTab);
        selectedTab.goBack();
        colorPrivateButton();
        colorLikeButton();
    }

    @FXML
    public void goForwardHistory() {
        Tab activeTab = tabPane.getSelectionModel().getSelectedItem();
        TabHolder selectedTab = openedTabs.get(activeTab);
        selectedTab.goForward();
        colorPrivateButton();
        colorLikeButton();
    }

    private boolean isSiteLiked() {
        String url =  openedTabs.get(tabPane.getSelectionModel().getSelectedItem()).getUrl();

        if (sitesSettings.containsKey(url)) {
            return sitesSettings.get(url).isLiked();
        }
        return false;

    }
    public void colorLikeButton() {
        if (isSiteLiked()) {
            likeButton.setStyle("-fx-background-color: red; -fx-text-fill: white;");
        } else {
            likeButton.setStyle("");
        }
    }

    @FXML
    public void toggleLikeSite() {
        String url =  openedTabs.get(tabPane.getSelectionModel().getSelectedItem()).getUrl();
        SiteSettings currentSettings;

        if (sitesSettings.containsKey(url)) {
            currentSettings = sitesSettings.get(url);
        } else {
            currentSettings = new SiteSettings(url);
        }

        currentSettings.setLiked(!currentSettings.isLiked());
        sitesSettings.put(url, currentSettings);
        colorLikeButton();
    }

    @FXML
    public void showHistory() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(HISTORY_FXML_PATH));
            Stage historyStage = new Stage();
            Scene scene = new Scene(loader.load());
            historyStage.setScene(scene);
            historyStage.setTitle("История");
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(STYLESSHEET_FXML_PATH)).toExternalForm());

            HistoryController historyController = loader.getController();
            historyController.setWindowController(this);

            for (HistoryItem item : history) {
                historyController.addHistoryItem(item);
            }

            historyStage.initModality(Modality.NONE);
            historyStage.show();
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    @FXML
        public void showLikedSites() {
        List<String> urls = new ArrayList<>();
        for (String key : sitesSettings.keySet()) {
            SiteSettings settings = sitesSettings.get(key);
            if (settings.isLiked()) {
                urls.add(key);
            }
        }
        showLinksWindow("Избранные сайты", urls, this::openTabByUrl);
    }

    @FXML
    public void showPrivateSites() {
        List<String> urls = new ArrayList<>();
        for (String key : sitesSettings.keySet()) {
            SiteSettings settings = sitesSettings.get(key);
            if (settings.isPrivate()) {
                urls.add(key);
            }
        }
        showLinksWindow("'Секретные' сайты", urls, this::openTabByUrl);
    }


    private void showLinksWindow(final String title, final List<String> links, Consumer<String> onLinkClicked) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(DISPLAY_LINKS_FXML_PATH));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle(title);

            LinksViewController controller = loader.getController();

            controller.setLinks(links);
            controller.setOnLinkClicked(onLinkClicked);

            stage.initModality(Modality.NONE);
            stage.show();
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }


    @FXML
    public void codeEditor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(CODE_EDITOR_FXML_PATH));
            Stage editorStage = new Stage();
            editorStage.setScene(new Scene(loader.load()));

            CodeEditorController controller = loader.getController();
            final Tab tab = tabPane.getSelectionModel().getSelectedItem();
            final WebEngine webEngine = ((WebView) tab.getContent()).getEngine();

            String oldTabName = tab.getText();
            String currentHtml = webEngine.executeScript("document.documentElement.outerHTML").toString();
            webEngine.setJavaScriptEnabled(true);

            controller.setCode(currentHtml);

            controller.setListener(updatedCode -> {
                webEngine.loadContent(updatedCode);
                Platform.runLater(() -> tab.setText(oldTabName));

            });

            editorStage.initModality(Modality.APPLICATION_MODAL);
            editorStage.setTitle("Редактор HTML");
            editorStage.showAndWait();
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    @FXML
    public void saveCurrentPage() {

        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab == null) {
            Logger.info("Нет активной вкладки.");
            return;
        }

        TabHolder tabHolder = openedTabs.get(selectedTab);
        if (tabHolder == null) {
            Logger.info("Содержимое вкладки не найдено.");
            return;
        }

        String pageContent = tabHolder.getWebView().getEngine().executeScript("document.documentElement.outerHTML").toString();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить сайт");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("HTML файлы", "*.html"),
                new FileChooser.ExtensionFilter("ZIP архив", "*.zip")
        );

        File file = fileChooser.showSaveDialog(new Stage());
        if (file == null) {
            return;
        }

        try {
            if (file.getName().endsWith(".html")) {
                Files.write(file.toPath(), pageContent.getBytes());
            } else if (file.getName().endsWith(".zip")) {
                try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(file))) {
                    ZipEntry entry = new ZipEntry("page.html");
                    zos.putNextEntry(entry);
                    zos.write(pageContent.getBytes());
                    zos.closeEntry();
                }
            } else {
                Logger.error("Неверный формат файла.");
            }
            Logger.info("Сохранение завершено: " + file.getAbsolutePath());
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }


    public void addToHistory(String url, Long openingTime, Long diffTime) {
        if (!globalPrivateModeEnabled && (!sitesSettings.containsKey(url) || !sitesSettings.get(url).isPrivate())) {
            history.add(new HistoryItem(url, timeToFullTimeString(openingTime), timeToTimeString(diffTime)));
        }
    }

    private String timeToFullTimeString(Long time) {
        LocalDateTime dateTime = Instant.ofEpochMilli(time)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return dateTime.format(dateTimeFormatter);
    }

    private String timeToTimeString(Long time) {
        Duration duration = Duration.ofMillis(time);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void saveState() {
        saveHistoryToJson(new File(HISTORY_SAVE_PATH));
        saveSiteSettingsToJson(getSiteSettingsList(), new File(SETTINGS_SAVE_PATH));
    }

    public void loadState() {
        loadHistory();
        loadSiteSettings();
    }

    public void loadHistory() {
        loadHistoryFromJson(new File(HISTORY_SAVE_PATH));
    }

    public void loadSiteSettings() {
        List<SiteSettings> lst = loadSiteSettingsFromJson(new File(SETTINGS_SAVE_PATH));
        for (SiteSettings settings : lst) {
            sitesSettings.put(settings.getUrl(), settings);
        }
    }

    private void saveHistoryToJson(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Logger.info(history);
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, history);
            System.out.println("История успешно сохранена в файл: " + file.getAbsolutePath());
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    private void loadHistoryFromJson(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            List<HistoryItem> loadedHistory = mapper.readValue(file, new TypeReference<List<HistoryItem>>() {
            });
            history.clear();
            history.addAll(loadedHistory);
            Logger.info("Successfully loaded: " + file.getAbsolutePath());
        } catch (Exception e) {
           Logger.error("History was not found by default path");
        }
    }

    private void saveSiteSettingsToJson(List<SiteSettings> settings, File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, settings);
            System.out.println("Настройки сайтов успешно сохранены в файл: " + file.getAbsolutePath());
        } catch (Exception e) {
            Logger.error(e.getMessage());
        }
    }

    private List<SiteSettings> loadSiteSettingsFromJson(File file) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(file, new TypeReference<List<SiteSettings>>() {
            } );

        } catch (Exception e) {
            Logger.error(e.getMessage());
            return List.of();
        }
    }

    private void purifySiteSettings() {
        for (String url : sitesSettings.keySet()) {
            SiteSettings setting = sitesSettings.get(url);
            if (!setting.isLiked() && !setting.isPrivate()) {
                sitesSettings.remove(url);
            }
        }
    }

    private List<SiteSettings> getSiteSettingsList() {
        purifySiteSettings();
        return new ArrayList<>(sitesSettings.values());
    }
}
