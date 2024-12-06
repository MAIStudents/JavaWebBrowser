package ru.mai.lessons.rpks.controllers;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebEngine;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.mai.lessons.rpks.utils.History;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Контроллер для браузера, управляющий вкладками, загрузкой страниц,
 * историей посещенных сайтов, избранным и приватными режимами.
 */
public final class BrowserController implements Initializable {

  /**
   * Логгер для вывода информации о действиях в приложении.
   */
  private static final Logger LOG = Logger.getLogger(BrowserController.class);

  /**
   * Базовые настройки кнопки вкладки.
   */
  private static final String TAB_COMMON_SETTINGS =
      "-fx-pref-width: 175; "
          + "-fx-pref-height: 39; "
          + "-fx-padding: 0 0 0 5; "
          + "-fx-alignment: center_left; "
          + "-fx-spacing: 40;";

  /**
   * Настройки активной кнопки вкладки.
   */
  private static final String TAB_ACTIVE_SETTINGS =
      "-fx-pref-width: 175; "
          + "-fx-pref-height: 39; "
          + "-fx-background-color: #ddd; "
          + "-fx-padding: 0 0 0 5; "
          + "-fx-alignment: center_left; "
          + "-fx-spacing: 40;";

  /**
   * Переменная, указывающая на глобальный приватный режим.
   */
  private boolean globalPrivateMode = false;

  @FXML
  public HBox tabBar;

  @FXML
  private Button closeAppButton;

  @FXML
  private TabPane tabPane;

  @FXML
  private TextField urlField;

  @FXML
  private Button reloadButton;

  @FXML
  private Button addButton;

  @FXML
  public Button favoriteButton;

  /**
   * Множество избранных URL-адресов.
   */
  private final Set<String> favorites = new LinkedHashSet<>();

