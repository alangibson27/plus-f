package com.socialthingy.plusf.spectrum.dialog;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.Optional;

public class ConnectionDetailsDialog {
    public static Optional<Pair<String, Integer>> getConnectionDetails(final String what) {
        final Dialog<Pair<String, Integer>> dialog = new Dialog<>();
        dialog.setTitle(String.format("Connect to %s", what));
        dialog.setHeaderText(String.format("Enter the host and port of the %s", what));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        final TextField host = new TextField();
        host.setPromptText("Hostname");
        final TextField port = new TextField();
        port.setPromptText("Port");

        grid.add(new Label("Hostname:"), 0, 0);
        grid.add(host, 1, 0);
        grid.add(new Label("Port:"), 0, 1);
        grid.add(port, 1, 1);

        final DecimalFormat format = new DecimalFormat("#");
        port.setTextFormatter(new TextFormatter<>(c -> {
            if (c.getControlNewText().isEmpty()) {
                return c;
            }

            final ParsePosition parsePosition = new ParsePosition(0);
            final Object object = format.parse(c.getControlNewText(), parsePosition);

            if (object == null || parsePosition.getIndex() < c.getControlNewText().length()) {
                return null;
            } else {
                return c;
            }
        }));

        final Node okButton = dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        host.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() && port.getText().trim().isEmpty());
        });

        port.textProperty().addListener((observable, oldValue, newValue) -> {
            okButton.setDisable(newValue.trim().isEmpty() && host.getText().trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(host::requestFocus);
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(host.getText(), Integer.parseInt(port.getText()));
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
