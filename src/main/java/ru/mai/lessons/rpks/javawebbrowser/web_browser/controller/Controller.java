package ru.mai.lessons.rpks.javawebbrowser.web_browser.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import ru.mai.lessons.rpks.javawebbrowser.Main;
import ru.mai.lessons.rpks.javawebbrowser.commons.Pair;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.model.Website;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.model.favourite_model.FavouritesModel;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.model.history_model.HistoryModel;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.model.save_module.SaveBlacklistModel;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.model.single_tab.SingleTab;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.model.web_module.WebModule;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.model.web_module.state.WebSiteModuleState;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.view.settings.SettingsView;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.view.websites_holder.state.WebsiteHolderRenderState;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.view.websites_holder.state.WebsiteHolderStorageState;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Controller {

    private FavouritesModel favouriteWebsites;
    private SingleTab singleTab;
    private SettingsView settingsContainer;
    private HistoryModel historyWebsites;
    private WebModule webModule;

    @FXML
    private TextField url;

    @FXML
    private TabPane browserTabs;

    @FXML
    private Button favouritesBtn;

    @FXML
    private Button websiteSaver;

    @FXML
    private VBox htmlPreview;

    @FXML
    private TextArea previewHtmlArea;

    @FXML
    private Button htmlPreviewBtn;

    @FXML
    public void openSettingsTabOnClick() {
        int tabId;
        if ((tabId = singleTab.singleTabExists(Main.BUNDLE.getString("settings.title"))) == SingleTab.NOT_OPENED) {
            Tab settingsTab = new Tab(Main.BUNDLE.getString("settings.title"));

            settingsTab.setContent(settingsContainer);

            browserTabs.getTabs().add(settingsTab);
            browserTabs.getSelectionModel().selectLast();
            return;
        }
        browserTabs.getSelectionModel().select(tabId);
    }

    private void toggleWebsiteFavouriteStatus(Website website) {
        if (favouriteWebsites.isWebsiteFavourite(website)) {
            favouriteWebsites.removeWebsiteFromFavourites(website);
            favouritesBtn.setText("✰");
            return;
        }
        favouriteWebsites.addWebsiteToFavourites(website);
        favouritesBtn.setText("★");
    }

    public void setWebsiteFavouriteIcon(Website website) {
        if (website.equals(Website.NOT_LOADABLE_PAGE)) {
            favouritesBtn.setText("✰");
            return;
        }
        if (favouriteWebsites.isWebsiteFavourite(website)) {
            favouritesBtn.setText("★");
            return;
        }
        favouritesBtn.setText("✰");
    }

    @FXML
    public void addToFavouritesOnClick() {
        Tab selectedTab = browserTabs.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            WebEngine engine = ((WebView) selectedTab.getContent()).getEngine();

            if (engine.getLocation().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.NONE, Main.BUNDLE.getString("favourites.cannotAddToFavouritesText"), ButtonType.CLOSE);
                alert.setTitle(Main.BUNDLE.getString("favourites.cannotAddToFavouritesTitle"));
                alert.show();
                return;
            }

            Website website = new Website(engine.getTitle(), engine.getLocation());
            toggleWebsiteFavouriteStatus(website);
        }
    }

    @FXML
    public void refreshPageOnClick() {
        Tab selectedTab = browserTabs.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof WebView) {
            WebEngine engine = ((WebView) selectedTab.getContent()).getEngine();
            if (engine.getLocation().isEmpty()) {
                return;
            }
            engine.reload();
            favouritesBtn.setDisable(true);
            websiteSaver.setDisable(true);
            htmlPreviewBtn.setDisable(true);
            historyWebsites.addToHistory(new Website(selectedTab.getText(), engine.getLocation()));
        }
    }

    public void onLoadProgressActionPicker(Document newValue, Tab webPageTab, WebEngine engine) {
        switch (webModule.onWebsiteLoad(newValue)) {
            case WebSiteModuleState.NotReady ignored1 -> {}
            case WebSiteModuleState.NoTitle ignored2 -> webPageTab.setText(Main.BUNDLE.getString("website.untitled"));
            case WebSiteModuleState.Success success -> {
                String tabUrl = engine.getLocation();

                webPageTab.setText(success.title().length() > 20 ? success.title().substring(0, 21) + "..." : success.title());
                this.url.setText(tabUrl);

                Website website = new Website(success.title(), tabUrl);
                setWebsiteFavouriteIcon(website);
                saveWebsiteToHistory(website);
                favouritesBtn.setDisable(false);
                websiteSaver.setDisable(false);
                htmlPreviewBtn.setDisable(false);
            }
        }
    }

    private void addNewBrowserTab(String siteUrl) {
        favouritesBtn.setDisable(true);
        websiteSaver.setDisable(true);
        htmlPreviewBtn.setDisable(true);

        WebView site = new WebView();
        WebEngine engine = site.getEngine();

        engine.setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 14_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 YaBrowser/23.9.0.2325 Yowser/2.5 Safari/537.36");

        if (!siteUrl.startsWith("http://") && !siteUrl.startsWith("https://")) {
            siteUrl = "http://" + siteUrl;
        }

        Tab webPageTab = new Tab(Main.BUNDLE.getString("website.loading"));
        webPageTab.setContent(site);

        browserTabs.getTabs().add(webPageTab);
        browserTabs.getSelectionModel().selectLast();

        if (!webModule.validateUrl(siteUrl)) {
            engine.load(getClass().getResource("/url-does-not-exist.html").toString());
            webPageTab.setText(Main.BUNDLE.getString("url.notFound"));
            favouritesBtn.setDisable(false);
            websiteSaver.setDisable(false);
            htmlPreviewBtn.setDisable(false);
            this.url.setText(engine.getLocation());
            return;
        }

        engine.load(siteUrl);

        engine.documentProperty().addListener((observable, oldValue, newValue) -> onLoadProgressActionPicker(newValue, webPageTab, engine));
    }

    @FXML
    public void searchButtonOnClick() {
        addNewBrowserTab(url.getText());

    }

    private void defaultTabSettings() {
        url.setText("");
        setWebsiteFavouriteIcon(Website.NOT_LOADABLE_PAGE);
        favouritesBtn.setDisable(true);
        websiteSaver.setDisable(true);
        htmlPreviewBtn.setDisable(true);
        htmlPreview.setVisible(false);
        htmlPreview.setManaged(false);
    }

    public void saveWebsiteToHistory(Website website) {
        historyWebsites.addToHistory(website);
    }

    private void prepareWebsite(WebEngine engine) {
        String tabUrl = engine.getLocation();

        if (tabUrl == null) {
            return;
        }

        url.setText(tabUrl);

        Website website = new Website(engine.getTitle(), tabUrl);
        setWebsiteFavouriteIcon(website);

        favouritesBtn.setDisable(false);
        websiteSaver.setDisable(false);
        htmlPreviewBtn.setDisable(false);
    }

    private void fillWithFavourites() {
        if (favouriteWebsites.isFavouritesStorageEmpty()) {
            settingsContainer.getFavouritesHolder().render(new WebsiteHolderRenderState.NO_ITEMS(Main.BUNDLE.getString("favourites.noTabsInFavourites")));
        } else {
            settingsContainer.getFavouritesHolder().render(new WebsiteHolderRenderState.ITEMS_EXIST(new WebsiteHolderStorageState.ForFavourites(favouriteWebsites.getFavouriteWebsites())));
        }
    }

    private void fillWithHistory() {
        if (historyWebsites.isHistoryEmpty()) {
            settingsContainer.getHistoryHolder().render(new WebsiteHolderRenderState.NO_ITEMS(Main.BUNDLE.getString("history.empty")));
        } else {
            settingsContainer.getHistoryHolder().render(new WebsiteHolderRenderState.ITEMS_EXIST(new WebsiteHolderStorageState.ForHistory(historyWebsites.getHistoryWebsites())));
        }
    }

    @FXML
    public void historyBackOnClick() {
        if (!(browserTabs.getSelectionModel().getSelectedItem().getContent() instanceof WebView site)) {
            return;
        }
        WebHistory history = site.getEngine().getHistory();
        var entries = history.getEntries();

        Platform.runLater(() -> history.go((entries.size() > 1 && history.getCurrentIndex() > 0) ? -1 : 0));
    }

    @FXML
    public void historyFrontOnClick() {
        if (!(browserTabs.getSelectionModel().getSelectedItem().getContent() instanceof WebView site)) {
            return;
        }
        WebHistory history = site.getEngine().getHistory();
        var entries = history.getEntries();

        Platform.runLater(() -> history.go((entries.size() > 1 && history.getCurrentIndex() < entries.size() - 1) ? 1 : 0));
    }

    private void fillWithBlacklists() {
        settingsContainer.getDisabledHistoryCheckBox().setSelected(historyWebsites.isHistoryDisabled());
        if (historyWebsites.isHistoryDisabled()) {
            settingsContainer.getSitesToDisableArea().setDisable(true);
        }

        StringBuilder disabledSites = new StringBuilder();
        historyWebsites.getBlacklisted().forEach(site -> disabledSites.append(site).append('\n'));
        if (disabledSites.length() != 0) {
            disabledSites.setLength(disabledSites.length() - 1);
        }
        settingsContainer.getSitesToDisableArea().setText(disabledSites.toString());
    }

    @FXML
    public void togglePreviewOnClick() {
        if (browserTabs.getSelectionModel().getSelectedItem() == null || !(browserTabs.getSelectionModel().getSelectedItem().getContent() instanceof WebView site)) {
            return;
        }
        htmlPreview.setManaged(!htmlPreview.isManaged());
        htmlPreview.setVisible(!htmlPreview.isVisible());
        if (htmlPreview.isVisible()) {
            String html = (String) site.getEngine().executeScript("new XMLSerializer().serializeToString(document)");
            previewHtmlArea.setText(html);
        }
    }

    @FXML
    public void closePreviewOnClick() {
        htmlPreview.setManaged(false);
        htmlPreview.setVisible(false);
    }

    @FXML
    public void updateHtmlOnClick() {
        WebView site = (WebView) browserTabs.getSelectionModel().getSelectedItem().getContent();
        site.getEngine().loadContent(previewHtmlArea.getText());
    }

    @FXML
    public void saveWebsiteOnClick() throws IOException {
        if (!(browserTabs.getSelectionModel().getSelectedItem().getContent() instanceof WebView site)) {
            return;
        }
        String html = (String) site.getEngine().executeScript("new XMLSerializer().serializeToString(document)");
        String fileName = site.getEngine().getTitle().replaceAll("\\|/|:|\\*|\\?|\"|<|>|\\|", "") + "_" + System.currentTimeMillis();

        File whereToSave = new File(fileName + ".zip");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(whereToSave));
        ZipEntry entry = new ZipEntry(fileName + ".html");
        out.putNextEntry(entry);

        byte[] htmlData = html.getBytes();
        out.write(htmlData, 0, htmlData.length);
        out.closeEntry();

        out.close();

        Alert alert = new Alert(Alert.AlertType.NONE, MessageFormat.format(Main.BUNDLE.getString("creator.saveMessage"), whereToSave.getAbsolutePath()), ButtonType.CLOSE);
        alert.setTitle(Main.BUNDLE.getString("creator.success"));
        alert.show();
    }

    @FXML
    public void initialize() throws IOException {
        htmlPreview.setManaged(false);
        htmlPreview.setVisible(false);
        favouritesBtn.setDisable(true);
        websiteSaver.setDisable(true);
        htmlPreviewBtn.setDisable(true);

        singleTab = new SingleTab(browserTabs);

        try(Reader favouritesReader = new FileReader((Main.class.getResource("favourites_save.json") + "").substring(6));
                Reader historyReader = new FileReader((Main.class.getResource("history_save.json") + "").substring(6));
                Reader blacklistReader = new FileReader((Main.class.getResource("blacklist_save.json") + "").substring(6))) {
            Gson gson = new Gson();

            LinkedList<Website> favourites = gson.fromJson(favouritesReader, new TypeToken<LinkedList<Website>>() {}.getType());
            favouriteWebsites = new FavouritesModel(favourites == null ? new LinkedList<>() : favourites);

            settingsContainer = new SettingsView(browserTabs, singleTab, this);

            LinkedList<Pair<String, Website>> history = gson.fromJson(historyReader, new TypeToken<LinkedList<Pair<String, Website>>>() {}.getType());
            SaveBlacklistModel blacklist = gson.fromJson(blacklistReader, SaveBlacklistModel.class);
            if (blacklist == null) {
                historyWebsites = new HistoryModel(settingsContainer.getDisabledHistoryCheckBox(), settingsContainer.getSitesToDisableArea(), history == null ? new LinkedList<>() : history, new LinkedList<>(), false);
            } else {
                historyWebsites = new HistoryModel(settingsContainer.getDisabledHistoryCheckBox(), settingsContainer.getSitesToDisableArea(), history == null ? new LinkedList<>() : history, blacklist.getDisabledWebsites(), blacklist.getIsHistoryDisabled());
            }
        }

        webModule = new WebModule();

        browserTabs.getSelectionModel().selectedItemProperty().addListener((ov, oldValue, clickedTab) -> {
            defaultTabSettings();

            if (clickedTab == null) {
                return;
            }

            String clickedTabText = clickedTab.getText();

            if (clickedTabText.equals(Main.BUNDLE.getString("settings.history"))) {
                fillWithHistory();
            } else if (clickedTabText.equals(Main.BUNDLE.getString("settings.favourites"))) {
                fillWithFavourites();
            } else if (clickedTabText.equals(Main.BUNDLE.getString("settings.title"))) {
                fillWithBlacklists();
            } else {
                prepareWebsite(((WebView) clickedTab.getContent()).getEngine());
            }
        });
    }

    public List<Pair<String, Website>> getHistoryWebsites() {
        return historyWebsites.getHistoryWebsites();
    }

    public List<Website> getFavouriteWebsites() {
        return favouriteWebsites.getFavouriteWebsites();
    }

    public LinkedList<String> getBlacklistedSites() {
        return settingsContainer.getSitesToDisable();
    }

    public boolean areAllSitesDisabled() {
        return settingsContainer.getDisabledHistoryCheckBox().isSelected();
    }

}