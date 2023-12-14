package ru.mai.lessons.rpks.helpClasses;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public class HistoryTableViewDataProvider {
    LocalDateTime date;
    LocalTime time;
    String address;

    public HistoryTableViewDataProvider(LocalDateTime date, LocalTime time, String address) {
        this.date = date;
        this.time = time;
        this.address = address;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getAddress() {
        return address;
    }
}
