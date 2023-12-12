package ru.mai.lessons.rpks.javawebbrowser.history;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class HistoryJSON {

    public void serialize(List<HistoryEntry> historyEntries, Writer writer) {
        Gson gson = new Gson();
        gson.toJson(historyEntries, writer);
    }

    public List<HistoryEntry> deserializeToListHistoryEntry(String json) {

        Gson gson = new Gson();
        List<HistoryEntry> list = gson.fromJson(json, new TypeToken<ArrayList<HistoryEntry>>(){}.getType());

        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }
}
