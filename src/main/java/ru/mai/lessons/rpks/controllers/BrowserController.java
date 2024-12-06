package ru.mai.lessons.rpks.controllers;

import javafx.application.Platform;
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
import javafx.scene.web.WebEngine;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ru.mai.lessons.rpks.controllers.MenuController;

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


  @FXML
  public HBox tabBar;

  @FXML
  private Button closeAppButton;

  @FXML
  public TabPane tabPane;

  @FXML
  private TextField urlField;

  @FXML
  private Button reloadButton;

  @FXML
  private Button addButton;

  @FXML
  public Button favoriteButton;

  @FXML
  private void toggleGlobalPrivateMode() {
    menuController.toggleGlobalPrivateMode(tabPane);
  }

  @FXML
  private void toggleSitePrivateMode() {
    menuController.toggleSitePrivateMode(tabPane);
  }

  @FXML
  private void saveHistoryToXML() {
    menuController.saveHistoryToXML(tabPane);
  }

  @FXML
  private void editHtml() {
    menuController.editHtml(tabPane);
  }

  @FXML
  private void showHistory() {
    menuController.showHistory(tabPane, tabBar, urlField);
  }

  /**
   * Множество избранных URL-адресов.
   */
  private final Set<String> favorites = new LinkedHashSet<>();

  private final MenuController menuController = new MenuController();

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
          System.out.println("Tab closed at index: " + removedIndex);

          for (int i = removedIndex; i < tabBar.getChildren().size() - 1; i++) {
            HBox box = (HBox) tabBar.getChildren().get(i);
            int curIdx = (int) box.getUserData() - 1;
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
          System.out.println("Switched to tab with URL: " + currentUrl);
          urlField.setText(currentUrl);
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
    if (input.isEmpty()) {
      return;
    }

    urlField.setText(formatUrl(urlField.getText()));

    Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
    if (selectedTab != null) {
      TabController currentTab = (TabController) selectedTab.getUserData();
      loadPage(input, currentTab, tabPane, tabBar, urlField);
      updateUrlFieldForSelectedTab();
    }
  }

  /**
   * Загружает страницу по URL в указанную вкладку.
   *
   * @param url               URL страницы.
   * @param pageTabController Контроллер вкладки.
   */
  static void loadPage(final String url,
                       final TabController pageTabController,
                       final TabPane tabPane,
                       final HBox tabBar,
                       final TextField urlField) {
    if (url == null || url.isEmpty() || pageTabController == null) {
      return;
    }

    String formattedUrl = formatUrl(url);

    pageTabController.getWebEngine().getLoadWorker().exceptionProperty().removeListener(pageTabController.getExceptionListener());
    ChangeListener<Throwable> exceptionListener = (observableValue, oldException, newException) -> {
      if (newException != null) {
        String yandexSearchUrl = "https://ya.ru/text?q=" + url;
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
        }
      }
    };

    pageTabController.setStateListener(stateListener);
    pageTabController.getWebEngine().getLoadWorker().stateProperty().addListener(stateListener);

    pageTabController.getWebEngine().load(formattedUrl);
  }

  /**
   * Проверяет, является ли URL валидным.
   *
   * @param url URL для проверки.
   * @return true, если URL валиден, иначе false.
   */
  private static boolean isValidUrl(final String url) {
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
  private static String formatUrl(final String url) {
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
    if (tabPane != null && tabPane.getTabs().size() == 5) {
      addButton.setVisible(false);
      addButton.setDisable(true);
    }

    TabController newTabController = createTabController("https://ya.ru");

    WebEngine webEngine = newTabController.getWebEngine();
    webEngine.setJavaScriptEnabled(true);

    addPageTitleListener(webEngine, newTabController);

    Tab newTab = newTabController.getTab();

    if (newTab != null) {
      configureTab(newTab);
      addTabToPane(newTab);
      addTabToTabBar(newTabController, newTab);
    }
  }

  /**
   * Создает новый экземпляр контроллера вкладки с заданным URL.
   *
   * @param url URL, который будет использован для создания новой вкладки.
   * @return Новый экземпляр TabController.
   */
  private TabController createTabController(String url) {
    TabController newTabController = new TabController(url);
    newTabController.getHistoryController().addEntry(url);
    return newTabController;
  }

  /**
   * Добавляет слушателя изменения состояния загрузки страницы для веб-движка.
   * При успешной загрузке страницы обновляет заголовок вкладки.
   *
   * @param webEngine        Экземпляр WebEngine, связанный с вкладкой.
   * @param newTabController Контроллер вкладки, для которой добавляется слушатель.
   */
  private void addPageTitleListener(WebEngine webEngine, TabController newTabController) {
    webEngine.getLoadWorker().stateProperty().addListener((observable, oldState, newState) -> {
      if (newState == Worker.State.SUCCEEDED) {
        updateTabTitle(webEngine, newTabController);
      }
    });
  }

  /**
   * Обновляет заголовок вкладки в зависимости от заголовка страницы.
   * Если заголовок слишком длинный, он будет обрезан и добавлены многоточия.
   *
   * @param webEngine        Экземпляр WebEngine для получения заголовка страницы.
   * @param newTabController Контроллер вкладки, для которой обновляется заголовок.
   */
  private void updateTabTitle(WebEngine webEngine, TabController newTabController) {
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

  /**
   * Конфигурирует вкладку, устанавливая её стиль.
   *
   * @param newTab Вкладка, которую необходимо настроить.
   */
  private void configureTab(Tab newTab) {
    newTab.setStyle("-fx-background-color: none; -fx-opacity: 0;");
  }

  /**
   * Добавляет вкладку в панель вкладок и устанавливает её активной.
   *
   * @param newTab Вкладка, которую необходимо добавить в панель вкладок.
   */
  private void addTabToPane(Tab newTab) {
    if (tabPane != null) {
      tabPane.getTabs().add(newTab);
      tabPane.getSelectionModel().select(newTab);
    }
  }

  /**
   * Добавляет элемент вкладки (включая текст, кнопку закрытия и другие элементы) в панель вкладок.
   *
   * @param newTabController Контроллер вкладки.
   * @param newTab           Вкладка, которую необходимо добавить в панель вкладок.
   */
  private void addTabToTabBar(TabController newTabController, Tab newTab) {
    HBox newHBoxTab = createTabBarEntry(newTabController, newTab);
    tabBar.getChildren().add(tabBar.getChildren().size() - 1, newHBoxTab);
    changeActiveTab(newHBoxTab);
  }

  /**
   * Создает элемент панели вкладок с кнопкой закрытия и текстом вкладки.
   *
   * @param newTabController Контроллер вкладки.
   * @param newTab           Вкладка, для которой создается элемент панели.
   * @return Созданный элемент панели вкладок.
   */
  private HBox createTabBarEntry(TabController newTabController, Tab newTab) {
    HBox newHBoxTab = new HBox();
    newHBoxTab.setUserData(tabPane.getTabs().size() - 1);
    newHBoxTab.setStyle(TAB_COMMON_SETTINGS);
    newHBoxTab.setOnMouseClicked(_ -> {
      changeActiveTab(newHBoxTab);
      tabPane.getSelectionModel().select((int) newHBoxTab.getUserData());
      updateUrlFieldForSelectedTab();
    });

    Text text = createTabText();
    newHBoxTab.getChildren().add(text);

    HBox box = new HBox();
    HBox.setHgrow(box, Priority.ALWAYS);
    newHBoxTab.getChildren().add(box);

    Button tabCloseButton = getTubButton();
    newHBoxTab.getChildren().add(tabCloseButton);

    return newHBoxTab;
  }

  /**
   * Создает текстовый элемент для вкладки.
   *
   * @return Созданный текстовый элемент.
   */
  private Text createTabText() {
    Text text = new Text("Яндекс");
    text.setStyle("-fx-font-size: 16;");
    return text;
  }

  /**
   * Обновляет поле URL в интерфейсе, устанавливая его значение равным текущему URL выбранной вкладки.
   */
  private void updateUrlFieldForSelectedTab() {
    TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    urlField.setText(pageTabController.getWebEngine().getLocation());
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

      if (tabPane.getSelectionModel().getSelectedIndex() == tabBar.getChildren().size() - 2
          && !tabPane.getTabs().isEmpty()) {
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
        System.out.println("Added to favorites url: " + currentURL);
      } else {
        System.out.println("This url is already in favorites: " + currentURL);
      }
    } else {
      System.out.println("No active tab to add to favorites");
    }
  }

  /**
   * Обрабатывает выбор фаворитов для отображения в новом окне.
   * Если список фаворитов пуст, выводится предупреждение.
   */
  @FXML
  private void selectFavorites() {
    if (favorites.isEmpty()) {
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
        loadPage(favorite, pageTabController, tabPane, tabBar, urlField);
        updateUrlFieldForSelectedTab();
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
      return;
    }

    String downloadsDir = getDownloadsDirectory();
    if (downloadsDir == null) {
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

    } catch (IOException e) {
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
      pageTabController.getWebEngine().load(previousUrl);
      urlField.setText(previousUrl);
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
      pageTabController.getWebEngine().load(nextUrl);
      urlField.setText(nextUrl);
    }
  }

  /**
   * Закрывает приложение.
   */
  private void closeApp() {
    Platform.exit();
  }

}