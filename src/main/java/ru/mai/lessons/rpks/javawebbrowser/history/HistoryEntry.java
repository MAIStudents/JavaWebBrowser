package ru.mai.lessons.rpks.javawebbrowser.history;

import java.util.Date;

public class HistoryEntry {

    private final String url;
    private final String title;
    private final Date startDate;
    private int duration;


    public HistoryEntry(String url, String title, Date startDate) {
        this.url = url;
        this.title = title;
        this.startDate = startDate;
        this.duration = -1;
    }

    public long getStartDateLong() {
        return startDate.getTime();
    }
    public Date getStartDate() {
        return startDate;
    }
    public void setDuration(int duration) {
        this.duration = duration;
    }


    public String toString() {
        return "URL: " + url + ", "
                + "Title: " + title + ", "
                + "startDate: " + startDate + ", "
                + "Duration: " + duration;
    }
}
