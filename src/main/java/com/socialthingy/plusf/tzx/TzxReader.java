package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.util.Try;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TzxReader {
    private static final Logger logger = Logger.getLogger(TzxReader.class.getName());

    private Map<Integer, Function<InputStream, Try<? extends TzxBlock>>> blockMappers = new HashMap<>();

    private final InputStream tzxFile;

    public TzxReader(final InputStream tzxFile) {
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

    public Tzx readTzx() throws TzxException, IOException {
        try {
            final String header = readText();
            if (!header.equals("ZXTape!")) {
                throw new TzxException("Incorrect header");
            }

            final String version = String.format("%d.%d", tzxFile.read(), tzxFile.read());
            logger.info(String.format("TZX version %s", version));

            final List<TzxBlock> blocks = new ArrayList<>();
            int blockType = tzxFile.read();
            int blockId = 0;
            while (blockType != -1) {
                blockId++;
                final Function<InputStream, Try<? extends TzxBlock>> mapper = blockMappers.get(blockType);
                logger.info(String.format("Reading block #%d: type %d", blockId, blockType));
                if (mapper == null) {
                    logger.info("Unsupported block");
                } else {
                    final Try<? extends TzxBlock> block = mapper.apply(tzxFile);
                    block.ifSuccess(b -> {
                        blocks.add(b);
                        logger.info(String.format("Block description: %s", b.toString()));
                    });
                    block.ifFailure(ex -> logger.log(Level.WARNING, "Error reading block", ex));
                }

                blockType = tzxFile.read();
            }

            logger.info("Reached end of file");
            return new Tzx(version, blocks);
        } finally {
            tzxFile.close();
        }
    }

    private String readText() throws IOException {
        byte[] bytes = new byte[8];
        byte nb = (byte) tzxFile.read();
        int idx = 0;
        while (nb != 0x1a) {
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
