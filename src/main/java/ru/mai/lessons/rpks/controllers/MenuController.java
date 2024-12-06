package ru.mai.lessons.rpks.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.w3c.dom.Text;
import ru.mai.lessons.rpks.utils.History;

import java.io.File;
import java.io.IOException;

public final class MenuController {
  /**
   * Переменная, указывающая на глобальный приватный режим.
   */
  private boolean globalPrivateMode = false;

  /**
   * Включает или отключает глобальный режим приватности.
   * При активации этого режима история браузера не сохраняется.
   * Меняет стиль интерфейса в зависимости от состояния режима.
   *
   *  @param tabPane Панель сайта.
   */
  void toggleGlobalPrivateMode(final TabPane tabPane) {
    TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    if (pageTabController != null) {
      globalPrivateMode = !globalPrivateMode;
      HistoryController.setHistoryEnabled(!globalPrivateMode);
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
   *
   * @param tabPane Панель сайта.
   */
  void toggleSitePrivateMode(final TabPane tabPane) {
    TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    if (pageTabController != null) {
      String currentUrl = pageTabController.getWebEngine().getLocation();
      if (HistoryController.isSiteExcluded(currentUrl)) {
        pageTabController.getHistoryController().removeExcludedSite(currentUrl);
      } else {
        pageTabController.getHistoryController().addExcludedSite(currentUrl);
      }
    }
  }

  /**
   * Сохраняет историю посещенных сайтов в XML-файл в директории ресурсов.
   * Если возникла ошибка при создании каталога или записи файла, выводится сообщение об ошибке.
   *
   * @param tabPane Панель сайта.
   */
  void saveHistoryToXML(final TabPane tabPane) {
    TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    if (pageTabController != null) {
      try {
        File resourcesDir = new File("src/main/resources/xml");
        if (!resourcesDir.exists() && !resourcesDir.mkdirs()) {
          System.out.println("Failed to create resources directory");
          return;
        }

        File file = new File(resourcesDir, "/history.xml");
        pageTabController.getHistoryController().saveHistoryToXml(file);
      } catch (IOException e) {
        System.out.println("Failed to save history to XML in resources: " + e.getMessage());
      }
    }
  }

  /**
   * Открывает редактор HTML-кода для текущей страницы.
   *
   *  @param tabPane Панель сайта.
   */
  void editHtml(final TabPane tabPane) {
    TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();

    if (pageTabController != null) {
      pageTabController.getHtmlController().viewAndEditHtml();
    }
  }

  /**
   * Открывает окно для просмотра и редактирования истории посещенных страниц.
   * Если нет активной вкладки, выводится предупреждение.
   *
   * @param tabPane Панель вкладок, содержащая текущую активную вкладку.
   * @param tabBar Панель, содержащая вкладки, для использования в других функциях.
   */
  void showHistory(final TabPane tabPane, final HBox tabBar, final TextField urlField) {
    TabController pageTabController = getActiveTabController(tabPane);
    if (pageTabController == null) {
      return;
    }

    Stage historyStage = createHistoryStage();
    TableView<History.HistoryDto> tableView = createHistoryTableView(historyStage, tabPane, tabBar, pageTabController, urlField);

    VBox layout = new VBox(10);
    layout.setPadding(new Insets(10));
    layout.getChildren().add(tableView);

    Scene scene = new Scene(layout, 600, 400);
    historyStage.setScene(scene);

    historyStage.show();
  }

  /**
   * Получает контроллер активной вкладки.
   *
   * @param tabPane Панель вкладок, содержащая текущую активную вкладку.
   * @return Контроллер вкладки или null, если вкладка не активна.
   */
  private TabController getActiveTabController(TabPane tabPane) {
    Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
    if (selectedTab == null) {
      return null;
    }
    return (TabController) selectedTab.getUserData();
  }

  /**
   * Создает окно для отображения истории.
   *
   * @return Новый экземпляр Stage, настроенный для отображения как модальное окно.
   */
  private Stage createHistoryStage() {
    Stage historyStage = new Stage();
    historyStage.initModality(Modality.APPLICATION_MODAL);
    historyStage.setTitle("History");
    return historyStage;
  }

  /**
   * Создает таблицу для отображения истории посещений.
   *
   * @param historyStage Окно, в котором будет отображаться таблица.
   * @param tabPane Панель вкладок для передачи в колонки.
   * @param tabBar Панель вкладок для передачи в колонки.
   * @param pageTabController Контроллер активной вкладки, из которого берется история.
   * @return Созданная и настроенная таблица для отображения истории.
   */
  private TableView<History.HistoryDto> createHistoryTableView(final Stage historyStage,
                                                               final TabPane tabPane,
                                                               final HBox tabBar,
                                                               final TabController pageTabController,
                                                               final TextField urlField) {
    TableView<History.HistoryDto> tableView = new TableView<>();
    TableColumn<History.HistoryDto, String> urlColumn = getStringTableColumn(historyStage, tabPane, tabBar, urlField);
    TableColumn<History.HistoryDto, String> visitDateColumn = new TableColumn<>("Visited");
    visitDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTimestamp()));
    visitDateColumn.setPrefWidth(300);

    tableView.getColumns().addAll(urlColumn, visitDateColumn);

    tableView.getItems().addAll(pageTabController.getHistoryController()
        .getHistoryListGlobal().stream()
        .map(History.HistoryDto::new)
        .toList());

    return tableView;
  }

