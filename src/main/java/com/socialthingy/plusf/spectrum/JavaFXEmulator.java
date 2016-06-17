package com.socialthingy.plusf.spectrum;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.Timer;
import com.socialthingy.plusf.spectrum.dialog.CancelableProgressDialog;
import com.socialthingy.plusf.spectrum.dialog.ErrorDialog;
import com.socialthingy.plusf.spectrum.display.JavaFXBorder;
import com.socialthingy.plusf.spectrum.display.JavaFXDisplay;
import com.socialthingy.plusf.spectrum.input.SpectrumKeyboard;
import com.socialthingy.plusf.spectrum.io.IOMultiplexer;
import com.socialthingy.plusf.spectrum.io.SinglePortIO;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.spectrum.remote.*;
import com.socialthingy.plusf.tzx.*;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.*;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.socialthingy.plusf.spectrum.UIBuilder.*;
import static com.socialthingy.plusf.spectrum.dialog.CodenameDialog.getCodename;
import static javafx.scene.input.KeyCode.*;

public class JavaFXEmulator extends Application {
    private static final String PREF_LAST_SNAPSHOT_DIRECTORY = "last-snapshot-directory";
    private static final String DISPLAY_REFRESH_TIMER_NAME = "display.refresh";
    private static final int LOCAL_PORT = Settings.COMPUTER_PORT;

    private final Timer displayRefreshTimer;
    private final MetricRegistry metricRegistry;
    private final JavaFXDisplay display;
    private final JavaFXBorder border;
    private final Label statusLabel;
    private final AtomicLong timestamper = new AtomicLong(0);
    private final File prefsFile = new File(System.getProperty("user.home"), "plusf.properties");
    private final Properties userPrefs = new Properties();

    private ComputerLoop computerLoop;
    private ULA ula;
    private Computer computer;
    private SpectrumKeyboard keyboard;
    private int[] memory;
    private MenuItem easyConnectItem;
    private MenuItem disconnectItem;
    private MenuItem playTapeItem;
    private MenuItem stopTapeItem;
    private MenuItem resumeTapeItem;
    private Optional<NetworkPeer<GuestState, EmulatorState>> hostRelay = Optional.empty();
    private DatagramSocket socket;
    private SinglePortIO guestKempstonJoystick;
    private Optional<TzxPlayer> tzxPlayer = Optional.empty();
    private Optional<PlayableTzx> tape = Optional.empty();

    private Stage primaryStage;
    private IOMultiplexer ioMux;

    public static void main(final String ... args) {
        com.guigarage.flatterfx.FlatterFX.style();
        Application.launch(args);
    }

