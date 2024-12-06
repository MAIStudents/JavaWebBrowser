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

import java.io.*;

/**
 * Контроллер для отображения и редактирования HTML-кода, используемого в WebEngine.
 * Предоставляет возможность загрузки, редактирования и сохранения HTML-страниц.
 */
public final class HTMLController {
  /**
   * Объект WebEngine, который используется для работы с HTML-контентом.
   */
  private final WebEngine webEngine;

  /**
   * Конструктор класса HTMLController.
   *
   * @param webEngine WebEngine, который будет использоваться для взаимодействия с HTML-контентом.
   */
  public HTMLController(final WebEngine webEngine) {
    this.webEngine = webEngine;
  }

  /**
   * Открывает окно для просмотра и редактирования HTML-кода, загруженного в WebEngine.
   * В этом окне пользователь может очистить, применить изменения или загрузить/сохранить HTML-файл.
   */
  public void viewAndEditHtml() {
    String html = (String) webEngine.executeScript("document.documentElement.outerHTML");

    Stage stage = new Stage();
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.setTitle("View and Edit HTML");

    TextArea textArea = new TextArea(html);
    textArea.setWrapText(false);

    Button clearButton = new Button("Clear");
    clearButton.setOnAction(_ -> textArea.clear());

    Button applyButton = new Button("Apply Changes");
    applyButton.setOnAction(_ -> {
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

  /**
   * Создает кнопку для загрузки HTML-файла в текстовое поле.
   *
   * @param stage Окно, в котором будет отображаться диалог выбора файла.
   * @param textArea Текстовое поле для отображения содержимого файла.
   * @return Кнопка для загрузки HTML-файла.
   */
  private static Button getLoadFromFileButton(final Stage stage,
                                              final TextArea textArea) {
    Button loadFromFileButton = new Button("Load from file");
    loadFromFileButton.setOnAction(_ -> {
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
          System.out.println("Error loading HTML file: " + e);
        }
      } else {
        System.out.println("File is null");
      }
    });
    return loadFromFileButton;
  }

  /**
   * Создает кнопку для сохранения текущего содержимого текстового поля в файл.
   *
   * @param stage Окно, в котором будет отображаться диалог сохранения файла.
   * @param textArea Текстовое поле, содержимое которого будет сохранено.
   * @return Кнопка для сохранения содержимого в файл.
   */
  private static Button getSaveToFileButton(final Stage stage,
                                            final TextArea textArea) {
    Button saveToFileButton = new Button("Save");
    saveToFileButton.setOnAction(_ -> {
      FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle("Save HTML File");
      fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));
      File file = fileChooser.showSaveDialog(stage);
      if (file != null) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
          writer.write(textArea.getText());
        } catch (IOException e) {
          System.out.println("Error saving HTML file: " + e.getMessage());
        }
      }
    });
    return saveToFileButton;
  }
}
