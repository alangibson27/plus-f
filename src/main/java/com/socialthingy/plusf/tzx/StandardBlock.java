package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;

import java.time.Duration;

import static com.socialthingy.plusf.util.Bitwise.binary;

public class StandardBlock implements TzxBlock {

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

    public StandardBlock(
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
    }

    public StandardBlock(final Duration pauseLength, final int[] data) {
        this(pauseLength, data, 2168, 667, 735, 855, 1710, data[0] < 128 ? 8063 : 3223, 8);
    }

    public Duration getPauseLength() {
        return pauseLength;
    }

    public int[] getData() {
        return data;
    }

    @Override
    public void write(final RepeatingList<Bit> tape) {
        // pilot tone
        boolean state = true;
        for (int i = 0; i < pilotToneLength; i++) {
            tape.add(new Bit(state, "pilot"), pilotPulseLength);
            state = !state;
        }

        // sync 1 - off pulse
        state = false;
        tape.add(new Bit(state, "sync 1"), sync1PulseLength);
        state = !state;

        // sync 2 - on pulse
        tape.add(new Bit(state, "sync 2"), sync2PulseLength);
        state = !state;

        // data
        for (int i = 0; i < data.length; i++) {
            final int b;
            if (i == data.length - 1) {
                b = data[i] & finalByteMask[finalByteBitsUsed - 1];
            } else {
                b = data[i];
            }

            for (int bit = 7; bit >= 0; bit --) {
                final boolean high = (b & (1 << bit)) != 0;
                final int pulseLen = high ? onePulseLength : zeroPulseLength;
                tape.add(new Bit(state, "data"), pulseLen);
                state = !state;
                tape.add(new Bit(state, "data"), pulseLen);
                state = !state;
            }
        }

        // pause
        tape.add(new Bit(false, "pause"), 3500000 * (int) pauseLength.getSeconds());
    }
}
