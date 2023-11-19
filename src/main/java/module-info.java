module ru.mai.lessons.rpks {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires json.simple;
    requires com.google.gson;
    requires org.apache.commons.lang3;
    requires org.apache.commons.io;
    requires org.jsoup;


    opens ru.mai.lessons.rpks to javafx.fxml;
    exports ru.mai.lessons.rpks;
}