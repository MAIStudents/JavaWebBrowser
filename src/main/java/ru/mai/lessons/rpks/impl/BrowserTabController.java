package ru.mai.lessons.rpks.impl;

import javafx.application.Platform;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
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
    private final BrowserController webBrowserController;
    private String firstWebSite;
    WebEngine browser;
    private WebHistory history;
    private ObservableList<WebHistory.Entry> historyEntryList;

    private final Logger logger = Logger.getLogger(getClass().getName());

    public BrowserTabController(BrowserController browserController, Tab tab, String firstWebSite) {
        this.webBrowserController = browserController;
        this.tab = tab;
        this.firstWebSite = firstWebSite;
        this.tab.setContent(this);

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
            browser.load("https://www.google.com");
    }

    public void goBack() {
        getHistory().go(historyEntryList.size() > 1 && getHistory().getCurrentIndex() > 0 ? -1 : 0);
    }

    public void goForward() {
        getHistory().go(historyEntryList.size() > 1 && getHistory().getCurrentIndex() < historyEntryList.size() - 1 ? 1 : 0);
    }

    public void toFavourites() {
        // todo: check if this page already is in favourites. If it is, delete it from list
        // todo: add to json file (?)
        String url = browser.locationProperty().getValue();
        webBrowserController.addToFavouritesList(url);
        urlIsInFavourites();
    }

    private void urlIsInFavourites() {
        if (webBrowserController.getFavouritesList()== null) { return;}
        if (webBrowserController.getFavouritesList().contains(browser.locationProperty().getValue())) {
            toFavouritesFontIcon.setIconColor(Color.web("#FFFF00"));
        } else {
            toFavouritesFontIcon.setIconColor(Color.web("#FFFFFF"));
        }
    }

    public void downloadHTMLPageToHTML() {
        try {
            URI u = new URI(getHistory().getEntries().get(getHistory().getCurrentIndex()).getUrl());
            String new_ur = u.getHost() + u.getRawPath().replaceAll("/", ".");
            DirectoryChooser directoryChooser = new DirectoryChooser();
            String dirPath = directoryChooser.getPathFromDirectoryChooser(webBrowserController.getTabPane().getScene().getWindow());
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
            String dirPath = directoryChooser.getPathFromDirectoryChooser(webBrowserController.getTabPane().getScene().getWindow());

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        browser = webView.getEngine();
        tab.textProperty().bind(webView.getEngine().titleProperty());

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
        searchBar.setOnAction(a -> loadWebSite(searchBar.getText()));
        searchBar.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue)
                Platform.runLater(() -> searchBar.selectAll());
        });

        // <--- Load favourite property of webpage --->
        browser.getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (Worker.State.SUCCEEDED.equals(newValue)) {
                urlIsInFavourites();
            }
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
                webBrowserController.getTabPane().getTabs().add(webBrowserController.getTabPane().getTabs().indexOf(tab) + 1,
                        webBrowserController.createNewTab(getHistory().getEntries().get(getHistory().getCurrentIndex() - 1).getUrl()).getTab());
        });

        // <--- forwardButton --->
        forwardButton.setOnAction(a -> {
            goForward();
        });
        forwardButton.disableProperty().bind(getHistory().currentIndexProperty().greaterThanOrEqualTo(list.sizeProperty().subtract(1)));
        forwardButton.setOnMouseReleased(m -> {
            if (m.getButton() == MouseButton.MIDDLE) //Create and add it next to this tab
                webBrowserController.getTabPane().getTabs().add(webBrowserController.getTabPane().getTabs().indexOf(tab) + 1,
                        webBrowserController.createNewTab(getHistory().getEntries().get(getHistory().getCurrentIndex() + 1).getUrl()).getTab());
        });

        // <--- Load the website --->
        loadWebSite(firstWebSite);

        // <--- To favourites button --->
        toFavouritesButton.setOnAction(a -> toFavourites());

        // <--- Download HTML code --->
        downloadPageMenuItem.setOnAction(printPage -> downloadHTMLPageToHTML());
        downloadPageZipMenuItem.setOnAction(printPage -> downloadHTMLPageToZip());

        // <--- Favourites list --->
        favouritesMenuItem.setOnAction(a -> webBrowserController.createNewFavouritesTab());
    }

    public Tab getTab() {
        return tab;
    }
}
