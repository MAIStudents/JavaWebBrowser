package ru.mai.lessons.rpks.controler;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import ru.mai.lessons.rpks.WebViewExample;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ResourceBundle;

public class HTMLEditorController implements Initializable {
    @FXML
    private TextArea textArea;
    private File fileHTML;
    private final static String PATH_DOWNLOADS = WebViewExample.class.getResource("").getPath() + "downloads/";
    private final static String EDIT_HTML_FILE = PATH_DOWNLOADS + "index.html";


    MainController mainController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileHTML = new File(EDIT_HTML_FILE);
        try {
            textArea.setText(Files.readString(fileHTML.toPath()));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public void setControllerApplication(MainController controller) {
        this.mainController = controller;
    }

    public void save() {
        try (FileWriter fileWriter = new FileWriter(fileHTML)) {
            fileWriter.write(textArea.getText());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

        mainController.loadContent(textArea.getText());
    }
}
