package com.socialthingy.qaopm.spectrum;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.newdawn.slick.*;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class SlickComputer extends BasicGame {
    private static final String DISPLAY_REFRESH_TIMER_NAME = "display.refresh";
    private final Computer computer;
    private final SlickDisplay display;
    private final Map<Integer, Character> spectrumKeys = new HashMap<>();
    private final Timer displayRefreshTimer;

    public static void main(final String ... args) throws IOException, SlickException {
        final SlickComputer slickComputer = new SlickComputer();
        slickComputer.start(args);
    }

    public SlickComputer() {
        super("QAOPM");
        final MetricRegistry metricRegistry = new MetricRegistry();
        final int[] memory = new int[0x10000];
        computer = new Computer(memory, metricRegistry);
        display = new SlickDisplay(memory);
        displayRefreshTimer = metricRegistry.timer(DISPLAY_REFRESH_TIMER_NAME);

        spectrumKeys.put(Input.KEY_A, 'a');
        spectrumKeys.put(Input.KEY_B, 'b');
        spectrumKeys.put(Input.KEY_C, 'c');
        spectrumKeys.put(Input.KEY_D, 'd');
        spectrumKeys.put(Input.KEY_E, 'e');
        spectrumKeys.put(Input.KEY_F, 'f');
        spectrumKeys.put(Input.KEY_G, 'g');
        spectrumKeys.put(Input.KEY_H, 'h');
        spectrumKeys.put(Input.KEY_I, 'i');
        spectrumKeys.put(Input.KEY_J, 'j');
        spectrumKeys.put(Input.KEY_K, 'k');
        spectrumKeys.put(Input.KEY_L, 'l');
        spectrumKeys.put(Input.KEY_M, 'm');
        spectrumKeys.put(Input.KEY_N, 'n');
        spectrumKeys.put(Input.KEY_O, 'o');
        spectrumKeys.put(Input.KEY_P, 'p');
        spectrumKeys.put(Input.KEY_Q, 'q');
        spectrumKeys.put(Input.KEY_R, 'r');
        spectrumKeys.put(Input.KEY_S, 's');
        spectrumKeys.put(Input.KEY_T, 't');
        spectrumKeys.put(Input.KEY_U, 'u');
        spectrumKeys.put(Input.KEY_V, 'v');
        spectrumKeys.put(Input.KEY_W, 'w');
        spectrumKeys.put(Input.KEY_X, 'x');
        spectrumKeys.put(Input.KEY_Y, 'y');
        spectrumKeys.put(Input.KEY_Z, 'z');

        spectrumKeys.put(Input.KEY_0, '0');
        spectrumKeys.put(Input.KEY_1, '1');
        spectrumKeys.put(Input.KEY_2, '2');
        spectrumKeys.put(Input.KEY_3, '3');
        spectrumKeys.put(Input.KEY_4, '4');
        spectrumKeys.put(Input.KEY_5, '5');
        spectrumKeys.put(Input.KEY_6, '6');
        spectrumKeys.put(Input.KEY_7, '7');
        spectrumKeys.put(Input.KEY_8, '8');
        spectrumKeys.put(Input.KEY_9, '9');

        spectrumKeys.put(Input.KEY_CAPITAL, '^');
        spectrumKeys.put(Input.KEY_LCONTROL, '$');
        spectrumKeys.put(Input.KEY_SPACE, ' ');
        spectrumKeys.put(Input.KEY_ENTER, '_');

    }

    public void start(final String ... args) throws IOException, SlickException {
        computer.loadRom(args[0]);
        if (args.length > 1) {
            computer.loadSnapshot(args[1]);
        }

        final AppGameContainer gameContainer = new AppGameContainer(this);
        gameContainer.setDisplayMode(256, 192, false);
        gameContainer.setTargetFrameRate(50);
        gameContainer.start();
    }

    @Override
    public void init(GameContainer gameContainer) throws SlickException {
        gameContainer.getInput().addKeyListener(new SpectrumKeyListener());
    }

    @Override
    public void update(GameContainer gameContainer, int i) throws SlickException {
        computer.singleCycle();
    }

    @Override
    public void render(GameContainer gameContainer, Graphics graphics) throws SlickException {
        final Timer.Context timer = displayRefreshTimer.time();
        try {
            graphics.drawImage(display.refresh(), 0, 0);
        } finally {
            timer.stop();
        }
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

    private class SpectrumKeyListener implements KeyListener {
        @Override
        public void keyPressed(final int key, final char c) {
            final Character spectrumKey = spectrumKeys.get(key);
            if (spectrumKey != null) {
                computer.getUla().keyDown(spectrumKey);
            } else if (key == Input.KEY_ESCAPE) {
                dump(System.out);
            }
        }

        @Override
        public void keyReleased(int key, char c) {
            final Character spectrumKey = spectrumKeys.get(key);
            if (spectrumKey != null) {
                computer.getUla().keyUp(spectrumKey);
            }
        }

        @Override
        public void setInput(Input input) {

        }

        @Override
        public boolean isAcceptingInput() {
            return true;
        }

        @Override
        public void inputEnded() {

        }

        @Override
        public void inputStarted() {

        }
    }
}

class SlickDisplay extends DisplaySupport<Image> {

    private ImageBuffer buf = new ImageBuffer(256, 192);

    protected SlickDisplay(int[] memory) {
        super(memory);
    }

    @Override
    public Image refresh() {
        super.draw((x, y, colour) -> {
            final int r = (int) colour.getRed() * 0xff;
            final int g = (int) colour.getGreen() * 0xff;
            final int b = (int) colour.getBlue() * 0xff;
            buf.setRGBA(x, y, r, g, b, 255);
        });

        final Image localImage = new Image(buf);
        return localImage;
    }
}