  /**
   * Создает и возвращает столбец таблицы для отображения URL в истории.
   * В столбце отображаются ссылки, которые можно кликнуть для перехода на соответствующие страницы.
   *
   * @param historyStage Окно, в котором будет отображаться таблица.
   * @param tabPane Панель вкладок.
   * @param tabBar Панель сайта.
   * @return Столбец с URL.
   */
  private static TableColumn<History.HistoryDto, String> getStringTableColumn(final Stage historyStage,
                                                                              final TabPane tabPane,
                                                                              final HBox tabBar,
                                                                              final TextField urlField) {
    TableColumn<History.HistoryDto, String> urlColumn = new TableColumn<>("URL");
    urlColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUrl()));
    urlColumn.setCellFactory(col -> createHyperlinkCell(historyStage, tabPane, tabBar, urlField));
    urlColumn.setPrefWidth(300);
    return urlColumn;
  }

  /**
   * Создает и возвращает ячейку таблицы, которая отображает гиперссылку.
   * При клике на ссылку происходит переход на соответствующую страницу.
   *
   * @param historyStage Окно, в котором отображается таблица.
   * @param tabPane Панель вкладок.
   * @param tabBar Панель сайта.
   * @return Ячейка таблицы с гиперссылкой.
   */
  private static TableCell<History.HistoryDto, String> createHyperlinkCell(final Stage historyStage,
                                                                           final TabPane tabPane,
                                                                           final HBox tabBar,
                                                                           final TextField urlField) {
    return new TableCell<>() {
      @Override
      protected void updateItem(String url, boolean empty) {
        super.updateItem(url, empty);
        if (empty || url == null) {
          setText(null);
          setGraphic(null);
        } else {
          setGraphic(createHyperlink(url, historyStage, tabPane, tabBar, urlField));
        }
      }
    };
  }

  /**
   * Создает гиперссылку для отображения в ячейке таблицы.
   * При клике на ссылку происходит загрузка страницы в активной вкладке.
   *
   * @param url URL, который будет отображен как гиперссылка.
   * @param historyStage Окно, в котором отображается таблица.
   * @param tabPane Панель вкладок.
   * @param tabBar Панель сайта.
   * @return Гиперссылка для отображения в ячейке.
   */
  private static Hyperlink createHyperlink(String url,
                                           final Stage historyStage,
                                           final TabPane tabPane,
                                           final HBox tabBar,
                                           final TextField urlField) {
    Hyperlink link = new Hyperlink(url);
    link.setOnAction(_ -> handleLinkClick(url, historyStage, tabPane, tabBar, urlField));
    return link;
  }

  /**
   * Обрабатывает клик по гиперссылке, загружая соответствующую страницу в активной вкладке.
   *
   * @param url URL страницы для загрузки.
   * @param historyStage Окно, в котором отображается таблица.
   * @param tabPane Панель вкладок.
   * @param tabBar Панель сайта.
   */
  private static void handleLinkClick(final String url,
                                      final Stage historyStage,
                                      final TabPane tabPane,
                                      final HBox tabBar,
                                      final TextField urlField) {
    TabController pageTabController = (TabController) tabPane.getSelectionModel().getSelectedItem().getUserData();
    BrowserController.loadPage(url, pageTabController, tabPane, tabBar, urlField);
    urlField.setText(url);
    historyStage.close();
  }
}
