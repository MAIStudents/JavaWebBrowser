module org.example.javaweb {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires jdk.httpserver;
    requires com.google.gson;
    requires org.jsoup;
    requires annotations;
    requires java.desktop;

    opens ru.mai.lessons.rpks to javafx.fxml;
    exports ru.mai.lessons.rpks;
}