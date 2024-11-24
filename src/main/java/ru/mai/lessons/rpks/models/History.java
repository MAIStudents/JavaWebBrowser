package ru.mai.lessons.rpks.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class History {
  private String url;

  private LocalDateTime timestamp;

  private Duration duration;

  public History() {}

  public History(String url, LocalDateTime timestamp, Duration duration) {
    this.url = url;
    this.timestamp = timestamp;
    this.duration = duration;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public Duration getDuration() {
    return duration;
  }

  public void setDuration(Duration duration) {
    this.duration = duration;
  }

  public static class HistoryDto {
    private String url;
    private String timestamp; // Преобразованный формат
    private String duration;  // Преобразованный формат

    public HistoryDto(History history) {
      this.url = history.getUrl();
      this.timestamp = history.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
      this.duration = formatDuration(history.getDuration()); // Преобразуем Duration в читаемый формат
    }

    private String formatDuration(Duration duration) {
      long hours = duration.toHours();
      long minutes = duration.toMinutes() % 60;
      long seconds = duration.getSeconds() % 60;
      return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Геттеры и сеттеры
    public String getUrl() {
      return url;
    }

    public void setUrl(String url) {
      this.url = url;
    }

    public String getTimestamp() {
      return timestamp;
    }

    public void setTimestamp(String timestamp) {
      this.timestamp = timestamp;
    }

    public String getDuration() {
      return duration;
    }

    public void setDuration(String duration) {
      this.duration = duration;
    }
  }

}
