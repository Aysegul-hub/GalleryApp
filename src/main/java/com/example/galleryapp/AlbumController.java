package com.example.galleryapp;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import java.util.Comparator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class AlbumController {

    // =========================================================
    // FXML
    // =========================================================

    @FXML
    private FlowPane albumPane;

    @FXML
    private Label emptyLabel;


    // =========================================================
    // ALBÜMLERİN BULUNACAĞI KLASÖR
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

            Files.createDirectories(albumsFolder);

            loadAlbums();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    // =========================================================
    // ALBÜMLERİ YÜKLE
    // =========================================================

    private void loadAlbums() {

        try {

            Files.createDirectories(albumsFolder);

            List<Path> albums;

            try (var stream = Files.list(albumsFolder)) {

                albums = stream
                        .filter(Files::isDirectory)
                        .toList();
            }


            albumPane.getChildren().clear();


            boolean empty = albums.isEmpty();

            emptyLabel.setVisible(empty);
            emptyLabel.setManaged(empty);


            for (Path album : albums) {

                createAlbumCard(album);
            }


        } catch (IOException e) {

            e.printStackTrace();
        }
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
    // YENİ ALBÜM OLUŞTUR
    // =========================================================

    @FXML
    public void createAlbum() {

        TextInputDialog dialog =
                new TextInputDialog();

        dialog.setTitle("Yeni Albüm");
        dialog.setHeaderText("Yeni bir albüm oluştur");
        dialog.setContentText("Albüm adı:");


        Optional<String> result =
                dialog.showAndWait();


        if (result.isEmpty()) {
            return;
        }


        String albumName =
                result.get().trim();


        if (albumName.isEmpty()) {

            showMessage(
                    "Albüm adı boş bırakılamaz."
            );

            return;
        }


        try {

            Path albumPath =
                    albumsFolder.resolve(
                            albumName
                    );


            if (Files.exists(albumPath)) {

                showMessage(
                        "Bu isimde bir albüm zaten var."
                );

                return;
            }


            Files.createDirectory(
                    albumPath
            );


            loadAlbums();


        } catch (IOException e) {

            e.printStackTrace();

            showMessage(
                    "Albüm oluşturulurken bir hata oluştu."
            );
        }
    }


    // =========================================================
    // ALBÜM KARTI
    // =========================================================

    private void createAlbumCard(Path album) {

        // -----------------------------------------------------
        // 4'LÜ ÖNİZLEME
        // -----------------------------------------------------

        GridPane preview =
                new GridPane();

        preview.setHgap(2);
        preview.setVgap(2);


        preview.setPrefSize(
                230,
                170
        );


        List<Path> mediaFiles =
                getMediaFiles(album);


        for (int i = 0; i < 4; i++) {

            StackPane cell =
                    new StackPane();

            cell.setPrefSize(
                    114,
                    84
            );


            cell.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 6;"
            );


            if (i < mediaFiles.size()) {

                Path media =
                        mediaFiles.get(i);

                Image image =
                        createPreviewImage(media);

                if (image != null) {

                    ImageView imageView =
                            new ImageView(image);

                    imageView.setFitWidth(114);
                    imageView.setFitHeight(84);

                    imageView.setPreserveRatio(false);
                    imageView.setSmooth(true);

                    cell.getChildren().add(imageView);
                }

            } else {

                Label emptyCell =
                        new Label("＋");

                emptyCell.setStyle(
                        "-fx-text-fill: #bbbbbb;" +
                                "-fx-font-size: 24px;"
                );

                cell.getChildren().add(emptyCell);
            }


            int column =
                    i % 2;

            int row =
                    i / 2;


            preview.add(
                    cell,
                    column,
                    row
            );
        }


        // -----------------------------------------------------
        // ALBÜM ADI
        // -----------------------------------------------------

        Label nameLabel =
                new Label(
                        album.getFileName()
                                .toString()
                );


        nameLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: white;"
        );


        // -----------------------------------------------------
        // OLUŞTURULMA TARİHİ
        // -----------------------------------------------------

        Label dateLabel =
                new Label(
                        getCreationDate(album)
                );


        dateLabel.setStyle(
                "-fx-font-size: 12px;" +
                        "-fx-text-fill: #888888;"
        );


        // =====================================================
        // ALBÜM SİL BUTONU
        // =====================================================

        Button deleteButton =
                new Button("Albümü Sil");


        deleteButton.setPrefWidth(
                214
        );


        deleteButton.setStyle(
                "-fx-background-color: #3A3A3A;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;" +
                        "-fx-border-color: #555555;" +
                        "-fx-cursor: hand;"
        );


        // -----------------------------------------------------
        // ALBÜM SİLME İŞLEMİ
        // -----------------------------------------------------

        deleteButton.setOnAction(event -> {

            event.consume();

            deleteAlbum(album);
        });


        // -----------------------------------------------------
        // ALBÜM KARTI
        // -----------------------------------------------------

        VBox card =
                new VBox(
                        8,
                        preview,
                        nameLabel,
                        dateLabel,
                        deleteButton
                );


        card.setPrefWidth(
                230
        );


        card.setAlignment(
                Pos.CENTER_LEFT
        );


        card.setStyle(
                "-fx-background-color: #303030;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 8;" +
                        "-fx-border-color: #555555;" +
                        "-fx-border-radius: 12;"
        );


        // -----------------------------------------------------
        // TIKLANINCA ALBÜMÜ AÇ
        // -----------------------------------------------------

        card.setOnMouseClicked(event -> {

            System.out.println(
                    "KART TIKLANDI: " + album.getFileName()
            );

            openAlbum(album);

            event.consume();
        });


        albumPane.getChildren()
                .add(card);
    }


    // =========================================================
    // ALBÜMÜ SİL
    // =========================================================

    // =========================================================
