package com.socialthingy.plusf.spectrum;

import com.socialthingy.plusf.spectrum.dialog.CancelableProgressDialog;
import com.socialthingy.plusf.spectrum.dialog.ErrorDialog;
import com.socialthingy.plusf.spectrum.display.JavaFXDoubleSizeDisplay;
import com.socialthingy.plusf.spectrum.input.JavaFXJoystick;
import com.socialthingy.plusf.spectrum.remote.*;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Optional;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicLong;

import static com.socialthingy.plusf.spectrum.UIBuilder.buildUI;
import static com.socialthingy.plusf.spectrum.UIBuilder.installStatusLabelUpdater;
import static com.socialthingy.plusf.spectrum.UIBuilder.registerMenuItem;
import static com.socialthingy.plusf.spectrum.dialog.CodenameDialog.getCodename;
import static javafx.scene.input.KeyCode.*;

public class JavaFXGuest extends Application {
    private static final int LOCAL_PORT = Settings.GUEST_PORT;

    private final JavaFXGuestDoubleSizeDisplay display;
    private final Label statusLabel;
    private MenuItem easyConnectItem;
    private MenuItem disconnectItem;
    private Optional<NetworkPeer<EmulatorState, GuestState>> guestRelay = Optional.empty();
    private final int[] memory = new int[0x10000];
    private final JavaFXJoystick javaFXJoystick = new JavaFXJoystick();
    private EmulatorState lastHostData;
    private final AtomicLong timestamper = new AtomicLong(0);
    private DatagramSocket socket;

    public static void main(final String ... args) {
        Application.launch(args);
    }

    public JavaFXGuest() {
        display = new JavaFXGuestDoubleSizeDisplay();
        statusLabel = new Label("Not connected to computer");
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        buildUI(primaryStage, display, statusLabel, getMenuBar());

        final Timer statusBarTimer = installStatusLabelUpdater(statusLabel, () -> guestRelay);
        primaryStage.setOnCloseRequest(we -> {
            statusBarTimer.cancel();
            guestRelay.ifPresent(NetworkPeer::disconnect);
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
            int count = 0;

            @Override
            public void handle(long now) {
                if (lastHostData != null) {
                    System.arraycopy(lastHostData.getMemory(), 0x4000, memory, 0x4000, 0x1b00);
                    Platform.runLater(() -> {
                        display.setBorderLines(lastHostData.getBorderLines());
                        display.render(memory, lastHostData.isFlashActive(), true);
                        display.refreshScreen();
                        display.refreshBorder();
                    });
                }

                if (count % 5 == 0) {
                    guestRelay.ifPresent(g ->
                        g.sendDataToPartner(new GuestState(GuestStateType.JOYSTICK_STATE.ordinal(), javaFXJoystick.serialise()))
                    );
                }

                count++;
            }
        };
        screenUpdater.start();
    }

    @Override
    public void stop() {
        System.out.println("Closing");
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        CancelableProgressDialog.shutdown();
    }

    private void handleKeypress(final KeyEvent ke) {
        javaFXJoystick.handle(ke);
        if (ke.isConsumed()) {
            guestRelay.ifPresent(g ->
                g.sendDataToPartner(new GuestState(GuestStateType.JOYSTICK_STATE.ordinal(), javaFXJoystick.serialise()))
            );
        }
    }

    private MenuBar getMenuBar() {
        final MenuBar menuBar = new MenuBar();

        final Menu fileMenu = new Menu("File");
        registerMenuItem(fileMenu, "Quit", Optional.of(Q), ae -> System.exit(0));

        final Menu networkMenu = new Menu("Network");
        easyConnectItem = registerMenuItem(networkMenu, "Connect to computer", Optional.of(C), this::easyConnectToComputer);
        disconnectItem = registerMenuItem(networkMenu, "Disconnect from computer", Optional.of(D), this::disconnectFromComputer);
        disconnectItem.setDisable(true);

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(networkMenu);
        return menuBar;
    }

    private DatagramSocket getSocket() throws SocketException {
        if (this.socket == null) {
            socket = new DatagramSocket(LOCAL_PORT);
            socket.setSoTimeout(30000);
        }

        return this.socket;
    }

    private void easyConnectToComputer(final ActionEvent ae) {
        final Optional<String> codename = getCodename("emulator");
        codename.ifPresent(cn -> {
            try {
                final Task<SocketAddress> computerAddress = new GuestConnectionSetup(getSocket(), cn);
                CancelableProgressDialog.show(
                    computerAddress,
                    "Connecting to emulator ... please wait",
                    "Connecting to Emulator",
                    addr -> guestRelay = relayTo(addr)
                );
            } catch (SocketException e) {
                ErrorDialog.show(
                    "Connection Error",
                    "Unable to connect to emulator. Please try again later.",
                    Optional.of(e)
                );
            }
        });
    }

    private Optional<NetworkPeer<EmulatorState, GuestState>> relayTo(SocketAddress sa) {
        try {
            easyConnectItem.setDisable(true);
            disconnectItem.setDisable(false);

            return Optional.of(
                    new NetworkPeer<>(
                            this::update,
                            GuestState::serialise,
                            EmulatorState::deserialise,
                            timestamper::getAndIncrement,
                            getSocket(),
                            sa
                    )
            );
        } catch (SocketException ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }

    private void disconnectFromComputer(final ActionEvent ae) {
        guestRelay.ifPresent(r -> {
            r.disconnect();
            guestRelay = Optional.empty();
            easyConnectItem.setDisable(false);
            disconnectItem.setDisable(true);
        });
    }

    private void update(final EmulatorState hostData) {
        this.lastHostData = hostData;
    }

    private class JavaFXGuestDoubleSizeDisplay extends JavaFXDoubleSizeDisplay {
        public void setBorderLines(final int[] borderLines) {
            System.arraycopy(borderLines, 0, this.borderLines, 0, borderLines.length);
        }
    }
}

