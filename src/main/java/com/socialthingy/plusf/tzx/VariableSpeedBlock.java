package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import static com.socialthingy.plusf.util.Bitwise.binary;

public class VariableSpeedBlock extends TzxBlock {

    public static Try<VariableSpeedBlock> readStandardSpeedBlock(final InputStream tzxFile) {
        try {
            final Duration pauseLength = Duration.ofMillis(nextWord(tzxFile));
            final int dataLength = nextWord(tzxFile);
            final int[] data = new int[dataLength];
            for (int i = 0; i < dataLength; i++) {
                data[i] = nextByte(tzxFile);
            }

            return Try.success(new VariableSpeedBlock(pauseLength, data));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    public static Try<VariableSpeedBlock> readTurboSpeedBlock(final InputStream tzxFile) {
        try {
            final int pilotPulseLength = nextWord(tzxFile);
            final int sync1PulseLength = nextWord(tzxFile);
            final int sync2PulseLength = nextWord(tzxFile);
            final int zeroPulseLength = nextWord(tzxFile);
            final int onePulseLength = nextWord(tzxFile);
            final int pilotToneLength = nextWord(tzxFile);
            final int finalByteBitsUsed = nextByte(tzxFile);
            final Duration pauseLength = Duration.ofMillis(nextWord(tzxFile));
            final int dataLength = nextTriple(tzxFile);

            final int[] data = new int[dataLength];
            for (int i = 0; i < dataLength; i++) {
                data[i] = nextByte(tzxFile);
            }

            return Try.success(
                new VariableSpeedBlock(pauseLength, data, pilotPulseLength, sync1PulseLength, sync2PulseLength, zeroPulseLength, onePulseLength, pilotToneLength, finalByteBitsUsed)
            );
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    private static final int[] finalByteMask = new int[] {
        binary("10000000"),
        binary("11000000"),
        binary("11100000"),
        binary("11110000"),
        binary("11111000"),
        binary("11111100"),
        binary("11111110"),
        binary("11111111")
    };

    private final Duration pauseLength;
    private final int[] data;
    private final int pilotPulseLength;
    private final int sync1PulseLength;
    private final int sync2PulseLength;
    private final int zeroPulseLength;
    private final int onePulseLength;
    private final int pilotToneLength;
    private final int finalByteBitsUsed;
    private String description;

    private VariableSpeedBlock(
        final Duration pauseLength,
        final int[] data,
        final int pilotPulseLength,
        final int sync1PulseLength,
        final int sync2PulseLength,
        final int zeroPulseLength,
        final int onePulseLength,
        final int pilotToneLength,
        final int finalByteBitsUsed
    ) {
        this.pauseLength = pauseLength;
        this.data = data;
        this.pilotPulseLength = pilotPulseLength;
        this.sync1PulseLength = sync1PulseLength;
        this.sync2PulseLength = sync2PulseLength;
        this.zeroPulseLength = zeroPulseLength;
        this.onePulseLength = onePulseLength;
        this.pilotToneLength = pilotToneLength;
        this.finalByteBitsUsed = finalByteBitsUsed;
        this.description = "turbo";
    }

    private VariableSpeedBlock(final Duration pauseLength, final int[] data) {
        this(pauseLength, data, 2168, 667, 735, 855, 1710, data[0] < 128 ? 8063 : 3223, 8);
        this.description = "standard";
    }

    public Duration getPauseLength() {
        return pauseLength;
    }

    public int[] getData() {
        return data;
    }

    @Override
    public boolean write(final RepeatingList<Bit> tape, final boolean initialState) {
        // pilot tone
        boolean state = false;
        for (int i = 0; i < pilotToneLength; i++) {
            tape.add(new Bit(state, "pilot"), pilotPulseLength);
            state = !state;
        }

        // sync 1 - off pulse
        tape.add(new Bit(state, "sync 1"), sync1PulseLength);
        state = !state;

        // sync 2 - on pulse
        tape.add(new Bit(state, "sync 2"), sync2PulseLength);
        state = !state;

        // data
        for (int i = 0; i < data.length; i++) {
            final int b;
            final int lastBit;
            if (i == data.length - 1) {
                b = data[i] & finalByteMask[finalByteBitsUsed - 1];
                lastBit = 8 - finalByteBitsUsed;
            } else {
                b = data[i];
                lastBit = 0;
            }

            for (int bit = 7; bit >= lastBit; bit --) {
                final boolean high = (b & (1 << bit)) != 0;
                final int pulseLen = high ? onePulseLength : zeroPulseLength;
                tape.add(new Bit(state, "data"), pulseLen);
                state = !state;
                tape.add(new Bit(state, "data"), pulseLen);
                state = !state;
            }
        }

        if (!pauseLength.isZero()) {
            tape.add(new Bit(state, "end"), 3500);
            tape.add(new Bit(false, "pause"), 3500 * (int) pauseLength.toMillis());
            state = false;
        }

        return state;
    }

    @Override
    public String toString() {
        return String.format("%s speed block", description);
    }
}
