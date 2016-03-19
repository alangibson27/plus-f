package com.socialthingy.qaopm.snapshot;

import com.socialthingy.qaopm.util.Word;
import com.socialthingy.qaopm.z80.Processor;

import java.io.IOException;
import java.io.InputStream;

public class SnapshotLoader {
    private InputStream inputStream;
    private int aValue;
    private int fValue;
    private int cValue;
    private int bValue;
    private int lValue;
    private int hValue;
    private int pcValue;
    private int spValue;
    private int iValue;
    private int rValue;
    private int eValue;
    private int dValue;
    private int bcPValue;
    private int dePValue;
    private int hlPValue;
    private int aPValue;
    private int fPValue;
    private int iyValue;
    private int ixValue;
    private int iff0;
    private int iff1;
    private int interruptMode;
    private int[][] memoryPages;

    public SnapshotLoader(final InputStream inputStream) {
        this.inputStream = inputStream;
        memoryPages = new int[12][];
        for (int i = 0; i < 12; i++) {
            memoryPages[i] = new int[0x4000];
        }
    }

    public void read(final Processor processor, final int[] memory) throws IOException {
        final boolean memoryIsCompressed = extractCommonHeaders();
        if (pcValue == 0x0000) {
            final int headerLength = Word.from(inputStream.read(), inputStream.read());
            pcValue = Word.from(inputStream.read(), inputStream.read());

            final int hwMode = inputStream.read();
            if (hwMode != 0) {
                throw new IOException("Unsupported machine version. Only 48k snapshots supported.");
            }

            skipByte(); // byte 35
            skipByte(); // byte 36
            skipByte(); // byte 37
            skipByte(); // byte 38
            skipBytes(16); // bytes 39-54

            if (headerLength > 23) {
                skipByte(); // byte 55
                skipByte(); // byte 56
                skipByte(); // byte 57
                skipByte(); // byte 58
                skipByte(); // byte 59
                skipByte(); // byte 60
                skipByte(); // byte 61
                skipByte(); // byte 62
                skipBytes(10); // bytes 63-72
                skipBytes(10); // bytes 73-82
                skipByte(); // byte 83
                skipByte(); // byte 84
                skipByte(); // byte 85
            }

            if (headerLength > 54) {
                skipByte(); // byte 86
            }

            int blockStart = inputStream.read();
            while (blockStart != -1) {
                final int blockLength = Word.from(blockStart, inputStream.read());
                final int pageNumber = inputStream.read();
                if (blockLength == 0xffff) {
                    throw new IllegalStateException("uncompressed memory block");
                } else {
                    loadMemoryFromCompressedBinary(0, 0x4000, memoryPages[pageNumber]);
                }

                blockStart = inputStream.read();
            }
        } else {
            extractV1Memory(memoryIsCompressed);
        }

        commitChanges(processor, memory);
    }

    private void extractV1Memory(final boolean memoryIsCompressed) throws IOException {
        final int[] wholeMemory = new int[0xc000];
        if (memoryIsCompressed) {
            loadMemoryFromCompressedBinary(0x0000, 0xc000, wholeMemory);
        } else {
            loadMemoryFromBinary(0x0000, wholeMemory);
        }

        final int[] endMarker = new int[]{
                inputStream.read(), inputStream.read(), inputStream.read(), inputStream.read()
        };

        if (endMarker[0] != 0x00 || endMarker[1] != 0xed || endMarker[2] != 0xed || endMarker[3] != 0x00) {
            throw new IOException(".z80 file format invalid");
        }

        System.arraycopy(wholeMemory, 0x4000, memoryPages[4], 0x0000, 0x4000);
        System.arraycopy(wholeMemory, 0x8000, memoryPages[5], 0x0000, 0x4000);
        System.arraycopy(wholeMemory, 0x0000, memoryPages[8], 0x0000, 0x4000);
    }

    private boolean extractCommonHeaders() throws IOException {
        aValue = inputStream.read();
        fValue = inputStream.read();
        cValue = inputStream.read();
        bValue = inputStream.read();
        lValue = inputStream.read();
        hValue = inputStream.read();
        pcValue = Word.from(inputStream.read(), inputStream.read());
        spValue = Word.from(inputStream.read(), inputStream.read());
        iValue = inputStream.read();

        final int rLowBits = inputStream.read() & 0b01111111;
        final int indicator = inputStream.read();
        final int rHighBit = (indicator & 0b1) << 7;
        rValue = rHighBit | rLowBits;
        final int borderColour = (indicator & 0b1110) >> 1;
        final boolean memoryIsCompressed = (indicator & 0b00100000) != 0;

        eValue = inputStream.read();
        dValue = inputStream.read();

        bcPValue = Word.from(inputStream.read(), inputStream.read());
        dePValue = Word.from(inputStream.read(), inputStream.read());
        hlPValue = Word.from(inputStream.read(), inputStream.read());
        aPValue = inputStream.read();
        fPValue = inputStream.read();

        iyValue = Word.from(inputStream.read(), inputStream.read());
        ixValue = Word.from(inputStream.read(), inputStream.read());

        iff0 = inputStream.read();
        iff1 = inputStream.read();

        final int indicator2 = inputStream.read();
        interruptMode = indicator2 & 0b11;
        return memoryIsCompressed;
    }

    private void commitChanges(final Processor processor, final int[] memory) {
        processor.register("a").set(aValue);
        processor.register("f").set(fValue);
        processor.register("c").set(cValue);
        processor.register("b").set(bValue);
        processor.register("l").set(lValue);
        processor.register("h").set(hValue);
        processor.register("pc").set(pcValue);
        processor.register("sp").set(spValue);
        processor.register("i").set(iValue);
        processor.register("r").set(rValue);
        processor.register("e").set(eValue);
        processor.register("d").set(dValue);

        processor.register("bc'").set(bcPValue);
        processor.register("de'").set(dePValue);
        processor.register("hl'").set(hlPValue);
        processor.register("a'").set(aPValue);
        processor.register("f'").set(fPValue);

        processor.register("iy").set(iyValue);
        processor.register("ix").set(ixValue);

        processor.setIff(0, iff0 > 0);
        processor.setIff(1, iff1 > 0);

        processor.setInterruptMode(interruptMode);

        System.arraycopy(memoryPages[4], 0, memory, 0x8000, 0x4000);
        System.arraycopy(memoryPages[5], 0, memory, 0xc000, 0x4000);
        System.arraycopy(memoryPages[8], 0, memory, 0x4000, 0x4000);
    }

    private void loadMemoryFromCompressedBinary(final int base, final int length, final int[] memory) throws IOException {
        int next = base;
        final int top = base + length;
        while (next < top) {
            final int nextByte = inputStream.read();
            if (nextByte == 0xed) {
                final int nextByte2 = inputStream.read();
                if (nextByte2 == 0xed) {
                    final int repetitions = inputStream.read();
                    final int value = inputStream.read();

                    for (int i = 0; i < repetitions; i++) {
                        memory[next++] = value;
                    }
                } else{
                    memory[next++] = nextByte;
                    memory[next++] = nextByte2;
                }
            } else {
                memory[next++] = nextByte;
            }
        }
    }

    private void loadMemoryFromBinary(final int base, final int[] memory) {
    }

    private void skipByte() throws IOException {
        skipBytes(1);
    }

    private void skipBytes(final int bytes) throws IOException {
        inputStream.skip(bytes);
    }
}
