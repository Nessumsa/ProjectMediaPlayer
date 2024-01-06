module org.example.projectmediaplayer {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;

    opens org.example.projectmediaplayer to javafx.fxml;
    exports org.example.projectmediaplayer;
}