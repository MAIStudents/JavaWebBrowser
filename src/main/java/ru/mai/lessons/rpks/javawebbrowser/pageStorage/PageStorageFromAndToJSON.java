package ru.mai.lessons.rpks.javawebbrowser.pageStorage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

public class PageStorageFromAndToJSON {

    private final Set<String> pages;

    public PageStorageFromAndToJSON(String json) {
        pages = toStringSet(json);
    }

    public void serialize(Writer writer) {
        Gson gson = new Gson();
        gson.toJson(pages, writer);
    }

    public Set<String> toStringSet(String json) {

        Gson gson = new Gson();
        Set<String> set = gson.fromJson(json, new TypeToken<Set<String>>(){}.getType());

        if (set == null) {
            return new HashSet<>();
        }
        return set;
    }

    public void addPage(String page) {
        pages.add(page);
    }
    public void removeFrom(String page) {
        pages.remove(page);
    }

    public boolean isPagePresent(String page) {
        return pages.contains(page);
    }
}
