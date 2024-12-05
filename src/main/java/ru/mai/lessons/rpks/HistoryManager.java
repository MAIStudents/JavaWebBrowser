package ru.mai.lessons.rpks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.time.LocalDateTime;
import java.util.List;


public class HistoryManager {
    private final ObservableList<HistoryEntry> history;

    public HistoryManager() {
        history = FXCollections.observableArrayList();
    }

    public HistoryEntry getHistoryEntryByUrl(String url, LocalDateTime visitTime) {
        for (HistoryEntry entry : history) {
            if (entry.getUrl().equals(url) && entry.getVisitTime().equals(visitTime)) {
                return entry;
            }
        }
        return null;
    }

    public void updateHistoryEntry(HistoryEntry entry) {

        for (int i = 0; i < history.size(); i++) {
            HistoryEntry currentEntry = history.get(i);
            if (currentEntry.getUrl().equals(entry.getUrl()) && currentEntry.getVisitTime().equals(entry.getVisitTime())) {
                history.set(i, entry);
                return;
            }
        }
    }


    public void addHistoryEntry(HistoryEntry entry) {

        history.add(entry);
    }

    public ObservableList<HistoryEntry> getHistory() {
        return history;
    }

    public void removeHistoryEntry(String url) {
        history.removeIf(entry -> entry.getUrl().equals(url));
    }

    public void clearHistory() {
        history.clear();
    }


    public void saveHistoryToFile(String filename) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        List<HistoryEntry> historyList = history.stream().toList();
        objectMapper.writeValue(new File(filename), historyList);
    }


    public void loadHistoryFromFile(String filename) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        List<HistoryEntry> historyList = objectMapper.readValue(new File(filename), objectMapper.getTypeFactory().constructCollectionType(List.class, HistoryEntry.class));
        history.setAll(historyList);
    }


}
