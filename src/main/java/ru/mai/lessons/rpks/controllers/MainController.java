package ru.mai.lessons.rpks.controllers;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class MainController implements Initializable {
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
    addNewTab("https://www.google.com");

    goButton.setOnAction(actionEvent -> loadPageFromTextBar());
    reloadButton.setOnAction(actionEvent -> reloadPage());
    addButton.setOnAction(actionEvent -> addNewTab("https://www.google.com"));
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

    String formattedUrl = url.startsWith("http://") || url.startsWith("https://") ? url : "https://" + url;

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

    log.info("Loading URL: {}", formattedUrl);
    pageTabController.getWebEngine().load(formattedUrl);
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

  private void addNewTab(String url) {
    PageTabController newTabController = new PageTabController(url);
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
    VBox favoriteList = new VBox(10);
    favoriteList.setPadding(new Insets(10));
    favoriteList.setAlignment(Pos.BOTTOM_LEFT);

    for (String favorite : favorites) {
      Button favoriteUrlButton = new Button(favorite);
      PageTabController pageTabController = (PageTabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
      favoriteUrlButton.setOnAction(actionEvent -> loadPage(favorite, pageTabController));
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
  public void goBack(ActionEvent actionEvent) {
  }

  @FXML
  public void goForward(ActionEvent actionEvent) {
  }

  private void closeApp() {
    log.info("Closing application...");
    Platform.exit();

  }

}
