package ru.mai.lessons.rpks.Management;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SiteSettings {

    private String url;
    private boolean isLiked = false;
    private boolean isPrivate = false;

    public SiteSettings(String url) {
        this.url = url;
    }
    public SiteSettings() {
    }

}
