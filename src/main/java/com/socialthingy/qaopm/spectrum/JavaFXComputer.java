package com.socialthingy.qaopm.spectrum;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.socialthingy.qaopm.spectrum.remote.Host;
import com.socialthingy.qaopm.z80.Processor;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;

import static com.socialthingy.qaopm.spectrum.UIBuilder.BORDER;
import static com.socialthingy.qaopm.spectrum.UIBuilder.registerMenuItem;
import static javafx.scene.input.KeyCode.*;

public class JavaFXComputer extends Application {

    private static final String DISPLAY_REFRESH_TIMER_NAME = "display.refresh";

    private final Timer displayRefreshTimer;
    private final ComputerLoop computerLoop;
    private final MetricRegistry metricRegistry;
    private final JavaFXDisplay display;
    private final JavaFXBorder border;
    private final Label statusLabel;
    private final ArrayList<KeyCode> allowedGuestKeys;

    private ULA ula;
    private Computer computer;
    private SpectrumKeyboard keyboard;
    private int[] memory;
    private MenuItem connectItem;
    private MenuItem disconnectItem;
    private Optional<Host> hostRelay = Optional.empty();

    private Stage primaryStage;

    public static void main(final String ... args) {
        Application.launch(args);
    }

    public JavaFXComputer() {
        metricRegistry = new MetricRegistry();
        displayRefreshTimer = metricRegistry.timer(DISPLAY_REFRESH_TIMER_NAME);
        computerLoop = new ComputerLoop();
        display = new JavaFXDisplay();
        border = new JavaFXBorder();
        statusLabel = new Label("No guest connected");
        allowedGuestKeys = new ArrayList<>();

        allowedGuestKeys.add(KeyCode.Q);
    }

    private void newComputer() throws IOException {
        final KempstonJoystick kempstonJoystick = new KempstonJoystick();
        final IOMultiplexer ioMux = new IOMultiplexer();
        ioMux.register(0x1f, kempstonJoystick);

        memory = new int[0x10000];
        computer = new Computer(new Processor(memory, ioMux), memory, new Timings(50, 60, 3500000), metricRegistry);
        ula = new ULA(computer, BORDER, BORDER);
        ioMux.register(0xfe, ula);
        keyboard = new SpectrumKeyboard(ula, kempstonJoystick);
        final String romFile = getParameters().getRaw().get(0);
        computer.loadRom(romFile);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        newComputer();

        final Iterator<String> params = getParameters().getRaw().iterator();
        params.next();
        if (params.hasNext()) {
            computer.loadSnapshot(new File(params.next()));
        }

        UIBuilder.buildUI(primaryStage, display, border, statusLabel, getMenuBar());
        primaryStage.setTitle("QAOPM Spectrum Emulator");
        primaryStage.show();

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                dump(System.out);
            }
        });

        primaryStage.addEventHandler(
            KeyEvent.KEY_PRESSED,
            e -> {
                if (!allowedGuestKeys.contains(e.getCode())) {
                    keyboard.handle(e);
                }
            }
        );
        primaryStage.addEventHandler(
            KeyEvent.KEY_RELEASED,
            e -> {
                if (!allowedGuestKeys.contains(e.getCode())) {
                    keyboard.handle(e);
                }
            }
        );
        computerLoop.start();
    }

    private MenuBar getMenuBar() {
        final MenuBar menuBar = new MenuBar();

        final Menu fileMenu = new Menu("File");
        registerMenuItem(fileMenu, "Load ...", Optional.of(L), this::loadSnapshot);
        registerMenuItem(fileMenu, "Quit", Optional.of(Q), ae -> System.exit(0));

        final Menu networkMenu = new Menu("Network");
        registerMenuItem(networkMenu, "Get contact info ...", Optional.of(I), ContactInfoFinder::getContactInfo);
        connectItem = registerMenuItem(networkMenu, "Connect to guest ...", Optional.of(C), this::connectToGuest);
        disconnectItem = registerMenuItem(networkMenu, "Disconnect from guest", Optional.of(D), this::disconnectFromGuest);
        disconnectItem.setDisable(true);

        final Menu computerMenu = new Menu("Computer");
        registerMenuItem(computerMenu, "Reset", Optional.of(R), this::resetComputer);

        final Menu controlsMenu = new Menu("Controls");
        final CheckMenuItem kempstonJoystickItem = new CheckMenuItem("Kempston Joystick");
        kempstonJoystickItem.selectedProperty().addListener(
            (obs, oldValue, newValue) -> keyboard.setKempstonEnabled(true)
        );
        kempstonJoystickItem.setAccelerator(
            new KeyCodeCombination(K, KeyCombination.ALT_DOWN)
        );
        controlsMenu.getItems().add(kempstonJoystickItem);

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(computerMenu);
        menuBar.getMenus().add(networkMenu);
        menuBar.getMenus().add(controlsMenu);
        return menuBar;
    }

    private void connectToGuest(final ActionEvent ae) {
    }

    private void disconnectFromGuest(final ActionEvent ae) {
    }

    private void resetComputer(final ActionEvent actionEvent) {
        computerLoop.stop();
        try {
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Reset Computer?");
            alert.setHeaderText("Are you sure you want to reset the computer?");
            alert.getDialogPane().getChildren().stream()
                    .filter(node -> node instanceof Label)
                    .forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
            final Optional<ButtonType> clicked = alert.showAndWait();
            clicked.ifPresent(bt -> {
                if (bt == ButtonType.OK) {
                    try {
                        newComputer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } finally {
            computerLoop.start();
        }
    }

    private void loadSnapshot(final ActionEvent ae) {
        computerLoop.stop();
        try {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load Snapshot File");
            final File chosen = fileChooser.showOpenDialog(primaryStage);

            if (chosen != null) {
                try {
                    computer.loadSnapshot(chosen);
                } catch (IOException ex) {
                    final Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Loading Error");
                    alert.setHeaderText("Unable to load snapshot");
                    alert.setContentText(
                            String.format("An error occurred while loading the snapshot file:\n%s", ex.getMessage())
                    );
                    alert.getDialogPane().getChildren().stream()
                            .filter(node -> node instanceof Label)
                            .forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
                    alert.showAndWait();
                }
            }
        } finally {
            computerLoop.start();
        }
    }

    public void dump(final PrintStream out) {
        computer.dump(out);
    }

    private class ComputerLoop extends AnimationTimer {
        private boolean flashActive = false;
        private int flashCycleCount = 16;

        @Override
        public void handle(final long now) {
            final Timer.Context timer = displayRefreshTimer.time();
            try {
                final int[] borderLines = ula.getBorderLines();
//                host.ifPresent(h -> h.sendToGuest(allowedGuestKeys, memory, borderLines, flashActive));
                display.refresh(memory, flashActive);
                border.refresh(borderLines);
                computer.singleCycle();
                flashCycleCount--;
                if (flashCycleCount < 0) {
                    flashCycleCount = 16;
                    flashActive = !flashActive;
                }
            } finally {
                timer.stop();
            }
        }
    }
}

