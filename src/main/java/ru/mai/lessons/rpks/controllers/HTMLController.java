package ru.mai.lessons.rpks.controllers;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebEngine;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Modality;
import ru.mai.lessons.rpks.utils.Logger;

import java.io.*;


public class HTMLController {
  private static final Logger log = Logger.getLogger(HTMLController.class);
  private final WebEngine webEngine;

  public HTMLController(WebEngine webEngine) {
    this.webEngine = webEngine;
  }

  public void viewAndEditHtml () {
    String html = (String) webEngine.executeScript("document.documentElement.outerHTML");

    Stage stage = new Stage();
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.setTitle("View and Edit HTML");

    TextArea textArea = new TextArea(html);
    textArea.setWrapText(false);

    Button clearButton = new Button("Clear");
    clearButton.setOnAction(actionEvent -> textArea.clear());

    Button applyButton = new Button("Apply Changes");
    applyButton.setOnAction(actionEvent -> {
      String editedHtml = textArea.getText();
      webEngine.executeScript(
              "document.open();" +
                      "document.write(`" + editedHtml.replace("`", "\\`") + "`);" +
                      "document.close();"
      );
      stage.close();
    });

    Button saveToFileButton = getSaveToFileButton(stage, textArea);

    Button loadFromFileButton = getLoadFromFileButton(stage, textArea);

    HBox buttonBox = new HBox(10);
    buttonBox.getChildren().addAll(clearButton, applyButton, saveToFileButton, loadFromFileButton);
    buttonBox.setPadding(new javafx.geometry.Insets(10));

    BorderPane borderPane = new BorderPane();
    borderPane.setTop(buttonBox);
    borderPane.setCenter(textArea);

    Scene scene = new Scene(borderPane, 800, 600);
    stage.setScene(scene);
    stage.show();
  }

  private static Button getLoadFromFileButton(Stage stage, TextArea textArea) {
    Button loadFromFileButton = new Button("Load from file");
    loadFromFileButton.setOnAction(actionEvent -> {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Load HTML File");
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));
      File file = fileChooser.showOpenDialog(stage);
      if (file != null) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
          StringBuilder content = new StringBuilder();
          String line;
          while ((line = reader.readLine()) != null) {
            content.append(line).append("\n");
          }
          textArea.setText(content.toString());
        } catch (IOException e) {
          log.error("Error loading HTML file", e);
        }
      } else {
        log.warn("File is null");
      }
    });
    return loadFromFileButton;
  }

  private static Button getSaveToFileButton(Stage stage, TextArea textArea) {
    Button saveToFileButton = new Button("Save");
    saveToFileButton.setOnAction(actionEvent -> {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Save HTML File");
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));
      File file = fileChooser.showSaveDialog(stage);
      if (file != null) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
          writer.write(textArea.getText());
        } catch (IOException e) {
          log.error("Error saving HTML file", e);
        }
      }
    });
    return saveToFileButton;
  }
}
