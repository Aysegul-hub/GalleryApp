package com.example.galleryapp;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import javafx.animation.PauseTransition;

import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.util.Duration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

    public class AlbumMediaController {

        @FXML
        private Label albumNameLabel;

        @FXML
        private Label albumCountLabel;

        @FXML
        private TilePane mediaPane;


        // =========================================================
        // KLASÖRLER
        // =========================================================

        private final Path mediaFolder =
                Path.of(
                        "C://Users//ayseg//Desktop//GalleryAppMedia"
                );

        private final Path albumsFolder =
                Path.of(
                        "C://Users//ayseg//Desktop//GalleryAppMedia//albums"
                );


        private Path currentAlbum;


        // =========================================================
        // ALBÜMÜ AÇ
        // =========================================================

        public void setAlbum(Path album) {

            this.currentAlbum = album;

            albumNameLabel.setText(
                    album.getFileName().toString()
            );

            loadAlbum();
        }


        // =========================================================
        // ALBÜMÜ YÜKLE
        // =========================================================

        private void loadAlbum() {

            if (currentAlbum == null) {
                return;
            }

            try {

                Files.createDirectories(currentAlbum);

                List<Path> files;

                try (var stream = Files.list(currentAlbum)) {

                    files = stream
                            .filter(Files::isRegularFile)
                            .filter(path -> {

                                String name =
                                        path.getFileName()
                                                .toString()
                                                .toLowerCase();

                                return isImage(name)
                                        || isVideo(name);
                            })
                            .toList();
                }


                mediaPane.getChildren().clear();


                albumCountLabel.setText(
                        files.size() + " öğe"
                );


                for (Path file : files) {

                    createMediaCard(file);
                }


            } catch (IOException e) {

                e.printStackTrace();
            }
        }


        // =========================================================
        // MEDYA KARTI
        // =========================================================



        private void createMediaCard(Path file) {

            StackPane mediaBox =
                    new StackPane();

            mediaBox.setPrefSize(
                    220,
                    160
            );

            mediaBox.setMinSize(
                    220,
                    160
            );

            mediaBox.setMaxSize(
                    220,
                    160
            );

            mediaBox.setStyle(
                    "-fx-background-color: #303030;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-radius: 12;" +
                            "-fx-border-color: #555555;"
            );


            String name =
                    file.getFileName()
                            .toString()
                            .toLowerCase();


            // =====================================================
            // FOTOĞRAF
            // =====================================================

            if (isImage(name)) {

                try {

                    Image image =
                            new Image(
                                    file.toUri().toString(),
                                    220,
                                    160,
                                    true,
                                    true
                            );


                    ImageView imageView =
                            new ImageView(image);


                    imageView.setFitWidth(220);
                    imageView.setFitHeight(160);

                    imageView.setPreserveRatio(true);
                    imageView.setSmooth(true);


                    mediaBox.getChildren()
                            .add(imageView);


                    // ---------------------------------------------
                    // FOTOĞRAFA TIKLA
                    // ---------------------------------------------

                    mediaBox.setOnMouseClicked(
                            event -> openImage(file)
                    );


                    // Fare üzerine gelince el işareti
                    mediaBox.setStyle(
                            "-fx-background-color: #303030;" +
                                    "-fx-background-radius: 12;" +
                                    "-fx-border-radius: 12;" +
                                    "-fx-border-color: #666666;" +
                                    "-fx-cursor: hand;"
                    );


                } catch (Exception e) {

                    e.printStackTrace();
                }
            }


            // =====================================================
            // VİDEO
            // =====================================================

            else if (isVideo(name)) {

                try {

                    Media media =
                            new Media(
                                    file.toUri().toString()
                            );


                    MediaPlayer player =
                            new MediaPlayer(media);


                    MediaView mediaView =
                            new MediaView(player);


                    mediaView.setFitWidth(220);
                    mediaView.setFitHeight(160);

                    mediaView.setPreserveRatio(true);


                    mediaBox.getChildren()
                            .add(mediaView);


                    // ---------------------------------------------
                    // VİDEONUN İLK KARESİNİ GÖSTER
                    // ---------------------------------------------

                    player.setOnReady(() -> {

                        player.seek(Duration.ZERO);

                        player.play();


                        PauseTransition pause =
                                new PauseTransition(
                                        Duration.millis(300)
                                );


                        pause.setOnFinished(
                                event -> player.pause()
                        );


                        pause.play();
                    });


                    // ---------------------------------------------
                    // VİDEOYA TIKLA
                    // ---------------------------------------------

                    mediaBox.setOnMouseClicked(
                            event -> {

                                player.stop();

                                openVideo(file);
                            }
                    );


                    mediaBox.setStyle(
                            "-fx-background-color: #303030;" +
                                    "-fx-background-radius: 12;" +
                                    "-fx-border-radius: 12;" +
                                    "-fx-border-color: #666666;" +
                                    "-fx-cursor: hand;"
                    );


                } catch (Exception e) {

                    e.printStackTrace();

                    Label videoLabel =
                            new Label("▶");

                    videoLabel.setStyle(
                            "-fx-font-size: 40px;" +
                                    "-fx-text-fill: white;"
                    );

                    mediaBox.getChildren()
                            .add(videoLabel);


                    mediaBox.setOnMouseClicked(
                            event -> openVideo(file)
                    );
                }
            }


            // =====================================================
            // DOSYA ADI
            // =====================================================

            Label nameLabel =
                    new Label(
                            file.getFileName().toString()
                    );


            nameLabel.setStyle(
                    "-fx-text-fill: #aaaaaa;" +
                            "-fx-font-size: 12px;"
            );


            nameLabel.setMaxWidth(220);


            // =====================================================
            // KART
            // =====================================================

            VBox card =
                    new VBox(
                            8,
                            mediaBox,
                            nameLabel
                    );


            card.setAlignment(
                    Pos.CENTER
            );


            mediaPane.getChildren()
                    .add(card);
        }
        // =========================================================
// FOTOĞRAFI BÜYÜK AÇ
// =========================================================

        private void openImage(Path file) {

            try {

                Image image =
                        new Image(
                                file.toUri().toString()
                        );


                ImageView imageView =
                        new ImageView(image);


                imageView.setPreserveRatio(true);

                imageView.setFitWidth(1000);
                imageView.setFitHeight(700);


                StackPane root =
                        new StackPane(
                                imageView
                        );


                root.setStyle(
                        "-fx-background-color: #181818;"
                );


                Scene scene =
                        new Scene(
                                root,
                                1100,
                                750
                        );


                Stage stage =
                        new Stage();


                stage.setTitle(
                        file.getFileName().toString()
                );


                stage.setScene(scene);

                stage.show();


            } catch (Exception e) {

                e.printStackTrace();
            }
        }

        // =========================================================
// VİDEOYU BÜYÜK AÇ
// =========================================================

        // =========================================================
// VİDEOYU BÜYÜK AÇ
// =========================================================

        private void openVideo(Path file) {

            try {

                Media media =
                        new Media(
                                file.toUri().toString()
                        );

                MediaPlayer player =
                        new MediaPlayer(media);

                MediaView mediaView =
                        new MediaView(player);


                mediaView.setPreserveRatio(true);
                mediaView.setFitWidth(1000);
                mediaView.setFitHeight(600);


                // =====================================================
                // OYNAT / DURDUR
                // =====================================================

                Button playButton =
                        new Button("▶");

                playButton.setStyle(
                        "-fx-background-color: #3A3A3A;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;" +
                                "-fx-background-radius: 20;" +
                                "-fx-min-width: 40px;" +
                                "-fx-min-height: 40px;" +
                                "-fx-cursor: hand;"
                );


                // =====================================================
                // BAŞA SAR
                // =====================================================

                Button restartButton =
                        new Button("↻");

                restartButton.setStyle(
                        "-fx-background-color: #3A3A3A;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 18px;" +
                                "-fx-background-radius: 20;" +
                                "-fx-min-width: 40px;" +
                                "-fx-min-height: 40px;" +
                                "-fx-cursor: hand;"
                );


                // =====================================================
                // SÜRE LABEL
                // =====================================================

                Label currentTimeLabel =
                        new Label("00:00");

                currentTimeLabel.setStyle(
                        "-fx-text-fill: white;" +
                                "-fx-font-size: 13px;"
                );


                Label totalTimeLabel =
                        new Label("00:00");

                totalTimeLabel.setStyle(
                        "-fx-text-fill: #aaaaaa;" +
                                "-fx-font-size: 13px;"
                );


                // =====================================================
                // SÜRE SLIDER
                // =====================================================

                Slider timeSlider =
                        new Slider();

                timeSlider.setMin(0);
                timeSlider.setMax(100);
                timeSlider.setValue(0);

                timeSlider.setPrefWidth(600);


                // =====================================================
                // SES
                // =====================================================

                Label volumeLabel =
                        new Label("🔊");

                volumeLabel.setStyle(
                        "-fx-text-fill: white;" +
                                "-fx-font-size: 16px;"
                );


                Slider volumeSlider =
                        new Slider();

                volumeSlider.setMin(0);
                volumeSlider.setMax(1);
                volumeSlider.setValue(1);

                volumeSlider.setPrefWidth(100);


                // =====================================================
                // KONTROLLER
                // =====================================================

                HBox controls =
                        new HBox(
                                10,
                                restartButton,
                                playButton,
                                currentTimeLabel,
                                timeSlider,
                                totalTimeLabel,
                                volumeLabel,
                                volumeSlider
                        );


                controls.setAlignment(
                        Pos.CENTER
                );


                controls.setPadding(
                        new Insets(
                                10,
                                15,
                                10,
                                15
                        )
                );


                controls.setStyle(
                        "-fx-background-color: #222222;"
                );


                // =====================================================
                // ANA LAYOUT
                // =====================================================

                BorderPane root =
                        new BorderPane();

                root.setCenter(
                        mediaView
                );

                root.setBottom(
                        controls
                );


                root.setStyle(
                        "-fx-background-color: #181818;"
                );


                // =====================================================
                // PLAY / PAUSE
                // =====================================================

                playButton.setOnAction(
                        event -> {

                            if (player.getStatus()
                                    == MediaPlayer.Status.PLAYING) {

                                player.pause();

                                playButton.setText("▶");

                            } else {

                                player.play();

                                playButton.setText("⏸");
                            }
                        }
                );


                // =====================================================
                // BAŞA SAR
                // =====================================================

                restartButton.setOnAction(
                        event -> {

                            player.seek(
                                    Duration.ZERO
                            );

                            player.play();

                            playButton.setText("⏸");
                        }
                );


                // =====================================================
                // SES
                // =====================================================

                volumeSlider.valueProperty()
                        .addListener(
                                (obs, oldValue, newValue) -> {

                                    player.setVolume(
                                            newValue.doubleValue()
                                    );
                                }
                        );


                // =====================================================
                // VİDEO HAZIR OLDUĞUNDA
                // =====================================================

                player.setOnReady(
                        () -> {

                            Duration total =
                                    player.getTotalDuration();


                            totalTimeLabel.setText(
                                    formatTime(total)
                            );


                            timeSlider.setMax(
                                    total.toSeconds()
                            );


                            player.play();

                            playButton.setText("⏸");
                        }
                );


                // =====================================================
                // VİDEO İLERLEDİKÇE SLIDER GÜNCELLE
                // =====================================================

                player.currentTimeProperty()
                        .addListener(
                                (obs, oldTime, newTime) -> {

                                    if (!timeSlider.isValueChanging()) {

                                        timeSlider.setValue(
                                                newTime.toSeconds()
                                        );
                                    }


                                    currentTimeLabel.setText(
                                            formatTime(newTime)
                                    );
                                }
                        );


                // =====================================================
                // SLIDER'DAN VİDEODA İLERİ GERİ GİT
                // =====================================================

                timeSlider.setOnMousePressed(
                        event -> {

                            player.pause();

                        }
                );


                timeSlider.setOnMouseReleased(
                        event -> {

                            player.seek(
                                    Duration.seconds(
                                            timeSlider.getValue()
                                    )
                            );


                            player.play();

                            playButton.setText("⏸");
                        }
                );


                // =====================================================
                // VİDEO BİTİNCE
                // =====================================================

                player.setOnEndOfMedia(
                        () -> {

                            playButton.setText("▶");

                            timeSlider.setValue(
                                    timeSlider.getMax()
                            );
                        }
                );


                // =====================================================
                // PENCERE
                // =====================================================

                Scene scene =
                        new Scene(
                                root,
                                1100,
                                700
                        );


                Stage stage =
                        new Stage();


                stage.setTitle(
                        file.getFileName().toString()
                );


                stage.setScene(
                        scene
                );


                // =====================================================
                // PENCERE KAPANINCA PLAYER'I KAPAT
                // =====================================================

                stage.setOnCloseRequest(
                        event -> {

                            player.stop();

                            player.dispose();
                        }
                );


                stage.show();


            } catch (Exception e) {

                e.printStackTrace();

                showMessage(
                        "Video açılırken hata oluştu."
                );
            }


        }

        // =========================================================
// SÜREYİ 00:00 FORMATINA ÇEVİR
// =========================================================

        private String formatTime(Duration duration) {

            if (duration == null
                    || duration.isUnknown()) {

                return "00:00";
            }


            int totalSeconds =
                    (int) Math.floor(
                            duration.toSeconds()
                    );


            int hours =
                    totalSeconds / 3600;


            int minutes =
                    (totalSeconds % 3600) / 60;


            int seconds =
                    totalSeconds % 60;


            if (hours > 0) {

                return String.format(
                        "%02d:%02d:%02d",
                        hours,
                        minutes,
                        seconds
                );
            }


            return String.format(
                    "%02d:%02d",
                    minutes,
                    seconds
            );
        }



        // =========================================================
        // GALLERY'DEN ALBÜME EKLE
        // =========================================================

        @FXML
        private void addMedia() {

            try {

                Files.createDirectories(mediaFolder);


                List<Path> galleryFiles;

                try (var stream = Files.list(mediaFolder)) {

                    galleryFiles =
                            stream
                                    .filter(Files::isRegularFile)
                                    .filter(path -> {

                                        String name =
                                                path.getFileName()
                                                        .toString()
                                                        .toLowerCase();

                                        return isImage(name)
                                                || isVideo(name);
                                    })
                                    .toList();
                }


                if (galleryFiles.isEmpty()) {

                    showMessage(
                            "Gallery'de eklenecek fotoğraf veya video yok."
                    );

                    return;
                }


                List<String> fileNames =
                        galleryFiles.stream()
                                .map(path ->
                                        path.getFileName()
                                                .toString()
                                )
                                .toList();


                ChoiceDialog<String> dialog =
                        new ChoiceDialog<>(
                                fileNames.get(0),
                                fileNames
                        );

                dialog.setTitle("Gallery'den Ekle");

                dialog.setHeaderText(
                        "\"" +
                                currentAlbum.getFileName() +
                                "\" albümüne eklenecek dosyayı seç."
                );

                dialog.setContentText(
                        "Dosya:"
                );


                Optional<String> result =
                        dialog.showAndWait();


                if (result.isEmpty()) {
                    return;
                }


                Path selectedFile =
                        mediaFolder.resolve(
                                result.get()
                        );


                Path destination =
                        currentAlbum.resolve(
                                selectedFile
                                        .getFileName()
                                        .toString()
                        );


                Files.copy(
                        selectedFile,
                        destination,
                        StandardCopyOption.REPLACE_EXISTING
                );


                loadAlbum();


                showMessage(
                        "Öğe albüme eklendi."
                );


            } catch (IOException e) {

                e.printStackTrace();

                showMessage(
                        "Öğe albüme eklenirken hata oluştu."
                );
            }
        }


        // =========================================================
        // GERİ
        // =========================================================

        @FXML
        private void goBack(ActionEvent event)
                throws IOException {

            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/example/galleryapp/albums.fxml"
                            )
                    );


            Scene scene =
                    new Scene(
                            loader.load()
                    );


            Stage stage =
                    (Stage)
                            ((Node) event.getSource())
                                    .getScene()
                                    .getWindow();


            stage.setScene(scene);
        }


        // =========================================================
        // MESAJ
        // =========================================================

        private void showMessage(
                String message) {

            Alert alert =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alert.setTitle("Bilgi");
            alert.setHeaderText(null);
            alert.setContentText(message);

            alert.showAndWait();
        }


        // =========================================================
        // FOTOĞRAF MI?
        // =========================================================

        private boolean isImage(
                String fileName) {

            return fileName.endsWith(".jpg")
                    || fileName.endsWith(".jpeg")
                    || fileName.endsWith(".png")
                    || fileName.endsWith(".gif")
                    || fileName.endsWith(".bmp")
                    || fileName.endsWith(".webp");
        }


        // =========================================================
        // VİDEO MU?
        // =========================================================

        private boolean isVideo(
                String fileName) {

            return fileName.endsWith(".mp4")
                    || fileName.endsWith(".avi")
                    || fileName.endsWith(".mov")
                    || fileName.endsWith(".mkv")
                    || fileName.endsWith(".webm");
        }
    }

