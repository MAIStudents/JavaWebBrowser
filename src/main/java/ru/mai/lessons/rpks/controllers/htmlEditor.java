package ru.mai.lessons.rpks.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.jsoup.Jsoup;
import ru.mai.lessons.rpks.managers.SavingManager;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class htmlEditor {
    @FXML
    public CodeArea htmlCode;
    @FXML
    public Button ApplyButton;

    public Tab tab;
    private static final Logger logger = Logger.getLogger(htmlEditor.class.getName());

    public void applyChanges(ActionEvent ignoredEvent) {
        AnchorPane tabContent = (AnchorPane) tab.getContent();
        WebView webView = (WebView) tabContent.lookup("#webView");
        WebEngine webEngine = webView.getEngine();

        webEngine.loadContent(htmlCode.getText());
    }

    public void save(ActionEvent ignoredEvent) {
        FileChooser fileChooser = SavingManager.createFileChooser();

        File selectedFile = fileChooser.showSaveDialog(new Stage());

        if (selectedFile == null) {
            logger.log(Level.INFO, "Saving was canceled");
            return;
        }

        SavingManager.createZip(selectedFile, htmlCode.getText());

        logger.log(Level.INFO, "Page successfully saved here: " + selectedFile.getAbsolutePath());
    }

    public static void open(FXMLLoader loader, String html, Tab tab, boolean changeToSavePage) throws IOException {
        Stage editorStage = new Stage();
        editorStage.initModality(Modality.APPLICATION_MODAL);
        editorStage.setTitle("HTML Editor");

        Parent root = loader.load();

        htmlEditor editor = loader.getController();

        if (!changeToSavePage) {
            editor.htmlCode.appendText(Jsoup.parse(html).outerHtml());
            editor.htmlCode.setParagraphGraphicFactory(LineNumberFactory.get(editor.htmlCode));
            editor.tab = tab;
        } else {
            editor.ApplyButton.setText("Save page");
            editor.ApplyButton.setOnAction(editor::save);
        }

        editorStage.setScene(new Scene(root));
        editorStage.show();
    }
}
