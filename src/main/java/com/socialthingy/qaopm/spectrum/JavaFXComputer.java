package com.socialthingy.qaopm.spectrum;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class JavaFXComputer extends Application {

    private static final String DISPLAY_REFRESH_TIMER_NAME = "display.refresh";

    private final Computer computer;
    private final Map<KeyCode, Character> spectrumKeys = new HashMap<>();
    private ImageView imageView;
    private final JavaFXDisplay display;
    private final Timer displayRefreshTimer;

    public static void main(final String ... args) {
        Application.launch(args);
    }

    public JavaFXComputer() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final int[] memory = new int[0x10000];
        computer = new Computer(memory, metricRegistry);
        display = new JavaFXDisplay(memory);

        displayRefreshTimer = metricRegistry.timer(DISPLAY_REFRESH_TIMER_NAME);

        spectrumKeys.put(KeyCode.A, 'a');
        spectrumKeys.put(KeyCode.B, 'b');
        spectrumKeys.put(KeyCode.C, 'c');
        spectrumKeys.put(KeyCode.D, 'd');
        spectrumKeys.put(KeyCode.E, 'e');
        spectrumKeys.put(KeyCode.F, 'f');
        spectrumKeys.put(KeyCode.G, 'g');
        spectrumKeys.put(KeyCode.H, 'h');
        spectrumKeys.put(KeyCode.I, 'i');
        spectrumKeys.put(KeyCode.J, 'j');
        spectrumKeys.put(KeyCode.K, 'k');
        spectrumKeys.put(KeyCode.L, 'l');
        spectrumKeys.put(KeyCode.M, 'm');
        spectrumKeys.put(KeyCode.N, 'n');
        spectrumKeys.put(KeyCode.O, 'o');
        spectrumKeys.put(KeyCode.P, 'p');
        spectrumKeys.put(KeyCode.Q, 'q');
        spectrumKeys.put(KeyCode.R, 'r');
        spectrumKeys.put(KeyCode.S, 's');
        spectrumKeys.put(KeyCode.T, 't');
        spectrumKeys.put(KeyCode.U, 'u');
        spectrumKeys.put(KeyCode.V, 'v');
        spectrumKeys.put(KeyCode.W, 'w');
        spectrumKeys.put(KeyCode.X, 'x');
        spectrumKeys.put(KeyCode.Y, 'y');
        spectrumKeys.put(KeyCode.Z, 'z');

        spectrumKeys.put(KeyCode.DIGIT0, '0');
        spectrumKeys.put(KeyCode.DIGIT1, '1');
        spectrumKeys.put(KeyCode.DIGIT2, '2');
        spectrumKeys.put(KeyCode.DIGIT3, '3');
        spectrumKeys.put(KeyCode.DIGIT4, '4');
        spectrumKeys.put(KeyCode.DIGIT5, '5');
        spectrumKeys.put(KeyCode.DIGIT6, '6');
        spectrumKeys.put(KeyCode.DIGIT7, '7');
        spectrumKeys.put(KeyCode.DIGIT8, '8');
        spectrumKeys.put(KeyCode.DIGIT9, '9');

        spectrumKeys.put(KeyCode.SHIFT, '^');
        spectrumKeys.put(KeyCode.CONTROL, '$');
        spectrumKeys.put(KeyCode.SPACE, ' ');
        spectrumKeys.put(KeyCode.ENTER, '_');
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final String romFile = getParameters().getRaw().get(0);
        computer.loadRom(romFile);

        if (getParameters().getRaw().size() > 1) {
            final String snapshotFile = getParameters().getRaw().get(1);
            computer.loadSnapshot(snapshotFile);
        }

        Group root = new Group();
        Scene scene = new Scene(root);

        imageView = new ImageView(display.getScreen());
        root.getChildren().add(imageView);
        primaryStage.setScene(scene);
        primaryStage.show();

        final SpectrumKeyHandler keyHandler = new SpectrumKeyHandler();
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
        primaryStage.addEventHandler(KeyEvent.KEY_RELEASED, keyHandler);

        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::singleRefresh, 0, 30, TimeUnit.MILLISECONDS);
    }

    public void singleRefresh() {
        Platform.runLater(() -> {
            final Timer.Context timer = displayRefreshTimer.time();
            try {
                imageView.setImage(display.refresh());
            } finally {
                timer.stop();
            }
        });

        computer.singleCycle();
    }

    public void dump(final PrintStream out) {
        computer.dump(out);

        out.printf(
                "Display refresh: count=%d avg=%f p99=%f max=%d rate=%f\n",
                displayRefreshTimer.getCount(),
                displayRefreshTimer.getSnapshot().getMean() / 1000000,
                displayRefreshTimer.getSnapshot().get99thPercentile() / 1000000,
                displayRefreshTimer.getSnapshot().getMax() / 1000000,
                displayRefreshTimer.getOneMinuteRate()
        );
    }

    private class SpectrumKeyHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(KeyEvent event) {
            final Character spectrumKey = spectrumKeys.get(event.getCode());
            if (spectrumKey != null) {
                if (event.getEventType() == KeyEvent.KEY_PRESSED) {
                    computer.getUla().keyDown(spectrumKey);
                } else if (event.getEventType() == KeyEvent.KEY_RELEASED) {
                    computer.getUla().keyUp(spectrumKey);
                }
            } else if (event.getCode() == KeyCode.ESCAPE && event.getEventType() == KeyEvent.KEY_PRESSED) {
                dump(System.out);
            }
        }
    }
}

class JavaFXDisplay extends DisplaySupport<WritableImage> {

    private final WritableImage screen = new WritableImage(256, 192);

    public JavaFXDisplay(final int[] memory) {
        super(memory);

    }

    public WritableImage getScreen() {
        return screen;
    }

    @Override
    public WritableImage refresh() {
        final WritableImage image = new WritableImage(256, 192);
        final PixelWriter pw = image.getPixelWriter();
        super.draw(pw::setColor);
        return image;
    }
}
