package com.socialthingy.qaopm.spectrum;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.Optional;

public class UIBuilder {
    public static final int BORDER = 16;
    public static final int SCREEN_WIDTH = 256;
    public static final int DISPLAY_WIDTH = SCREEN_WIDTH + (BORDER * 2);
    public static final int DISPLAY_HEIGHT = ULA.SCREEN_HEIGHT + (BORDER * 2);

    public static void buildUI(
        final Stage primaryStage,
        final JavaFXDisplay display,
        final JavaFXBorder border,
        final Label statusLabel,
        final MenuBar menuBar
    ) {
        final ImageView borderImage = new ImageView(border.getBorder());
        borderImage.setFitWidth(DISPLAY_WIDTH * 2);
        borderImage.setFitHeight(DISPLAY_HEIGHT * 2);

        final ImageView screenImage = new ImageView(display.getScreen());
        screenImage.setFitHeight(ULA.SCREEN_HEIGHT * 2);
        screenImage.setFitWidth(SCREEN_WIDTH * 2);

        final StackPane sp = new StackPane(borderImage, screenImage);

        BorderPane root = new BorderPane();
        root.setCenter(sp);
        root.setTop(menuBar);
        root.setBottom(statusLabel);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
    }

    public static MenuItem registerMenuItem(
        final Menu menu,
        final String name,
        final Optional<KeyCode> accelerator,
        final EventHandler<ActionEvent> action
    ) {
        final MenuItem item = new MenuItem(name);
        item.setOnAction(action);
        accelerator.ifPresent(a ->
            item.setAccelerator(new KeyCodeCombination(a, KeyCombination.ALT_DOWN))
        );
        menu.getItems().add(item);
        return item;
    }

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

        Platform.runLater(() -> host.requestFocus());
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return new Pair<>(host.getText(), Integer.parseInt(port.getText()));
            }
            return null;
        });

        return dialog.showAndWait();
    }

}
