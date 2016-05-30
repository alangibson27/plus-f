package com.socialthingy.plusf.spectrum;

import com.socialthingy.plusf.spectrum.dialog.ContactInfoFinder;
import com.socialthingy.plusf.spectrum.display.JavaFXBorder;
import com.socialthingy.plusf.spectrum.display.JavaFXDisplay;
import com.socialthingy.plusf.spectrum.input.KempstonJoystick;
import com.socialthingy.plusf.spectrum.remote.GuestState;
import com.socialthingy.plusf.spectrum.remote.NetworkPeer;
import com.socialthingy.plusf.spectrum.remote.SpectrumState;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Optional;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicLong;

import static com.socialthingy.plusf.spectrum.UIBuilder.buildUI;
import static com.socialthingy.plusf.spectrum.UIBuilder.installStatusLabelUpdater;
import static com.socialthingy.plusf.spectrum.UIBuilder.registerMenuItem;
import static com.socialthingy.plusf.spectrum.dialog.ConnectionDetailsDialog.getConnectionDetails;
import static com.socialthingy.plusf.spectrum.remote.GuestState.ABSENT;
import static javafx.scene.input.KeyCode.*;

public class JavaFXGuest extends Application {
    private static final int LOCAL_PORT = 7001;

    private final JavaFXDisplay display;
    private final JavaFXBorder border;
    private final Label statusLabel;
    private MenuItem connectItem;
    private MenuItem disconnectItem;
    private Optional<NetworkPeer<SpectrumState, GuestState>> guestRelay = Optional.empty();
    private final int[] memory = new int[0x10000];
    private final KempstonJoystick kempstonJoystick = new KempstonJoystick();
    private SpectrumState lastHostData;
    private final AtomicLong timestamper = new AtomicLong(0);

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
        buildUI(primaryStage, display, border, statusLabel, getMenuBar());

        final Timer statusBarTimer = installStatusLabelUpdater(statusLabel, () -> guestRelay);
        primaryStage.setOnCloseRequest(we -> {
            statusBarTimer.cancel();
            guestRelay.ifPresent(g -> g.disconnect());
        });

        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ALT) {
                e.consume();
            }
        });

        primaryStage.setTitle("+F Spectrum Guest");
        primaryStage.show();
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, this::handleKeypress);
        primaryStage.addEventHandler(KeyEvent.KEY_RELEASED, this::handleKeypress);

        final AnimationTimer screenUpdater = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastHostData != null) {
                    System.arraycopy(lastHostData.getMemory(), 0x4000, memory, 0x4000, 0x1b00);
                    Platform.runLater(() -> {
                        border.refresh(lastHostData.getBorderLines());
                        display.refresh(memory, lastHostData.isFlashActive());
                    });
                }
            }
        };
        screenUpdater.start();
    }

    private void handleKeypress(final KeyEvent ke) {
        kempstonJoystick.handle(ke);
        guestRelay.ifPresent(g ->
            g.sendDataToPartner(new GuestState(0x1f, ABSENT, kempstonJoystick.getPortValue()))
        );
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
                        GuestState::serialise,
                        SpectrumState::deserialise,
                        timestamper::getAndIncrement,
                        LOCAL_PORT,
                        new InetSocketAddress(result.get().getKey(), result.get().getValue())
                    )
                );

                guestRelay.ifPresent(g -> {
                    g.sendDataToPartner(new GuestState(ABSENT, ABSENT, ABSENT));
                    connectItem.setDisable(true);
                    disconnectItem.setDisable(false);
                });
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
        this.lastHostData = hostData;
    }
}

