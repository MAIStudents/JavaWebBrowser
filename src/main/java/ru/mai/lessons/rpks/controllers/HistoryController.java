package ru.mai.lessons.rpks.controllers;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import ru.mai.lessons.rpks.models.History;
import ru.mai.lessons.rpks.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class HistoryController {
  private static final Logger log = Logger.getLogger(HistoryController.class);

  private static final List<History> historyListGlobal = new ArrayList<>();
  private List<History> historyList = new ArrayList<>();
  private int currentIndex = -1;

  private static boolean historyEnabled = true;
  private static final Set<String> excludedSites = new HashSet<>();

  public void addEntry(String url) {
    if (!historyEnabled || isSiteExcluded(url)) {
      return;
    }

    if (!historyList.isEmpty() && currentIndex >= 0 && currentIndex < historyList.size() &&
            Objects.equals(historyList.get(currentIndex).getUrl() + '/', url)) {
      log.debug("URL {} already matches the current entry. Skipping.", url);
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
    historyListGlobal.add(new History(url, now, Duration.ZERO));
  }

  public String getCurrent () {
    if (currentIndex > 0) {
      return historyList.get(currentIndex).getUrl();
    }
    return null;
  }

  public String goBack() {
    while (currentIndex > 0) {
      currentIndex--;
      if (!excludedSites.contains(historyList.get(currentIndex).getUrl())) {
        log.debug("Going back to: {}", historyList.get(currentIndex).getUrl());
        return historyList.get(currentIndex).getUrl();
      }
    }
    return null;
  }

  public String goForward() {
    while (currentIndex < historyList.size() - 1) {
      currentIndex++;
      if (!excludedSites.contains(historyList.get(currentIndex).getUrl())) {
        log.debug("Going forward to: {}", historyList.get(currentIndex).getUrl());
        return historyList.get(currentIndex).getUrl();
      }
    }
    return null;
  }

  public static void setHistoryEnabled(boolean enabled) {
    historyEnabled = enabled;
  }

  public void addExcludedSite(String url) {
    String domain = extractDomain(url);
    if (domain != null) {
      excludedSites.add(domain);
    }
  }

  public void removeExcludedSite(String url) {
    String domain = extractDomain(url);
    if (domain != null) {
      excludedSites.remove(domain);
    }
  }

  public static boolean isSiteExcluded(String url) {
    String domain = extractDomain(url);
    return excludedSites.contains(domain);
  }

  private static String extractDomain(String url) {
    try {
      URI uri = new URI(url);
      String host = uri.getHost();
      if (host == null) {
        return null;
      }
      log.info("Domain {}", host);
      return host.startsWith("www.") ? host.substring(4) : host;
    } catch (URISyntaxException e) {
      log.error("Failed to extract domain from URL: {}", url, e);
      return null;
    }
  }

  public void saveHistoryToXml(File file) throws IOException {
    List<History.HistoryDto> historyDtoList = historyListGlobal.stream()
            .map(History.HistoryDto::new)
            .toList();
    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.writeValue(file, historyDtoList);
  }

  public List<History> getHistoryList () {
    return new ArrayList<>(historyList);
  }

  public List<History> getHistoryListGlobal () {
    return new ArrayList<>(historyListGlobal);
  }
}
