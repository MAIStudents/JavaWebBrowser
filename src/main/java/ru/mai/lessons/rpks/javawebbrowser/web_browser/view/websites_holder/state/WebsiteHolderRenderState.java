package ru.mai.lessons.rpks.javawebbrowser.web_browser.view.websites_holder.state;

public sealed interface WebsiteHolderRenderState {

    record NO_ITEMS(String noItemsText) implements WebsiteHolderRenderState {}

    record ITEMS_EXIST(WebsiteHolderStorageState websites) implements WebsiteHolderRenderState {}

}
