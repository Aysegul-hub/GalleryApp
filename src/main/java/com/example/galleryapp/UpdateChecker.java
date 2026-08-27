package com.example.galleryapp;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {

    private static final String GITHUB_API =
            "https://api.github.com/repos/Aysegul-hub/GalleryApp/releases/latest";

    public static void checkForUpdate() {

        Thread thread = new Thread(() -> {

            try {
                HttpClient client = HttpClient.newHttpClient();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(GITHUB_API))
                        .header("Accept", "application/vnd.github+json")
                        .build();

                HttpResponse<String> response =
                        client.send(request, HttpResponse.BodyHandlers.ofString());

                String json = response.body();

                // GitHub'daki son sürümü bul
                Pattern versionPattern =
                        Pattern.compile("\"tag_name\"\\s*:\\s*\"([^\"]+)\"");

                Matcher versionMatcher = versionPattern.matcher(json);

                if (!versionMatcher.find()) {
                    System.out.println("GitHub sürümü bulunamadı.");
                    return;
                }

                String latestVersion =
                        versionMatcher.group(1).replace("v", "");

                System.out.println("Mevcut sürüm: " + Main.APP_VERSION);
                System.out.println("GitHub sürümü: " + latestVersion);

                if (!Main.APP_VERSION.equals(latestVersion)) {

                    // GitHub'daki .exe dosyasının indirme adresini bul
                    Pattern exePattern =
                            Pattern.compile(
                                    "\"browser_download_url\"\\s*:\\s*\"([^\"]+\\.exe)\""
                            );

                    Matcher exeMatcher = exePattern.matcher(json);

                    if (!exeMatcher.find()) {
                        System.out.println("GitHub release içinde .exe bulunamadı.");
                        return;
                    }

                    String downloadUrl = exeMatcher.group(1);

                    Platform.runLater(() -> {

                        Alert alert = new Alert(
                                Alert.AlertType.INFORMATION
                        );

                        alert.setTitle("Gallery App Güncelleme");
                        alert.setHeaderText("Yeni sürüm bulundu!");
                        alert.setContentText(
                                "Yeni bir Gallery App güncellemesi mevcut!\n\n" +
                                        "Mevcut sürüm: " + Main.APP_VERSION + "\n" +
                                        "Yeni sürüm: " + latestVersion +
                                        "\n\nGüncelleme yapmak ister misiniz?"
                        );

                        ButtonType yesButton =
                                new ButtonType("Yes");

                        ButtonType noButton =
                                new ButtonType("No");

                        alert.getButtonTypes().setAll(
                                yesButton,
                                noButton
                        );

                        alert.showAndWait().ifPresent(button -> {

                            if (button == yesButton) {

                                System.out.println(
                                        "Kullanıcı güncellemeyi seçti."
                                );

                                downloadAndUpdate(downloadUrl);

                            } else {

                                System.out.println(
                                        "Kullanıcı güncellemeyi reddetti."
                                );
                            }
                        });
                    });

                } else {

                    System.out.println("Uygulama güncel.");

                }

            } catch (IOException | InterruptedException e) {

                System.out.println(
                        "Güncelleme kontrolü yapılamadı."
                );

                e.printStackTrace();
            }

        });

        thread.setDaemon(true);
        thread.start();
    }


    private static void downloadAndUpdate(String downloadUrl) {

        Thread downloadThread = new Thread(() -> {

            try {

                System.out.println(
                        "Güncelleme indiriliyor..."
                );

                // Geçici klasöre indir
                Path tempDirectory =
                        Files.createTempDirectory("GalleryAppUpdate");

                Path newExe =
                        tempDirectory.resolve("GalleryApp-New.exe");

                HttpClient client =
                        HttpClient.newHttpClient();

                HttpRequest request =
                        HttpRequest.newBuilder()
                                .uri(URI.create(downloadUrl))
                                .build();

                HttpResponse<InputStream> response =
                        client.send(
                                request,
                                HttpResponse.BodyHandlers.ofInputStream()
                        );

                try (InputStream inputStream =
                             response.body()) {

                    Files.copy(
                            inputStream,
                            newExe,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }

                System.out.println(
                        "Güncelleme indirildi."
                );

                // Çalışan EXE'nin yolu
                String currentExe =
                        ProcessHandle.current()
                                .info()
                                .command()
                                .orElse(null);

                if (currentExe == null ||
                        !currentExe.toLowerCase().endsWith(".exe")) {

                    System.out.println(
                            "Güncelleme sadece .exe üzerinden çalıştırıldığında yapılabilir."
                    );

                    Platform.runLater(() -> {

                        Alert alert =
                                new Alert(Alert.AlertType.INFORMATION);

                        alert.setTitle("Gallery App");
                        alert.setHeaderText("Güncelleme indirildi");
                        alert.setContentText(
                                "Güncelleme dosyası indirildi.\n\n" +
                                        "Gerçek güncelleme için uygulamayı .exe dosyasından çalıştırmanız gerekiyor."
                        );

                        alert.showAndWait();
                    });

                    return;
                }

                // Güncelleme scripti oluştur
                Path updateScript =
                        tempDirectory.resolve("update.bat");

                String script =
                        "@echo off\n" +
                                "timeout /t 2 /nobreak > nul\n" +
                                "copy /Y \"" +
                                newExe.toAbsolutePath() +
                                "\" \"" +
                                currentExe +
                                "\"\n" +
                                "start \"\" \"" +
                                currentExe +
                                "\"\n" +
                                "exit\n";

                Files.writeString(
                        updateScript,
                        script
                );

                System.out.println(
                        "Güncelleme başlatılıyor..."
                );

                // Java uygulamasını kapat
                new ProcessBuilder(
                        "cmd",
                        "/c",
                        "start",
                        "",
                        updateScript.toAbsolutePath().toString()
                ).start();

                Platform.runLater(() -> {

                    Alert alert =
                            new Alert(Alert.AlertType.INFORMATION);

                    alert.setTitle("Gallery App");
                    alert.setHeaderText("Güncelleme hazırlanıyor");
                    alert.setContentText(
                            "Gallery App güncellenecek ve yeniden başlatılacak."
                    );

                    alert.showAndWait();

                    Platform.exit();
                });

            } catch (Exception e) {

                System.out.println(
                        "Güncelleme sırasında hata oluştu."
                );

                e.printStackTrace();
            }

        });

        downloadThread.setDaemon(true);
        downloadThread.start();
    }
}