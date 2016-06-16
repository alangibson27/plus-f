package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.util.Word;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class TzxReader {
    private static final Logger logger = Logger.getLogger(TzxReader.class.getName());

    private final InputStream tzxFile;

    public TzxReader(final InputStream tzxFile) {
        this.tzxFile = tzxFile;
    }

    public Tzx readTzx() throws TzxException, IOException {
        final String header = readText();
        if (!header.equals("ZXTape!")) {
            throw new TzxException("Incorrect header");
        }

        final String version = String.format("%d.%d", nextByte(), nextByte());
        logger.info(String.format("TZX version %s", version));

        final List<TzxBlock> blocks = new ArrayList<>();
        int blockType = tzxFile.read();
        int blockId = 0;
        while (blockType != -1) {
            blockId++;
            switch (blockType) {
                case 0x10:
                    logger.info(String.format("Block %d: Standard block", blockId));
                    blocks.add(readStandardSpeedBlock());
                    break;

                default:
                    logger.info(String.format("Block %d: Unsupported block (type %d)", blockId, blockType));
            }

            blockType = tzxFile.read();
        }

        logger.info("Reached end of file");
        return new Tzx(version, blocks);
    }

    private TzxBlock readStandardSpeedBlock() throws IOException {
        final Duration pauseLength = Duration.ofMillis(nextWord());
        final int dataLength = nextWord();
        final int[] data = new int[dataLength];
        for (int i = 0; i < dataLength; i++) {
            data[i] = nextByte();
        }

        return new StandardSpeedBlock(pauseLength, data);
    }

    private String readText() throws IOException {
        byte[] bytes = new byte[8];
        byte nb = (byte) nextByte();
        int idx = 0;
        while (nb != 0x1a) {
            bytes[idx++] = nb;
            nb = (byte) nextByte();

            if (idx == bytes.length) {
                byte[] newBytes = new byte[bytes.length * 2];
                System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                bytes = newBytes;
            }
        }

        return new String(bytes, 0, idx);
    }

    private int nextByte() throws IOException {
        return tzxFile.read();
    }

    private int nextWord() throws IOException {
        return Word.from(tzxFile.read(), tzxFile.read());
    }
}
