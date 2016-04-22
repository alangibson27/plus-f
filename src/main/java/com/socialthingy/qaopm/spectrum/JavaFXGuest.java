package com.socialthingy.qaopm.spectrum;

import com.socialthingy.qaopm.spectrum.remote.Guest;
import com.socialthingy.qaopm.spectrum.remote.SpectrumState;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
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

    private final JavaFXDisplay display;
    private final JavaFXBorder border;
    private final Label statusLabel;
    private MenuItem connectItem;
    private MenuItem disconnectItem;
    private Optional<Guest> guestRelay = Optional.empty();

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
                        final String text = String.format(
                            "Connected - average delay: %f ms, out-of-sequence: %d",
                            g.getAverageLatency(),
                            g.getOutOfOrderPacketCount()
                        );
                        statusLabel.setText(text);
                    });
                });
            }
        }, 0L, 5000L);

        primaryStage.setTitle("QAOPM Spectrum Emulator - GUEST");
        primaryStage.show();
//        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, guest::sendKeypress);
//        primaryStage.addEventHandler(KeyEvent.KEY_RELEASED, guest::sendKeypress);
    }

    private MenuBar getMenuBar() {
        final MenuBar menuBar = new MenuBar();

        final Menu fileMenu = new Menu("File");
        registerMenuItem(fileMenu, "Quit", Optional.of(Q), ae -> System.exit(0));

        final Menu networkMenu = new Menu("Network");
        registerMenuItem(networkMenu, "Get contact info ...", Optional.of(I), ContactInfoFinder::getContactInfo);
        connectItem = registerMenuItem(networkMenu, "Connect to computer ...", Optional.of(C), this::connectToComputer);
        disconnectItem = registerMenuItem(networkMenu, "Disconnect from computer", Optional.of(D), this::disconnectFromComputer);
        disconnectItem.setDisable(true);

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(networkMenu);
        return menuBar;
    }

    private void connectToComputer(final ActionEvent ae) {
        final Optional<Pair<String, Integer>> result = getConnectionDetails();

        if (result.isPresent()) {
            try {
                guestRelay = Optional.of(
                    new Guest(
                        System::currentTimeMillis,
                        new InetSocketAddress(result.get().getKey(), result.get().getValue()),
                        this::update
                    )
                );

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
            r.disconnectFromHost();
            guestRelay = Optional.empty();
            connectItem.setDisable(true);
            disconnectItem.setDisable(false);
        });
    }

    private void update(final SpectrumState hostData) {
        final int[] memory = new int[0x10000];
        for (int i = 0; i < hostData.getScreen().length; i++) {
            memory[16384 + i] = (int) hostData.getScreen()[i] & 0xff;
        }
        border.refresh(hostData.getBorderLines());
        display.refresh(memory, hostData.isFlashActive());
    }
}

