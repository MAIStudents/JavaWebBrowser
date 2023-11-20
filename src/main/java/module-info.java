module ru.mai.lessons.rpks.javawebbrowser {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.desktop;
    requires com.google.gson;

    opens ru.mai.lessons.rpks.javawebbrowser.web_browser.model to com.google.gson;
    opens ru.mai.lessons.rpks.javawebbrowser.web_browser.model.save_module to com.google.gson;

    exports ru.mai.lessons.rpks.javawebbrowser;
    exports ru.mai.lessons.rpks.javawebbrowser.web_browser.controller;
    opens ru.mai.lessons.rpks.javawebbrowser.web_browser.controller to javafx.fxml;

    exports ru.mai.lessons.rpks.javawebbrowser.html_page_creator.controller;
    opens ru.mai.lessons.rpks.javawebbrowser.html_page_creator.controller to javafx.fxml;

    exports ru.mai.lessons.rpks.javawebbrowser.commons;
    opens ru.mai.lessons.rpks.javawebbrowser.commons to javafx.fxml, com.google.gson;
}