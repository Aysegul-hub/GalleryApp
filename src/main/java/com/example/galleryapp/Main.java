package com.example.galleryapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.application.Platform;

import java.io.IOException;


public class Main extends Application {

    //Uygulamaya kendini tanıtıyoruz.
    public static final String APP_VERSION ="1.1.1";

    @Override
    public void start(Stage stage) throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("main.fxml"));

        Scene scene = new Scene(fxmlLoader.load(),1000,700);

        stage.setTitle("Gallery App");
        stage.setScene(scene);
        stage.show();


        UpdateChecker.checkForUpdate();
    }

}
