package com.socialthingy.plusf.spectrum;

import com.socialthingy.plusf.spectrum.display.JavaFXBorder;
import com.socialthingy.plusf.spectrum.display.JavaFXDisplay;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.spectrum.remote.NetworkPeer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.Serializable;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Supplier;

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

    public static <T extends Serializable> Timer installStatusLabelUpdater(
        final Label label,
        final Supplier<Optional<NetworkPeer<T>>> currentNetworkPeer
    ) {
        final Timer statusBarTimer = new Timer();
        statusBarTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    final Optional<NetworkPeer<T>> networkPeer = currentNetworkPeer.get();
                    if (!networkPeer.isPresent()) {
                        label.setText("Not connected");
                    }
                    networkPeer.ifPresent(g -> {
                        if (g.awaitingCommunication()) {
                            label.setText("Awaiting communication");
                        } else {
                            final String text = String.format(
                                    "Connected - average delay: %.2f ms, out-of-sequence: %d",
                                    g.getAverageLatency(),
                                    g.getOutOfOrderPacketCount()
                            );
                            label.setText(text);
                        }
                    });
                });
            }
        }, 0L, 5000L);

        return statusBarTimer;
    }
}