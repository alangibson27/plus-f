package com.socialthingy.qaopm.spectrum;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.socialthingy.qaopm.spectrum.remote.Host;
import com.socialthingy.qaopm.z80.Processor;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.Scanner;

import static javafx.scene.input.KeyCode.*;

public class JavaFXComputer extends Application {

    public static final int SCREEN_WIDTH = 256;
    public static final int BORDER = 16;
    public static final int DISPLAY_WIDTH = SCREEN_WIDTH + (BORDER * 2);
    public static final int DISPLAY_HEIGHT = ULA.SCREEN_HEIGHT + (BORDER * 2);

    private static final String DISPLAY_REFRESH_TIMER_NAME = "display.refresh";

    private final Timer displayRefreshTimer;
    private final ComputerLoop computerLoop;
    private final MetricRegistry metricRegistry;
    private final JavaFXDisplay display;
    private final JavaFXBorder border;
    private final ArrayList<KeyCode> allowedGuestKeys;

    private ULA ula;
    private Computer computer;
    private SpectrumKeyboard keyboard;
    private int[] memory;
    private Optional<Host> host;

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

        final int localPort = Integer.parseInt(params.next());
        host = Optional.of(new Host(e -> keyboard.handle(e), localPort));

        final Scanner input = new Scanner(System.in);
        System.out.print("Guest IP address: ");
        final String guestAddress = input.nextLine();
        System.out.print("Guest port: ");
        final int guestPort = input.nextInt();

        host.get().connectToGuest(guestAddress, guestPort);

        final ImageView borderImage = new ImageView(border.getBorder());
        borderImage.setFitWidth(DISPLAY_WIDTH * 2);
        borderImage.setFitHeight(DISPLAY_HEIGHT * 2);

        final ImageView screenImage = new ImageView(display.getScreen());
        screenImage.setFitHeight(ULA.SCREEN_HEIGHT * 2);
        screenImage.setFitWidth(SCREEN_WIDTH * 2);

        final StackPane sp = new StackPane(borderImage, screenImage);

        BorderPane root = new BorderPane();
        root.setBottom(sp);
        root.setTop(getMenuBar());

        Scene scene = new Scene(root);

        primaryStage.setTitle("QAOPM Spectrum Emulator");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
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
        menuBar.getMenus().add(controlsMenu);
        return menuBar;
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

    private void registerMenuItem(
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
                host.ifPresent(h -> h.sendToGuest(allowedGuestKeys, memory, borderLines, flashActive));
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

