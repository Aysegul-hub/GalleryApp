module com.example.galleryapp {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.galleryapp to javafx.fxml;
    exports com.example.galleryapp;
}