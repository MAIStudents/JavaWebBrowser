package ru.mai.lessons.rpks.javawebbrowser.web_browser.model.single_tab;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.util.List;

public class SingleTab {

    public static final int NOT_OPENED = -1;

    private final List<Tab> tabs;

    public SingleTab(TabPane tabs) {
        this.tabs = tabs.getTabs();
    }

    public int singleTabExists(String name) {
        for (int i = 0; i < tabs.size(); ++i) {
            if (tabs.get(i).getText().equals(name)) {
                return i;
            }
        }
        return -1;
    }

}
