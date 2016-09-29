package com.socialthingy.plusf.spectrum;

import com.socialthingy.plusf.spectrum.display.JavaFXDoubleSizeDisplay;
import com.socialthingy.plusf.spectrum.network.PeerAdapter;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

public class UIBuilder {

    public static Pane buildUI(
        final Stage primaryStage,
        final JavaFXDoubleSizeDisplay display,
        final Node statusArea,
        final MenuBar menuBar
    ) {
        try (InputStream iconStream = UIBuilder.class.getResourceAsStream("/plus-f.png")) {
            primaryStage.getIcons().add(new Image(iconStream));
        } catch (IOException ex) {
            // NOP.
        }

        BorderPane root = new BorderPane();
        root.setCenter(display.getDisplay());
        root.setTop(menuBar);
        root.setBottom(statusArea);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        return root;
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

    public static CheckMenuItem registerCheckMenuItem(
        final Menu menu,
        final String name,
        final Optional<KeyCode> accelerator,
        final EventHandler<ActionEvent> action
    ) {
        final CheckMenuItem item = new CheckMenuItem(name);
        item.setOnAction(action);
        accelerator.ifPresent(a ->
                item.setAccelerator(new KeyCodeCombination(a, KeyCombination.ALT_DOWN))
        );
        menu.getItems().add(item);
        return item;
    }

    public static Timer installStatusLabelUpdater(
        final Label label,
        final PeerAdapter<?> peer
    ) {
        final Timer statusBarTimer = new Timer();
        statusBarTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (!peer.isConnected()) {
                        label.setText("Not connected");
                        label.setTextFill(Color.BLACK);
                    } else {
                        peer.updateStatistics();
                        final String text = String.format(
                            "Delay: %.2f ms, unordered: %d, size: %.2f K",
                            peer.getAverageLatency(),
                            peer.getOutOfOrderPacketCount(),
                            peer.getAveragePacketSize()
                        );
                        label.setText(text);
                        label.setTextFill(peer.getConnectionHealth());
                    }
                });
            }
        }, 0L, 2500L);

        return statusBarTimer;
    }
}
