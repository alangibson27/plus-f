package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;

import java.time.Duration;

public class StandardSpeedBlock implements TzxBlock {
    private final Duration pauseLength;
    private final int[] data;

    public StandardSpeedBlock(final Duration pauseLength, final int[] data) {
        this.pauseLength = pauseLength;
        this.data = data;
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
        final int pulses = data[0] < 128 ? 8063 : 3223;
        boolean state = true;
        for (int i = 0; i < pulses; i++) {
            tape.add(new Bit(state, "pilot"), 2168);
            state = !state;
        }

        // sync 1 - off pulse
        state = false;
        tape.add(new Bit(state, "sync 1"), 667);
        state = !state;

        // sync 2 - on pulse
        tape.add(new Bit(state, "sync 2"), 735);
        state = !state;

        // data
        for (int b: data) {
            for (int bit = 7; bit >= 0; bit --) {
                final boolean high = (b & (1 << bit)) != 0;
                final int pulseLen = high ? 1710 : 855;
                tape.add(new Bit(state, "data"), pulseLen);
                state = !state;
                tape.add(new Bit(state, "data"), pulseLen);
                state = !state;
            }
        }

        // pause
        tape.add(new Bit(false, "pause"), 3500000);
    }
}
