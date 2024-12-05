package ru.mai.lessons.rpks;

import java.util.Objects;

public class PrivateEntry {
    private String url;


    public PrivateEntry() {}

    public PrivateEntry(String urlShort) {
        this.url = urlShort;

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String urlShort) {
        this.url = urlShort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrivateEntry that = (PrivateEntry) o;
        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}
