package com.socialthingy.qaopm.spectrum;

import com.socialthingy.qaopm.spectrum.remote.GuestState;
import com.socialthingy.qaopm.spectrum.remote.NetworkPeer;
import com.socialthingy.qaopm.spectrum.remote.SpectrumState;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

import static com.socialthingy.qaopm.spectrum.UIBuilder.getConnectionDetails;
import static com.socialthingy.qaopm.spectrum.UIBuilder.registerMenuItem;
import static javafx.scene.input.KeyCode.*;

public class JavaFXGuest extends Application {
    private static final int LOCAL_PORT = 7001;

    private final JavaFXDisplay display;
    private final JavaFXBorder border;
    private final Label statusLabel;
    private MenuItem connectItem;
    private MenuItem disconnectItem;
    private Optional<NetworkPeer<SpectrumState>> guestRelay = Optional.empty();
    private final int[] memory = new int[0x10000];

    public static void main(final String ... args) {
        Application.launch(args);
    }

    public JavaFXGuest() {
        display = new JavaFXDisplay();
        border = new JavaFXBorder();
        statusLabel = new Label("Not connected to computer");
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        UIBuilder.buildUI(primaryStage, display, border, statusLabel, getMenuBar());

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (!guestRelay.isPresent()) {
                        statusLabel.setText("Not connected to computer");
                    }
                    guestRelay.ifPresent(g -> {
                        if (g.awaitingCommunication()) {
                            statusLabel.setText("Awaiting communication from computer");
                        } else {
                            final String text = String.format(
                                    "Connected - average delay: %f ms, out-of-sequence: %d",
                                    g.getAverageLatency(),
                                    g.getOutOfOrderPacketCount()
                            );
                            statusLabel.setText(text);
                        }
                    });
                });
            }
        }, 0L, 5000L);

        primaryStage.setTitle("QAOPM Spectrum Emulator - GUEST");
        primaryStage.show();
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, this::keyPressed);
        primaryStage.addEventHandler(KeyEvent.KEY_RELEASED, this::keyReleased);
    }

    private void keyPressed(final KeyEvent ke) {
    }

    private void keyReleased(final KeyEvent ke) {
    }

    private MenuBar getMenuBar() {
        final MenuBar menuBar = new MenuBar();

        final Menu fileMenu = new Menu("File");
        registerMenuItem(fileMenu, "Quit", Optional.of(Q), ae -> System.exit(0));

        final Menu networkMenu = new Menu("Network");
        registerMenuItem(networkMenu, "Get contact info ...", Optional.of(I), ae -> ContactInfoFinder.getContactInfo(LOCAL_PORT));
        connectItem = registerMenuItem(networkMenu, "Connect to computer ...", Optional.of(C), this::connectToComputer);
        disconnectItem = registerMenuItem(networkMenu, "Disconnect from computer", Optional.of(D), this::disconnectFromComputer);
        disconnectItem.setDisable(true);

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(networkMenu);
        return menuBar;
    }

    private void connectToComputer(final ActionEvent ae) {
        final Optional<Pair<String, Integer>> result = getConnectionDetails("computer");

        if (result.isPresent()) {
            try {
                guestRelay = Optional.of(
                    new NetworkPeer<>(
                        this::update,
                        System::currentTimeMillis,
                        LOCAL_PORT,
                        new InetSocketAddress(result.get().getKey(), result.get().getValue())
                    )
                );

                guestRelay.get().sendDataToPartner(new GuestState(-1, -1, -1));

                connectItem.setDisable(true);
                disconnectItem.setDisable(false);
            } catch (SocketException e) {
                final Alert errorDialog = new Alert(
                    Alert.AlertType.ERROR,
                    "Unable to connect to computer.",
                    ButtonType.OK
                );

                errorDialog.showAndWait();
            }
        }
    }

    private void disconnectFromComputer(final ActionEvent ae) {
        guestRelay.ifPresent(r -> {
            r.disconnect();
            guestRelay = Optional.empty();
            connectItem.setDisable(false);
            disconnectItem.setDisable(true);
        });
    }

    private void update(final SpectrumState hostData) {
        System.arraycopy(hostData.getScreen(), 0x0000, memory, 0x4000, 0x1b00);
        Platform.runLater(() -> {
            border.refresh(hostData.getBorderLines());
            display.refresh(memory, hostData.isFlashActive());
        });
    }
}

