package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.tape.BlockBits;

import java.io.IOException;
import java.io.InputStream;

public abstract class TapeBlock {
    protected static String getFixedLengthString(final InputStream tapeFile) throws IOException {
        final int length = nextByte(tapeFile);
        final byte[] buf = new byte[length];
        for (int i = 0; i < length; i++) {
            buf[i] = (byte) nextByte(tapeFile);
        }
        return new String(buf);
    }

    public BlockBits getBitList(final SignalState signalState) {
        return new EmptyBlockBits();
    }

    public String getDisplayName() {
        return toString();
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

    private class EmptyBlockBits implements BlockBits {
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
