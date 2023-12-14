package ru.mai.lessons.rpks.impl;

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
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import ru.mai.lessons.rpks.helpClasses.DirectoryChooser;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EditHTMLTabController extends StackPane implements Initializable {
    // todo: get html code of this page in area
    // todo: if textarea changed,
    private final Logger logger = Logger.getLogger(getClass().getName());

    @FXML
    private JFXButton saveBtn;

    @FXML
    private TextArea textArea;

    @FXML
    private JFXButton viewChangesBtn;

    @FXML
    private WebView webView;

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
            logger.log(Level.SEVERE, "", ex);
        }
    }

    private void loadFromTextArea() {
        webView.getEngine().loadContent(textArea.getText());
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ChangeListener<Worker.State> listener = new ChangeListener<Worker.State>() {
            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                if (newValue == Worker.State.SUCCEEDED) {
                    Document doc = webView.getEngine().getDocument();
                    try {
                        Transformer transformer = TransformerFactory.newInstance().newTransformer();
                        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
                        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                        StringWriter writer = new StringWriter();
                        transformer.transform(new DOMSource(doc), new StreamResult(writer));
                        textArea.setText(writer.toString());
                        loadFromTextArea();
                        webView.getEngine().getLoadWorker().stateProperty().removeListener(this);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
        webView.getEngine().getLoadWorker().stateProperty().addListener(listener);
        webView.getEngine().load(address);

        if (!isEditable) {
            textArea.setEditable(false);
            header.setText("Просмотр HTML страницы");
        }

        // from textarea to engine
        viewChangesBtn.setOnAction(a -> loadFromTextArea());

        saveBtn.setOnAction(a -> saveHTMLPage());
    }
}