    public JavaFXEmulator() {
        metricRegistry = new MetricRegistry();
        displayRefreshTimer = new Timer(new SlidingTimeWindowReservoir(1, TimeUnit.SECONDS));
        metricRegistry.register(DISPLAY_REFRESH_TIMER_NAME, displayRefreshTimer);
        computerLoop = new ComputerLoop();
        display = new JavaFXDisplay();
        border = new JavaFXBorder();
        statusLabel = new Label("No guest connected");

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
        ula = new ULA(BORDER, BORDER);
        computer = new Computer(
            new Processor(memory, ioMux),
            ula,
            memory,
            new Timings(50, 60, 3500000),
            metricRegistry
        );
        guestKempstonJoystick = new SinglePortIO(0x1f);
        keyboard = new SpectrumKeyboard(ula);

        ioMux.register(0xfe, ula);
        ioMux.register(0x1f, guestKempstonJoystick);

        final String romFile = getParameters().getRaw().get(0);
        computer.loadRom(romFile);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        newComputer();
        Memory.enableRomProtection();

        final Iterator<String> params = getParameters().getRaw().iterator();
        params.next();
        if (params.hasNext()) {
            final int borderColour = computer.loadSnapshot(new File(params.next()));
            ula.setBorder(borderColour);
        }

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case ESCAPE:
                    dump(System.out);
                    break;

                case F9:
                    final boolean enabled = computer.toggleMemoryProtectionEnabled();
                    System.out.println("Memory protection " + (enabled ? "enabled" : "disabled"));
            }
        });

        primaryStage.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ALT) {
                e.consume();
            }
        });
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> keyboard.handle(e));
        primaryStage.addEventHandler(KeyEvent.KEY_RELEASED, e -> keyboard.handle(e));

        buildUI(primaryStage, display, border, statusLabel, getMenuBar());

        final java.util.Timer statusBarTimer = installStatusLabelUpdater(statusLabel, () -> hostRelay);
        primaryStage.setOnCloseRequest(we -> {
            statusBarTimer.cancel();
            hostRelay.ifPresent(h -> h.disconnect());
        });
        primaryStage.setTitle("+F Spectrum Emulator");
        primaryStage.show();

        new Thread(() -> {
            final Scanner scanner = new Scanner(System.in);
            while (true) {
                final String line = scanner.nextLine();
                if (line.startsWith("break ")) {
                    final String[] breakpoints = line.split(" ");
                    computer.setBreakPoints(
                        Arrays.asList(breakpoints)
                                .stream()
                                .skip(1)
                                .map(a -> Integer.parseInt(a, 16))
                                .collect(Collectors.toList())
                    );
                }
            }
        }).start();

        computerLoop.play();
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

        final CheckMenuItem fastMode = new CheckMenuItem("Fast mode");
        fastMode.setSelected(false);
        fastMode.setOnAction(this::toggleFastMode);
        fastMode.setAccelerator(new KeyCodeCombination(F, KeyCombination.ALT_DOWN));
        computerMenu.getItems().add(fastMode);

        final Menu tapeMenu = new Menu("Tape");
        playTapeItem = registerMenuItem(tapeMenu, "Play from Start", Optional.of(P), this::playTape);
        playTapeItem.setDisable(true);

        stopTapeItem = registerMenuItem(tapeMenu, "Stop/Pause", Optional.of(S), this::stopTape);
        stopTapeItem.setDisable(true);

        resumeTapeItem = registerMenuItem(tapeMenu, "Resume", Optional.of(U), this::resumeTape);
        resumeTapeItem.setDisable(true);

        menuBar.getMenus().add(fileMenu);
        menuBar.getMenus().add(computerMenu);
        menuBar.getMenus().add(tapeMenu);
        menuBar.getMenus().add(networkMenu);
        return menuBar;
    }

    private void playTape(final ActionEvent ae) {
        tape = tzxPlayer.map(TzxPlayer::getPlayableTzx);
        tape.ifPresent(t -> {
            t.play();
            ula.setTape(t);
            stopTapeItem.setDisable(false);
            resumeTapeItem.setDisable(true);
        });
    }

    private void stopTape(final ActionEvent ae) {
        tape.ifPresent(t -> {
            t.stop();
            stopTapeItem.setDisable(true);
            resumeTapeItem.setDisable(false);
        });
    }

    private void resumeTape(final ActionEvent ae) {
        tape.ifPresent(t -> {
            t.play();
            stopTapeItem.setDisable(false);
            resumeTapeItem.setDisable(true);
        });
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

    private void toggleFastMode(final ActionEvent ae) {
        computerLoop.stop();
        final boolean fastMode = ((CheckMenuItem) ae.getSource()).isSelected();
        computerLoop = new ComputerLoop(fastMode ? 100.0 : 50.0);
        computerLoop.play();
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
            fileChooser.setTitle("Load Snapshot File");
            if (userPrefs.containsKey(PREF_LAST_SNAPSHOT_DIRECTORY)) {
                fileChooser.setInitialDirectory(new File(userPrefs.getProperty(PREF_LAST_SNAPSHOT_DIRECTORY)));
            }
            final File chosen = fileChooser.showOpenDialog(primaryStage);

            if (chosen != null) {
                try {
                    final Optional<Integer> borderColour = detectAndLoad(chosen);
                    borderColour.ifPresent(bc -> ula.setBorder(bc));
                    userPrefs.setProperty(PREF_LAST_SNAPSHOT_DIRECTORY, chosen.getParent());
                    savePrefs();
                } catch (IOException | TzxException ex) {
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

    private Optional<Integer> detectAndLoad(final File chosen) throws IOException, TzxException {
        final byte[] buf = new byte[256];
        try (final InputStream is = new FileInputStream(chosen)) {
            is.read(buf);
        }

        if (TzxReader.recognises(buf)) {
            final Tzx tzx = new TzxReader(chosen).readTzx();
            tzxPlayer = Optional.of(new TzxPlayer(tzx));
            playTapeItem.setDisable(false);
            return Optional.empty();
        } else {
            return Optional.of(computer.loadSnapshot(chosen));
        }
    }

    private void withComputerPaused(final Runnable r) {
        computerLoop.pause();
        try {
            r.run();
        } finally {
            computerLoop.play();
        }
    }

    private void dump(final PrintStream out) {
        computer.dump(out);
    }

    private class ComputerLoop extends Transition {
        private boolean flashActive = false;
        private int flashCycleCount = 0x10;
        private final int[] screenBytes = new int[0x1b00];
        private boolean updateDisplay = true;

        public ComputerLoop() {
            this(50.0);
        }

        public ComputerLoop(final double frameRate) {
            super(frameRate);
            setCycleCount(Transition.INDEFINITE);
            setCycleDuration(Duration.millis(1000.0));
        }

        @Override
        protected void interpolate(double frac) {
            final Timer.Context timer = displayRefreshTimer.time();
            try {
                if (updateDisplay) {
                    final int[] borderLines = ula.getBorderLines();
                    hostRelay.ifPresent(h -> {
                        h.sendDataToPartner(new EmulatorState(memory, borderLines, flashActive));
                    });
                    display.refresh(memory, flashActive);
                    border.refresh(borderLines);
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
