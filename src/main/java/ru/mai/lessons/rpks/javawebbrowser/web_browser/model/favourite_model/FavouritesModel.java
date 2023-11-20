package ru.mai.lessons.rpks.javawebbrowser.web_browser.model.favourite_model;

import ru.mai.lessons.rpks.javawebbrowser.web_browser.model.Website;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FavouritesModel {

    private final LinkedList<Website> favouriteWebsites;

    public FavouritesModel(LinkedList<Website> favouriteWebsites) {
        this.favouriteWebsites = favouriteWebsites;
    }

    public boolean isWebsiteFavourite(Website website) {
        return favouriteWebsites.stream()
                .anyMatch(item -> item.location().equals(website.location()));
    }

    public void removeWebsiteFromFavourites(Website website) {
        favouriteWebsites.remove(website);
    }

    public void addWebsiteToFavourites(Website website) {
        favouriteWebsites.addFirst(website);
    }

    public boolean isFavouritesStorageEmpty() {
        return favouriteWebsites.isEmpty();
    }

    public List<Website> getFavouriteWebsites() {
        return Collections.unmodifiableList(favouriteWebsites);
    }

}
