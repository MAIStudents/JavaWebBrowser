package ru.mai.lessons.rpks.impl;

import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import com.jfoenix.controls.JFXButton;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.util.ResourceBundle;

public class EditHTMLTabController extends StackPane implements Initializable {
    // todo: get html code of this page in area
    // todo: if textarea changed,

    @FXML
    private JFXButton saveBtn;

    @FXML
    private TextArea textArea;

    @FXML
    private JFXButton viewChangesBtn;

    @FXML
    private WebView webView;

    BrowserController browserController;
    Tab tab;
    String address;
    boolean isEditable = true;

    public EditHTMLTabController(BrowserController browserController, Tab tab, String address, boolean isEditable) {
        this.browserController = browserController;
        this.tab = tab;
        this.address = address;
        this.isEditable = isEditable;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        WebEngine engine = webView.getEngine();
        String html = (String) engine.executeScript("document.documentElement.outerHTML");
        // todo: retrieve initial HTML code from page
        // todo: add to textarea
        // todo: code from textarea add to browser
        //
        textArea.setText(html);
        textArea.setEditable(isEditable);
        engine.load(address);

    }
}
