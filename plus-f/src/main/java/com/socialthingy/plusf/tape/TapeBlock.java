package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.util.Word;

import java.io.IOException;
import java.io.InputStream;

public abstract class TapeBlock implements BlockSignalProvider {
    public static final int[] MILLISECOND_PULSE = {3500};

    protected static String getFixedLengthString(final InputStream tapeFile) throws IOException {
        final int length = nextByte(tapeFile);
        final byte[] buf = new byte[length];
        for (int i = 0; i < length; i++) {
            buf[i] = (byte) nextByte(tapeFile);
        }
        return new String(buf);
    }

    public BlockSignal getBlockSignal(final SignalState signalState) {
        return new EmptyBlockSignal();
    }

    public String getDisplayName() {
        return toString();
    }

    public boolean isDataBlock() {
        return false;
    }

    public boolean shouldStopTape() {
        return false;
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

    private class EmptyBlockSignal implements BlockSignal {
        @Override
        public int skip(int count) {
            return 0;
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Boolean next() {
            return null;
        }
    }
}
