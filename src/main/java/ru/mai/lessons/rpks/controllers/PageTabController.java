package ru.mai.lessons.rpks.controllers;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.scene.control.Tab;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class PageTabController {
  private final HistoryController historyController;
  private final HTMLController htmlController;
  private final Tab tab;
  private final WebView webView;
  private final WebEngine webEngine;
  private ChangeListener<Throwable> listener;
  private ChangeListener<Worker.State> stateListener;

  public PageTabController (String url) {
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

  public HTMLController getHtmlController() { return htmlController; }

  public HistoryController getHistoryController() {
    return historyController;
  }

  public Tab getTab() {
    return tab;
  }

  public WebEngine getWebEngine() {
    return webEngine;
  }

  public WebView getWebView() {
    return webView;
  }

  public ChangeListener<Throwable> getExceptionListener() {
    return listener;
  }

  public void setExceptionListener(ChangeListener<Throwable> exceptionListener) {
    this.listener = exceptionListener;
  }

  public ChangeListener<Worker.State> getStateListener() {
    return stateListener;
  }

  public void setStateListener(ChangeListener<Worker.State> stateListener) {
    this.stateListener = stateListener;
  }
}
