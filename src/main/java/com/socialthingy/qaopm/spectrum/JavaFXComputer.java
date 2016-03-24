package com.socialthingy.qaopm.spectrum;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.socialthingy.qaopm.z80.Processor;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import static javafx.scene.input.KeyCode.*;

public class JavaFXComputer extends Application {

    private static final int SCREEN_WIDTH = 256;
    private static final int SCREEN_HEIGHT = 192;
    public static final int BORDER_WIDTH = SCREEN_WIDTH + 96;
    public static final int BORDER_HEIGHT = SCREEN_HEIGHT + 96;

    private static final String DISPLAY_REFRESH_TIMER_NAME = "display.refresh";

    private final Timer displayRefreshTimer;
    private final ComputerLoop computerLoop;
    private final MetricRegistry metricRegistry;
    private final JavaFXDisplay display;
    private final JavaFXBorder border;

    private final ULA ula = new ULA();
    private Computer computer;
    private SpectrumKeyboard keyboard;
    private int[] memory;

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
    }

    private void newComputer() throws IOException {
        final KempstonJoystick kempstonJoystick = new KempstonJoystick();
        final IOMultiplexer ioMux = new IOMultiplexer();
        ioMux.register(0xfe, ula);
        ioMux.register(0x1f, kempstonJoystick);

        memory = new int[0x10000];
        computer = new Computer(new Processor(memory, ioMux), memory, new Timings(50, 60, 3500000), metricRegistry);
        keyboard = new SpectrumKeyboard(ula, kempstonJoystick);
        final String romFile = getParameters().getRaw().get(0);
        computer.loadRom(romFile);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        newComputer();

        if (getParameters().getRaw().size() > 1) {
            final String snapshotFile = getParameters().getRaw().get(1);
            computer.loadSnapshot(new File(snapshotFile));
        }

        final ImageView borderImage = new ImageView(border.getBorder());
        borderImage.setFitWidth(BORDER_WIDTH * 2);
        borderImage.setFitHeight(BORDER_HEIGHT * 2);

        final ImageView screenImage = new ImageView(display.getScreen());
        screenImage.setFitHeight(SCREEN_HEIGHT * 2);
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

        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, e -> keyboard.handle(e));
        primaryStage.addEventHandler(KeyEvent.KEY_RELEASED, e -> keyboard.handle(e));
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
        final Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Computer?");
        alert.setHeaderText("Are you sure you want to reset the computer?");
        alert.getDialogPane().getChildren().stream()
                .filter(node -> node instanceof Label)
                .forEach(node -> ((Label) node).setMinHeight(Region.USE_PREF_SIZE));
        final Optional<ButtonType> clicked = alert.showAndWait();
        clicked.ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                computerLoop.stop();
                try {
                    newComputer();
                    computerLoop.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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
                display.refresh(memory, flashActive);
                border.refresh(ula);
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

class JavaFXBorder {
    private final WritableImage border = new WritableImage(1, JavaFXComputer.BORDER_HEIGHT);
    private final PixelWriter pw = border.getPixelWriter();

    public WritableImage getBorder() {
        return border;
    }

    public WritableImage refresh(final ULA ula) {
        pw.setPixels(0, 0, 1, JavaFXComputer.BORDER_HEIGHT, PixelFormat.getIntArgbInstance(), ula.getBorderLines(), 0, 1);
        return border;
    }
}

class JavaFXDisplay extends DisplaySupport<WritableImage> {
    private final WritableImage screen = new WritableImage(256, 192);
    private final PixelWriter pw = screen.getPixelWriter();
    private final int[] pixels = new int[256 * 192];

    public WritableImage getScreen() {
        return screen;
    }

    @Override
    public WritableImage refresh(final int[] memory, final boolean flashActive) {
        super.draw(memory, flashActive, this::setPixel);
        pw.setPixels(0, 0, 256, 192, PixelFormat.getIntArgbInstance(), pixels, 0, 256);
        return screen;
    }

    private void setPixel(final int x, final int y, final Color color) {
        pixels[x + (y * 256)] = color.getRGB();
    }
}
