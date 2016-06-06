package com.socialthingy.plusf.spectrum.dialog;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.Optional;

public class CodenameDialog {
    public static Optional<String> getCodename(final String what) {
        final Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(String.format("Connect to %s", what));
        dialog.setHeaderText(String.format("Enter the codename of the %s", what));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        final TextField codename = new TextField();
        codename.setPromptText("Codename");

        grid.add(new Label("Codename:"), 0, 0);
        grid.add(codename, 1, 0);

        final Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        codename.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() || newValue.trim().length() > 32);
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(codename::requestFocus);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return codename.getText().trim();
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
