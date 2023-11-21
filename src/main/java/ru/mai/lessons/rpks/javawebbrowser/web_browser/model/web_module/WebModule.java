package ru.mai.lessons.rpks.javawebbrowser.web_browser.model.web_module;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.model.web_module.state.WebSiteModuleState;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class WebModule {

    public boolean validateUrl(String siteUrl) {
        try {
            URL url = new URL(siteUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            if (connection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                return false;
            }
        } catch (IOException ignored) {
            return false;
        }
        return true;
    }

    public WebSiteModuleState onWebsiteLoad(Document newValue) {
        if (newValue == null) {
            return new WebSiteModuleState.NotReady();
        }
        NodeList head = newValue.getElementsByTagName("head");
        if (head.getLength() == 0) {
            return new WebSiteModuleState.NoTitle();
        } else {
            NodeList title = ((Element) head.item(0)).getElementsByTagName("title");
            if (title.getLength() != 0) {
                String pageTitle = title.item(0).getTextContent();
                return new WebSiteModuleState.Success(pageTitle);
            }
        }
        return new WebSiteModuleState.NoTitle();
    }

}
