package ru.mai.lessons.rpks;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class LikeManager {
    private final ObservableList<LikedEntry> liked;

    public LikeManager() {
        liked = FXCollections.observableArrayList();
    }

    public void addLikedEntry(LikedEntry entry) {
        if (liked.stream().noneMatch(e -> e.getUrl().equals(entry.getUrl()))) {
            liked.add(entry);
        }
    }

    public void removeLikedEntry(String url) {
        liked.removeIf(entry -> entry.getUrl().equals(url));
    }

    public ObservableList<LikedEntry> getLiked() {
        return liked;
    }

    public void saveLikedToFile(String filename) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        List<LikedEntry> likedList = liked.stream().toList();
        objectMapper.writeValue(new File(filename), likedList);
    }


    public void loadLikedFromFile(String filename) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        List<LikedEntry> likedList = objectMapper.readValue(new File(filename), objectMapper.getTypeFactory().constructCollectionType(List.class, LikedEntry.class));
        liked.setAll(likedList);
    }


}
