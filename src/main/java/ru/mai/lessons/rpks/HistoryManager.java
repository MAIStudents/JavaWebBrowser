package ru.mai.lessons.rpks;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class HistoryManager {
    private ObservableList<HistoryEntry> history;

    public HistoryManager() {
        history = FXCollections.observableArrayList();
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

    public void clearHistory() {///////////////////////мб не пригодится
        history.clear();
    }

    public List<HistoryEntry> getValidHistory() {
        return history.stream()
                .filter(HistoryEntry::isValid)
                .collect(Collectors.toList());
    }


    public void saveHistoryToFile(String filename) throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(history);
        }
    }


    public void loadHistoryFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            history = (ObservableList<HistoryEntry>) in.readObject();
        }
    }


}
