module com.example.galleryapp {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.net.http;




    opens com.example.galleryapp to javafx.fxml;
    exports com.example.galleryapp;
}