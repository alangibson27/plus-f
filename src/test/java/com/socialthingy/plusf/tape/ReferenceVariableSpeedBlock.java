package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.tape.TapeBlock.Bit;

import java.time.Duration;

import static com.socialthingy.plusf.util.Bitwise.binary;

public class ReferenceVariableSpeedBlock {

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

    public ReferenceVariableSpeedBlock(
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

    public boolean write(final RepeatingList<Bit> tape, final boolean initialState) {
        // pilot tone
        boolean state = false;
        for (int i = 0; i < pilotToneLength; i++) {
            tape.add(new Bit(state, "pilot"), pilotPulseLength);
            state = !state;
        }

        // sync 1 - on pulse
        tape.add(new Bit(state, "sync 1"), sync1PulseLength);
        state = !state;

        // sync 2 - off pulse
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
}
