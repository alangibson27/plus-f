package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;
import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;

import static com.socialthingy.plusf.util.Bitwise.binary;

public class PureToneBlock extends TzxBlock {

    public static Try<PureToneBlock> read(final InputStream tzxFile) {
        try {
            final int pulseLength = nextWord(tzxFile);
            final int toneLength = nextWord(tzxFile);

            return Try.success(new PureToneBlock(pulseLength, toneLength));
        } catch (IOException ex) {
            return Try.failure(ex);
        }
    }

    private final int pulseLength;
    private final int toneLength;

    private PureToneBlock(final int pulseLength, final int toneLength) {
        this.pulseLength = pulseLength;
        this.toneLength = toneLength;
    }

    @Override
    public boolean write(final RepeatingList<Bit> tape, final boolean initialState) {
        boolean state = false;
        for (int i = 0; i < toneLength; i++) {
            tape.add(new Bit(state, "pure tone"), pulseLength);
            state = !state;
        }

        return state;
    }

    @Override
    public String toString() {
        return String.format("Pure tone block: %d pulses of %d", toneLength, pulseLength);
    }
}
