package com.example.galleryapp;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class GalleryController {

    @FXML
    private TilePane galleryPane;

    @FXML
    private Label emptyLabel;


    @FXML
    private ScrollPane galleryScrollPane;


    // =========================================================
    // GALERİ KLASÖRÜ
    // =========================================================

    private final Path mediaFolder =
            Path.of(
                    "C://Users//ayseg//Desktop//GalleryAppMedia"
            );


    // =========================================================
    // TARİH FORMATI
    // =========================================================

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern(
                    "dd MMMM yyyy"
            );


    // =========================================================
    // GALERİ AÇILDIĞINDA
    // =========================================================

    @FXML
    public void initialize() {

        try {

            Files.createDirectories(mediaFolder);

            System.out.println("================================");
            System.out.println("GALERİNİN BAKTIĞI KLASÖR:");
            System.out.println(mediaFolder.toAbsolutePath());
            System.out.println("================================");

            loadGallery();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // SÜTUN SAYISINI EKRANA GÖRE AYARLA
    // =========================================================

    private void updateColumns(double width) {

        // Bir kart yaklaşık 230 px
        // Aralarında 15 px boşluk var

        int columns =
                Math.max(
                        1,
                        (int) ((width - 20) / 245)
                );

        galleryPane.setPrefColumns(
                columns
        );
    }

    private void loadGallery() {

        try {

            Files.createDirectories(mediaFolder);

            List<Path> files;

            try (var stream = Files.list(mediaFolder)) {

                files = stream
                        .filter(Files::isRegularFile)

                        // FOTOĞRAF VE VİDEOLARI AL
                        .filter(path -> {
                            String name = path
                                    .getFileName()
                                    .toString()
                                    .toLowerCase();

                            return isImage(name) || isVideo(name);
                        })

                        // YENİLER ÜSTTE
                        .sorted((a, b) -> {
                            try {
                                return Files.getLastModifiedTime(b)
                                        .compareTo(
                                                Files.getLastModifiedTime(a)
                                        );

                            } catch (IOException e) {
                                return 0;
                            }
                        })

                        .toList();
            }

            // Önce eski kartları temizle
            galleryPane.getChildren().clear();

            // Galeri boş mu?
            boolean empty = files.isEmpty();

            emptyLabel.setVisible(empty);
            emptyLabel.setManaged(empty);

            // Dosyaları ekle
            for (Path file : files) {

                String name = file
                        .getFileName()
                        .toString()
                        .toLowerCase();

                if (isImage(name)) {

                    createImageTile(file);

                } else if (isVideo(name)) {

                    createVideoTile(file);
                }
            }

            System.out.println("Galeride bulunan dosya sayısı: " + files.size());

            for (Path file : files) {
                System.out.println(file);
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    // =========================================================
    // GALERİYİ YÜKLE
    // =========================================================



    // =========================================================
    // FOTOĞRAF KARTI
    // =========================================================

    private void createImageTile(Path file) {

        try {

            // Fotoğraf
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


            // Fotoğrafın kutusu
            StackPane imageBox =
                    createBaseTile();

            imageBox
                    .getChildren()
                    .add(imageView);


            // Tarih
            Label dateLabel =
                    createDateLabel(file);


            // Fotoğraf + tarih
            VBox card =
                    new VBox(
                            8,
                            imageBox,
                            dateLabel
                    );

            card.setAlignment(
                    Pos.CENTER
            );


            // Tıklayınca fotoğrafı aç
            imageBox.setOnMouseClicked(
                    event ->
                            openImage(file)
            );


            galleryPane
                    .getChildren()
                    .add(card);

        } catch (Exception e) {

            e.printStackTrace();
        }

    }


    // =========================================================
    // VİDEO KARTI
    // =========================================================

    private void createVideoTile(Path file) {

        try {

            Media media = new Media(file.toUri().toString());

            MediaPlayer mediaPlayer = new MediaPlayer(media);

            MediaView mediaView = new MediaView(mediaPlayer);

            mediaView.setFitWidth(220);
            mediaView.setFitHeight(160);
            mediaView.setPreserveRatio(true);

            // Videoyu otomatik oynatma
            mediaPlayer.setMute(true);

            StackPane videoBox = new StackPane();

            videoBox.setPrefSize(220, 160);
            videoBox.setMinSize(220, 160);
            videoBox.setMaxSize(220, 160);

            // Video görüntüsü
            videoBox.getChildren().add(mediaView);

            // ▶ işareti
            Label playIcon = new Label("▶");

            playIcon.setStyle(
                    "-fx-font-size: 35px;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-color: rgba(0,0,0,0.45);" +
                            "-fx-background-radius: 50%;" +
                            "-fx-padding: 8px 12px 8px 12px;"
            );

            videoBox.getChildren().add(playIcon);

            // Video hazır olduğunda ilk kareye git
            mediaPlayer.setOnReady(() -> {

                mediaPlayer.seek(Duration.ZERO);

            });

            // Tarih
            Label dateLabel = createDateLabel(file);

            VBox card = new VBox(
                    8,
                    videoBox,
                    dateLabel
            );

            card.setAlignment(Pos.CENTER);

            // TIKLANINCA VİDEOYU AÇ
            videoBox.setOnMouseClicked(event -> {

                openVideo(file);

            });

            galleryPane
                    .getChildren()
                    .add(card);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // VİDEO KAPAĞI
    // =========================================================

    private void createVideoThumbnail(
            Path file,
            StackPane tile,
            Label playIcon) {

        try {

            Media media =
                    new Media(
                            file.toUri().toString()
                    );


            MediaPlayer player =
                    new MediaPlayer(media);

            player.setMute(true);


            MediaView mediaView =
                    new MediaView(player);


            mediaView.setFitWidth(220);
            mediaView.setFitHeight(160);
            mediaView.setPreserveRatio(true);


            player.setOnReady(() -> {

                player.seek(
                        Duration.ZERO
                );


                Platform.runLater(() -> {

                    try {

                        Image snapshot =
                                mediaView.snapshot(
                                        null,
                                        null
                                );


                        if (snapshot != null) {

                            ImageView thumbnail =
                                    new ImageView(
                                            snapshot
                                    );

                            thumbnail.setFitWidth(
                                    220
                            );

                            thumbnail.setFitHeight(
                                    160
                            );

                            thumbnail.setPreserveRatio(
                                    true
                            );

                            thumbnail.setSmooth(
                                    true
                            );


                            tile.getChildren()
                                    .remove(playIcon);


                            tile.getChildren()
                                    .add(
                                            thumbnail
                                    );


                            tile.getChildren()
                                    .add(
                                            playIcon
                                    );


                            StackPane.setAlignment(
                                    playIcon,
                                    Pos.CENTER
                            );
                        }


                        player.dispose();

                    } catch (Exception e) {

                        player.dispose();

                        e.printStackTrace();
                    }
                });
            });


            player.setOnError(() -> {

                System.out.println(
                        "Video thumbnail oluşturulamadı: "
                                + file
                );

                player.dispose();
            });


        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // TEMEL MEDYA KUTUSU
    // =========================================================

    private StackPane createBaseTile() {

        StackPane tile =
                new StackPane();

        tile.setPrefSize(
                230,
                170
        );

        tile.setMinSize(
                230,
                170
        );

        tile.setMaxSize(
                230,
                170
        );

        tile.setAlignment(
                Pos.CENTER
        );

        tile.setStyle(
                "-fx-background-color: #303030;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #555555;" +
                        "-fx-border-width: 1;"
        );


        return tile;
    }


    // =========================================================
    // TARİH LABEL'I
    // =========================================================



    // =========================================================
    // DOSYANIN TARİHİNİ AL
    // =========================================================

    private LocalDateTime getFileDate(
            Path file) {

        try {

            FileTime fileTime =
                    Files.getLastModifiedTime(
                            file
                    );


            return LocalDateTime.ofInstant(
                    fileTime.toInstant(),
                    ZoneId.systemDefault()
            );

        } catch (IOException e) {

            return LocalDateTime.MIN;
        }
    }


    // =========================================================
    // BÜYÜK FOTOĞRAF
    // =========================================================

    private void openImage(Path file) {

        Image image =
                new Image(
                        file.toUri().toString()
                );


        ImageView imageView =
                new ImageView(image);

        imageView.setPreserveRatio(
                true
        );

        imageView.setFitWidth(
                1100
        );

        imageView.setFitHeight(
                750
        );

        imageView.setSmooth(
                true
        );


        StackPane root =
                new StackPane(
                        imageView
                );

        root.setStyle(
                "-fx-background-color: #242424;"
        );


        Scene scene =
                new Scene(
                        root,
                        1200,
                        800
                );


        Stage stage =
                new Stage();

        stage.setTitle(
                file.getFileName()
                        .toString()
        );

        stage.setScene(scene);

        stage.show();
    }


    // =========================================================
    // VİDEO OYNATICI
    // =========================================================

    private void openVideo(Path file) {

        try {

            Media media =
                    new Media(
                            file.toUri().toString()
                    );


            MediaPlayer mediaPlayer =
                    new MediaPlayer(media);


            MediaView mediaView =
                    new MediaView(
                            mediaPlayer
                    );


            mediaView.setPreserveRatio(
                    true
            );

            mediaView.setFitWidth(
                    1100
            );

            mediaView.setFitHeight(
                    650
            );


            // -------------------------------------------------
            // PLAY / PAUSE
            // -------------------------------------------------

            Button playButton =
                    new Button("▶");

            playButton.setStyle(
                    "-fx-font-size: 18px;"
            );


            playButton.setOnAction(
                    event -> {

                        if (mediaPlayer.getStatus()
                                == MediaPlayer.Status.PLAYING) {

                            mediaPlayer.pause();

                            playButton.setText("▶");

                        } else {

                            mediaPlayer.play();

                            playButton.setText("⏸");
                        }
                    }
            );


            // -------------------------------------------------
            // BAŞA SAR
            // -------------------------------------------------

            Button restartButton =
                    new Button("↻");

            restartButton.setStyle(
                    "-fx-font-size: 18px;"
            );


            restartButton.setOnAction(
                    event -> {

                        mediaPlayer.seek(
                                Duration.ZERO
                        );

                        mediaPlayer.play();

                        playButton.setText("⏸");
                    }
            );


            // -------------------------------------------------
            // SES
            // -------------------------------------------------

            Label lowVolume =
                    new Label("🔈");

            Label highVolume =
                    new Label("🔊");


            Slider volumeSlider =
                    new Slider(
                            0,
                            1,
                            0.5
                    );

            volumeSlider.setPrefWidth(
                    130
            );


            mediaPlayer.volumeProperty()
                    .bind(
                            volumeSlider
                                    .valueProperty()
                    );


            // -------------------------------------------------
            // İLERLEME
            // -------------------------------------------------

            Slider progressSlider =
                    new Slider();

            progressSlider.setPrefWidth(
                    600
            );


            // -------------------------------------------------
            // SÜRE LABEL
            // -------------------------------------------------

            Label timeLabel =
                    new Label(
                            "00:00 / 00:00"
                    );

            timeLabel.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-size: 13px;"
            );


            // Video zamanı değişince
            // slider ve süreyi güncelle
            mediaPlayer.currentTimeProperty()
                    .addListener(
                            (obs, oldTime, newTime) -> {

                                if (!progressSlider
                                        .isValueChanging()) {

                                    Duration total =
                                            mediaPlayer
                                                    .getTotalDuration();


                                    if (total != null
                                            && !total.isUnknown()
                                            && total.toMillis() > 0) {

                                        double percent =
                                                newTime.toMillis()
                                                        / total.toMillis()
                                                        * 100;


                                        progressSlider.setValue(
                                                percent
                                        );
                                    }


                                    timeLabel.setText(
                                            formatDuration(
                                                    newTime
                                            )
                                                    + " / "
                                                    + formatDuration(
                                                    mediaPlayer
                                                            .getTotalDuration()
                                            )
                                    );
                                }
                            }
                    );


            // Slider'a tıklanınca videoda o noktaya git
            progressSlider.setOnMouseReleased(
                    event -> {

                        Duration total =
                                mediaPlayer
                                        .getTotalDuration();


                        if (total != null
                                && !total.isUnknown()) {

                            double position =
                                    progressSlider
                                            .getValue()
                                            / 100.0;


                            mediaPlayer.seek(
                                    Duration.millis(
                                            total.toMillis()
                                                    * position
                                    )
                            );
                        }
                    }
            );


            // -------------------------------------------------
            // KONTROLLER
            // -------------------------------------------------

            HBox controls =
                    new HBox(
                            10,
                            restartButton,
                            playButton,
                            progressSlider,
                            timeLabel,
                            lowVolume,
                            volumeSlider,
                            highVolume
                    );


            controls.setAlignment(
                    Pos.CENTER
            );


            controls.setStyle(
                    "-fx-padding: 12;" +
                            "-fx-background-color: #303030;"
            );


            // -------------------------------------------------
            // ANA EKRAN
            // -------------------------------------------------

            BorderPane root =
                    new BorderPane();


            root.setCenter(
                    mediaView
            );


            root.setBottom(
                    controls
            );


            root.setStyle(
                    "-fx-background-color: #242424;"
            );


            Scene scene =
                    new Scene(
                            root,
                            1200,
                            800
                    );


            Stage stage =
                    new Stage();


            stage.setTitle(
                    file.getFileName()
                            .toString()
            );


            stage.setScene(
                    scene
            );


            stage.show();


            // Video hazır olunca başlat
            mediaPlayer.setOnReady(
                    () -> {

                        mediaPlayer.play();

                        playButton.setText("⏸");

                        timeLabel.setText(
                                "00:00 / "
                                        + formatDuration(
                                        mediaPlayer
                                                .getTotalDuration()
                                )
                        );
                    }
            );


            // Pencere kapanınca player'ı temizle
            stage.setOnCloseRequest(
                    event ->
                            mediaPlayer.dispose()
            );


        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // SÜREYİ 00:00 ŞEKLİNE ÇEVİR
    // =========================================================


    private Label createDateLabel(Path file) {

        Label dateLabel = new Label();

        try {

            LocalDateTime date =
                    LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(file).toInstant(),
                            ZoneId.systemDefault()
                    );

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("dd.MM.yyyy");

            dateLabel.setText(
                    date.format(formatter)
            );

        } catch (IOException e) {

            dateLabel.setText("");
        }

        dateLabel.setStyle(
                "-fx-text-fill: #aaaaaa;" +
                        "-fx-font-size: 13px;"
        );

        return dateLabel;
    }

    // Son değiştirme tarihini yazacak...



    private String formatDuration(
            Duration duration) {

        if (duration == null
                || duration.isUnknown()) {

            return "00:00";
        }


        int totalSeconds =
                (int)
                        Math.floor(
                                duration.toSeconds()
                        );


        int minutes =
                totalSeconds / 60;


        int seconds =
                totalSeconds % 60;


        return String.format(
                "%02d:%02d",
                minutes,
                seconds
        );
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