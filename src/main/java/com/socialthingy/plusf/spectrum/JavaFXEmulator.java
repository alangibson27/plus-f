package com.socialthingy.plusf.spectrum;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.Timer;
import com.socialthingy.plusf.spectrum.dialog.CancelableProgressDialog;
import com.socialthingy.plusf.spectrum.dialog.ErrorDialog;
import com.socialthingy.plusf.spectrum.display.Icons;
import com.socialthingy.plusf.spectrum.display.JavaFXDisplay;
import com.socialthingy.plusf.spectrum.input.JavaFXKeyboard;
import com.socialthingy.plusf.spectrum.io.IOMultiplexer;
import com.socialthingy.plusf.spectrum.io.SinglePortIO;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.spectrum.remote.*;
import com.socialthingy.plusf.tape.*;
import com.socialthingy.plusf.z80.Processor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Iterator;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.socialthingy.plusf.spectrum.UIBuilder.*;
import static com.socialthingy.plusf.spectrum.dialog.CodenameDialog.getCodename;
import static com.socialthingy.plusf.spectrum.display.Icons.iconFrom;
import static javafx.scene.input.KeyCode.*;

public class JavaFXEmulator extends Application {
    private static final String PREF_LAST_SNAPSHOT_DIRECTORY = "last-snapshot-directory";
    private static final String DISPLAY_REFRESH_TIMER_NAME = "display.refresh";
    private static final int LOCAL_PORT = Settings.COMPUTER_PORT;

    private final Timer displayRefreshTimer;
    private final MetricRegistry metricRegistry;
    private final JavaFXDisplay display;
    private final Label statusLabel;
    private final Label speedLabel;
    private final AtomicLong timestamper = new AtomicLong(0);
    private final File prefsFile = new File(System.getProperty("user.home"), "plusf.properties");
    private final Properties userPrefs = new Properties();

    private ScheduledExecutorService cycleTimer = Executors.newSingleThreadScheduledExecutor();
    private SingleCycle singleCycle;
    private ScheduledFuture<?> cycleLoop;
    private ULA ula;
    private Computer computer;
    private JavaFXKeyboard keyboard;
    private int[] memory;
    private int[] memoryForDisplay;
    private MenuItem easyConnectItem;
    private MenuItem disconnectItem;
    private MenuItem playTapeItem;
    private MenuItem stopTapeItem;
    private MenuItem rewindTapeToStartItem;
    private Optional<NetworkPeer<GuestState, EmulatorState>> hostRelay = Optional.empty();
    private DatagramSocket socket;
    private SinglePortIO guestKempstonJoystick;
    private TapePlayer tapePlayer;

    private Stage primaryStage;
    private IOMultiplexer ioMux;
    private EmulatorSpeed speed = EmulatorSpeed.NORMAL;

    public static void main(final String ... args) {
        Application.launch(args);
    }

    public JavaFXEmulator() {
        metricRegistry = new MetricRegistry();
        displayRefreshTimer = new Timer(new SlidingTimeWindowReservoir(1, TimeUnit.SECONDS));
        metricRegistry.register(DISPLAY_REFRESH_TIMER_NAME, displayRefreshTimer);
        singleCycle = new SingleCycle();
        display = new JavaFXDisplay();
        statusLabel = new Label("No guest connected");
        speedLabel = new Label("Normal speed");
        tapePlayer = new TapePlayer();

        if (prefsFile.exists()) {
            try (final FileReader fr = new FileReader(prefsFile)) {
                userPrefs.load(fr);
            } catch (IOException ex) {
                System.out.println(
                        String.format("Unable to read preferences file %s. Saved preferences will not be used.", prefsFile.getAbsolutePath())
                );
            }
        }
    }

    private void newComputer() throws IOException {
        ioMux = new IOMultiplexer();
        memory = new int[0x10000];
        memoryForDisplay = new int[0x10000];
        keyboard = new JavaFXKeyboard();
        ula = new ULA(display, keyboard, tapePlayer);
        computer = new Computer(
            new Processor(memory, ioMux),
            ula,
            memory,
            new Timings(50, 60, 3500000),
            metricRegistry
        );
        guestKempstonJoystick = new SinglePortIO(0x1f);

        ioMux.register(0xfe, ula);
        ioMux.register(0x1f, guestKempstonJoystick);

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
            final int borderColour = computer.loadSnapshot(new File(params.next()));
            display.setBorder(borderColour);
        }

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case ESCAPE:
                    dump(System.out);
                    break;

