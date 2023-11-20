package ru.mai.lessons.rpks.javawebbrowser.web_browser.model;

public record Website(String title, String location) {

    public static final Website NOT_LOADABLE_PAGE = new Website("", "");

    public Website(String title, String location) {
        this.title = title;

        int to = location.indexOf("/?");
        if (to == -1) {
            this.location = location;
        } else {
            this.location = location.substring(0, to + 1);
        }
    }

}
