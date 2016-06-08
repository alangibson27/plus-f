package com.socialthingy.plusf.spectrum.dialog;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import static javafx.scene.control.Alert.AlertType.WARNING;

public class ErrorDialog {

    public static void show(final String title, final String message, final Optional<Throwable> exception) {
        if (Platform.isFxApplicationThread()) {
            showDialog(title, exception.map(Throwable::getMessage).orElse(message));
        } else {
            Platform.runLater(() -> showDialog(title, message));
        }

        exception.ifPresent(Throwable::printStackTrace);
    }

    private static void showDialog(final String title, final String message) {
        final Alert infoDialog = new Alert(WARNING);
        infoDialog.setHeaderText(title);
        infoDialog.setContentText(message);
        infoDialog.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        infoDialog.show();
    }

}