// ALBÜMÜ SİL
// =========================================================

    private void deleteAlbum(Path album) {

        Alert alert =
                new Alert(
                        Alert.AlertType.CONFIRMATION
                );

        alert.setTitle(
                "Albümü Sil"
        );

        alert.setHeaderText(
                "\"" +
                        album.getFileName().toString() +
                        "\" albümü silinsin mi?"
        );

        alert.setContentText(
                "Albümdeki fotoğraf ve videolar silinecek.\n" +
                        "Gallery'deki orijinal medyalar silinmeyecek."
        );

        Optional<ButtonType> result =
                alert.showAndWait();

        if (result.isEmpty()
                || result.get() != ButtonType.OK) {

            return;
        }


        try {

            // =====================================================
            // ALBÜMÜN İÇİNDEKİ TÜM DOSYA VE KLASÖRLERİ SİL
            // =====================================================

            if (Files.exists(album)) {

                try (var stream = Files.walk(album)) {

                    List<Path> paths =
                            stream
                                    .sorted(
                                            Comparator.reverseOrder()
                                    )
                                    .toList();

                    for (Path path : paths) {

                        Files.deleteIfExists(path);
                    }
                }
            }


            // =====================================================
            // LİSTEYİ YENİLE
            // =====================================================

            loadAlbums();


            showMessage(
                    "\"" +
                            album.getFileName().toString() +
                            "\" albümü silindi."
            );


        } catch (IOException e) {

            e.printStackTrace();

            showMessage(
                    "Albüm silinirken bir hata oluştu.\n\n" +
                            "Hata: " +
                            e.getMessage()
            );
        }
    }


    // =========================================================
    // ALBÜMDEKİ FOTOĞRAF / VİDEOLAR
    // =========================================================

    private List<Path> getMediaFiles(
            Path album) {

        try {

            try (var stream =
                         Files.list(album)) {

                return stream

                        .filter(
                                Files::isRegularFile
                        )

                        .filter(
                                path ->
                                        isImage(
                                                path
                                                        .getFileName()
                                                        .toString()
                                                        .toLowerCase()
                                        )
                                                ||
                                                isVideo(
                                                        path
                                                                .getFileName()
                                                                .toString()
                                                                .toLowerCase()
                                                )
                        )

                        .toList();
            }

        } catch (IOException e) {

            e.printStackTrace();

            return List.of();
        }
    }


    // =========================================================
    // ÖNİZLEME FOTOĞRAFI
    // =========================================================

    private Image createPreviewImage(
            Path file) {

        try {

            String name =
                    file.getFileName()
                            .toString()
                            .toLowerCase();


            // FOTOĞRAF

            if (isImage(name)) {

                return new Image(
                        file.toUri().toString(),
                        114,
                        84,
                        false,
                        true
                );
            }


            // VİDEO

            if (isVideo(name)) {

                return null;
            }


        } catch (Exception e) {

            e.printStackTrace();
        }


        return null;
    }


    // =========================================================
    // ALBÜMÜ AÇ
    // =========================================================

    private void openAlbum(Path album) {

        try {

            System.out.println(
                    "ALBÜME TIKLANDI: " + album
            );


            FXMLLoader loader =
                    new FXMLLoader(
                            getClass().getResource(
                                    "/com/example/galleryapp/album-media.fxml"
                            )
                    );


            if (loader.getLocation() == null) {

                System.out.println(
                        "HATA: album-media.fxml bulunamadı!"
                );

                showMessage(
                        "album-media.fxml bulunamadı."
                );

                return;
            }


            Scene scene =
                    new Scene(
                            loader.load()
                    );


            AlbumMediaController controller =
                    loader.getController();


            if (controller == null) {

                System.out.println(
                        "HATA: AlbumMediaController oluşturulamadı!"
                );

                showMessage(
                        "AlbumMediaController bulunamadı."
                );

                return;
            }


            controller.setAlbum(
                    album
            );


            Stage stage =
                    (Stage)
                            albumPane
                                    .getScene()
                                    .getWindow();


            stage.setScene(scene);

            stage.show();


        } catch (Exception e) {

            e.printStackTrace();


            showMessage(
                    "Albüm açılırken hata oluştu:\n"
                            + e.getMessage()
            );
        }
    }


    // =========================================================
    // OLUŞTURULMA TARİHİ
    // =========================================================

    private String getCreationDate(
            Path album) {

        try {

            FileTime time =
                    Files.getLastModifiedTime(
                            album
                    );


            LocalDateTime date =
                    LocalDateTime.ofInstant(
                            time.toInstant(),
                            ZoneId.systemDefault()
                    );


            return dateFormatter.format(
                    date
            );


        } catch (IOException e) {

            return "";
        }
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