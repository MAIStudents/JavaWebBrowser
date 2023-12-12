module ru.mai.lessons.rpks.javawebbrowser {
    requires javafx.fxml;
    requires javafx.web;

    requires com.google.gson;

    requires log4j;
    opens ru.mai.lessons.rpks.javawebbrowser.history to com.google.gson;

    opens ru.mai.lessons.rpks.javawebbrowser to javafx.fxml;
    exports ru.mai.lessons.rpks.javawebbrowser;
}