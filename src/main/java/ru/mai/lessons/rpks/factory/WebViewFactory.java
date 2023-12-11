package ru.mai.lessons.rpks.factory;

import javafx.scene.layout.AnchorPane;
import javafx.scene.web.WebView;

public class WebViewFactory {
    public static WebView create(double width, double height, double top, double right, double bottom, double left, String idWebView) {
        WebView webView = new WebView();

        webView.prefWidth(width);
        webView.prefHeight(height);
        AnchorPane.setTopAnchor(webView, top);
        AnchorPane.setRightAnchor(webView, right);
        AnchorPane.setBottomAnchor(webView, bottom);
        AnchorPane.setLeftAnchor(webView, left);
        webView.setId(idWebView);

        return webView;
    }
}
