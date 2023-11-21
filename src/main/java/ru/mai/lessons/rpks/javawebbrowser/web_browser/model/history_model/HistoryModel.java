package ru.mai.lessons.rpks.javawebbrowser.web_browser.model.history_model;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import ru.mai.lessons.rpks.javawebbrowser.commons.Pair;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.model.Website;

import java.text.SimpleDateFormat;
import java.util.*;

public class HistoryModel {

    private final LinkedList<Pair<String, Website>> historyWebsites;
    private final List<String> blacklisted;

    private boolean isHistoryDisabled;

    public HistoryModel(CheckBox isHistoryDisabledCheckbox, TextArea blacklistedWebsites, LinkedList<Pair<String, Website>> historyWebsites, List<String> blacklisted, boolean isHistoryDisabled) {
        this.historyWebsites = historyWebsites;
        this.blacklisted = blacklisted;
        this.isHistoryDisabled = isHistoryDisabled;

        if (isHistoryDisabled) {
            updateBlacklistedWebsites(blacklistedWebsites.getText());
        }

        isHistoryDisabledCheckbox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> this.isHistoryDisabled = newValue);

        blacklistedWebsites.focusedProperty().addListener((ObservableValue<? extends Boolean> observableValue, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
                blacklisted.clear();
                updateBlacklistedWebsites(blacklistedWebsites.getText());
            }
        });
    }

    private void updateBlacklistedWebsites(String value) {
        blacklisted.addAll(Arrays.stream(value
                        .split("\\n"))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList());
    }

    public void addToHistory(Website website) {
        if (isHistoryDisabled) {
            return;
        }
        if (website.location().isEmpty()) {
            return;
        }
        String lowercaseLocation = website.location().toLowerCase();
        boolean isBlacklisted = isHistoryDisabled || blacklisted.stream()
                .anyMatch(lowercaseLocation::contains);
        if (!isBlacklisted && (historyWebsites.isEmpty() || !historyWebsites.getFirst().getValue().location().equals(website.location()))) {
            String timeAsString = new SimpleDateFormat("dd-MM-yy hh:mm:ss").format(new Date());
            historyWebsites.addFirst(Pair.of(timeAsString, website));
        }
    }

    public List<Pair<String, Website>> getHistoryWebsites() {
        return Collections.unmodifiableList(historyWebsites);
    }

    public boolean isHistoryEmpty() {
        return historyWebsites.isEmpty();
    }

    public boolean isHistoryDisabled() {
        return isHistoryDisabled;
    }

    public List<String> getBlacklisted() {
        return Collections.unmodifiableList(blacklisted);
    }

}
