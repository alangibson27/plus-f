package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;
import com.socialthingy.plusf.util.Word;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public abstract class TzxBlock {
    protected static String getFixedLengthString(final InputStream tzxFile) throws IOException {
        final int length = nextByte(tzxFile);
        final byte[] buf = new byte[length];
        for (int i = 0; i < length; i++) {
            buf[i] = (byte) nextByte(tzxFile);
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

    protected static int nextByte(final InputStream tzxFile) throws IOException {
        return tzxFile.read();
    }

    protected static int nextWord(final InputStream tzxFile) throws IOException {
        return Word.from(tzxFile.read(), tzxFile.read());
    }
}
