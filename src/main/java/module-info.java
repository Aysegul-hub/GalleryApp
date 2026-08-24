module com.example.galleryapp {

    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;



    opens com.example.galleryapp to javafx.fxml;
    exports com.example.galleryapp;
}