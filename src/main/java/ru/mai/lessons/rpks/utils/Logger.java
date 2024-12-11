package ru.mai.lessons.rpks.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

  private final Class<?> clazz;
  private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public Logger(Class<?> clazz) {
    this.clazz = clazz;
  }

  public static Logger getLogger(Class<?> clazz) {
    return new Logger(clazz);
  }

  public void debug(String message, Object... args) {
    log("DEBUG", message, args);
  }

  public void info(String message, Object... args) {
    log("INFO", message, args);
  }

  public void warn(String message, Object... args) {
    log("WARN", message, args);
  }

  public void error(String message, Object... args) {
    log("ERROR", message, args);
  }

  private void log(String level, String message, Object... args) {
    String timestamp = LocalDateTime.now().format(formatter);
    String formattedMessage = format(message, args);
    System.out.printf("%s [%s] [%s] %s%n", timestamp, level, clazz.getSimpleName(), formattedMessage);
  }

  private String format(String message, Object... args) {
    for (Object arg : args) {
      message = message.replaceFirst("\\{\\}", arg != null ? arg.toString() : "null");
    }
    return message;
  }
}
