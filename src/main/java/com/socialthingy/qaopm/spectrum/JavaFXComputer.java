package com.socialthingy.qaopm.spectrum;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.awt.Color;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static javafx.scene.input.KeyCode.*;

public class JavaFXComputer extends Application {

    private static final String DISPLAY_REFRESH_TIMER_NAME = "display.refresh";

    private final Computer computer;
    private final Map<KeyCode, Character> spectrumKeys = new HashMap<>();
    private final Map<KeyCode, List<KeyCode>> convenienceKeys = new HashMap<>();
    private final JavaFXDisplay display;
    private final Timer displayRefreshTimer;

    public static void main(final String ... args) {
        Application.launch(args);
    }

    public JavaFXComputer() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final int[] memory = new int[0x10000];
        computer = new Computer(memory, new Timings(50, 60, 3500000), metricRegistry);
        display = new JavaFXDisplay(memory);

        displayRefreshTimer = metricRegistry.timer(DISPLAY_REFRESH_TIMER_NAME);

        spectrumKeys.put(A, 'a');
        spectrumKeys.put(B, 'b');
        spectrumKeys.put(C, 'c');
        spectrumKeys.put(D, 'd');
        spectrumKeys.put(E, 'e');
        spectrumKeys.put(F, 'f');
        spectrumKeys.put(G, 'g');
        spectrumKeys.put(H, 'h');
        spectrumKeys.put(I, 'i');
        spectrumKeys.put(J, 'j');
        spectrumKeys.put(K, 'k');
        spectrumKeys.put(L, 'l');
        spectrumKeys.put(M, 'm');
        spectrumKeys.put(N, 'n');
        spectrumKeys.put(O, 'o');
        spectrumKeys.put(P, 'p');
        spectrumKeys.put(Q, 'q');
        spectrumKeys.put(R, 'r');
        spectrumKeys.put(S, 's');
        spectrumKeys.put(T, 't');
        spectrumKeys.put(U, 'u');
        spectrumKeys.put(V, 'v');
        spectrumKeys.put(W, 'w');
        spectrumKeys.put(X, 'x');
        spectrumKeys.put(Y, 'y');
        spectrumKeys.put(Z, 'z');

        spectrumKeys.put(DIGIT0, '0');
        spectrumKeys.put(DIGIT1, '1');
        spectrumKeys.put(DIGIT2, '2');
        spectrumKeys.put(DIGIT3, '3');
        spectrumKeys.put(DIGIT4, '4');
        spectrumKeys.put(DIGIT5, '5');
        spectrumKeys.put(DIGIT6, '6');
        spectrumKeys.put(DIGIT7, '7');
        spectrumKeys.put(DIGIT8, '8');
        spectrumKeys.put(DIGIT9, '9');

        spectrumKeys.put(SHIFT, '^');
        spectrumKeys.put(CONTROL, '$');
        spectrumKeys.put(SPACE, ' ');
        spectrumKeys.put(ENTER, '_');

        addConvenienceKey(BACK_SPACE, SHIFT, DIGIT0);
        addConvenienceKey(COMMA, CONTROL, N);
        addConvenienceKey(PERIOD, CONTROL, M);
        addConvenienceKey(UP, SHIFT, DIGIT7);
        addConvenienceKey(DOWN, SHIFT, DIGIT6);
        addConvenienceKey(LEFT, SHIFT, DIGIT5);
        addConvenienceKey(RIGHT, SHIFT, DIGIT8);
        addConvenienceKey(COLON, CONTROL, Z);
        addConvenienceKey(SLASH, CONTROL, V);
        addConvenienceKey(MINUS, CONTROL, J);
        addConvenienceKey(PLUS, CONTROL, K);
        addConvenienceKey(EQUALS, CONTROL, L);
        addConvenienceKey(SEMICOLON, CONTROL, O);
        addConvenienceKey(AT, CONTROL, DIGIT2);
        addConvenienceKey(POUND, CONTROL, DIGIT3);
        addConvenienceKey(QUOTE, CONTROL, DIGIT7);
        addConvenienceKey(UNDERSCORE, CONTROL, DIGIT0);
    }

    private void addConvenienceKey(final KeyCode convenienceKey, final KeyCode ... spectrumKeys) {
        convenienceKeys.put(convenienceKey, Arrays.asList(spectrumKeys));
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final String romFile = getParameters().getRaw().get(0);
        computer.loadRom(romFile);

        if (getParameters().getRaw().size() > 1) {
            final String snapshotFile = getParameters().getRaw().get(1);
            computer.loadSnapshot(snapshotFile);
        }

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root);

        final ImageView imageView = new ImageView(display.getScreen());
        imageView.setFitWidth(256 * 2);
        imageView.setFitHeight(192 * 2);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        root.setCenter(imageView);

        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();

        imageView.fitWidthProperty().bind(primaryStage.widthProperty());
        imageView.fitHeightProperty().bind(primaryStage.heightProperty());

        final SpectrumKeyHandler keyHandler = new SpectrumKeyHandler();
        primaryStage.addEventHandler(KeyEvent.KEY_PRESSED, keyHandler);
        primaryStage.addEventHandler(KeyEvent.KEY_RELEASED, keyHandler);

        new AnimationTimer() {
            @Override
            public void handle(final long now) {
                final Timer.Context timer = displayRefreshTimer.time();
                try {
                    display.refresh();
                    computer.singleCycle();
                } finally {
                    timer.stop();
                }
            }
        }.start();
    }

    public void dump(final PrintStream out) {
        computer.dump(out);
    }

    private class SpectrumKeyHandler implements EventHandler<KeyEvent> {

        @Override
        public void handle(final KeyEvent event) {
            final KeyCode keyCode = event.getCode();
            final EventType<KeyEvent> eventType = event.getEventType();

            if (handleSpectrumKey(keyCode, eventType)) {
                return;
            }

            if (convenienceKeys.containsKey(keyCode)) {
                convenienceKeys.get(keyCode)
                        .forEach(sk -> handleSpectrumKey(sk, eventType));
            }

            if (keyCode == KeyCode.ESCAPE && eventType == KeyEvent.KEY_PRESSED) {
                dump(System.out);
            }
        }

        private boolean handleSpectrumKey(final KeyCode keyCode, final EventType<KeyEvent> eventType) {
            final Character spectrumKey = spectrumKeys.get(keyCode);
            if (spectrumKey != null) {
                if (eventType == KeyEvent.KEY_PRESSED) {
                    computer.getUla().keyDown(spectrumKey);
                } else if (eventType == KeyEvent.KEY_RELEASED) {
                    computer.getUla().keyUp(spectrumKey);
                }
            }

            return spectrumKey != null;
        }
    }
}

class JavaFXDisplay extends DisplaySupport<WritableImage> {

    private final WritableImage screen = new WritableImage(256, 192);
    private final PixelWriter pw = screen.getPixelWriter();
    private final int[] pixels = new int[256 * 192];

    public JavaFXDisplay(final int[] memory) {
        super(memory);

    }

    public WritableImage getScreen() {
        return screen;
    }

    @Override
    public WritableImage refresh() {
        super.draw(this::setPixel);
        pw.setPixels(0, 0, 256, 192, PixelFormat.getIntArgbInstance(), pixels, 0, 256);
        return screen;
    }

    private void setPixel(final int x, final int y, final Color color) {
        pixels[x + (y * 256)] = color.getRGB();
    }
}
