package ru.mai.lessons.rpks.impl;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import ru.mai.lessons.rpks.helpClasses.DirectoryChooser;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.Scanner;

import org.apache.log4j.Logger;

public class EditHTMLTabController extends StackPane implements Initializable {
    private final Logger logger = Logger.getLogger(EditHTMLTabController.class.getName());

    @FXML
    private JFXButton saveBtn;

    @FXML
    private TextArea textArea;

    @FXML
    private HTMLEditor editor;

    @FXML
    private JFXButton viewChangesBtn;

    @FXML
    private Label header;

    BrowserController browserController;
    Tab tab;
    String address;
    boolean isEditable = true;

    public EditHTMLTabController(BrowserController browserController, Tab tab, String address, boolean isEditable) {
        this.browserController = browserController;
        this.tab = tab;
        this.address = address;
        this.isEditable = isEditable;
        this.tab.setContent(this);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxml/EditHTMLTab.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
    }

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

    private void loadTextAreaFromHTMLEditor() {
        textArea.setText(editor.getHtmlText());
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LoadPageThread loadPageThread =
                new LoadPageThread(address, editor, textArea);
        loadPageThread.start();

        if (!isEditable) {
            textArea.setEditable(false);
            viewChangesBtn.setDisable(true);
            viewChangesBtn.setVisible(false);
            header.setText("Просмотр HTML страницы");
        }

        // from textarea to engine
        viewChangesBtn.setOnAction(a -> loadTextAreaFromHTMLEditor());

        saveBtn.setOnAction(a -> saveHTMLPage());
        logger.info("Initializing of view/edit HTML tab done");
    }

    class LoadPageThread extends Thread {

        String PageSrc;
        HTMLEditor editor;
        TextArea textArea;

        public LoadPageThread(String src, HTMLEditor editor, TextArea textArea) {
            PageSrc = src;
            this.editor = editor;
            this.textArea = textArea;
        }

        @Override
        public void run() {
            String result = loadPage(PageSrc);

            Platform.runLater(() -> {
                //update html code in HTMLEditor
                editor.setHtmlText(result);

                //get html code from HTMLEditor
                String html = editor.getHtmlText();
                textArea.setText(html);
            });
        }

        private String loadPage(String src) {

            StringBuilder pageCode = new StringBuilder();

            try {
                URL url = new URL(src);
                URLConnection connection = url.openConnection();
                InputStream inputStream = connection.getInputStream();
                Scanner scanner = new Scanner(inputStream);

                while (scanner.hasNextLine()) {
                    pageCode.append(scanner.nextLine()).append("\n");
                }

            } catch (IOException ex) {
                logger.error(ex.getMessage());
            }

            return pageCode.toString();
        }
    }

}
