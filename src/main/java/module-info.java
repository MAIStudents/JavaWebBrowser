module ru.mai.lessons.rpks.javawebbrowser {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens ru.mai.lessons.rpks.javawebbrowser to javafx.fxml;
    exports ru.mai.lessons.rpks.javawebbrowser;
}