package ru.mai.lessons.rpks;

import java.time.LocalDateTime;

public class HistoryEntry {
    private String url;
    private LocalDateTime visitTime;
    private long timeSpent;
    private boolean isValid;

    public HistoryEntry(String url, LocalDateTime visitTime, long timeSpent, boolean isValid) {
        this.url = url;
        this.visitTime = visitTime;
        this.timeSpent = timeSpent;
        this.isValid = isValid;
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

    public boolean isValid() {
        return isValid;
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

    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }
}
