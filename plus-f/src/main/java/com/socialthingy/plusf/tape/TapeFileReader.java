package com.socialthingy.plusf.tape;

import com.socialthingy.plusf.util.Try;

import java.io.*;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TapeFileReader {
    private static final Logger logger = Logger.getLogger(TapeFileReader.class.getName());

    private Map<Integer, Function<InputStream, Try<? extends TapeBlock>>> blockMappers = new HashMap<>();

    private final InputStream tzxFile;

    public static boolean recognises(byte[] start) {
        return "ZXTape!".equals(new String(start, 0, 7));
    }

    public TapeFileReader(final File tzxFile) throws FileNotFoundException {
        this(new FileInputStream(tzxFile));
    }

    public TapeFileReader(final InputStream tzxFile) {
        this.tzxFile = tzxFile;

        blockMappers.put(0x10, VariableSpeedBlock::readStandardSpeedBlock);
        blockMappers.put(0x11, VariableSpeedBlock::readTurboSpeedBlock);
        blockMappers.put(0x12, PureToneBlock::read);
        blockMappers.put(0x13, PulseSequenceBlock::read);
        blockMappers.put(0x14, PureDataBlock::read);
        blockMappers.put(0x20, PauseBlock::read);
        blockMappers.put(0x21, GroupStartBlock::read);
        blockMappers.put(0x22, GroupEndBlock::read);
        blockMappers.put(0x23, JumpBlock::read);
        blockMappers.put(0x24, LoopStartBlock::read);
        blockMappers.put(0x25, LoopEndBlock::read);
        blockMappers.put(0x30, TextDescriptionBlock::read);
        blockMappers.put(0x31, MessageBlock::read);
        blockMappers.put(0x32, ArchiveInfoBlock::read);
    }

    public Tape readTap() throws TapeException, IOException {
        try {
            int lenLo = tzxFile.read();
            int lenHi = tzxFile.read();
            final List<TapeBlock> blocks = new ArrayList<>();
            int blockId = 0;
            while (lenLo >= 0 && lenHi >= 0) {
                blockId++;
                final int len = (lenHi << 8) + lenLo;
                logger.fine(String.format("Block #%d: %d bytes", blockId, len));
                VariableSpeedBlock.readTapBlock(tzxFile, len).ifSuccess(blocks::add);

                lenLo = tzxFile.read();
                lenHi = tzxFile.read();
            }
            addSuffixBlocks(blocks);

            logger.fine("Reached end of file");
            return Tape.apply("TAP", blocks);
        } finally {
            tzxFile.close();
        }
    }

    public Tape readTzx() throws TapeException, IOException {
        try {
            final String header = readText();
            if (!header.equals("ZXTape!")) {
                throw new TapeException("Incorrect header");
            }

            final String version = String.format("%d.%d", tzxFile.read(), tzxFile.read());
            logger.fine(String.format("TZX version %s", version));

            final List<TapeBlock> blocks = new ArrayList<>();
            int blockType = tzxFile.read();
            int blockId = 0;
            while (blockType != -1) {
                blockId++;
                final Function<InputStream, Try<? extends TapeBlock>> mapper = blockMappers.get(blockType);
                logger.fine(String.format("Reading block #%d: type %d", blockId, blockType));
                if (mapper == null) {
                    logger.fine("Unsupported block");
                } else {
                    final Try<? extends TapeBlock> block = mapper.apply(tzxFile);
                    block.ifSuccess(b -> {
                        blocks.add(b);
                        logger.fine(String.format("Block description: %s", b.toString()));
                    });
                    block.ifFailure(ex -> logger.log(Level.WARNING, "Error reading block", ex));
                }

                blockType = tzxFile.read();
            }
            addSuffixBlocks(blocks);

            logger.fine("Reached end of file");
            return Tape.apply(version, blocks);
        } finally {
            tzxFile.close();
        }
    }

    private void addSuffixBlocks(final List<TapeBlock> blocks) {
        blocks.add(new GroupStartBlock("End of tape"));
        blocks.add(new PulseSequenceBlock(SignalState.Adjustment.NO_CHANGE, new int[] {3500}));
        blocks.add(new PauseBlock(Duration.ofMillis(1)));
        blocks.add(new PauseBlock(Duration.ZERO));
        blocks.add(new GroupEndBlock());
    }

    private String readText() throws IOException {
        byte[] bytes = new byte[8];
        byte nb = (byte) tzxFile.read();
        int idx = 0;
        while (nb != 0x1a && nb != -1) {
            bytes[idx++] = nb;
            nb = (byte) tzxFile.read();

            if (idx == bytes.length) {
                byte[] newBytes = new byte[bytes.length * 2];
                System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
                bytes = newBytes;
            }
        }

        return new String(bytes, 0, idx);
    }

}
