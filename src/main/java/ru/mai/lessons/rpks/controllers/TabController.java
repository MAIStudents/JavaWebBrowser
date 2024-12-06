package ru.mai.lessons.rpks.controllers;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.scene.control.Tab;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

/**
 * Контроллер вкладки, управляющий загрузкой и отображением веб-страницы
 * в компоненте WebView. Он взаимодействует с контроллерами истории и HTML.
 */
public final class TabController {

  /** Контроллер истории для управления историей посещений вкладки. */
  private final HistoryController historyController;

  /** Контроллер для работы с HTML-контентом на вкладке. */
  private final HTMLController htmlController;

  /** Вкладка, представляющая веб-страницу. */
  private final Tab tab;

  /** Компонент WebView, который отображает веб-страницу. */
  private final WebView webView;

  /** Движок WebEngine для загрузки и взаимодействия с веб-страницей. */
  private final WebEngine webEngine;

  /** Листенер для обработки ошибок при работе с WebEngine. */
  private ChangeListener<Throwable> listener;

  /** Листенер для отслеживания изменения состояния работы WebEngine. */
  private ChangeListener<Worker.State> stateListener;

  /**
   * Конструктор контроллера вкладки, инициализирует компоненты вкладки
   * и загружает указанную веб-страницу.
   *
   * @param url URL веб-страницы, которая будет загружена в WebView
   */
  public TabController(final String url) {
    this.historyController = new HistoryController();
    this.webView = new WebView();
    this.webEngine = webView.getEngine();
    this.webEngine.load(url);
    this.htmlController = new HTMLController(this.webEngine);
    this.listener = (observableValue, throwable, t1) -> {};
    this.stateListener = (observableValue, throwable, t1) -> {};

    VBox.setVgrow(webView, Priority.ALWAYS);
    VBox vBox = new VBox(webView);

    this.tab = new Tab("New Tab", vBox);
    this.tab.setUserData(this);
  }

  /**
   * Получает контроллер для работы с HTML-контентом на вкладке.
   *
   * @return контроллер HTML
   */
  public HTMLController getHtmlController() {
    return htmlController;
  }

  /**
   * Получает контроллер для управления историей посещений вкладки.
   *
   * @return контроллер истории
   */
  public HistoryController getHistoryController() {
    return historyController;
  }

  /**
   * Получает вкладку, которая представляет текущую веб-страницу.
   *
   * @return вкладка с веб-страницей
   */
  public Tab getTab() {
    return tab;
  }

  /**
   * Получает WebEngine, который используется для загрузки и отображения веб-страницы.
   *
   * @return движок WebEngine
   */
  public WebEngine getWebEngine() {
    return webEngine;
  }

  /**
   * Получает WebView, который используется для отображения веб-страницы.
   *
   * @return компонент WebView
   */
  public WebView getWebView() {
    return webView;
  }

  /**
   * Получает слушатель ошибок, возникающих в процессе работы с WebEngine.
   *
   * @return слушатель ошибок
   */
  public ChangeListener<Throwable> getExceptionListener() {
    return listener;
  }

  /**
   * Устанавливает слушатель для обработки ошибок, возникающих в процессе работы с WebEngine.
   *
   * @param exceptionListener слушатель для ошибок
   */
  public void setExceptionListener(ChangeListener<Throwable> exceptionListener) {
    this.listener = exceptionListener;
  }

  /**
   * Получает слушатель для отслеживания изменений состояния WebEngine.
   *
   * @return слушатель состояния
   */
  public ChangeListener<Worker.State> getStateListener() {
    return stateListener;
  }

  /**
   * Устанавливает слушатель для отслеживания изменений состояния WebEngine.
   *
   * @param stateListener слушатель для состояния
   */
  public void setStateListener(final ChangeListener<Worker.State> stateListener) {
    this.stateListener = stateListener;
  }
}
