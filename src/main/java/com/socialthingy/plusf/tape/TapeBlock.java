package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.RepeatingList;
import com.socialthingy.plusf.util.Word;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public abstract class TapeBlock {
    protected static String getFixedLengthString(final InputStream tapeFile) throws IOException {
        final int length = nextByte(tapeFile);
        final byte[] buf = new byte[length];
        for (int i = 0; i < length; i++) {
            buf[i] = (byte) nextByte(tapeFile);
        }
        return new String(buf);
    }

    public abstract boolean write(RepeatingList<Bit> tape, boolean initialState);

    public static class Bit {
        private final boolean state;
        private final String stage;

        public Bit(final boolean state, final String stage) {
            this.state = state;
            this.stage = stage;
        }

        public boolean getState() {
            return state;
        }

        public String getStage() {
            return stage;
        }

        public String toString() {
            return String.format("%d [%s]", state ? 1 : 0, stage);
        }
    }

    public Iterator<Bit> bits(final SignalState signalState) {
        final RepeatingList<Bit> tape = new RepeatingList<>();
        signalState.set(write(tape, signalState.get()));
        return tape.iterator();
    }

    protected static int nextByte(final InputStream tzxFile) throws IOException {
        return tzxFile.read();
    }

    protected static int nextWord(final InputStream tzxFile) throws IOException {
        return Word.from(tzxFile.read(), tzxFile.read());
    }

    protected static int nextTriple(final InputStream tzxFile) throws IOException {
        return tzxFile.read() + (tzxFile.read() << 8) + (tzxFile.read() << 16);
    }
}
