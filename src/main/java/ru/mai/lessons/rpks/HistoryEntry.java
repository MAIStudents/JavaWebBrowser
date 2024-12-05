package ru.mai.lessons.rpks;

import java.time.LocalDateTime;

public class HistoryEntry {
    private String url;
    private LocalDateTime visitTime;
    private long timeSpent;

    public HistoryEntry() {}


    public HistoryEntry(String url, LocalDateTime visitTime, long timeSpent) {
        this.url = url;
        this.visitTime = visitTime;
        this.timeSpent = timeSpent;
    }

    public String getUrl() {
        return url;
    }

    public LocalDateTime getVisitTime() {
        return visitTime;
    }

    public long getTimeSpent() {
        return timeSpent;
    }



    public void setUrl(String url) {
        this.url = url;
    }

    public void setVisitTime(LocalDateTime visitTime) {
        this.visitTime = visitTime;
    }

    public void setTimeSpent(long timeSpent) {
        this.timeSpent = timeSpent;
    }

}
