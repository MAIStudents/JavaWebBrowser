package ru.mai.lessons.rpks.managers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ru.mai.lessons.rpks.controllers.Browser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HistoryManager {
    private static final String HISTORY_FILE = Objects.requireNonNull(HistoryManager.class.getResource("/json/history.json")).getPath();
    private static final String EXCLUDED_SITES_FILE = Objects.requireNonNull(HistoryManager.class.getResource("/json/excludedSites.json")).getPath();
    private static List<HistoryEntry> history = new ArrayList<>();
    private static List<String> excludedSites = new ArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(HistoryManager.class.getName());

    static {
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        loadHistory();
        loadExcludedSites();
    }

    public static void clearHistory() {
        history = new ArrayList<>();
        saveHistory();
    }

    public static void addEntry(String url, String title, String host, long timeSpent) {
        if (Browser.isHistoryEnabled) {
            if (title == null || title.isEmpty() || host == null || host.isEmpty()) {
                logger.log(Level.SEVERE, "Error occurred: title or host are invalid");
                return;
            }

            if (timeSpent < 0) {
                logger.log(Level.SEVERE, "Error occurred: time spent < 0");
                return;
            }

            if (!isExcluded(host)) {
                LocalDateTime timestamp = LocalDateTime.ofInstant(Instant.now(), ZoneId.systemDefault());
                HistoryEntry entry = new HistoryEntry(url, title, host, timestamp, timeSpent);
                history.add(entry);
                saveHistory();
            }
        }
    }

    public static void deleteEntry(HistoryEntry entry) {
        if (history.contains(entry)) {
            history.remove(entry);
            saveHistory();
        }
    }

    public static void excludeSite(String host) {
        if (!excludedSites.contains(host)) {
            excludedSites.add(host);
            saveExcludedSites();
            removeExcludedSitesFromHistory();
        }
    }

    public static void includeSite(String host) {
        if (excludedSites.contains(host)) {
            excludedSites.remove(host);
            saveExcludedSites();
        }
    }

    public static List<HistoryEntry> getHistory() {
        return new ArrayList<>(history);
    }

    public static List<String> getExcludedSites() {
        return new ArrayList<>(excludedSites);
    }

    private static boolean isExcluded(String url) {
        return excludedSites.stream().anyMatch(url::contains);
    }

    private static void loadHistory() {
        File file = new File(HISTORY_FILE);
        if (!file.exists() || file.length() == 0) {
            history = new ArrayList<>();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            history = objectMapper.readValue(reader, new TypeReference<>() {
            });
        } catch (IOException e) {
            logger.log(Level.SEVERE, Browser.LOADING_ERROR + Browser.HISTORY_TAB_NAME, e);
            history = new ArrayList<>();
        }
    }

    public static void saveHistory() {
        try (FileWriter writer = new FileWriter(HISTORY_FILE)) {
            objectMapper.writeValue(writer, history);
            logger.log(Level.INFO, "History was successfully saved");
        } catch (IOException e) {
            logger.log(Level.SEVERE, Browser.SAVING_ERROR + Browser.HISTORY_TAB_NAME, e);
        }
    }

    private static void loadExcludedSites() {
        File file = new File(EXCLUDED_SITES_FILE);
        if (!file.exists() || file.length() == 0) {
            excludedSites = new ArrayList<>();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            excludedSites = objectMapper.readValue(reader, new TypeReference<>() {
            });
        } catch (IOException e) {
            logger.log(Level.SEVERE, Browser.LOADING_ERROR + Browser.EXCLUDED_SITES_TAB_NAME, e);
            excludedSites = new ArrayList<>();
        }
    }

    private static void saveExcludedSites() {
        try (FileWriter writer = new FileWriter(EXCLUDED_SITES_FILE)) {
            objectMapper.writeValue(writer, excludedSites);
            logger.log(Level.INFO, "Excluded sites were successfully saved");
        } catch (IOException e) {
            logger.log(Level.SEVERE, Browser.SAVING_ERROR + Browser.EXCLUDED_SITES_TAB_NAME, e);
        }
    }

    private static void removeExcludedSitesFromHistory() {
        history = history.stream()
                .filter(entry -> excludedSites.stream().noneMatch(entry.host()::contains))
                .collect(Collectors.toList());
        saveHistory();
    }

    public record HistoryEntry(String url, String title, String host, LocalDateTime timestamp, long timeSpent) {
    }
}
