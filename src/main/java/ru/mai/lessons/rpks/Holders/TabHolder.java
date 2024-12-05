package ru.mai.lessons.rpks.Holders;
import javafx.scene.control.*;
import org.tinylog.Logger;

public class TabHolder {
    private String url;
    private final TabPane parentPane;
    private final Tab tab;

    public TabHolder(TabPane parentPane, Tab tab) {
        this.parentPane = parentPane;
        this.tab = tab;
    }

    public void loadTab() {
        Logger.info("Loading tab " + tab.getText());

    }

}
