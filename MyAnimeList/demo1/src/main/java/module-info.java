module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    opens org.example.demo1 to javafx.fxml;
    exports org.example.demo1;
}
