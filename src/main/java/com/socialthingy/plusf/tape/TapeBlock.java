package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.util.Word;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static java.util.Collections.emptyIterator;

public abstract class TapeBlock {
    protected static String getFixedLengthString(final InputStream tapeFile) throws IOException {
        final int length = nextByte(tapeFile);
        final byte[] buf = new byte[length];
        for (int i = 0; i < length; i++) {
            buf[i] = (byte) nextByte(tapeFile);
        }
        return new String(buf);
    }

    public Iterator<Boolean> bits(final SignalState signalState) {
        return emptyIterator();
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
