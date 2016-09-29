package com.socialthingy.plusf.spectrum;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.Timer;
import com.socialthingy.plusf.spectrum.display.Icons;
import com.socialthingy.plusf.spectrum.display.JavaFXDoubleSizeDisplay;
import com.socialthingy.plusf.spectrum.input.HostInputMultiplexer;
import com.socialthingy.plusf.spectrum.input.JavaFXJoystick;
import com.socialthingy.plusf.spectrum.input.JavaFXKeyboard;
import com.socialthingy.plusf.spectrum.io.IOMultiplexer;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.spectrum.joystick.Joystick;
import com.socialthingy.plusf.spectrum.joystick.KempstonJoystickInterface;
import com.socialthingy.plusf.spectrum.joystick.SinclairJoystickInterface;
import com.socialthingy.plusf.spectrum.network.EmulatorPeerAdapter;
import com.socialthingy.plusf.spectrum.network.EmulatorState;
import com.socialthingy.plusf.spectrum.network.GuestState;
import com.socialthingy.plusf.spectrum.network.GuestStateType;
import com.socialthingy.plusf.tape.Tape;
import com.socialthingy.plusf.tape.TapeException;
import com.socialthingy.plusf.tape.TapeFileReader;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.socialthingy.plusf.spectrum.UIBuilder.buildUI;
import static com.socialthingy.plusf.spectrum.UIBuilder.installStatusLabelUpdater;
import static com.socialthingy.plusf.spectrum.UIBuilder.registerMenuItem;
import static com.socialthingy.plusf.spectrum.dialog.CodenameDialog.getCodename;
import static com.socialthingy.plusf.spectrum.display.Icons.iconFrom;
import static javafx.scene.input.KeyCode.*;

public class JavaFXEmulator extends Application {
    private static final String PREF_LAST_SNAPSHOT_DIRECTORY = "last-snapshot-directory";
    private static final String PREF_MODEL = "initial-model";
    private static final String DISPLAY_REFRESH_TIMER_NAME = "display.refresh";

    private final Timer displayRefreshTimer;
    private final MetricRegistry metricRegistry;
    private final JavaFXDoubleSizeDisplay display;
    private final Label statusLabel;
    private final Label speedLabel;
    private final AtomicLong timestamper = new AtomicLong(0);
    private final File prefsFile = new File(System.getProperty("user.home"), "plusf.properties");
    private final Properties userPrefs = new Properties();

    private ScheduledExecutorService cycleTimer = Executors.newSingleThreadScheduledExecutor();
    private SingleCycle singleCycle;
    private ScheduledFuture<?> cycleLoop;
    private Model currentModel = Model._48K;
    private ULA ula;
    private Computer computer;
    private int[] memory;
    private int cycleLimit = 0;
    private int currentCycle = 0;
    private MenuItem easyConnectItem;
    private MenuItem disconnectItem;
    private MenuItem playTapeItem;
    private MenuItem stopTapeItem;
    private MenuItem rewindTapeToStartItem;
    private EmulatorPeerAdapter hostPeer = new EmulatorPeerAdapter(this::receiveGuestInput);
    private TapePlayer tapePlayer;

    private JavaFXKeyboard keyboard;
    private JavaFXJoystick hostJoystick;
    private Joystick guestJoystick;
    private KempstonJoystickInterface kempstonJoystickInterface;
    private SinclairJoystickInterface sinclair1JoystickInterface;
    private HostInputMultiplexer hostInputMultiplexer;

    private Stage primaryStage;
    private IOMultiplexer ioMux;
    private EmulatorSpeed speed = EmulatorSpeed.NORMAL;
    private BooleanProperty hostJoystickEnabledProperty;

    public static void main(final String ... args) {
        Application.launch(args);
    }

    public JavaFXEmulator() {
        metricRegistry = new MetricRegistry();
        displayRefreshTimer = new Timer(new SlidingTimeWindowReservoir(1, TimeUnit.SECONDS));
        metricRegistry.register(DISPLAY_REFRESH_TIMER_NAME, displayRefreshTimer);
        singleCycle = new SingleCycle();
        display = new JavaFXDoubleSizeDisplay();
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

        if (userPrefs.containsKey(PREF_MODEL)) {
            try {
                currentModel = Model.valueOf(userPrefs.getProperty(PREF_MODEL));
            } catch (IllegalArgumentException e) {
                currentModel = Model._48K;
            }
        }
    }

