package ru.mai.lessons.rpks.javawebbrowser.web_browser.model.web_module.state;

public sealed interface WebSiteModuleState {

    record NotReady() implements WebSiteModuleState {}

    record NoTitle() implements WebSiteModuleState {}

    record Success(
            String title
    ) implements WebSiteModuleState {}

}
