package ru.mai.lessons.rpks.helpClasses;

import java.time.LocalTime;
import java.util.Date;

public class HistoryTableViewDataProvider {
    Date date;
    LocalTime time;
    String address;

    public HistoryTableViewDataProvider(Date date, LocalTime time, String address) {
        this.date = date;
        this.time = time;
        this.address = address;
    }

    public Date getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getAddress() {
        return address;
    }
}
