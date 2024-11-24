package ru.mai.lessons.rpks.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.extern.slf4j.Slf4j;
import ru.mai.lessons.rpks.models.History;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;


@Slf4j
public class HistoryController {
  private List<History> historyList = new ArrayList<>();
  private int currentIndex = -1;

  private static boolean historyEnabled = true;
  private final Set<String> excludedSites = new HashSet<>();

  public void addEntry(String url) {
    if (!historyEnabled || isSiteExcluded(url)) {
      return;
    }

//    if (currentIndex >= 0) {
//      log.debug("{}", historyList.get(currentIndex).getUrl());
//      log.debug("{}", url);
//    }

    if (!historyList.isEmpty() && currentIndex >= 0 && currentIndex < historyList.size() &&
            Objects.equals(historyList.get(currentIndex).getUrl() + '/', url)) {
      log.debug("URL {} already matches the current entry. Skipping.", url);
      return;
    }

//    log.debug("Add to history {}", url);

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
//    log.debug("current index {}", currentIndex);

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

  public void setHistoryEnabled(boolean enabled) {
    this.historyEnabled = enabled;
  }

  public void addExcludedSite(String site) {
    excludedSites.add(site);
  }

  public void removeExcludedSite(String site) {
    excludedSites.remove(site);
  }

  public boolean isSiteExcluded(String url) {
    return excludedSites.contains(url);
  }

  public void saveHistoryToXml(File file) throws IOException {
    List<History.HistoryDto> historyDtoList = historyList.stream()
            .map(History.HistoryDto::new)
            .toList();
    XmlMapper xmlMapper = new XmlMapper();
    xmlMapper.writeValue(file, historyDtoList);
  }

}
