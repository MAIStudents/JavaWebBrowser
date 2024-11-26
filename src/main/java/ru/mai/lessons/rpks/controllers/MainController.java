package ru.mai.lessons.rpks.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.mai.lessons.rpks.models.History;
import ru.mai.lessons.rpks.utils.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MainController implements Initializable {

  private static final Logger log = Logger.getLogger(MainController.class);

  private boolean globalPrivateMode = false;

  @FXML
  private TabPane tabPane;

  @FXML
  private HBox hBox;

  @FXML
  private TextField urlField;

  @FXML
  private Button goButton;

  @FXML
  private Button reloadButton;

  @FXML
  private Button addButton;

  @FXML
  private Button delButton;

  @FXML
  public Button favoriteButton;
  private final Set<String> favorites = new LinkedHashSet<>();

  @FXML
  private MenuButton menuButton;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    addNewTab();

    goButton.setOnAction(actionEvent -> loadPageFromTextBar());
    reloadButton.setOnAction(actionEvent -> reloadPage());
    addButton.setOnAction(actionEvent -> addNewTab());
    delButton.setOnAction(actionEvent -> delTab());
    favoriteButton.setOnAction(actionEvent -> addToFavorites());

    Platform.runLater(() -> {
      Stage mainStage = (Stage) tabPane.getScene().getWindow();
      mainStage.setOnCloseRequest(event -> closeApp());
    });

    tabPane.getTabs().addListener((ListChangeListener.Change<?extends Tab> change) -> {
      if (tabPane.getTabs().isEmpty()) {
        closeApp();
      }
    });
  }

  private void loadPageFromTextBar() {
    String input = urlField.getText().trim();
    log.debug("{}", input);
    if (input.isEmpty()) {
      log.warn("URL field is empty");
      return;
    }

    Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
    if (selectedTab != null) {
      PageTabController currentTab = (PageTabController) selectedTab.getUserData();
      loadPage(input, currentTab);
    } else {
      log.warn("No selected tab to load the page");
    }
  }

  private void loadPage(String url, PageTabController pageTabController) {
    if (url == null || url.isEmpty()) {
      log.warn("URL is empty or null");
      return;
    }
    if (pageTabController == null) {
      log.warn("Error with current tab");
      return;
    }

    String formattedUrl = formatUrl(url);
    log.info("Formatted URL: {}", formattedUrl);

    pageTabController.getWebEngine().getLoadWorker().exceptionProperty().removeListener(pageTabController.getExceptionListener());
    ChangeListener<Throwable> exceptionListener = (observableValue, oldException, newException) -> {
      if (newException != null) {
        log.error("Error loading URL: {}", formattedUrl, newException);

        String googleSearchUrl = "https://www.google.com/search?q=" + url;
        log.info("Redirecting to Google Search: {}", googleSearchUrl);
        pageTabController.getWebEngine().load(googleSearchUrl);
      }
    };
    pageTabController.setExceptionListener(exceptionListener);
    pageTabController.getWebEngine().getLoadWorker().exceptionProperty().addListener(exceptionListener);

    pageTabController.getWebEngine().getLoadWorker().stateProperty().removeListener(pageTabController.getStateListener());
    ChangeListener<Worker.State> stateListener = (observable, oldState, newState) -> {
      if (newState == Worker.State.SUCCEEDED) {
        String currentUrl = pageTabController.getWebEngine().getLocation();
        if (isValidUrl(currentUrl) && !Objects.equals(pageTabController.getHistoryController().getCurrent(), currentUrl)) {
          pageTabController.getHistoryController().addEntry(currentUrl);
          log.info("Successfully loaded URL: {}", currentUrl);
        } else {
          log.warn("Invalid URL, not adding to history: {}", currentUrl);
        }
      }
    };
    pageTabController.setStateListener(stateListener);
    pageTabController.getWebEngine().getLoadWorker().stateProperty().addListener(stateListener);

    log.info("Loading URL: {}", formattedUrl);
    pageTabController.getWebEngine().load(formattedUrl);
  }

  private boolean isValidUrl(String url) {
    try {
      new java.net.URL(url);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private String formatUrl(String url) {
    return url.startsWith("http://") || url.startsWith("https://") ? url : "https://" + url;
  }

  private void reloadPage() {
    Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
    if (selectedTab != null) {
      PageTabController currentTab = (PageTabController) selectedTab.getUserData();
      if (currentTab != null) {
        currentTab.getWebEngine().reload();
      }
    }
  }

  private void addNewTab() {
    PageTabController newTabController = new PageTabController("https://www.google.com");
    newTabController.getHistoryController().addEntry("https://www.google.com");
    Tab newTab = newTabController.getTab();
    tabPane.getTabs().add(newTab);
    tabPane.getSelectionModel().select(newTab);
    log.info("Add new tab");
  }

  private void delTab() {
    Tab curentTab = tabPane.getSelectionModel().getSelectedItem();
    if (curentTab != null) {
      tabPane.getTabs().remove(curentTab);
      log.info("Tab remove");
    }
  }

  private void addToFavorites() {
    PageTabController pageTabController = (PageTabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    if (pageTabController != null) {
      String currentURL = pageTabController.getWebEngine().getLocation();
      if (favorites.add(currentURL)) {
        log.info("Added to favorites url: {}", currentURL);
      } else {
        log.info("This url is already in favorites {}", currentURL);
      }
    } else {
      log.warn("No active tab to add to favorites");
    }
  }

  @FXML
  private void selectFavorites () {
    if (favorites.isEmpty()) {
      log.warn("No favorites to show");
      return;
    }

    Stage favoriteStage = new Stage();
    favoriteStage.initModality(Modality.APPLICATION_MODAL);
    VBox favoriteList = new VBox(10);
    favoriteList.setPadding(new Insets(10));
    favoriteList.setAlignment(Pos.BOTTOM_LEFT);

    for (String favorite : favorites) {
      Button favoriteUrlButton = new Button(favorite);
      PageTabController pageTabController = (PageTabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
      favoriteUrlButton.setOnAction(actionEvent -> {
        loadPage(favorite, pageTabController);
        favoriteStage.close();
      });
      favoriteList.getChildren().add(favoriteUrlButton);
    }

    ScrollPane scrollPane = new ScrollPane(favoriteList);
    Scene scene = new Scene(scrollPane, 200, 300);
    favoriteStage.setScene(scene);
    favoriteStage.setTitle("Favorites");

    Stage primaryStage = (Stage) tabPane.getScene().getWindow();
    favoriteStage.setX(primaryStage.getX() + primaryStage.getWidth() - scene.getWidth());
    favoriteStage.setY(primaryStage.getY() + tabPane.getHeight() / 2);

    favoriteStage.show();
  }

  @FXML
  private void saveToZip() {
    Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
    if (currentTab != null) {
      PageTabController pageTabController = (PageTabController) currentTab.getUserData();
      if (pageTabController != null) {
        String pageContent = (String) pageTabController.getWebEngine().executeScript("document.documentElement.outerHTML");
        savePageToZip(pageContent);
      }
    }
  }

  private void savePageToZip(String pageContent) {
    if (pageContent == null || pageContent.isEmpty()) {
      log.error("Error with page content");
      return;
    }

    String downloadsDir = getDownloadsDirectory();
    if (downloadsDir == null) {
      log.error("Cannot determine Downloads directory");
      return;
    }

    PageTabController pageTabController = (PageTabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    String fileName = "default_page.html";
    if (pageTabController != null) {
      String url = pageTabController.getWebEngine().getLocation();
      if (url != null && !url.isEmpty()) {
        fileName = url.replaceAll("[^a-zA-Z0-9.-]", "_") + ".html";
      }
    }

    Path zipFilePath = Paths.get(downloadsDir, "saved_page.zip");
    try (FileOutputStream fileOutputStream = new FileOutputStream(zipFilePath.toFile());
         ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {

      ZipEntry zipEntry = new ZipEntry(fileName);
      zipOutputStream.putNextEntry(zipEntry);

      byte[] bytes = pageContent.getBytes(StandardCharsets.UTF_8);
      zipOutputStream.write(bytes, 0, bytes.length);
      zipOutputStream.closeEntry();

      log.info("Page saved to ZIP: {}", zipFilePath);
    } catch (IOException e) {
      log.error("Error while saving to ZIP: ", e);
      throw new RuntimeException(e);
    }
  }


  private String getDownloadsDirectory() {
    String os = System.getProperty("os.name").toLowerCase();
    String userHome = System.getProperty("user.home");

    if (os.contains("win")) {
      return userHome + "\\Downloads";
    } else if (os.contains("mac") || os.contains("nix") || os.contains("nux")) {
      return userHome + "/Downloads";
    } else {
      return null;
    }
  }

  @FXML
  public void goBack() {
    PageTabController pageTabController = (PageTabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    String previousUrl = pageTabController.getHistoryController().goBack();
    if (previousUrl != null) {
      log.info("Go to the previous URL {}", previousUrl);
      pageTabController.getWebEngine().load(previousUrl);
    } else {
      log.warn("Doesn't have previous URL");
    }
  }

  @FXML
  public void goForward() {
    PageTabController pageTabController = (PageTabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    String nextUrl = pageTabController.getHistoryController().goForward();
    if (nextUrl != null) {
      log.info("Go to the next URL {}", nextUrl);
      pageTabController.getWebEngine().load(nextUrl);
    } else {
      log.warn("Doesn't have next URL");
    }
  }

  @FXML
  private void toggleGlobalPrivateMode() {
    PageTabController pageTabController = (PageTabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    if (pageTabController != null) {
      globalPrivateMode = !globalPrivateMode;
      HistoryController.setHistoryEnabled(!globalPrivateMode);
      log.info("Global Private Mode: {}", globalPrivateMode ? "Enabled" : "Disabled");
      Scene mainScene = tabPane.getScene();
      if (globalPrivateMode) {
        mainScene.getRoot().setStyle("-fx-background-color: #2F4444; -fx-opacity: 1.0;");
      } else {
        mainScene.getRoot().setStyle("");
      }
    }
  }

  @FXML
  private void toggleSitePrivateMode() {
    PageTabController pageTabController = (PageTabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    if (pageTabController != null) {
      String currentUrl = pageTabController.getWebEngine().getLocation();
      if (HistoryController.isSiteExcluded(currentUrl)) {
        pageTabController.getHistoryController().removeExcludedSite(currentUrl);
        log.info("Site removed from private mode: {}", currentUrl);
      } else {
        pageTabController.getHistoryController().addExcludedSite(currentUrl);
        log.info("Site added to private mode: {}", currentUrl);
      }
    }
  }

  @FXML
  private void saveHistoryToResourcesXML() {
    PageTabController pageTabController = (PageTabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    if (pageTabController != null) {
      try {
        File resourcesDir = new File("src/main/resources/xml");
        if (!resourcesDir.exists() && !resourcesDir.mkdirs()) {
          log.error("Failed to create resources directory");
          return;
        }
        File file = new File(resourcesDir, "history.xml");
        pageTabController.getHistoryController().saveHistoryToXml(file);
        log.info("History saved to XML in resources: {}", file.getAbsolutePath());
      } catch (IOException e) {
        log.error("Failed to save history to XML in resources", e);
      }
    }
  }

  @FXML
  private void viewAndEditHtml() {
    PageTabController pageTabController = (PageTabController) tabPane.getSelectionModel().getSelectedItem().getUserData();

    if (pageTabController != null) {
      pageTabController.getHtmlController().viewAndEditHtml();
    }
  }

  @FXML
  private void showHistoryViewer () {
    PageTabController pageTabController = (PageTabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    if (pageTabController == null) {
      log.warn("No active tab to show history");
      return;
    }

    Stage historyStage = new Stage();
    historyStage.initModality(Modality.APPLICATION_MODAL);
    historyStage.setTitle("History");

    TableView<History.HistoryDto> tableView = new TableView<>();

    TableColumn<History.HistoryDto, String> urlColumn = getStringTableColumn(historyStage);

    TableColumn<History.HistoryDto, String> visitDateColumn = new TableColumn<>("Visited");
    visitDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTimestamp()));
    visitDateColumn.setPrefWidth(300);

    tableView.getColumns().addAll(urlColumn, visitDateColumn);

    tableView.getItems().addAll(pageTabController.getHistoryController()
            .getHistoryListGlobal().stream()
            .map(History.HistoryDto::new)
            .toList());

    VBox layout = new VBox(10);
    layout.setPadding(new Insets(10));
    layout.getChildren().add(tableView);

    Scene scene = new Scene(layout, 600, 400);
    historyStage.setScene(scene);

    historyStage.show();
  }

  private TableColumn<History.HistoryDto, String> getStringTableColumn(Stage historyStage) {
    TableColumn<History.HistoryDto, String> urlColumn = new TableColumn<>("URL");
    urlColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUrl()));
    urlColumn.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(String url, boolean empty) {
        super.updateItem(url, empty);
        if (empty || url == null) {
          setText(null);
          setGraphic(null);
        } else {
          Hyperlink link = new Hyperlink(url);
          link.setOnAction(actionEvent -> {
            PageTabController pageTabController = (PageTabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
            loadPage(url,pageTabController);
            historyStage.close();
          });
          setGraphic(link);
        }
      }
    });
    urlColumn.setPrefWidth(300);
    return urlColumn;
  }

  private void closeApp() {
    log.info("Closing application...");
    Platform.exit();
  }

}