                case F2:
                    computer.getProcessor().startDebugging();
            }
        });

        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ALT) {
                e.consume();
            }
        });
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> keyboard.handle(e));
        primaryStage.addEventHandler(KeyEvent.KEY_RELEASED, e -> keyboard.handle(e));

        final MenuBar menuBar = getMenuBar();
        final HBox tapeControls = TapeControls.getTapeControls(
            playTapeItem,
            stopTapeItem,
            rewindTapeToStartItem,
            tapePlayer.playingProperty()
        );

        tapePlayer.playAvailableProperty().addListener((observable, oldValue, newValue) -> {
            playTapeItem.setDisable(!newValue);
        });

        tapePlayer.stopAvailableProperty().addListener((observable, oldValue, newValue) -> {
            stopTapeItem.setDisable(!newValue);
        });

        tapePlayer.seekAvailableProperty().addListener((observable, oldValue, newValue) -> {
            rewindTapeToStartItem.setDisable(!newValue);
        });


        final GridPane statusPane = new GridPane();
        final ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(25);
        final ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(50);
        final ColumnConstraints column3 = new ColumnConstraints();
        column3.setPercentWidth(25);
        statusPane.getColumnConstraints().addAll(column1, column2, column3);
        GridPane.setHalignment(speedLabel, HPos.RIGHT);

        statusPane.setAlignment(Pos.CENTER_LEFT);
        statusPane.setStyle("-fx-border-width: 1px; -fx-border-color: #000000");
        statusPane.add(statusLabel, 0, 0);
        statusPane.add(tapeControls, 1, 0);
        statusPane.add(speedLabel, 2, 0);

        buildUI(primaryStage, display, statusPane, menuBar);

        final java.util.Timer statusBarTimer = installStatusLabelUpdater(statusLabel, () -> hostRelay);
        primaryStage.setOnCloseRequest(we -> {
            statusBarTimer.cancel();
            hostRelay.ifPresent(h -> h.disconnect());
            cycleLoop.cancel(true);
            cycleTimer.shutdownNow();
        });
        primaryStage.setTitle("+F Spectrum Emulator");
        primaryStage.show();

        cycleLoop = cycleTimer.scheduleAtFixedRate(singleCycle, 0, 20, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        System.out.println("Closing");
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        CancelableProgressDialog.shutdown();
    }

    private MenuBar getMenuBar() {
        final MenuBar menuBar = new MenuBar();
        menuBar.setUseSystemMenuBar(true);

        final Menu fileMenu = new Menu("File");
        registerMenuItem(fileMenu, "Load ...", Optional.of(L), this::load);
        registerMenuItem(fileMenu, "Quit", Optional.of(Q), ae -> System.exit(0));

        final Menu networkMenu = new Menu("Network");
        easyConnectItem = registerMenuItem(networkMenu, "Connect to guest", Optional.of(C), this::easyConnectToGuest);
        disconnectItem = registerMenuItem(networkMenu, "Disconnect from guest", Optional.of(D), this::disconnectFromGuest);
        disconnectItem.setDisable(true);

        final Menu computerMenu = new Menu("Computer");
        registerMenuItem(computerMenu, "Reset", Optional.of(R), this::resetComputer);

        final Menu speedSubMenu = new Menu("Speed");
        final ToggleGroup speedGroup = new ToggleGroup();
        for (EmulatorSpeed availableSpeed: EmulatorSpeed.values()) {
            final RadioMenuItem item = new RadioMenuItem(availableSpeed.displayName);
            item.setOnAction(ae -> {
                speed = availableSpeed;
                speedLabel.setText(String.format("%s speed", speed.displayName));
                changeSpeed();
            });
            item.setToggleGroup(speedGroup);
            item.setSelected(availableSpeed == speed);
            item.setAccelerator(new KeyCodeCombination(availableSpeed.shortcutKey));
            speedSubMenu.getItems().add(item);
        }

        computerMenu.getItems().add(speedSubMenu);

        final Menu tapeMenu = new Menu("Tape");
        playTapeItem = registerMenuItem(tapeMenu, "Play", Optional.empty(), this::playTape);
        playTapeItem.setAccelerator(new KeyCodeCombination(F9));
        playTapeItem.setGraphic(iconFrom(Icons.play));
        playTapeItem.setDisable(true);

        stopTapeItem = registerMenuItem(tapeMenu, "Stop", Optional.empty(), this::stopTape);
        stopTapeItem.setAccelerator(new KeyCodeCombination(F10));
        stopTapeItem.setGraphic(iconFrom(Icons.stop));
        stopTapeItem.setDisable(true);

        rewindTapeToStartItem = registerMenuItem(tapeMenu, "Rewind to Start", Optional.empty(), this::rewindTapeToStart);
        rewindTapeToStartItem.setAccelerator(new KeyCodeCombination(F11));
        rewindTapeToStartItem.setGraphic(iconFrom(Icons.rewindToStart));
        rewindTapeToStartItem.setDisable(true);

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(computerMenu);
        menuBar.getMenus().add(tapeMenu);
        menuBar.getMenus().add(networkMenu);
        return menuBar;
    }

    private void playTape(final Event ae) {
        tapePlayer.play();
    }

    private void stopTape(final Event ae) {
        tapePlayer.stop();
    }

    private void rewindTapeToStart(final Event ae) {
        try {
            tapePlayer.rewindToStart();
        } catch (TapeException e) {
            e.printStackTrace();
        }
    }

    private void easyConnectToGuest(final ActionEvent ae) {
        final Optional<String> codename = getCodename("guest");
        codename.ifPresent(cn -> {
            try {
                final Task<SocketAddress> computerAddress = new EmulatorConnectionSetup(getSocket(), cn);
                CancelableProgressDialog.show(
                        computerAddress,
                        "Connecting to guest ... please wait",
                        "Connecting to Guest",
                        addr -> hostRelay = relayTo(addr)
                );
            } catch (SocketException e) {
                ErrorDialog.show(
                        "Connection Error",
                        "Unable to connect to guest. Please try again later.",
                        Optional.of(e)
                );
            }
        });
    }

    private Optional<NetworkPeer<GuestState, EmulatorState>> relayTo(final SocketAddress sa) {
        try {
            easyConnectItem.setDisable(true);
            disconnectItem.setDisable(false);

            return Optional.of(
                    new NetworkPeer<>(
                            this::receiveGuestInput,
                            EmulatorState::serialise,
                            GuestState::deserialise,
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

    private void changeSpeed() {
        cycleLoop.cancel(false);
        cycleLoop = cycleTimer.scheduleAtFixedRate(singleCycle, 0, speed.period, speed.timeUnit);
    }

    private DatagramSocket getSocket() throws SocketException {
        if (this.socket == null) {
            socket = new DatagramSocket(LOCAL_PORT);
            socket.setSoTimeout(30000);
        }

        return this.socket;
    }

    private void receiveGuestInput(final GuestState guestState) {
        if (guestState.getPort() == 0x1f) {
            guestKempstonJoystick.setValue(guestState.getValue());
        }
    }

    private void disconnectFromGuest(final ActionEvent ae) {
        hostRelay.ifPresent(r -> {
            r.disconnect();
            hostRelay = Optional.empty();
            easyConnectItem.setDisable(false);
            disconnectItem.setDisable(true);
        });
    }

    private void resetComputer(final ActionEvent actionEvent) {
        withComputerPaused(() -> {
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
        });
    }

    private void savePrefs() {
        try (final FileWriter fw = new FileWriter(prefsFile)) {
            userPrefs.store(fw, "Plus-F user preferences");
        } catch (IOException ex) {
            System.out.println(
                String.format("Unable to read preferences file %s. User preferences will not be saved.", prefsFile.getAbsolutePath())
            );
        }
    }

    private void load(final ActionEvent ae) {
        withComputerPaused(() -> {
            final FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load TAP, TZX or Z80 file");
            if (userPrefs.containsKey(PREF_LAST_SNAPSHOT_DIRECTORY)) {
                fileChooser.setInitialDirectory(new File(userPrefs.getProperty(PREF_LAST_SNAPSHOT_DIRECTORY)));
            }
            final File chosen = fileChooser.showOpenDialog(primaryStage);

            if (chosen != null) {
                try {
                    final Optional<Integer> borderColour = detectAndLoad(chosen);
                    borderColour.ifPresent(bc -> display.setBorder(bc));
                    userPrefs.setProperty(PREF_LAST_SNAPSHOT_DIRECTORY, chosen.getParent());
                    savePrefs();
                } catch (IOException | TapeException ex) {
                    final Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Loading Error");
                    alert.setHeaderText("Unable to load file");
                    alert.setContentText(
                            String.format("An error occurred while loading the file file:\n%s", ex.getMessage())
                    );
                    alert.getDialogPane().getChildren().stream()
                            .filter(node -> node instanceof Label)
                            .forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
                    alert.showAndWait();
                }
            }
        });
    }

    private Optional<Integer> detectAndLoad(final File chosen) throws IOException, TapeException {
        final byte[] buf = new byte[256];
        try (final InputStream is = new FileInputStream(chosen)) {
            is.read(buf);
        }

        if (chosen.getName().toLowerCase().endsWith(".tap")) {
            final Tape tap = new TapeFileReader(chosen).readTap();
            tapePlayer.setTape(tap);
            return Optional.empty();
        } else if (TapeFileReader.recognises(buf)) {
            final Tape tzx = new TapeFileReader(chosen).readTzx();
            tapePlayer.setTape(tzx);
            return Optional.empty();
        } else {
            return Optional.of(computer.loadSnapshot(chosen));
        }
    }

    private void withComputerPaused(final Runnable r) {
        cycleLoop.cancel(false);
        try {
            r.run();
        } finally {
            changeSpeed();
        }
    }

    private void dump(final PrintStream out) {
        computer.dump(out);
    }

    private class SingleCycle implements Runnable {
        private boolean flashActive = false;
        private int flashCycleCount = 0x10;
        private boolean updateDisplay = true;

        @Override
        public void run() {
            final Timer.Context timer = displayRefreshTimer.time();
            try {
                if (updateDisplay) {
                    System.arraycopy(memory, 0x4000, memoryForDisplay, 0x4000, 0x1b00);
                    display.redrawBorder();
                    if (speed == EmulatorSpeed.NORMAL) {
                        hostRelay.ifPresent(h -> {
                            h.sendDataToPartner(new EmulatorState(memory, display.getBorderLines(), flashActive));
                        });
                    }

                    Platform.runLater(() -> {
                        display.refresh(memoryForDisplay, flashActive);
                    });
                }
                updateDisplay = !updateDisplay;
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

