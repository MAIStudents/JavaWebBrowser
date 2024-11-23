package ru.mai.lessons.rpks.controllers;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

public class PageTabController {
  private final Tab tab;
  private final WebView webView;
  private final WebEngine webEngine;
  private ChangeListener<Throwable> listener;

  public PageTabController (String url) {
    this.webView = new WebView();
    this.webEngine = webView.getEngine();
    this.webEngine.load(url);
    this.listener = (observableValue, throwable, t1) -> {
      if (t1 != null) {
        String googleSearchUrl = "https://www.google.com/search?q=" + url;
        webEngine.load(googleSearchUrl);
      }
    };

    VBox.setVgrow(webView, Priority.ALWAYS);
    VBox vBox = new VBox(webView);

    this.tab = new Tab("New Tab", vBox);
    this.tab.setUserData(this);
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
}
