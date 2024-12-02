package ru.mai.lessons.rpks.events;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.HBox;
import ru.mai.lessons.rpks.managers.FavSitesManager;
import ru.mai.lessons.rpks.managers.HistoryManager;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Event {

    public static EventHandler<ActionEvent> clearHistory(ListView<HBox> historyListView) {
        return actionEvent -> {
            HistoryManager.clearHistory();
            historyListView.getItems().clear();
        };
    }

    public static EventHandler<ActionEvent> removeHistoryEntry(ListView<HBox> listView, HBox hBox, HistoryManager.HistoryEntry entry) {
        return actionEvent -> {
            listView.getItems().remove(hBox);
            HistoryManager.deleteEntry(entry);
        };
    }

    public static EventHandler<ActionEvent> removeFavSiteEntry(ListView<HBox> listView, HBox hBox, FavSitesManager.SiteEntry entry) {
        return actionEvent -> {
            listView.getItems().remove(hBox);
            FavSitesManager.deleteEntry(entry);
        };
    }

    public static EventHandler<ActionEvent> excludeSiteFromHistory(String host, ListView<HBox> listView, HBox hBox) {
        return actionEvent -> {
            HistoryManager.excludeSite(host);
            listView.getItems().remove(hBox);
        };
    }

    public static EventHandler<ActionEvent> includeSiteToHistory(String host, ListView<HBox> listView, HBox hBox) {
        return actionEvent -> {
            HistoryManager.includeSite(host);
            listView.getItems().remove(hBox);
        };
    }

    public static EventHandler<javafx.event.Event> createNewTab(Tab tab, TabPane tabPane, Consumer<Tab> appendTabToTabPane, Supplier<Tab> createTab) {
        return actionEvent -> {
            if (tab.isSelected()) {
                appendTabToTabPane.accept(createTab.get());
                Platform.runLater(() -> tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2));
            }
        };
    }
}
