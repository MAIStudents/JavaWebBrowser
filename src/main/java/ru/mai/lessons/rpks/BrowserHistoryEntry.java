package ru.mai.lessons.rpks;

import java.net.URL;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BrowserHistoryEntry {
    private String url;
    private String title;
    private String visitDate;

    private String visitTime;

    public BrowserHistoryEntry(String url, String title, Date visitDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        this.url = url;
        this.title = title;
        this.visitDate = dateFormat.format(visitDate);
        this.visitTime = timeFormat.format(visitDate);
    }

    public String getUrl() {
        return url;
    }

}