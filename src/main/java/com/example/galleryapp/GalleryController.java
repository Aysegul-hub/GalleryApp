package com.example.galleryapp;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;



    public class GalleryController {

        @FXML
        private ImageView photo1;

        @FXML
        private ImageView photo2;


        @FXML
        public void initialize() {

            Image image1 = new Image(getClass().getResource("/com/example/galleryapp/images/photo1.jpeg").toExternalForm());
            photo1.setImage(image1);

            Image image2 = new Image(getClass().getResource("/com/example/galleryapp/images/photo2.jpg").toExternalForm());
            photo2.setImage(image2);


        }

    }

