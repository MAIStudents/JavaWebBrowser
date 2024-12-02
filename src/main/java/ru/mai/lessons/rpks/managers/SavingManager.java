package ru.mai.lessons.rpks.managers;

import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class SavingManager {
    private static final Logger logger = Logger.getLogger(SavingManager.class.getName());
    public static FileChooser createFileChooser() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить страницу как...");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP файлы", "*.zip"));
        fileChooser.setInitialFileName("page.zip");
        return fileChooser;
    }

    public static void createZip(File selectedFile, String htmlCode) {
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(new FileOutputStream(selectedFile));
             ByteArrayInputStream inputStream = new ByteArrayInputStream(htmlCode.getBytes())) {
            ZipEntry entry = new ZipEntry("page.html");
            zipOutputStream.putNextEntry(entry);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                zipOutputStream.write(buffer, 0, len);
            }

            zipOutputStream.closeEntry();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error occurred while creating ZIP archive", e);
        }
    }
}
