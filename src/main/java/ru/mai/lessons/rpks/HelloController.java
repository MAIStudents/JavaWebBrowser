package ru.mai.lessons.rpks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.*;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class HelloController {
    @FXML
    public Tab newTab;
    static private final Set<String> historyHide = new HashSet<>();
    @FXML
    private TabPane tabPane;
    @FXML
    private MenuItem history;
    @FXML
    private Menu favourites;
    @FXML
    private TextField addressLine;
    @FXML
    private WebView webView;
    @FXML
    private Button backButton;
    @FXML
    private Button nextButton;

    @FXML
    public void initialize() {
        webView.getEngine().load("https://www.google.com");

        webView.getEngine()
                .locationProperty()
                .addListener((observable, oldValue, newValue) -> changeAddress());
        webView.getEngine()
                .getHistory()
                .currentIndexProperty()
                .addListener((observable, oldValue, newValue) -> {
                    var history = webView.getEngine().getHistory();
                    nextButton.setDisable((int) newValue >= history.getEntries().size() - 1);
                    backButton.setDisable((int) newValue == 0);
                });

        if (Globals.favourites == null) {
            Globals.favourites = favourites;
        } else {
            favourites = Globals.favourites;
        }
    }

    @FXML
    protected void loadAddress() {
        String address = addressLine.getText();
        if (!address.startsWith("http://") && !address.startsWith("https://")) {
            address = "https://" + address;
        }
        webView.getEngine().load(address);
    }

    @FXML
    protected void backButtonClick() {
        webView.getEngine().getHistory().go(-1);
    }

    @FXML
    protected void nextButtonClick() {
        webView.getEngine().getHistory().go(1);
    }

    @FXML
    protected void reloadButtonClick() {
        webView.getEngine().reload();
    }

    @FXML
    protected void newTabClick() {
        try {
            Tab newTab = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("tab-template.fxml")));
            newTab.setClosable(true);
            tabPane.getTabs().add(tabPane.getTabs().size() - 1, newTab);
            tabPane.getSelectionModel().select(newTab);
        } catch (Exception ignored) {

        }
    }

    @FXML
    protected void changeAddress() {
        addressLine.setText(webView.getEngine().getLocation());
    }


    public static class HistoryObject {
        public String URL;
        public Date date;

        public HistoryObject(String URL, Date date) {
            this.date = date;
            this.URL = URL;
        }
    }

    @FXML
    protected void historyButtonClick() {
        try {
            final List<HistoryObject> toSortList = new ArrayList<>();
            tabPane.getTabs().forEach(x -> {
                var filtered = ((AnchorPane) x.getContent()).getChildren().filtered(k -> k instanceof WebView);
                if (filtered.isEmpty()) {
                    return;
                }
                WebView currentWebView = (WebView) filtered.getFirst();
                var currentHistory = currentWebView.getEngine().getHistory().getEntries();
                for (var element : currentHistory) {
                    try {
                        if (!historyHide.contains(new URI(element.getUrl()).getHost())) {
                            toSortList.add(new HistoryObject(element.getUrl(), element.getLastVisitedDate()));
                        }
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }

            });
            Gson obj = new GsonBuilder().setPrettyPrinting().create();
            toSortList.sort(Comparator.comparing(x -> x.date));
            try (FileWriter file = new FileWriter("History.json")) {
                obj.toJson(toSortList, file);
                file.flush();
            }
        } catch (Exception ignored) {

        }
    }

    @FXML
    protected void hideHistory() throws URISyntaxException {
        String url = new URI(webView.getEngine().getDocument().getDocumentURI()).getHost();
        historyHide.add(url);
    }

    @FXML
    protected void unHideHistory() throws URISyntaxException {
        String url = new URI(webView.getEngine().getDocument().getDocumentURI()).getHost();
        historyHide.remove(url);
    }

    @FXML
    protected void savePage() throws URISyntaxException, IOException {
        var currentWebView = getCurrentWebView();
        String html = currentWebView.getEngine().executeScript("document.documentElement.outerHTML").toString();
        String domainName = new URI(currentWebView.getEngine().getDocument().getDocumentURI()).getHost();
        Path tmp = Files.createTempDirectory("temp");
        Path htmlFile = tmp.resolve(domainName + ".html");
        Files.write(htmlFile, html.getBytes());

        try (ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(domainName + ".zip"))) {
            try (Stream<Path> pathStream = Files.walk(tmp)) {
                pathStream.forEach(path -> {
                    try {
                        if (Files.isRegularFile(path)) {
                            String toWrite = tmp.relativize(path).toString();
                            zip.putNextEntry(new ZipEntry(toWrite));
                            Files.copy(path, zip);
                            zip.closeEntry();
                        }
                    } catch (IOException ignored) {

                    }
                });
            }
        }
    }

    @FXML
    protected void openHtmlWindow() {
        Stage stage = new Stage();
        stage.setTitle("HTML редактор");
        TextArea htmlEditor = new TextArea();
        var currentWebView = getCurrentWebView();
        var tmpText = Jsoup.parse(currentWebView.getEngine().executeScript("document.documentElement.innerHTML").toString()).outerHtml();
        htmlEditor.setText(tmpText);
        htmlEditor.setPrefSize(600, 400);
        VBox editorLayout = getVBox(htmlEditor, currentWebView);

        Scene editorScene = new Scene(editorLayout, 600, 450);
        stage.setScene(editorScene);

        stage.show();
    }

    @FXML
    protected void openHelpWindow() {
        try {
            FXMLLoader infoWindow = new FXMLLoader(Objects.requireNonNull(getClass().getResource("help.fxml")));
            Stage stage = new Stage();
            Scene scene = new Scene(infoWindow.load());
            stage.setTitle("Справка");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    protected void makeFavourite() {
        var elem = favourites.getItems().filtered(x -> x.getUserData().equals(webView.getEngine().getLocation()));
        if (!elem.isEmpty()) {
            favourites.getItems().remove(elem.getFirst());
            return;
        }
        MenuItem fav = new MenuItem();
        fav.setText(webView.getEngine().getTitle());
        fav.setUserData(webView.getEngine().getLocation());
        fav.setOnAction((x) -> {
            var currentWebView = getCurrentWebView();
            currentWebView.getEngine().load(fav.getUserData().toString());
        });
        favourites.getItems().add(fav);
    }

    @FXML
    protected void disableHistory() {
        history.setDisable(!history.isDisable());
    }

    @NotNull
    private VBox getVBox(TextArea htmlEditor, WebView webView) {
        Button saveButton = new Button("Сохранить");
        saveButton.setOnAction(event -> {
            String htmlContent = Jsoup.parse(htmlEditor.getText()).outerHtml()
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\n", "\\n")
                    .replace("\r", "");
            webView.getEngine().executeScript("document.documentElement.innerHTML = '" + htmlContent + "';");
        });
        VBox editorLayout = new VBox(10, htmlEditor, saveButton);
        editorLayout.setStyle("-fx-padding: 10;");
        return editorLayout;
    }

    WebView getCurrentWebView() {
        return (WebView) ((AnchorPane) tabPane
                .getSelectionModel()
                .getSelectedItem()
                .getContent())
                .getChildren()
                .filtered(k -> k instanceof WebView)
                .getFirst();
    }
}