package ru.mai.lessons.rpks.impl;

import com.jfoenix.controls.JFXButton;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.web.HTMLEditor;
import ru.mai.lessons.rpks.helpClasses.DirectoryChooser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;


public class CreateHTMLTabController extends StackPane implements Initializable {
    private final Logger logger = Logger.getLogger(CreateHTMLTabController.class.getName());

    @FXML
    private HTMLEditor htmlEditor;

    @FXML
    private TextArea textArea;

    @FXML
    JFXButton saveBtn;

    BrowserController browserController;
    Tab tab;

    private void saveHTMLPage() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        String dirPath = directoryChooser.getPathFromDirectoryChooser(browserController.getTabPane().getScene().getWindow());

        File fos = new File(dirPath +
                File.separator +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("d-MMM-uuuu_HH-mm-ss")) + ".html");
        try {
            FileWriter writer = new FileWriter(fos);
            writer.write(textArea.getText());
            writer.flush();
            writer.close();
            logger.info("Downloaded successfully");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    public CreateHTMLTabController(BrowserController br, Tab tab) {
        this.browserController = br;
        this.tab = tab;
        this.tab.setContent(this);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/CreateHTMLTab.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

    private void getHTMLFromEditor() {
        String[] arr = htmlEditor.getHtmlText().split(">");
        StringBuilder overall = new StringBuilder();
        for (String str : arr) {
            overall.append(str).append(">").append(System.lineSeparator());
        }
        textArea.setText(overall.toString());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        htmlEditor.setOnKeyPressed(new EventHandler<>() {
            @Override
            public void handle(KeyEvent event) {
                if (isValidEvent(event)) {
                    getHTMLFromEditor();
                }
            }

            private boolean isValidEvent(KeyEvent event) {
                return !isSelectAllEvent(event)
                        && ((isPasteEvent(event)) || isCharacterKeyReleased(event));
            }

            private boolean isSelectAllEvent(KeyEvent event) {
                return event.isShortcutDown() && event.getCode() == KeyCode.A;
            }

            private boolean isPasteEvent(KeyEvent event) {
                return event.isShortcutDown() && event.getCode() == KeyCode.V;
            }

            private boolean isCharacterKeyReleased(KeyEvent event) {
                return switch (event.getCode()) {
                    case ALT, COMMAND, CONTROL, SHIFT -> false;
                    default -> true;
                };
            }
        });
        textArea.setEditable(false);
        saveBtn.setOnAction(a -> {
            getHTMLFromEditor();
            saveHTMLPage();
        });

        logger.info("Initializing of create HTML tab done");
    }
}
