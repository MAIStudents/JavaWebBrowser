module ru.mai.lessons.rpks {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;


    opens ru.mai.lessons.rpks to javafx.fxml;
    exports ru.mai.lessons.rpks;
}