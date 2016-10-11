package com.socialthingy.plusf.spectrum;

class JavaFXGuest {}
/*
import com.socialthingy.plusf.spectrum.display.JavaFXDoubleSizeDisplay;
import com.socialthingy.plusf.spectrum.input.JavaFXJoystick;
import com.socialthingy.plusf.spectrum.network.EmulatorState;
import com.socialthingy.plusf.spectrum.network.GuestPeerAdapter;
import com.socialthingy.plusf.spectrum.network.GuestState;
import com.socialthingy.plusf.spectrum.network.GuestStateType;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.Timer;

import static com.socialthingy.plusf.spectrum.UIBuilder.buildUI;
import static com.socialthingy.plusf.spectrum.UIBuilder.installStatusLabelUpdater;
import static com.socialthingy.plusf.spectrum.UIBuilder.registerMenuItem;
import static com.socialthingy.plusf.spectrum.dialog.CodenameDialog.getCodename;
import static javafx.scene.input.KeyCode.*;

public class JavaFXGuest extends Application {
    private final JavaFXGuestDoubleSizeDisplay display;
    private final Label statusLabel;
    private MenuItem easyConnectItem;
    private MenuItem disconnectItem;
    private GuestPeerAdapter guestPeer = new GuestPeerAdapter(this::update);
    private final int[] memory = new int[0x10000];
    private final JavaFXJoystick javaFXJoystick = new JavaFXJoystick();
    private EmulatorState lastHostData;

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

        final Timer statusBarTimer = installStatusLabelUpdater(statusLabel, guestPeer);
        primaryStage.setOnCloseRequest(we -> {
            statusBarTimer.cancel();
            guestPeer.shutdown();
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
                        display.renderMemory(memory, lastHostData.isFlashActive());
                        display.refreshScreen();
                        display.refreshBorder();
                    });
                }

                if (count % 5 == 0 && guestPeer.isConnected()) {
                    guestPeer.send(new GuestState(GuestStateType.JOYSTICK_STATE.ordinal(), javaFXJoystick.serialise()));
                }

                count++;
            }
        };
        screenUpdater.start();
    }

    @Override
    public void stop() {
        System.out.println("Closing");
        guestPeer.shutdown();
    }

    private void handleKeypress(final KeyEvent ke) {
        javaFXJoystick.handle(ke);
        if (ke.isConsumed() && guestPeer.isConnected()) {
            guestPeer.send(new GuestState(GuestStateType.JOYSTICK_STATE.ordinal(), javaFXJoystick.serialise()));
        }
    }

    private MenuBar getMenuBar() {
        final MenuBar menuBar = new MenuBar();

        final Menu fileMenu = new Menu("File");
        registerMenuItem(fileMenu, "Quit", Optional.of(Q), ae -> System.exit(0));

        final Menu networkMenu = new Menu("Network");
        easyConnectItem = registerMenuItem(networkMenu, "Connect to computer", Optional.of(C), this::easyConnectToComputer);
        easyConnectItem.disableProperty().bind(guestPeer.connectedProperty());
        disconnectItem = registerMenuItem(networkMenu, "Disconnect from computer", Optional.of(D), this::disconnectFromComputer);
        disconnectItem.disableProperty().bind(guestPeer.connectedProperty().not());

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(networkMenu);
        return menuBar;
    }

    private void easyConnectToComputer(final ActionEvent ae) {
        final Optional<String> codename = getCodename("emulator");
        codename.ifPresent(cn -> guestPeer.connect(cn));
    }

    private void disconnectFromComputer(final ActionEvent ae) {
        guestPeer.disconnect();
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
*/