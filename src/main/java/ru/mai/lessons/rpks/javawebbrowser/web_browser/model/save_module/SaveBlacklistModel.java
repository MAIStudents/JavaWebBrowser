package ru.mai.lessons.rpks.javawebbrowser.web_browser.model.save_module;

import java.util.LinkedList;

public class SaveBlacklistModel {

    private final boolean isHistoryDisabled;

    private final LinkedList<String> disabledWebsites;

    public boolean getIsHistoryDisabled() {
        return isHistoryDisabled;
    }

    public LinkedList<String> getDisabledWebsites() {
        return disabledWebsites;
    }

    public SaveBlacklistModel(boolean isHistoryDisabled, LinkedList<String> disabledWebsites) {
        this.isHistoryDisabled =isHistoryDisabled;
        this.disabledWebsites = disabledWebsites;
    }

}
