package com.socialthingy.plusf.spectrum.dialog;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.util.Optional;

import static javafx.scene.control.Alert.AlertType.WARNING;

public class ErrorDialog {

    public static void show(final String title, final String message, final Optional<Throwable> exception) {
        if (Platform.isFxApplicationThread()) {
            showDialog(title, message);
        } else {
            Platform.runLater(() -> showDialog(title, message));
        }

        exception.ifPresent(Throwable::printStackTrace);
    }

    private static void showDialog(final String title, final String message) {
        final Alert infoDialog = new Alert(WARNING);
        infoDialog.setHeaderText(title);
        infoDialog.setContentText(message);
        infoDialog.show();
    }

}
