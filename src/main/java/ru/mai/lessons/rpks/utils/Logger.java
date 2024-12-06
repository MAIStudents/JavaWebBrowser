package ru.mai.lessons.rpks.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Класс для логирования сообщений с разными уровнями важности.
 * Логи выводятся в консоль с отметкой времени, уровня логирования и имени класса.
 */
public class Logger {

  /** Класс, для которого создаётся логгер. */
  private final Class<?> clazz;

  /** Формат для времени в логах. */
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /**
   * Конструктор для создания нового экземпляра логгера.
   *
   * @param clazz Класс, для которого будет вестись логирование.
   */
  public Logger(final Class<?> clazz) {
    this.clazz = clazz;
  }

  /**
   * Получить новый экземпляр логгера для указанного класса.
   *
   * @param clazz Класс, для которого будет вестись логирование.
   * @return Новый экземпляр логгера.
   */
  public static Logger getLogger(final Class<?> clazz) {
    return new Logger(clazz);
  }

  /**
   * Логирование сообщения на уровне DEBUG.
   *
   * @param message Сообщение для логирования.
   * @param args Параметры, которые будут подставлены в сообщение.
   */
  public void debug(final String message,
                    final Object... args) {
    log("DEBUG", message, args);
  }

  /**
   * Логирование сообщения на уровне INFO.
   *
   * @param message Сообщение для логирования.
   * @param args Параметры, которые будут подставлены в сообщение.
   */
  public void info(final String message,
                   final Object... args) {
    log("INFO", message, args);
  }

  /**
   * Логирование сообщения на уровне WARN.
   *
   * @param message Сообщение для логирования.
   * @param args Параметры, которые будут подставлены в сообщение.
   */
  public void warn(final String message,
                   final Object... args) {
    log("WARN", message, args);
  }

  /**
   * Логирование сообщения на уровне ERROR.
   *
   * @param message Сообщение для логирования.
   * @param args Параметры, которые будут подставлены в сообщение.
   */
  public void error(final String message,
                    final Object... args) {
    log("ERROR", message, args);
  }

  /**
   * Внутренний метод для логирования сообщения с заданным уровнем.
   *
   * @param level Уровень логирования (например, DEBUG, INFO).
   * @param message Сообщение для логирования.
   * @param args Параметры, которые будут подставлены в сообщение.
   */
  private void log(final String level,
                   final String message,
                   final Object... args) {
    String timestamp = LocalDateTime.now().format(formatter);

    String formattedMessage = format(message, args);

    System.out.printf("%s [%s] [%s] %s%n", timestamp, level, clazz.getSimpleName(), formattedMessage);
  }

  /**
   * Форматирует сообщение, подставляя в него значения параметров.
   *
   * @param message Сообщение с placeholders (например, "{}").
   * @param args Параметры, которые будут подставлены вместо placeholders.
   * @return Форматированное сообщение.
   */
  private String format(String message,
                        final Object... args) {
    for (Object arg : args) {
      message = message.replaceFirst("\\{\\}", arg != null ? arg.toString() : "null");
    }
    return message;
  }
}
