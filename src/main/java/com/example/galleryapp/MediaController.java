package com.example.galleryapp;

import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class MediaController {

    // =========================================================
    // MEDIA KLASÖRÜ
    // =========================================================

    private final Path mediaFolder =
            Path.of(
                    "C://Users//ayseg//Desktop//GalleryAppMedia"
            );


    // =========================================================
    // FXML
    // =========================================================

    @FXML
    private TilePane mediaPane;

    @FXML
    private Label itemCountLabel;


    // =========================================================
    // SAYFADA OLUŞTURULAN MEDIA PLAYER'LAR
    // =========================================================

    private final List<MediaPlayer> previewPlayers =
            new ArrayList<>();


    // =========================================================
    // SAYFA AÇILDIĞINDA
    // =========================================================

    @FXML
    public void initialize() {

        try {

            Files.createDirectories(mediaFolder);

            loadMedia();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // GERİ
    // =========================================================

    @FXML
    private void goBack(ActionEvent event)
            throws IOException {

        // =====================================================
        // ÖNEMLİ:
        // Media sayfasındaki bütün video player'larını kapat.
        // Böylece Windows dosyaları kilitli tutmaz.
        // =====================================================

        disposePreviewPlayers();


        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/com/example/galleryapp/main.fxml"
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
    // MEDYA EKLE
    // =========================================================

    @FXML
    private void addMedia(ActionEvent event) {

        try {

            Files.createDirectories(
                    mediaFolder
            );


            // =================================================
            // DOSYA SEÇMEDEN ÖNCE PLAYER'LARI KAPAT
            // =================================================

            disposePreviewPlayers();


            FileChooser fileChooser =
                    new FileChooser();


            fileChooser.setTitle(
                    "Fotoğraf veya Video Seç"
            );


            // =================================================
            // FOTOĞRAFLAR
            // =================================================

            FileChooser.ExtensionFilter imageFilter =
                    new FileChooser.ExtensionFilter(
                            "Fotoğraflar",
                            "*.jpg",
                            "*.jpeg",
                            "*.png",
                            "*.gif",
                            "*.bmp",
                            "*.webp"
                    );


            // =================================================
            // VİDEOLAR
            // =================================================

            FileChooser.ExtensionFilter videoFilter =
                    new FileChooser.ExtensionFilter(
                            "Videolar",
                            "*.mp4",
                            "*.avi",
                            "*.mov",
                            "*.mkv",
                            "*.webm"
                    );


            // =================================================
            // TÜM MEDYALAR
            // =================================================

            FileChooser.ExtensionFilter mediaFilter =
                    new FileChooser.ExtensionFilter(
                            "Fotoğraf ve Videolar",
                            "*.jpg",
                            "*.jpeg",
                            "*.png",
                            "*.gif",
                            "*.bmp",
                            "*.webp",
                            "*.mp4",
                            "*.avi",
                            "*.mov",
                            "*.mkv",
                            "*.webm"
                    );


            fileChooser.getExtensionFilters()
                    .addAll(
                            mediaFilter,
                            imageFilter,
                            videoFilter
                    );


            Stage stage =
                    (Stage)
                            ((Node) event.getSource())
                                    .getScene()
                                    .getWindow();


            List<File> selectedFiles =
                    fileChooser.showOpenMultipleDialog(
                            stage
                    );


            if (selectedFiles == null
                    || selectedFiles.isEmpty()) {

                // Dosya seçilmediyse tekrar önizlemeleri oluştur.
                loadMedia();

                return;
            }


            int addedCount = 0;


            // =================================================
            // DOSYALARI KOPYALA
            // =================================================

            for (File file : selectedFiles) {

                Path source =
                        file.toPath();


                Path destination =
                        mediaFolder.resolve(
                                file.getName()
                        );


                try {

                    Files.copy(
                            source,
                            destination,
                            StandardCopyOption.REPLACE_EXISTING
                    );


                    addedCount++;


                } catch (IOException e) {

                    System.out.println(
                            "Dosya kopyalanamadı: "
                                    + file.getName()
                    );


                    e.printStackTrace();
                }
            }


            // =================================================
            // LİSTEYİ YENİLE
            // =================================================

            loadMedia();


            // =================================================
            // BİLDİRİM
            // =================================================

            if (addedCount > 0) {

                showMessage(
                        addedCount +
                                " medya başarıyla eklendi."
                );
            }


        } catch (Exception e) {

            e.printStackTrace();


            showMessage(
                    "Medya eklenirken bir hata oluştu."
            );
        }
    }


    // =========================================================
    // MEDYALARI YÜKLE
    // =========================================================

    private void loadMedia() {

        try {

            Files.createDirectories(
                    mediaFolder
            );


            // =================================================
            // ÖNCEKİ PLAYER'LARI KAPAT
            // =================================================

            disposePreviewPlayers();


            List<Path> files;


            try (var stream =
                         Files.list(mediaFolder)) {

                files =
                        stream

                                .filter(
                                        Files::isRegularFile
                                )

                                .filter(
                                        this::isMedia
                                )

                                .sorted(
                                        (a, b) -> {

                                            try {

                                                return Files
                                                        .getLastModifiedTime(b)
                                                        .compareTo(
                                                                Files.getLastModifiedTime(a)
                                                        );

                                            } catch (IOException e) {

                                                return 0;
                                            }
                                        }
                                )

                                .toList();
            }


            mediaPane
                    .getChildren()
                    .clear();


            itemCountLabel
                    .setText(
                            files.size() +
                                    " medya"
                    );


            for (Path file : files) {

                if (isImage(file)) {

                    createImageCard(file);

                } else if (isVideo(file)) {

                    createVideoCard(file);
                }
            }


        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // FOTOĞRAF KARTI
    // =========================================================

    private void createImageCard(Path file) {

        try {

            Image image =
                    new Image(
                            file.toUri().toString(),
                            230,
                            160,
                            true,
                            true
                    );


            ImageView imageView =
                    new ImageView(
                            image
                    );


            imageView.setFitWidth(
                    230
            );


            imageView.setFitHeight(
                    160
            );


            imageView.setPreserveRatio(
                    true
            );


            imageView.setSmooth(
                    true
            );


            StackPane imageBox =
                    createMediaBox();


            imageBox
                    .getChildren()
                    .add(imageView);


            // =================================================
            // TIKLAMA
            // =================================================

            imageBox.setOnMouseClicked(
                    event ->
                            showPreviewMessage()
            );


            Label nameLabel =
                    createNameLabel(
                            file
                    );


            VBox card =
                    new VBox(
                            8,
                            imageBox,
                            nameLabel
                    );


            card.setAlignment(
                    Pos.CENTER
            );


            mediaPane
                    .getChildren()
                    .add(card);


        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // VİDEO KARTI
    // =========================================================

    private void createVideoCard(Path file) {

        StackPane videoBox =
                createMediaBox();


        try {

            Media media =
                    new Media(
                            file.toUri().toString()
                    );


            MediaPlayer player =
                    new MediaPlayer(
                            media
                    );


            MediaView mediaView =
                    new MediaView(
                            player
                    );


            mediaView.setFitWidth(
                    230
            );


            mediaView.setFitHeight(
                    160
            );


            mediaView.setPreserveRatio(
                    true
            );


            player.setMute(
                    true
            );


            // =================================================
            // PLAYER'I LİSTEYE EKLE
            // =================================================

            previewPlayers.add(
                    player
            );


            videoBox
                    .getChildren()
                    .add(mediaView);


            // =================================================
            // VİDEO HAZIR OLDUĞUNDA
            // =================================================

            player.setOnReady(
                    () -> {

                        try {

                            player.seek(
                                    Duration.ZERO
                            );


                            player.play();


                            PauseTransition pause =
                                    new PauseTransition(
                                            Duration.millis(300)
                                    );


                            pause.setOnFinished(
                                    event -> {

                                        try {

                                            if (player.getStatus()
                                                    != MediaPlayer.Status.DISPOSED) {

                                                player.pause();
                                            }

                                        } catch (Exception ignored) {
                                        }
                                    }
                            );


                            pause.play();


                        } catch (Exception ignored) {
                        }
                    }
            );


            // =================================================
            // VİDEO HATASI
            // =================================================

            player.setOnError(
                    () -> {

                        System.out.println(
                                "Video önizlemesi açılamadı: "
                                        + file.getFileName()
                        );
                    }
            );


        } catch (Exception e) {

            System.out.println(
                    "Video önizlemesi oluşturulamadı: "
                            + file.getFileName()
            );


            e.printStackTrace();
        }


        // =====================================================
        // PLAY İKONU
        // =====================================================

        Label playIcon =
                new Label(
                        "▶️"
                );


        playIcon.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 28px;" +
                        "-fx-background-color: rgba(0,0,0,0.55);" +
                        "-fx-background-radius: 50%;" +
                        "-fx-padding: 8px 12px;"
        );


        videoBox
                .getChildren()
                .add(playIcon);


        StackPane.setAlignment(
                playIcon,
                Pos.CENTER
        );


        // =====================================================
        // TIKLAMA
        // =====================================================

        videoBox.setOnMouseClicked(
                event ->
                        showPreviewMessage()
        );


        Label nameLabel =
                createNameLabel(
                        file
                );


        VBox card =
                new VBox(
                        8,
                        videoBox,
                        nameLabel
                );


        card.setAlignment(
                Pos.CENTER
        );


        mediaPane
                .getChildren()
                .add(card);
    }


    // =========================================================
    // MEDYA KUTUSU
    // =========================================================

    private StackPane createMediaBox() {

        StackPane box =
                new StackPane();


        box.setPrefSize(
                240,
                170
        );


        box.setMinSize(
                240,
                170
        );


        box.setMaxSize(
                240,
                170
        );


        box.setAlignment(
                Pos.CENTER
        );


        box.setStyle(
                "-fx-background-color: #303030;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-color: #555555;" +
                        "-fx-border-width: 1;"
        );


        return box;
    }


    // =========================================================
    // DOSYA ADI
    // =========================================================

    private Label createNameLabel(
            Path file) {

        Label label =
                new Label(
                        file.getFileName()
                                .toString()
                );


        label.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;"
        );


        return label;
    }


    // =========================================================
    // ÖNİZLEME BİLDİRİMİ
    // =========================================================

    private void showPreviewMessage() {

        Alert alert =
                new Alert(
                        Alert.AlertType.INFORMATION
                );


        alert.setTitle(
                "Önizleme"
        );


        alert.setHeaderText(
                null
        );


        alert.setContentText(
                "Bu sayfadaki medyalar önizlemedir.\n" +
                        "Lütfen Gallery'den açınız."
        );


        // =====================================================
        // 10 SANİYE SONRA KAPAT
        // =====================================================

        PauseTransition delay =
                new PauseTransition(
                        Duration.seconds(10)
                );


        delay.setOnFinished(
                event ->
                        alert.close()
        );


        delay.play();


        alert.show();
    }


    // =========================================================
    // PLAYER'LARI TEMİZLE
    // =========================================================

    private void disposePreviewPlayers() {

        // =====================================================
        // BÜTÜN ÖNİZLEME PLAYER'LARINI DURDUR
        // =====================================================

        for (MediaPlayer player :
                new ArrayList<>(previewPlayers)) {

            try {

                player.stop();

            } catch (Exception ignored) {
            }


            try {

                player.dispose();

            } catch (Exception ignored) {
            }
        }


        // =====================================================
        // LİSTEYİ TEMİZLE
        // =====================================================

        previewPlayers.clear();
    }


    // =========================================================
    // MEDYA MI?
    // =========================================================

    private boolean isMedia(
            Path file) {

        return isImage(file)
                || isVideo(file);
    }


    // =========================================================
    // FOTOĞRAF MI?
    // =========================================================

    private boolean isImage(
            Path file) {

        String name =
                file.getFileName()
                        .toString()
                        .toLowerCase();


        return name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".png")
                || name.endsWith(".gif")
                || name.endsWith(".bmp")
                || name.endsWith(".webp");
    }


    // =========================================================
    // VİDEO MU?
    // =========================================================

    private boolean isVideo(
            Path file) {

        String name =
                file.getFileName()
                        .toString()
                        .toLowerCase();


        return name.endsWith(".mp4")
                || name.endsWith(".avi")
                || name.endsWith(".mov")
                || name.endsWith(".mkv")
                || name.endsWith(".webm");
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


        alert.setTitle(
                "Bilgi"
        );


        alert.setHeaderText(
                null
        );


        alert.setContentText(
                message
        );


        alert.showAndWait();
    }
}