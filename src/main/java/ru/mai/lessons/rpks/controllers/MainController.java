package ru.mai.lessons.rpks.controllers;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
public class MainController implements Initializable {
  @FXML
  private TabPane tabPane;

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
  private MenuButton menuButton;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    addNewTab("https://www.google.com");

    goButton.setOnAction(actionEvent -> loadPage());
    reloadButton.setOnAction(actionEvent -> reloadPage());
    addButton.setOnAction(actionEvent -> addNewTab("https://www.google.com"));
    delButton.setOnAction(actionEvent -> delTab());

    tabPane.getTabs().addListener((ListChangeListener.Change<?extends Tab> change) -> {
      if (tabPane.getTabs().isEmpty()) {
        closeApp();
      }
    });
  }

  private void loadPage() {
    String input = urlField.getText().trim();
    if (input.isEmpty()) {
      return;
    }

    Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
    if (selectedTab != null) {
      PageTabController currentTab = (PageTabController) selectedTab.getUserData();
      if (currentTab != null) {
        String url = input.startsWith("http://") || input.startsWith("https://") ? input : "https://" + input;
        currentTab.getWebEngine().getLoadWorker().exceptionProperty().addListener(((observableValue, oldValue, newValue) -> {
          if (newValue != null) {
            String googleSearchUrl = "https://www.google.com/search?q=" + input;
            currentTab.getWebEngine().load(googleSearchUrl);
          }
        }));
        currentTab.getWebEngine().load(url);
      }
    }
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
  }

  private void delTab() {
    Tab curentTab = tabPane.getSelectionModel().getSelectedItem();
    if (curentTab != null) {
      tabPane.getTabs().remove(curentTab);
    }
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

  private void closeApp() {
    log.info("Closing application...");
    Platform.exit();
  }
}
