package ru.mai.lessons.rpks.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import ru.mai.lessons.rpks.Application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ControllerHTMLEditor implements Initializable {
    private ControllerApplication controllerApplication;
    @FXML
    private TextArea textArea;
    private File fileHTML;
    private final static String PATH_DOWNLOADS = Application.class.getResource("").getPath() + "downloads/";
    private final static String EDIT_HTML_FILE = PATH_DOWNLOADS + "editHTML.html";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileHTML = new File(EDIT_HTML_FILE);
        try {
            textArea.setText(Files.readString(fileHTML.toPath()));
        } catch (IOException e) {
            System.err.println("IOException");
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
    }

    public void setControllerApplication(ControllerApplication controllerApplication) {
        this.controllerApplication = controllerApplication;
    }

    public void save() {
        try (FileWriter fileWriter = new FileWriter(fileHTML)) {
            fileWriter.write(textArea.getText());
        } catch (IOException e) {
            System.err.println("IOException");
            System.err.println(Arrays.toString(e.getStackTrace()));
        }

        controllerApplication.loadContent(textArea.getText());
    }
}
