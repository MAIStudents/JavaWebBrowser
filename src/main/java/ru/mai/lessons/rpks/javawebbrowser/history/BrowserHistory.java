package ru.mai.lessons.rpks.javawebbrowser.history;

import javafx.scene.web.WebHistory;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class BrowserHistory {

    public static final Logger log = Logger.getLogger(BrowserHistory.class.getName());

    private final List<HistoryEntry> currentHistory;
    private Date lastSync;

    public BrowserHistory() {
        currentHistory = new ArrayList<>();
        lastSync = new Date(System.currentTimeMillis());
    }

    public void sortHistoryEntryList(List<HistoryEntry> list) {
        list.sort((HistoryEntry l, HistoryEntry r) -> (int) (l.getStartDateLong() - r.getStartDateLong()));
    }

    public void calculateDuration(List<HistoryEntry> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            list.get(i).setDuration((int)
                    (list.get(i + 1).getStartDateLong() - list.get(i).getStartDateLong()));
        }
        if (list.size() > 0) {
            list.get(list.size() - 1).setDuration((int)
                    (getCurrentTimeLong() - list.get(list.size() - 1).getStartDateLong()));
        }
    }


    public void addHistoryList(List<HistoryEntry> list) {

        if (list.isEmpty()) {
            return;
        }

        if (currentHistory.isEmpty()) {
            currentHistory.addAll(list);
            return;
        }

        int ptrHistory = 0;
        int ptrList = 0;

        while (ptrHistory < currentHistory.size() && ptrList < list.size()) {
            if (currentHistory.get(ptrHistory).getStartDate()
                    .after(list.get(ptrList).getStartDate())) {
                currentHistory.add(ptrHistory, list.get(ptrList));
                ptrList++;
            }
            ptrHistory++;
        }

        if (ptrHistory == currentHistory.size()) {
            currentHistory.addAll(list.subList(ptrList, list.size()));
        }
    }

    public void addHistoryEntries(List<WebHistory.Entry> entries) {

        log.info("adding to history\n");
        List<HistoryEntry> entryList = new ArrayList<>(entries.size());

        for (WebHistory.Entry entry : entries) {

            if (entry.getLastVisitedDate() != null
                    && !entry.getLastVisitedDate().before(lastSync)) {
                entryList.add(new HistoryEntry(
                        entry.getUrl(),
                        entry.getTitle(),
                        entry.getLastVisitedDate()
                ));
            }
        }

        sortHistoryEntryList(entryList);
        calculateDuration(entryList);
        addHistoryList(entryList);
    }


    public List<HistoryEntry> getHistoryEntriesList() {
        return currentHistory;
    }


    public void clear() {
        lastSync = new Date(System.currentTimeMillis());
        currentHistory.clear();
    }

    private long getCurrentTimeLong() {
        return System.currentTimeMillis();
    }
}