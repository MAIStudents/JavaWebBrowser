package ru.mai.lessons.rpks.javawebbrowser.history;

import java.util.ArrayList;

import javafx.scene.web.WebHistory;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.List;

public class History {

    public static final Logger log = Logger.getLogger(History.class.getName());

    private final List<HistoryLine> currentHistory;
    private Date syncDate;

    public History() {
        currentHistory = new ArrayList<>();
        syncDate = new Date(System.currentTimeMillis());
    }

    public void sortHistory(List<HistoryLine> list) {
        list.sort((HistoryLine l, HistoryLine r) -> (int) (l.getLongStartDate() - r.getLongStartDate()));
    }

    public void calculateDuration(List<HistoryLine> list) {
        for (int i = 0; i < list.size() - 1; i++) {
            list.get(i).setDuration((int)
                    (list.get(i + 1).getLongStartDate() - list.get(i).getLongStartDate()));
        }
        if (list.size() > 0) {
            list.get(list.size() - 1).setDuration((int)
                    (getCurrentTimeLong() - list.get(list.size() - 1).getLongStartDate()));
        }
    }


    public void addHistory(List<HistoryLine> list) {

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

    public void addHistoryLine(List<WebHistory.Entry> entries) {

        log.info("adding to history\n");
        List<HistoryLine> entryList = new ArrayList<>(entries.size());

        for (WebHistory.Entry entry : entries) {

            if (entry.getLastVisitedDate() != null
                    && !entry.getLastVisitedDate().before(syncDate)) {
                entryList.add(new HistoryLine(
                        entry.getUrl(),
                        entry.getTitle(),
                        entry.getLastVisitedDate()
                ));
            }
        }

        sortHistory(entryList);
        calculateDuration(entryList);
        addHistory(entryList);
    }


    public List<HistoryLine> getHistoryLinesList() {
        return currentHistory;
    }


    public void clear() {
        syncDate = new Date(System.currentTimeMillis());
        currentHistory.clear();
    }

    private long getCurrentTimeLong() {
        return System.currentTimeMillis();
    }
}