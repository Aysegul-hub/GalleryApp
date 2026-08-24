package com.example.galleryapp;

import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.io.IOException;

public class MainController {

    @FXML
    private void openGallery(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/galleryapp/gallery.fxml"));


        Scene scene = new Scene(loader.load());

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

        boolean maximized = stage.isMaximized();

        double width = stage.getWidth();
        double height = stage.getHeight();

        double x = stage.getX();
        double y = stage.getY();

        stage.setScene(scene);

        stage.setWidth(width);
        stage.setHeight(height);
        stage.setX(x);
        stage.setY(y);


        if (maximized) {
            javafx.application.Platform.runLater(() -> {
                stage.setMaximized(true);

                    });

        }

    }

}
