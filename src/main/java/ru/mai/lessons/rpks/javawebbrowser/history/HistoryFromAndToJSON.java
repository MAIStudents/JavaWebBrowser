package ru.mai.lessons.rpks.javawebbrowser.history;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class HistoryFromAndToJSON {

    public void serialize(List<HistoryLine> historyEntries, Writer writer) {
        Gson gson = new Gson();
        gson.toJson(historyEntries, writer);
    }

    public List<HistoryLine> deserializeToListHistoryLines(String json) {

        Gson gson = new Gson();
        List<HistoryLine> list = gson.fromJson(json, new TypeToken<ArrayList<HistoryLine>>(){}.getType());

        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }
}
