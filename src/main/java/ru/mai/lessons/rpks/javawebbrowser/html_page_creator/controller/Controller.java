package ru.mai.lessons.rpks.javawebbrowser.html_page_creator.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.web.WebView;
import ru.mai.lessons.rpks.javawebbrowser.Main;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;

public class Controller {

    @FXML
    private WebView preview;

    @FXML
    private TextArea html;

    @FXML
    public void updateWebsiteOnClick() {
        String htmlCode = html.getText();
        preview.getEngine().loadContent(htmlCode, "text/html");
    }

    @FXML
    public void saveHtmlOnClick() throws IOException {
        String htmlCode = html.getText();
        File file = new File("website_" + System.currentTimeMillis() + ".html");
        PrintWriter out = new PrintWriter(file);
        out.println(htmlCode);
        out.close();

        Alert alert = new Alert(Alert.AlertType.NONE, MessageFormat.format(Main.BUNDLE.getString("creator.saveMessage"), file.getAbsolutePath()), ButtonType.CLOSE);
        alert.setTitle(Main.BUNDLE.getString("creator.success"));
        alert.show();
    }

}