    private void newComputer() throws IOException {
        ioMux = new IOMultiplexer();
        memory = Memory.configure(currentModel);
        keyboard = new JavaFXKeyboard();
        ula = new ULA(display, keyboard, tapePlayer, memory);

        computer = new Computer(
            new Processor(memory, ioMux),
            ula,
            memory,
            currentModel,
            metricRegistry
        );

        hostJoystick = new JavaFXJoystick();
        guestJoystick = new Joystick();
        kempstonJoystickInterface = new KempstonJoystickInterface();
        sinclair1JoystickInterface = new SinclairJoystickInterface(keyboard);
        hostInputMultiplexer = new HostInputMultiplexer(keyboard, hostJoystick);
        hostInputMultiplexer.deactivateJoystick();
        kempstonJoystickInterface.connect(guestJoystick);
        sinclair1JoystickInterface.connect(hostJoystick);

        ioMux.register(0xfe, ula);
        if (currentModel.ramPageCount > 1) {
            ioMux.register(0xfd, ula);
        }
        ioMux.register(0x1f, kempstonJoystickInterface);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        newComputer();

        final Map<String, String> params = getParameters().getNamed();
        if (params.containsKey("snapshot")) {
            final int borderColour = computer.loadSnapshot(new File(params.get("snapshot")));
            display.setBorder(borderColour);
        }

        if (params.containsKey("limit-cycles")) {
            try {
                cycleLimit = Integer.parseInt(params.getOrDefault("limit-cycles", "0"));
            } catch (NumberFormatException nfe) {
                cycleLimit = 0;
            }
        }

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case ESCAPE:
                    dump(System.out);
                    break;

                case F5:
                    computer.getProcessor().startDebugging();
            }
        });

        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ALT) {
                e.consume();
            }
        });

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> hostInputMultiplexer.handle(e));
        primaryStage.addEventHandler(KeyEvent.KEY_RELEASED, e -> hostInputMultiplexer.handle(e));

        final MenuBar menuBar = getMenuBar();
        final HBox tapeControls = TapeControls.getTapeControls(
            playTapeItem,
            stopTapeItem,
            rewindTapeToStartItem,
            tapePlayer.playingProperty()
        );

        tapePlayer.playAvailableProperty().addListener((observable, oldValue, newValue) -> playTapeItem.setDisable(!newValue));

        tapePlayer.stopAvailableProperty().addListener((observable, oldValue, newValue) -> stopTapeItem.setDisable(!newValue));

        tapePlayer.seekAvailableProperty().addListener((observable, oldValue, newValue) -> rewindTapeToStartItem.setDisable(!newValue));

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

        final java.util.Timer statusBarTimer = installStatusLabelUpdater(statusLabel, hostPeer);
        primaryStage.setOnCloseRequest(we -> {
            statusBarTimer.cancel();
            hostPeer.shutdown();
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
        hostPeer.shutdown();
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

        final CheckMenuItem hostJoystickEnabledMenuItem = new CheckMenuItem("Enable Host Joystick");
        hostJoystickEnabledProperty = hostJoystickEnabledMenuItem.selectedProperty();
        hostJoystickEnabledProperty.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                hostInputMultiplexer.activateJoystick();
            } else {
                hostInputMultiplexer.deactivateJoystick();
            }
        });

        final Menu computerMenu = new Menu("Computer");
        registerMenuItem(computerMenu, "Reset", Optional.of(R), this::resetComputer);
        computerMenu.getItems().add(hostJoystickEnabledMenuItem);

        final Menu modelSubMenu = new Menu("Model");
        final ToggleGroup modelGroup = new ToggleGroup();
        for (Model availableModel: Model.values()) {
            final RadioMenuItem item = new RadioMenuItem(availableModel.displayName);
            item.setOnAction(ae -> {
                if (currentModel != availableModel) {
                    changeModel(availableModel);
                }
            });
            item.setToggleGroup(modelGroup);
            item.setSelected(availableModel == currentModel);
            modelSubMenu.getItems().add(item);
        }

        computerMenu.getItems().add(modelSubMenu);

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
        codename.ifPresent(cn -> hostPeer.connect(cn));
    }

    private void changeSpeed() {
        cycleLoop.cancel(false);
        if (speed == EmulatorSpeed.TURBO) {
            cycleLoop = cycleTimer.schedule(singleCycle, 0, speed.timeUnit);
        } else {
            cycleLoop = cycleTimer.scheduleAtFixedRate(singleCycle, 0, speed.period, speed.timeUnit);
        }
    }

    private void receiveGuestInput(final GuestState guestState) {
        if (guestState.getEventType() == GuestStateType.JOYSTICK_STATE.ordinal()) {
            guestJoystick.deserialise(guestState.getEventValue());
        }
    }

    private void disconnectFromGuest(final ActionEvent ae) {
        hostPeer.disconnect();
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
                        resetJoystickSelection();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    private void resetJoystickSelection() {
        hostJoystickEnabledProperty.set(false);
    }

    private void changeModel(final Model newModel) {
        withComputerPaused(() -> {
            final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Change Model?");
            alert.setHeaderText("This will reset the computer. Do you want to continue?");
            alert.getDialogPane().getChildren().stream()
                    .filter(node -> node instanceof Label)
                    .forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
            final Optional<ButtonType> clicked = alert.showAndWait();
            clicked.ifPresent(bt -> {
                if (bt == ButtonType.OK) {
                    try {
                        currentModel = newModel;
                        userPrefs.setProperty(PREF_MODEL, currentModel.name());
                        savePrefs();
                        newComputer();
                        resetJoystickSelection();
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
        private int updateDisplay = 0;
        private long lastDisplayUpdate = 0;

        private boolean shouldUpdateDisplay() {
            return speed != EmulatorSpeed.TURBO || (System.currentTimeMillis() - lastDisplayUpdate) >= 40;
        }

        @Override
        public void run() {
            final Timer.Context timer = displayRefreshTimer.time();
            do {
                try {
                    if (cycleLimit > 0 && currentCycle > cycleLimit) {
                        System.exit(0);
                    }

                    if (shouldUpdateDisplay()) {
                        display.redrawBorder();
                        final boolean screenRefreshRequired = display.render(memory, flashActive, flashCycleCount == 0x10);
                        if (speed == EmulatorSpeed.NORMAL && hostPeer.isConnected()) {
                            hostPeer.send(new EmulatorState(memory, display.getBorderLines(), flashActive));
                        }

                        Platform.runLater(() -> {
                            if (screenRefreshRequired) {
                                display.refreshScreen();
                            }
                            display.refreshBorder();
                            lastDisplayUpdate = System.currentTimeMillis();
                        });
                    }
                    updateDisplay++;
                    computer.singleCycle();
                    flashCycleCount--;
                    currentCycle++;
                    if (flashCycleCount < 0) {
                        flashCycleCount = 0x10;
                        flashActive = !flashActive;
                    }
                } finally {
                    timer.stop();
                }
            } while (speed == EmulatorSpeed.TURBO);
        }
    }
}

