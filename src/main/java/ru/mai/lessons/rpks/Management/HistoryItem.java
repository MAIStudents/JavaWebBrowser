package ru.mai.lessons.rpks.Management;

import lombok.Getter;

@Getter
public class HistoryItem {
    private String url;
    private String entryTime;
    private String exitTime;

    public HistoryItem(String url, String entryTime, String exitTime) {
        this.url = url;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
    }

    public HistoryItem() {
    }

}
