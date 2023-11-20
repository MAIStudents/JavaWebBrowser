package ru.mai.lessons.rpks.javawebbrowser.web_browser.view.websites_holder.state;

import ru.mai.lessons.rpks.javawebbrowser.commons.Pair;
import ru.mai.lessons.rpks.javawebbrowser.web_browser.model.Website;

import java.util.List;

public sealed interface WebsiteHolderStorageState {

    record ForHistory(List<Pair<String, Website>> websites) implements WebsiteHolderStorageState {}

    record ForFavourites(List<Website> websites) implements WebsiteHolderStorageState {}

}
