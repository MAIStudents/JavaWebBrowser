package ru.mai.lessons.rpks.impl;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import org.apache.commons.io.FileUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import ru.mai.lessons.rpks.helpClasses.DirectoryChooser;
import ru.mai.lessons.rpks.helpClasses.HistoryTableViewDataProvider;

public class BrowserTabController extends StackPane implements Initializable {
    @FXML
    private JFXButton backwardButton;

    @FXML
    private BorderPane borderPane;

    @FXML
    private CheckMenuItem collectHistoryThisPageCheckedMenuItem;

    @FXML
    private MenuItem downloadPageMenuItem;

    @FXML
    private MenuItem downloadPageZipMenuItem;

    @FXML
    private MenuItem editHTMLMenuItem;

    @FXML
    private VBox errorPane;

    @FXML
    private MenuItem favouritesMenuItem;

    @FXML
    private JFXButton forwardButton;

    @FXML
    private JFXButton goButton;

    @FXML
    private MenuItem historyMenuItem;

    @FXML
    private MenuItem newHTMLMenuItem;

    @FXML
    private JFXButton reloadButton;

    @FXML
    private TextField searchBar;

    @FXML
    private JFXButton toFavouritesButton;

    @FXML
    private FontIcon toFavouritesFontIcon;

    @FXML
    private JFXButton tryAgain;

    @FXML
    private ProgressIndicator tryAgainIndicator;

    @FXML
    private MenuItem viewHTMLMenuItem;

    @FXML
    private WebView webView;

    private Tab tab;
    private final BrowserController browserController;
    private String firstWebSite;
    WebEngine browser;
    private WebHistory history;
    private ObservableList<WebHistory.Entry> historyEntryList;

    private final Logger logger = Logger.getLogger(getClass().getName());

