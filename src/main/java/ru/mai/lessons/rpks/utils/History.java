package ru.mai.lessons.rpks.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Класс для представления истории посещения URL.
 */
public class History {

  /**
   * URL, связанный с историей.
   */
  private String url;

  /**
   * Время, когда произошло посещение URL.
   */
  private LocalDateTime timestamp;

  /**
   * Продолжительность посещения URL.
   */
  private Duration duration;

  /**
   * Конструктор без параметров для создания пустого объекта истории.
   */
  public History() {}

  /**
   * Конструктор для создания объекта истории с заданными параметрами.
   *
   * @param url URL, связанный с историей
   * @param timestamp Время посещения URL
   * @param duration Продолжительность посещения
   */
  public History(final String url,
                 final LocalDateTime timestamp,
                 final Duration duration) {
    this.url = url;
    this.timestamp = timestamp;
    this.duration = duration;
  }

  /**
   * Получить URL, связанный с историей.
   *
   * @return URL, связанный с историей
   */
  public String getUrl() {
    return url;
  }

  /**
   * Установить URL для истории.
   *
   * @param url Новый URL
   */
  public void setUrl(final String url) {
    this.url = url;
  }

  /**
   * Получить время посещения URL.
   *
   * @return Время посещения URL
   */
  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  /**
   * Установить время посещения URL.
   *
   * @param timestamp Новое время посещения
   */
  public void setTimestamp(final LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * Получить продолжительность посещения URL.
   *
   * @return Продолжительность посещения
   */
  public Duration getDuration() {
    return duration;
  }

  /**
   * Установить продолжительность посещения URL.
   *
   * @param duration Новая продолжительность посещения
   */
  public void setDuration(final Duration duration) {
    this.duration = duration;
  }

  /**
   * Вложенный класс для представления DTO (Data Transfer Object) для истории.
   */
  public static class HistoryDto {

    /**
     * URL, связанный с историей.
     */
    private String url;

    /**
     * Время посещения, представленное в строковом формате.
     */
    private String timestamp;

    /**
     * Продолжительность посещения, представленная в строковом формате.
     */
    private String duration;

    /**
     * Конструктор для создания DTO на основе объекта {@link History}.
     *
     * @param history Объект истории
     */
    public HistoryDto(final History history) {
      this.url = history.getUrl();
      this.timestamp = history.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
      this.duration = formatDuration(history.getDuration());
    }

    /**
     * Форматирует продолжительность в строку вида "чч:мм:сс".
     *
     * @param duration Продолжительность для форматирования
     * @return Форматированная строка продолжительности
     */
    private String formatDuration(final Duration duration) {
      long hours = duration.toHours();
      long minutes = duration.toMinutes() % 60;
      long seconds = duration.getSeconds() % 60;
      return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Получить URL из DTO.
     *
     * @return URL
     */
    public String getUrl() {
      return url;
    }

    /**
     * Установить URL в DTO.
     *
     * @param url Новый URL
     */
    public void setUrl(final String url) {
      this.url = url;
    }

    /**
     * Получить время посещения из DTO.
     *
     * @return Время посещения в строковом формате
     */
    public String getTimestamp() {
      return timestamp;
    }

    /**
     * Установить время посещения в DTO.
     *
     * @param timestamp Новое время посещения в строковом формате
     */
    public void setTimestamp(final String timestamp) {
      this.timestamp = timestamp;
    }

    /**
     * Получить продолжительность из DTO.
     *
     * @return Продолжительность в строковом формате
     */
    public String getDuration() {
      return duration;
    }

    /**
     * Установить продолжительность в DTO.
     *
     * @param duration Новая продолжительность в строковом формате
     */
    public void setDuration(final String duration) {
      this.duration = duration;
    }
  }
}
