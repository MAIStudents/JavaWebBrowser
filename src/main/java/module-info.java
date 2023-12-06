module ru.mai.lessons.rpks.javawebbrowser {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires validatorfx;
    requires org.kordamp.ikonli.javafx;

    requires com.google.gson;

    requires log4j;
    opens ru.mai.lessons.rpks.javawebbrowser.history to com.google.gson;

    opens ru.mai.lessons.rpks.javawebbrowser to javafx.fxml;
    exports ru.mai.lessons.rpks.javawebbrowser;
}