package ru.mai.lessons.rpks.managers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.mai.lessons.rpks.controllers.Browser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FavSitesManager {
    private static final String FAV_SITES_FILE = Objects.requireNonNull(FavSitesManager.class.getResource("/json/favorites.json")).getPath();
    private static List<SiteEntry> favSites = new ArrayList<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(FavSitesManager.class.getName());

    static {
        loadSites();
    }

    public static ArrayList<SiteEntry> getSites() {
        return new ArrayList<>(favSites);
    }

    private static void loadSites() {
        File file = new File(FAV_SITES_FILE);
        if (!file.exists() || file.length() == 0) {
            favSites = new ArrayList<>();
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            favSites = objectMapper.readValue(reader, new TypeReference<>() {
            });
        } catch (IOException e) {
            logger.log(Level.SEVERE, Browser.LOADING_ERROR + Browser.FAVORITES_TAB_NAME, e);
            favSites = new ArrayList<>();
        }
    }

    public static void addEntry(String url, String title, String host) {
        if (title == null || title.isEmpty() || host == null || host.isEmpty()) {
            logger.log(Level.SEVERE, "Error occurred: title or host are invalid");
            return;
        }

        SiteEntry entry = new SiteEntry(url, title, host);
        favSites.add(entry);
        saveSites();
    }

    public static void deleteEntry(SiteEntry entry) {
        if (favSites.contains(entry)) {
            favSites.remove(entry);
            saveSites();
        }
    }

    public static void saveSites() {
        try (FileWriter writer = new FileWriter(FAV_SITES_FILE)) {
            objectMapper.writeValue(writer, favSites);
            logger.log(Level.INFO, "Favorites sites were successfully saved");
        } catch (IOException e) {
            logger.log(Level.SEVERE, Browser.SAVING_ERROR + Browser.FAVORITES_TAB_NAME, e);
        }
    }

    public record SiteEntry(String url, String title, String host) {
    }
}
