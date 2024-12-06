package ru.mai.lessons.rpks.controllers;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import ru.mai.lessons.rpks.utils.History;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Контроллер для управления историей посещенных сайтов.
 * Содержит методы для добавления записей, навигации по истории, управления исключениями и сохранения истории в файл.
 */
public final class HistoryController {
  /**
   * Глобальный список истории для хранения всех записей.
   */
  private static final List<History> GLOBAL_HISTORY = new ArrayList<>();

  /**
   * Локальный список истории для текущего сеанса.
   */
  private List<History> historyList = new ArrayList<>();

  /**
   * Индекс текущей записи в локальной истории.
   */
  private int currentIndex = -1;

  /**
   * Флаг, указывающий, разрешена ли история.
   */
  private static boolean historyEnabled = true;

  /**
   * Множество доменов, для которых история не будет сохраняться.
   */
  private static final Set<String> excludedSites = new HashSet<>();

  /**
   * Добавляет новую запись в историю посещений.
   *
   * @param url URL сайта, который был посещен.
   */
  public void addEntry(final String url) {
    if (!historyEnabled
        || isSiteExcluded(url)
        || !historyList.isEmpty() && currentIndex >= 0 && currentIndex < historyList.size() &&
        Objects.equals(historyList.get(currentIndex).getUrl() + '/', url)) {
      return;
    }

    LocalDateTime now = LocalDateTime.now();

    if (currentIndex < historyList.size() - 1) {
      historyList = historyList.subList(0, currentIndex + 1);
    }

    if (!historyList.isEmpty()) {
      History previous = historyList.get(currentIndex);
      previous.setDuration(Duration.between(previous.getTimestamp(), now));
    }

    historyList.add(new History(url, now, Duration.ZERO));
    currentIndex = historyList.size() - 1;
    GLOBAL_HISTORY.add(new History(url, now, Duration.ZERO));
  }

  /**
   * Получает текущий URL из истории.
   *
   * @return URL текущей записи, или null, если история пуста.
   */
  public String getCurrent () {
    return currentIndex > 0
        ? historyList.get(currentIndex).getUrl()
        : null;
  }

  /**
   * Переходит к предыдущей записи в истории.
   *
   * @return URL предыдущей записи, или null, если переход невозможен.
   */
  public String goBack() {
    while (currentIndex > 0) {
      if (!excludedSites.contains(historyList.get(currentIndex - 1).getUrl())) {
        return historyList.get(currentIndex--).getUrl();
      }
    }
    return null;
  }

  /**
   * Переходит к следующей записи в истории.
   *
   * @return URL следующей записи, или null, если переход невозможен.
   */
  public String goForward() {
    while (currentIndex < historyList.size() - 1) {
      if (!excludedSites.contains(historyList.get(currentIndex + 1).getUrl())) {
        return historyList.get(currentIndex++).getUrl();
      }
    }
    return null;
  }

  /**
   * Включает или выключает историю.
   *
   * @param enabled true, если история должна быть включена, false — если выключена.
   */
  public static void setHistoryEnabled(final boolean enabled) {
    historyEnabled = enabled;
  }

  /**
   * Добавляет домен в список исключенных сайтов.
   *
   * @param url URL сайта для добавления в список исключений.
   */
  public void addExcludedSite(final String url) {
    String domain = extractDomain(url);
    if (domain != null) {
      excludedSites.add(domain);
    }
  }

  /**
   * Удаляет домен из списка исключенных сайтов.
   *
   * @param url URL сайта для удаления из списка исключений.
   */
  public void removeExcludedSite(final String url) {
    String domain = extractDomain(url);
    if (domain != null) {
      excludedSites.remove(domain);
    }
  }

  /**
   * Проверяет, исключен ли сайт из истории.
   *
   * @param url URL сайта для проверки.
   * @return true, если сайт исключен, false — если нет.
   */
  public static boolean isSiteExcluded(final String url) {
    String domain = extractDomain(url);
    return excludedSites.contains(domain);
  }

  /**
   * Извлекает домен из URL.
   *
   * @param url URL для извлечения домена.
   * @return домен сайта, или null, если извлечение не удалось.
   */
  private static String extractDomain(final String url) {
    try {
      URI uri = new URI(url);
      String host = uri.getHost();
      if (host == null) {
        return null;
      }
      return host.startsWith("www.") ? host.substring(4) : host;
    } catch (URISyntaxException e) {
      System.out.println("Не удалось извлечь домен из URL(" + url + "): " + e);
      return null;
    }
  }

  /**
   * Сохраняет историю в XML файл.
   *
   * @param file файл, в который будет сохранена история.
   * @throws IOException если возникла ошибка при записи в файл.
   */
  public void saveHistoryToXml(final File file) throws IOException {
    List<History.HistoryDto> historyDtoList = GLOBAL_HISTORY.stream()
        .map(History.HistoryDto::new)
        .toList();
    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.writeValue(file, historyDtoList);
  }

  /**
   * Получает глобальный список истории посещений.
   *
   * @return копия глобального списка истории.
   */
  public List<History> getHistoryListGlobal () {
    return new ArrayList<>(GLOBAL_HISTORY);
  }
}