  /**
   * Инициализация контроллера, настройка действий и обработчиков событий.
   *
   * @param url            URL, переданный в FXML.
   * @param resourceBundle Ресурсный файл для локализации.
   */
  @Override
  public void initialize(final URL url,
                         final ResourceBundle resourceBundle) {
    addNewTab();

    reloadButton.setOnAction(_ -> reloadPage());
    addButton.setOnAction(_ -> addNewTab());
    favoriteButton.setOnAction(_ -> addToFavorites());
    closeAppButton.setOnAction(_ -> closeApp());

    Platform.runLater(() -> {
      Stage mainStage = (Stage) tabPane.getScene().getWindow();
      mainStage.setOnCloseRequest(_ -> closeApp());
    });

    tabPane.getTabs().addListener((ListChangeListener.Change<? extends Tab> change) -> {
      if (tabPane.getTabs().isEmpty()) {
        closeApp();
      }

      while (change.next()) {
        if (change.wasRemoved()) {
          int removedIndex = change.getFrom();
          LOG.info("Tab closed at index: {}", removedIndex);

          for (int i = removedIndex; i < tabBar.getChildren().size() - 1; i++) {
            HBox box = (HBox) tabBar.getChildren().get(i);
            int curIdx = (int)box.getUserData() - 1;
            box.setUserData(curIdx);

            Button btn = (Button) box.getChildren().get(2);
            btn.setUserData(curIdx);
          }
        }
      }
    });

    tabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldTab, newTab) -> {
      if (newTab != null) {
        TabController pageTabController = (TabController) newTab.getUserData();
        if (pageTabController != null) {
          String currentUrl = pageTabController.getWebEngine().getLocation();
          LOG.info("Switched to tab with URL: {}", currentUrl);
        }
      }
    });
  }

  /**
   * Загружает страницу, введенную в текстовом поле.
   */
  @FXML
  private void loadPageFromTextBar() {
    String input = urlField.getText().trim();
    LOG.debug("{}", input);
    if (input.isEmpty()) {
      LOG.warn("URL field is empty");
      return;
    }

    urlField.setText(formatUrl(urlField.getText()));

    Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
    if (selectedTab != null) {
      TabController currentTab = (TabController) selectedTab.getUserData();
      loadPage(input, currentTab);
    } else {
      LOG.warn("No selected tab to load the page");
    }
  }

  /**
   * Загружает страницу по URL в указанную вкладку.
   *
   * @param url               URL страницы.
   * @param pageTabController Контроллер вкладки.
   */
  private void loadPage(final String url,
                        final TabController pageTabController) {
    if (url == null || url.isEmpty()) {
      LOG.warn("URL is empty or null");
      return;
    }

    if (pageTabController == null) {
      LOG.warn("Error with current tab");
      return;
    }

    String formattedUrl = formatUrl(url);
    LOG.info("Formatted URL: {}", formattedUrl);

    pageTabController.getWebEngine().getLoadWorker().exceptionProperty().removeListener(pageTabController.getExceptionListener());
    ChangeListener<Throwable> exceptionListener = (observableValue, oldException, newException) -> {
      if (newException != null) {
        LOG.error("Error loading URL: {}", formattedUrl, newException);

        String yandexSearchUrl = "https://ya.ru/text?q=" + url;
        LOG.info("Redirecting to Yandex Search: {}", yandexSearchUrl);
        pageTabController.getWebEngine().load(yandexSearchUrl);
      }
    };

    pageTabController.setExceptionListener(exceptionListener);
    pageTabController.getWebEngine().getLoadWorker().exceptionProperty().addListener(exceptionListener);
    pageTabController.getWebEngine().getLoadWorker().stateProperty().removeListener(pageTabController.getStateListener());

    int curTabIdx = tabPane.getSelectionModel().getSelectedIndex();
    
    ChangeListener<Worker.State> stateListener = (observable, oldState, newState) -> {
      if (newState == Worker.State.SUCCEEDED) {
        String currentUrl = pageTabController.getWebEngine().getLocation();
        if (isValidUrl(currentUrl) && !Objects.equals(pageTabController.getHistoryController().getCurrent(), currentUrl)) {
          pageTabController.getHistoryController().addEntry(currentUrl);
          LOG.info("Successfully loaded URL: {}", currentUrl);

          String pageTitle = pageTabController.getWebEngine().getTitle();

          if (pageTitle != null) {
            pageTitle = pageTitle.length() > 10
                ? pageTitle.substring(0, 10) + "..."
                : pageTitle;
            pageTabController.getTab().setText(pageTitle);

            HBox curBox = (HBox) tabBar.getChildren().get(curTabIdx);
            Text curBoxText = (Text) curBox.getChildren().getFirst();
            curBoxText.setText(pageTitle);
          } else {
            pageTabController.getTab().setText("Яндекс");
          }


        } else {
          LOG.warn("Invalid URL, not adding to history: {}", currentUrl);
        }
      }
    };

    pageTabController.setStateListener(stateListener);
    pageTabController.getWebEngine().getLoadWorker().stateProperty().addListener(stateListener);

    LOG.info("Loading URL: {}", formattedUrl);
    pageTabController.getWebEngine().load(formattedUrl);
  }

  /**
   * Проверяет, является ли URL валидным.
   *
   * @param url URL для проверки.
   * @return true, если URL валиден, иначе false.
   */
  private boolean isValidUrl(final String url) {
    try {
      new java.net.URI(url);
      return true;
    } catch (URISyntaxException e) {
      return false;
    }
  }

  /**
   * Форматирует URL, добавляя "https://" в начале, если это необходимо.
   *
   * @param url URL для форматирования.
   * @return отформатированный URL.
   */
  private String formatUrl(final String url) {
    return url.startsWith("http://") || url.startsWith("https://") ? url : "https://" + url;
  }

  /**
   * Перезагружает страницу в текущей вкладке.
   */
  private void reloadPage() {
    Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
    if (selectedTab != null) {
      TabController currentTab = (TabController) selectedTab.getUserData();
      if (currentTab != null) {
        currentTab.getWebEngine().reload();
      }
    }
  }

  /**
   * Добавляет новую вкладку с начальной страницей.
   */
  private void addNewTab() {

    if (tabPane.getTabs().size() == 5) {
      addButton.setVisible(false);
      addButton.setDisable(true);
    }

    TabController newTabController = new TabController("https://ya.ru");
    newTabController.getHistoryController().addEntry("https://ya.ru");

    WebEngine webEngine = newTabController.getWebEngine();
    webEngine.setJavaScriptEnabled(true);

    webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
      if (newState == Worker.State.SUCCEEDED) {
        String pageTitle = webEngine.getTitle();

        Tab newTab = newTabController.getTab();

        if (pageTitle != null) {
          pageTitle = pageTitle.length() > 10
              ? pageTitle.substring(0, 10) + "..."
              : pageTitle;
          newTab.setText(pageTitle);
        } else {
          newTab.setText("Яндекс");
        }
      }
    });

    Tab newTab = newTabController.getTab();

    if (newTab == null) {
      LOG.info("Unsuccessful tab creation");
      return;
    }

    newTab.setStyle("-fx-background-color: none; -fx-opacity: 0;");
    tabPane.getTabs().add(newTab);
    tabPane.getSelectionModel().select(newTab);
    LOG.info("Add new tab");

    HBox newHBoxTab = new HBox();
    newHBoxTab.setUserData(tabPane.getTabs().size() - 1);
    newHBoxTab.setStyle(TAB_COMMON_SETTINGS);
    newHBoxTab.setOnMouseClicked(_ -> {
      changeActiveTab(newHBoxTab);
      tabPane.getSelectionModel().select((int) newHBoxTab.getUserData());
      TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
      urlField.setText(pageTabController.getWebEngine().getLocation());
    });

    Text text = new Text("Яндекс");
    text.setStyle("-fx-font-size: 16;");
    newHBoxTab.getChildren().add(text);

    HBox box = new HBox();
    HBox.setHgrow(box, Priority.ALWAYS);
    newHBoxTab.getChildren().add(box);

    Button tabCloseButton = getTubButton();
    newHBoxTab.getChildren().add(tabCloseButton);

    tabBar.getChildren().add(tabBar.getChildren().size() - 1, newHBoxTab);

    changeActiveTab(newHBoxTab);
  }

  /**
   * Кнопка закрытия вкладки.
   */
  private Button getTubButton() {
    Button tabCloseButton = new Button();
    tabCloseButton.setUserData(tabPane.getTabs().size() - 1);
    tabCloseButton.setStyle("-fx-font-size: 18; -fx-background-color: none; -fx-min-width: 39; -fx-pref-width: 39; -fx-cursor: hand;");
    tabCloseButton.setOnAction(_ -> {
      tabBar.getChildren().remove((int) tabCloseButton.getUserData());
      tabPane.getTabs().remove((int) tabCloseButton.getUserData());

      if (tabPane.getSelectionModel().getSelectedIndex() == tabBar.getChildren().size() - 2) {
        changeActiveTab((HBox) tabBar.getChildren().get(tabBar.getChildren().size() - 2));
      }
      addButton.setVisible(true);
      addButton.setDisable(false);
    });
    tabCloseButton.setText("×");
    return tabCloseButton;
  }

  /**
   * Меняет стили вкладок. Активной меняется цвет заднего фона.
   *
   * @param newActiveTab новая активная вкладка
   */
  private void changeActiveTab(HBox newActiveTab) {
    for (Node child : tabBar.getChildren()) {
      if (child != tabBar.getChildren().getLast()) {
        child.setStyle(TAB_COMMON_SETTINGS);
      }
    }

    newActiveTab.setStyle(TAB_ACTIVE_SETTINGS);
  }

  /**
   * Добавляет текущий URL в избранное.
   */
  private void addToFavorites() {
    TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    if (pageTabController != null) {
      String currentURL = pageTabController.getWebEngine().getLocation();
      if (favorites.add(currentURL)) {
        LOG.info("Added to favorites url: {}", currentURL);
      } else {
        LOG.info("This url is already in favorites {}", currentURL);
      }
    } else {
      LOG.warn("No active tab to add to favorites");
    }
  }

  /**
   * Обрабатывает выбор фаворитов для отображения в новом окне.
   * Если список фаворитов пуст, выводится предупреждение.
   */
  @FXML
  private void selectFavorites() {
    if (favorites.isEmpty()) {
      LOG.warn("No favorites to show");
      return;
    }

    Stage favoriteStage = new Stage();
    favoriteStage.initModality(Modality.APPLICATION_MODAL);
    VBox favoriteList = new VBox(10);
    favoriteList.setPadding(new Insets(10));
    favoriteList.setAlignment(Pos.BOTTOM_LEFT);

    for (String favorite : favorites) {
      Button favoriteUrlButton = new Button(favorite);
      TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
      pageTabController.getWebEngine().setJavaScriptEnabled(true);
      favoriteUrlButton.setOnAction(_ -> {
        loadPage(favorite, pageTabController);
        favoriteStage.close();
      });
      favoriteList.getChildren().add(favoriteUrlButton);
    }

    ScrollPane scrollPane = new ScrollPane(favoriteList);
    Scene scene = new Scene(scrollPane, 200, 300);
    favoriteStage.setScene(scene);
    favoriteStage.setTitle("Favorites");

    Stage primaryStage = (Stage) tabPane.getScene().getWindow();
    favoriteStage.setX(primaryStage.getX() + primaryStage.getWidth() - scene.getWidth());
    favoriteStage.setY(primaryStage.getY() + tabPane.getHeight() / 2);

    favoriteStage.show();
  }

  /**
   * Сохраняет текущую веб-страницу в формате ZIP.
   * Содержимое страницы извлекается через JavaScript, затем сохраняется в архив.
   */
  @FXML
  private void saveToZip() {
    Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
    if (currentTab != null) {
      TabController pageTabController = (TabController) currentTab.getUserData();
      if (pageTabController != null) {
        String pageContent = (String) pageTabController.getWebEngine().executeScript("document.documentElement.outerHTML");
        savePageToZip(pageContent);
      }
    }
  }

  /**
   * Сохраняет контент страницы в формате ZIP.
   * Если контент страницы пуст или произошла ошибка, выводится сообщение об ошибке.
   *
   * @param pageContent содержимое страницы, которое будет сохранено в архив.
   */
  private void savePageToZip(final String pageContent) {
    if (pageContent == null || pageContent.isEmpty()) {
      LOG.error("Error with page content");
      return;
    }

    String downloadsDir = getDownloadsDirectory();
    if (downloadsDir == null) {
      LOG.error("Cannot determine Downloads directory");
      return;
    }

    TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    String fileName = "default_page.html";
    if (pageTabController != null) {
      String url = pageTabController.getWebEngine().getLocation();
      if (url != null && !url.isEmpty()) {
        fileName = url.replaceAll("[^a-zA-Z0-9.-]", "_") + ".html";
      }
    }

    Path zipFilePath = Paths.get(downloadsDir, "saved_page.zip");
    try (FileOutputStream fileOutputStream = new FileOutputStream(zipFilePath.toFile());
         ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream)) {

      ZipEntry zipEntry = new ZipEntry(fileName);
      zipOutputStream.putNextEntry(zipEntry);

      byte[] bytes = pageContent.getBytes(StandardCharsets.UTF_8);
      zipOutputStream.write(bytes, 0, bytes.length);
      zipOutputStream.closeEntry();

      LOG.info("Page saved to ZIP: {}", zipFilePath);
    } catch (IOException e) {
      LOG.error("Error while saving to ZIP: ", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Получает путь к директории загрузок в зависимости от операционной системы.
   * Для Windows используется путь "\\Downloads", для macOS и Linux — "/Downloads".
   *
   * @return путь к директории загрузок или null, если не удалось определить.
   */
  private String getDownloadsDirectory() {
    String os = System.getProperty("os.name").toLowerCase();
    String userHome = System.getProperty("user.home");

    if (os.contains("win")) {
      return userHome + "\\Downloads";
    } else if (os.contains("mac") || os.contains("nix") || os.contains("nux")) {
      return userHome + "/Downloads";
    } else {
      return null;
    }
  }

  /**
   * Переходит на предыдущую страницу в истории.
   * Если предыдущий URL существует, то осуществляется переход.
   * Если истории нет, выводится предупреждение.
   */
  @FXML
  public void goBack() {
    TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    String previousUrl = pageTabController.getHistoryController().goBack();
    if (previousUrl != null) {
      LOG.info("Go to the previous URL {}", previousUrl);
      pageTabController.getWebEngine().load(previousUrl);
    } else {
      LOG.warn("Doesn't have previous URL");
    }
  }

  /**
   * Переходит на следующую страницу в истории.
   * Если следующий URL существует, то осуществляется переход.
   * Если истории нет, выводится предупреждение.
   */
  @FXML
  public void goForward() {
    TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    String nextUrl = pageTabController.getHistoryController().goForward();
    if (nextUrl != null) {
      LOG.info("Go to the next URL {}", nextUrl);
      pageTabController.getWebEngine().load(nextUrl);
    } else {
      LOG.warn("Doesn't have next URL");
    }
  }

  /**
   * Включает или отключает глобальный режим приватности.
   * При активации этого режима история браузера не сохраняется.
   * Меняет стиль интерфейса в зависимости от состояния режима.
   */
  @FXML
  private void toggleGlobalPrivateMode() {
    TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    if (pageTabController != null) {
      globalPrivateMode = !globalPrivateMode;
      HistoryController.setHistoryEnabled(!globalPrivateMode);
      LOG.info("Global Private Mode: {}", globalPrivateMode ? "Enabled" : "Disabled");
      Scene mainScene = tabPane.getScene();
      if (globalPrivateMode) {
        mainScene.getRoot().setStyle("-fx-background-color: #2F4444; -fx-opacity: 1.0;");
      } else {
        mainScene.getRoot().setStyle("");
      }
    }
  }

  /**
   * Включает или отключает приватный режим для текущего сайта.
   * Если сайт добавлен в список исключенных, он удаляется из этого списка и наоборот.
   */
  @FXML
  private void toggleSitePrivateMode() {
    TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    if (pageTabController != null) {
      String currentUrl = pageTabController.getWebEngine().getLocation();
      if (HistoryController.isSiteExcluded(currentUrl)) {
        pageTabController.getHistoryController().removeExcludedSite(currentUrl);
        LOG.info("Site removed from private mode: {}", currentUrl);
      } else {
        pageTabController.getHistoryController().addExcludedSite(currentUrl);
        LOG.info("Site added to private mode: {}", currentUrl);
      }
    }
  }

  /**
   * Сохраняет историю посещенных сайтов в XML-файл в директории ресурсов.
   * Если возникла ошибка при создании каталога или записи файла, выводится сообщение об ошибке.
   */
  @FXML
  private void saveHistoryToResourcesXML() {
    TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    if (pageTabController != null) {
      try {
        File resourcesDir = new File("src/main/resources/xml");
        if (!resourcesDir.exists() && !resourcesDir.mkdirs()) {
          LOG.error("Failed to create resources directory");
          return;
        }

        File file = new File(resourcesDir, "xml/history.xml");
        pageTabController.getHistoryController().saveHistoryToXml(file);
        LOG.info("History saved to XML in resources: {}", file.getAbsolutePath());
      } catch (IOException e) {
        LOG.error("Failed to save history to XML in resources", e);
      }
    }
  }

  /**
   * Открывает редактор HTML-кода для текущей страницы.
   */
  @FXML
  private void viewAndEditHtml() {
    TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();

    if (pageTabController != null) {
      pageTabController.getHtmlController().viewAndEditHtml();
    }
  }

  /**
   * Открывает окно для просмотра и редактирования истории посещенных страниц.
   * Если нет активной вкладки, выводится предупреждение.
   */
  @FXML
  private void showHistoryViewer() {
    TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    if (pageTabController == null) {
      LOG.warn("No active tab to show history");
      return;
    }

    Stage historyStage = new Stage();
    historyStage.initModality(Modality.APPLICATION_MODAL);
    historyStage.setTitle("History");

    TableView<History.HistoryDto> tableView = new TableView<>();

    TableColumn<History.HistoryDto, String> urlColumn = getStringTableColumn(historyStage);

    TableColumn<History.HistoryDto, String> visitDateColumn = new TableColumn<>("Visited");
    visitDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTimestamp()));
    visitDateColumn.setPrefWidth(300);

    tableView.getColumns().addAll(urlColumn, visitDateColumn);

    tableView.getItems().addAll(pageTabController.getHistoryController()
        .getHistoryListGlobal().stream()
        .map(History.HistoryDto::new)
        .toList());

    VBox layout = new VBox(10);
    layout.setPadding(new Insets(10));
    layout.getChildren().add(tableView);

    Scene scene = new Scene(layout, 600, 400);
    historyStage.setScene(scene);

    historyStage.show();
  }

  /**
   * Создает и возвращает столбец таблицы для отображения URL в истории.
   * В столбце отображаются ссылки, которые можно кликнуть для перехода на соответствующие страницы.
   *
   * @param historyStage окно, в котором будет отображаться таблица.
   * @return столбец с URL.
   */
  private TableColumn<History.HistoryDto, String> getStringTableColumn(final Stage historyStage) {
    TableColumn<History.HistoryDto, String> urlColumn = new TableColumn<>("URL");
    urlColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUrl()));
    urlColumn.setCellFactory(col -> new TableCell<>() {
      @Override
      protected void updateItem(String url, boolean empty) {
        super.updateItem(url, empty);
        if (empty || url == null) {
          setText(null);
          setGraphic(null);
        } else {
          Hyperlink link = new Hyperlink(url);
          link.setOnAction(_ -> {
            TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
            loadPage(url, pageTabController);
            historyStage.close();
          });
          setGraphic(link);
        }
      }
    });
    urlColumn.setPrefWidth(300);
    return urlColumn;
  }

  /**
   * Закрывает приложение.
   */
  private void closeApp() {
    LOG.info("Closing application");
    Platform.exit();
  }

}