    public BrowserTabController(BrowserController browserController, Tab tab, String firstWebSite) {
        this.browserController = browserController;
        this.tab = tab;
        this.firstWebSite = firstWebSite;
        this.tab.setContent(this);

        tab.setOnClosed(a -> {
            endRecording(false);
        });

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/BrowserTab.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "", ex);
        }
    }

    public void setHistory(WebHistory history) {
        this.history = history;
    }

    public WebHistory getHistory() {
        return history;
    }

    private void loadWebSite(String webAddress) {
        browser.load(webAddress);
    }

    public void reloadWebSite() {
        if (!getHistory().getEntries().isEmpty())
            browser.reload();
        else
            browser.load(firstWebSite);
    }

    public void goBack() {
        getHistory().go(historyEntryList.size() > 1 && getHistory().getCurrentIndex() > 0 ? -1 : 0);
    }

    public void goForward() {
        getHistory().go(historyEntryList.size() > 1 && getHistory().getCurrentIndex() < historyEntryList.size() - 1 ? 1 : 0);
    }

    public void toFavourites() {
        String url = browser.locationProperty().getValue();
        browserController.addToFavouritesList(url);
        urlIsInFavourites();
    }

    public void toIgnored() {
        String url = browser.locationProperty().getValue();
        browserController.addToIgnoredList(url);
    }

    private void urlIsInFavourites() {
        if (browserController.getFavouritesList().contains(browser.locationProperty().getValue())) {
            toFavouritesFontIcon.setIconColor(Color.web("#FFFF00"));
        } else {
            toFavouritesFontIcon.setIconColor(Color.web("#FFFFFF"));
        }
    }

    private void urlIsIgnored() {
        if (browserController.getIgnored().contains(browser.locationProperty().getValue())) {
            collectHistoryThisPageCheckedMenuItem.setSelected(true);
        }
    }

    public void downloadHTMLPageToHTML() {
        try {
            URI u = new URI(getHistory().getEntries().get(getHistory().getCurrentIndex()).getUrl());
            String new_ur = u.getHost() + u.getRawPath().replaceAll("/", ".");
            DirectoryChooser directoryChooser = new DirectoryChooser();
            String dirPath = directoryChooser.getPathFromDirectoryChooser(browserController.getTabPane().getScene().getWindow());
            File fos = new File(dirPath + File.separator + new_ur + ".html");
            FileUtils.copyURLToFile(u.toURL(), fos);
            System.out.println("Ended downloading");
        } catch (IOException | URISyntaxException e) {
            // todo: log
            System.out.println("exception occured" + e.getMessage());
        }
    }

    public void downloadHTMLPageToZip() {
        try {
            URI u = new URI(getHistory().getEntries().get(getHistory().getCurrentIndex()).getUrl());
            String fileName = u.getHost() + u.getRawPath().replaceAll("/", ".");
            String new_ur = fileName + "html";

            DirectoryChooser directoryChooser = new DirectoryChooser();
            String dirPath = directoryChooser.getPathFromDirectoryChooser(browserController.getTabPane().getScene().getWindow());

            URL url = u.toURL();

            InputStream in = new BufferedInputStream(url.openStream());
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(dirPath + File.separator + new_ur + ".zip"));
            ZipEntry e = new ZipEntry(new_ur);
            out.putNextEntry(e);

            byte[] data = in.readAllBytes();
            out.write(data, 0, data.length);

            out.closeEntry();
            out.close();
            in.close();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // todo: log errors
    }

    private boolean startRecording = false;
    private long startTime;
    private String currentURL;

    private void startRecording() {
        if (!browserController.trackHistory) {
            return;
        }
        if (!browserController.getIgnored().contains(browser.locationProperty().getValue()))  {
            startRecording = true;
            startTime = System.nanoTime();
            currentURL = browser.locationProperty().getValue();
        }
    }

    private void endRecording(boolean startNewRecording) {
        if (!startRecording) {
            return;
        }
        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000000;
        if (duration > 0) {
            int hour = (int) (duration / 3600);
            int mm = (int) (duration / 60);
            duration -= (3600L * hour + 60L * mm);
            browserController.addToHistoryList(new HistoryTableViewDataProvider(LocalDateTime.now(),
                    LocalTime.of(hour, mm, (int) duration), currentURL));
        }
        if (startNewRecording) {
            if (browserController.getIgnored().contains(browser.locationProperty().getValue())) {
                startRecording = false;
                return;
            }
            startTime = System.nanoTime();
            currentURL = browser.locationProperty().getValue();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        browser = webView.getEngine();
        tab.textProperty().bind(webView.getEngine().titleProperty());

        // <--- Load Property Favourite and Record history --->
        browser.locationProperty().addListener((observable, oldValue, newValue) -> {
            if (startRecording) {
                endRecording(true);
            }
            else {
                startRecording();
            }
            urlIsIgnored();
            urlIsInFavourites();
        });

        // <--- Load the website --->
        loadWebSite(firstWebSite);
        startRecording();

        // <--- History --->
        setHistory(browser.getHistory());
        historyEntryList = getHistory().getEntries();
        SimpleListProperty<WebHistory.Entry> list = new SimpleListProperty<>(historyEntryList);

        // <--- Tab --->
        tab.setTooltip(new Tooltip(""));
        tab.getTooltip().textProperty().bind(browser.titleProperty());

        // <--- Search bar --->
        browser.getLoadWorker().runningProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) // if !running
                searchBar.textProperty().unbind();
            else {
                searchBar.textProperty().bind(browser.locationProperty());
            }
        });
        searchBar.setOnAction(a -> {
            loadWebSite(searchBar.getText().startsWith("http://") ? searchBar.getText() : "http://" + searchBar.getText());
        });
        searchBar.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
                Platform.runLater(() -> searchBar.selectAll());
        });

        // <--- goButton --->
        goButton.setOnAction(searchBar.getOnAction());

        // <--- reloadButton --->
        reloadButton.setOnAction(a -> {
            reloadWebSite();
        });

        // <--- backwardButton --->
        backwardButton.setOnAction(a -> {
            goBack();
        });
        backwardButton.disableProperty().bind(getHistory().currentIndexProperty().isEqualTo(0));
        backwardButton.setOnMouseReleased(m -> {
            if (m.getButton() == MouseButton.MIDDLE) //Create and add it next to this tab
                browserController.getTabPane().getTabs().add(browserController.getTabPane().getTabs().indexOf(tab) + 1,
                        browserController.createNewTab(getHistory().getEntries().get(getHistory().getCurrentIndex() - 1).getUrl()).getTab());
        });

        // <--- forwardButton --->
        forwardButton.setOnAction(a -> {
            goForward();
        });
        forwardButton.disableProperty().bind(getHistory().currentIndexProperty().greaterThanOrEqualTo(list.sizeProperty().subtract(1)));
        forwardButton.setOnMouseReleased(m -> {
            if (m.getButton() == MouseButton.MIDDLE) //Create and add it next to this tab
                browserController.getTabPane().getTabs().add(browserController.getTabPane().getTabs().indexOf(tab) + 1,
                        browserController.createNewTab(getHistory().getEntries().get(getHistory().getCurrentIndex() + 1).getUrl()).getTab());
        });

        // <--- Download HTML code --->
        downloadPageMenuItem.setOnAction(printPage -> downloadHTMLPageToHTML());
        downloadPageZipMenuItem.setOnAction(printPage -> downloadHTMLPageToZip());

        // <--- To favourites button --->
        toFavouritesButton.setOnAction(a -> toFavourites());

        // <--- Favourites tab --->
        favouritesMenuItem.setOnAction(a -> browserController.createNewFavouritesTab());

        // <--- Ignored list --->
        collectHistoryThisPageCheckedMenuItem.setOnAction(a -> toIgnored());

        // <--- History tab --->
        historyMenuItem.setOnAction(a -> browserController.createNewHistoryTab());

        // <--- Create new HTML page --->
        newHTMLMenuItem.setOnAction(a -> browserController.createNewCreateHTMLPageTab());

        // <--- Edit or view HTML page --->
        viewHTMLMenuItem.setOnAction(a -> browserController.createNewEditHTMLTab(browser.locationProperty().getValue(), false));
        editHTMLMenuItem.setOnAction(a -> browserController.createNewEditHTMLTab(browser.locationProperty().getValue(), true));
    }

    public Tab getTab() {
        return tab;
    }
}
