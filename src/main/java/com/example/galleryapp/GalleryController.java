package com.example.galleryapp;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class GalleryController {

    // =========================================================
    // FXML
    // =========================================================

    @FXML
    private TilePane galleryPane;

    @FXML
    private ScrollPane galleryScrollPane;

    @FXML
    private Label itemCountLabel;

    @FXML
    private Button selectButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button addToAlbumButton;


    // =========================================================
    // SEÇİM SİSTEMİ
    // =========================================================

    private boolean selectionMode = false;

    private final List<Path> selectedFiles =
            new ArrayList<>();


    // =========================================================
    // GALERİ KLASÖRÜ
    // =========================================================

    private final Path mediaFolder =
            Path.of(
                    "C://Users//ayseg//Desktop//GalleryAppMedia"
            );


    // =========================================================
    // ALBÜMLER KLASÖRÜ
    // =========================================================

    private final Path albumsFolder =
            Path.of(
                    "C://Users//ayseg//Desktop//GalleryAppMedia//albums"
            );


    // =========================================================
    // TARİH FORMATI
    // =========================================================

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");


    // =========================================================
    // SAYFA AÇILDIĞINDA
    // =========================================================

    @FXML
    public void initialize() {

        try {

            Files.createDirectories(mediaFolder);
            Files.createDirectories(albumsFolder);

            // Başlangıçta seçim butonlarını gizle

            deleteButton.setVisible(false);
            deleteButton.setManaged(false);

            addToAlbumButton.setVisible(false);
            addToAlbumButton.setManaged(false);

            loadGallery();

            // Pencere genişliğine göre sütun sayısı

            if (galleryScrollPane != null) {

                galleryScrollPane.widthProperty()
                        .addListener(
                                (obs, oldValue, newValue) ->
                                        updateColumns(newValue.doubleValue())
                        );
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // SÜTUN SAYISI
    // =========================================================

    private void updateColumns(double width) {

        int columns =
                Math.max(
                        1,
                        (int) ((width - 20) / 245)
                );

        galleryPane.setPrefColumns(columns);
    }


    // =========================================================
    // SEÇ BUTONU
    // =========================================================

    @FXML
    private void toggleSelectionMode() {

        selectionMode = !selectionMode;

        selectedFiles.clear();

        if (selectionMode) {

            selectButton.setText("Seçimi Bitir");

            deleteButton.setVisible(true);
            deleteButton.setManaged(true);

            addToAlbumButton.setVisible(true);
            addToAlbumButton.setManaged(true);

        } else {

            selectButton.setText("Seç");

            deleteButton.setVisible(false);
            deleteButton.setManaged(false);

            addToAlbumButton.setVisible(false);
            addToAlbumButton.setManaged(false);
        }

        updateSelectionButtons();

        loadGallery();
    }


    // =========================================================
    // GERİ
    // =========================================================

    @FXML
    private void goBack(ActionEvent event) throws IOException {

        FXMLLoader loader =
                new FXMLLoader(
                        getClass().getResource(
                                "/com/example/galleryapp/main.fxml"
                        )
                );

        Scene scene =
                new Scene(loader.load());

        Stage stage =
                (Stage)
                        ((Node) event.getSource())
                                .getScene()
                                .getWindow();

        stage.setScene(scene);
    }


    // =========================================================
    // GALERİYİ YÜKLE
    // =========================================================

    private void loadGallery() {

        try {

            Files.createDirectories(mediaFolder);

            List<Path> files;

            try (var stream =
                         Files.list(mediaFolder)) {

                files =
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

                                // YENİLER ÜSTTE

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


            // Eski kartları temizle

            galleryPane.getChildren().clear();


            // =================================================
            // SAYILAR
            // =================================================

            long imageCount =
                    files.stream()
                            .filter(
                                    file ->
                                            isImage(
                                                    file.getFileName()
                                                            .toString()
                                                            .toLowerCase()
                                            )
                            )
                            .count();


            long videoCount =
                    files.stream()
                            .filter(
                                    file ->
                                            isVideo(
                                                    file.getFileName()
                                                            .toString()
                                                            .toLowerCase()
                                            )
                            )
                            .count();


            itemCountLabel.setText(
                    imageCount +
                            " fotoğraf • " +
                            videoCount +
                            " video • " +
                            files.size() +
                            " toplam öğe"
            );


            // =================================================
            // DOSYALARI OLUŞTUR
            // =================================================

            for (Path file : files) {

                String name =
                        file.getFileName()
                                .toString()
                                .toLowerCase();

                if (isImage(name)) {

                    createImageTile(file);

                } else if (isVideo(name)) {

                    createVideoTile(file);
                }
            }


        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // FOTOĞRAF KARTI
    // =========================================================

    private void createImageTile(Path file) {

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


            StackPane imageBox =
                    createBaseTile();


            imageBox.getChildren()
                    .add(imageView);


            Label dateLabel =
                    createDateLabel(file);


            VBox card =
                    new VBox(
                            8,
                            imageBox,
                            dateLabel
                    );

            card.setAlignment(Pos.CENTER);


            imageBox.setOnMouseClicked(
                    event -> {

                        if (selectionMode) {

                            toggleFileSelection(
                                    file,
                                    imageBox
                            );

                        } else {

                            openImage(file);
                        }
                    }
            );


            galleryPane.getChildren()
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

            Media media =
                    new Media(
                            file.toUri().toString()
                    );

            MediaPlayer mediaPlayer =
                    new MediaPlayer(media);

            MediaView mediaView =
                    new MediaView(mediaPlayer);


            mediaView.setFitWidth(220);
            mediaView.setFitHeight(160);
            mediaView.setPreserveRatio(true);


            mediaPlayer.setMute(true);


            StackPane videoBox =
                    createBaseTile();


            videoBox.getChildren()
                    .add(mediaView);


            Label playIcon =
                    new Label("▶");

            playIcon.setStyle(
                    "-fx-font-size: 35px;" +
                            "-fx-text-fill: white;" +
                            "-fx-background-color: rgba(0,0,0,0.45);" +
                            "-fx-background-radius: 50%;" +
                            "-fx-padding: 8px 12px 8px 12px;"
            );


            videoBox.getChildren()
                    .add(playIcon);


            StackPane.setAlignment(
                    playIcon,
                    Pos.CENTER
            );


            mediaPlayer.setOnReady(
                    () -> {

                        mediaPlayer.seek(
                                Duration.ZERO
                        );
                    }
            );


            Label dateLabel =
                    createDateLabel(file);


            VBox card =
                    new VBox(
                            8,
                            videoBox,
                            dateLabel
                    );

            card.setAlignment(Pos.CENTER);


            videoBox.setOnMouseClicked(
                    event -> {

                        if (selectionMode) {

                            toggleFileSelection(
                                    file,
                                    videoBox
                            );

                        } else {

                            openVideo(file);
                        }
                    }
            );


            galleryPane.getChildren()
                    .add(card);


        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // DOSYA SEÇME
    // =========================================================

    private void toggleFileSelection(
            Path file,
            StackPane box) {

        if (selectedFiles.contains(file)) {

            selectedFiles.remove(file);

            box.setStyle(
                    "-fx-background-color: #303030;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-radius: 12;" +
                            "-fx-border-color: #555555;" +
                            "-fx-border-width: 1;"
            );

        } else {

            selectedFiles.add(file);

            box.setStyle(
                    "-fx-background-color: #303030;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-radius: 12;" +
                            "-fx-border-color: #4A90E2;" +
                            "-fx-border-width: 4;"
            );
        }


        updateSelectionButtons();
    }


    // =========================================================
    // SEÇİM BUTONLARINI GÜNCELLE
    // =========================================================

    private void updateSelectionButtons() {

        boolean hasSelection =
                !selectedFiles.isEmpty();


        deleteButton.setDisable(
                !hasSelection
        );

        addToAlbumButton.setDisable(
                !hasSelection
        );
    }


    // =========================================================
    // SEÇİLENLERİ ALBÜME EKLE
    // =========================================================

    @FXML
    private void addSelectedToAlbum() {

        if (selectedFiles.isEmpty()) {

            showMessage(
                    "Önce fotoğraf veya video seçin."
            );

            return;
        }


        try {

            Files.createDirectories(
                    albumsFolder
            );


            List<String> albumNames;

            try (var stream =
                         Files.list(albumsFolder)) {

                albumNames =
                        stream
                                .filter(
                                        Files::isDirectory
                                )
                                .map(
                                        path ->
                                                path.getFileName()
                                                        .toString()
                                )
                                .toList();
            }


            if (albumNames.isEmpty()) {

                showMessage(
                        "Önce bir albüm oluşturmalısınız."
                );

                return;
            }


            ChoiceDialog<String> dialog =
                    new ChoiceDialog<>(
                            albumNames.get(0),
                            albumNames
                    );


            dialog.setTitle(
                    "Albüm Seç"
            );

            dialog.setHeaderText(
                    "Seçilen öğeler hangi albüme eklensin?"
            );

            dialog.setContentText(
                    "Albüm:"
            );


            Optional<String> result =
                    dialog.showAndWait();


            if (result.isEmpty()) {

                return;
            }


            String selectedAlbum =
                    result.get();


            Path albumPath =
                    albumsFolder.resolve(
                            selectedAlbum
                    );


            // =================================================
            // KOPYALAMA
            // =================================================
            // Gallery'deki orijinal dosyaya DOKUNMUYORUZ.
            // Sadece albüme kopyalıyoruz.

            for (Path file : selectedFiles) {

                Path destination =
                        albumPath.resolve(
                                file.getFileName()
                        );


                Files.copy(
                        file,
                        destination,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }


            showMessage(
                    selectedFiles.size() +
                            " öğe \"" +
                            selectedAlbum +
                            "\" albümüne eklendi."
            );


            selectedFiles.clear();

            updateSelectionButtons();

            loadGallery();


        } catch (IOException e) {

            e.printStackTrace();

            showMessage(
                    "Öğeler albüme eklenirken hata oluştu."
            );
        }
    }


    // =========================================================
    // SEÇİLENLERİ SİL
    // =========================================================

    @FXML
    private void deleteSelected() {

        if (selectedFiles.isEmpty()) {

            showMessage(
                    "Önce fotoğraf veya video seçin."
            );

            return;
        }


        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );


        alert.setTitle(
                "Dosyaları Sil"
        );

        alert.setHeaderText(
                null
        );

        alert.setContentText(
                selectedFiles.size() +
                        " öğeyi silmek istediğinize emin misiniz?"
        );


        Optional<ButtonType> result =
                alert.showAndWait();


        if (result.isEmpty()
                || result.get() != ButtonType.OK) {

            return;
        }


        for (Path file : selectedFiles) {

            try {

                Files.deleteIfExists(file);

            } catch (IOException e) {

                e.printStackTrace();
            }
        }


        selectedFiles.clear();

        updateSelectionButtons();

        loadGallery();
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
    // TARİH
    // =========================================================

    private Label createDateLabel(
            Path file) {

        Label dateLabel =
                new Label();


        try {

            LocalDateTime date =
                    LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(file)
                                    .toInstant(),
                            ZoneId.systemDefault()
                    );


            dateLabel.setText(
                    dateFormatter.format(date)
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


    // =========================================================
    // FOTOĞRAFI AÇ
    // =========================================================

    private void openImage(Path file) {

        Image image =
                new Image(
                        file.toUri().toString()
                );


        ImageView imageView =
                new ImageView(image);


        imageView.setPreserveRatio(true);

        imageView.setFitWidth(1100);

        imageView.setFitHeight(750);

        imageView.setSmooth(true);


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
    // VİDEOYU AÇ
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


            mediaView.setPreserveRatio(true);

            mediaView.setFitWidth(1100);

            mediaView.setFitHeight(650);


            // =================================================
            // PLAY / PAUSE
            // =================================================

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


            // =================================================
            // BAŞA SAR
            // =================================================

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


            // =================================================
            // SES
            // =================================================

            Label volumeLabel =
                    new Label("🔊");

            volumeLabel.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-size: 16px;"
            );

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
                            volumeSlider.valueProperty()
                    );


            // =================================================
            // İLERLEME
            // =================================================

            Slider progressSlider =
                    new Slider();


            progressSlider.setPrefWidth(
                    600
            );


            // =================================================
            // SÜRE
            // =================================================

            Label timeLabel =
                    new Label(
                            "00:00 / 00:00"
                    );


            timeLabel.setStyle(
                    "-fx-text-fill: white;" +
                            "-fx-font-size: 13px;"
            );


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
                                            formatDuration(newTime)
                                                    + " / "
                                                    + formatDuration(
                                                    total
                                            )
                                    );
                                }
                            }
                    );


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


            // =================================================
            // KONTROLLER
            // =================================================

            HBox controls =
                    new HBox(
                            10,
                            restartButton,
                            playButton,
                            progressSlider,
                            timeLabel,

                            volumeSlider

                    );


            controls.setAlignment(
                    Pos.CENTER
            );


            controls.setStyle(
                    "-fx-padding: 12;" +
                            "-fx-background-color: #303030;"
            );


            // =================================================
            // ANA EKRAN
            // =================================================

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


            stage.setScene(scene);

            stage.show();


            // =================================================
            // VİDEO HAZIR
            // =================================================

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


            // =================================================
            // PENCERE KAPANINCA
            // =================================================

            stage.setOnCloseRequest(
                    event ->
                            mediaPlayer.dispose()
            );


        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // SÜRE FORMATLA
    // =========================================================

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