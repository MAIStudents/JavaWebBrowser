package ru.mai.lessons.rpks.helpClasses;

import javafx.stage.Window;

import java.io.File;

public class DirectoryChooser {
    public String getPathFromDirectoryChooser(Window primaryStage) {
        javafx.stage.DirectoryChooser directoryChooser = new javafx.stage.DirectoryChooser();
        directoryChooser.setTitle("Select a directory to place a file in");
        directoryChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File dir = directoryChooser.showDialog(primaryStage);
        return dir.toPath().toString();
    }
}