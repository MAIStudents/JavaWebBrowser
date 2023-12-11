package ru.mai.lessons.rpks.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ControllerHTMLCreator implements Initializable {
    @FXML
    private TextArea textArea;
    private File currentFile;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        currentFile = null;
    }

    public void save() {
        if (currentFile == null) {
            saveAs();
        } else {
            saveToFile(currentFile);
        }
    }

    public void saveAs() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("HTML files (*.html)", "*.html");
        fileChooser.getExtensionFilters().add(extFilter);

        File selectedFile = fileChooser.showSaveDialog(null);

        if (selectedFile != null) {
            currentFile = selectedFile;

            if (!currentFile.getName().toLowerCase().endsWith(".html")) {
                currentFile = new File(currentFile.getParent(), currentFile.getName() + ".html");
            }

            saveToFile(currentFile);
        }
    }

    private void saveToFile(File file) {
        try (FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(textArea.getText());
        } catch (IOException e) {
            System.err.println("IOException");
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
