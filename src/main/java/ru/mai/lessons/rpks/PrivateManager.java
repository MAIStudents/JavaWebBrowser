package ru.mai.lessons.rpks;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PrivateManager {
    private final ObservableList<PrivateEntry> urls;

    public PrivateManager() {
        urls = FXCollections.observableArrayList();
    }

    public void add(PrivateEntry url) {
        if (urls.stream().noneMatch(entry -> entry.getUrl() != null && entry.getUrl().equals(url.getUrl()))) {
            urls.add(url);
        }
    }

    public void remove(String url) {
        urls.removeIf(entry -> entry.getUrl().equals(url));
    }

    public ObservableList<PrivateEntry> getPrivates() {
        return urls;
    }

}
