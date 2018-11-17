package com.socialthingy.plusf.snapshot;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.spectrum.io.Memory128K;
import com.socialthingy.plusf.spectrum.io.SpectrumMemory;
import com.socialthingy.plusf.spectrum.io.Memory48K;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.Processor;

import java.io.*;

public class Snapshot {
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
    private int borderColour;
    private Model model;
    private int lastWriteTo0x7ffd;

    private enum HWMode {
        HW_48K(0), HW_48K_IF1(1), HW_128K(3), UNSUPPORTED(-1);

        private int modeNumber;

        HWMode(final int modeNumber) {
            this.modeNumber = modeNumber;
        }

        public static HWMode from(final int modeNumber) {
            for (HWMode mode: values()) {
                if (mode.modeNumber == modeNumber) {
                    return mode;
                }
            }

            return UNSUPPORTED;
        }
    }

    public Snapshot(final InputStream inputStream) throws IOException {
        this.inputStream = inputStream;
        memoryPages = new int[12][];
        for (int i = 0; i < 12; i++) {
            memoryPages[i] = new int[0x4000];
        }
        read();
    }

    public SpectrumMemory getMemory(final Clock clock) {
        if (model == Model._48K) {
            final Memory48K memory = new Memory48K();
            memory.copyIntoPage(memoryPages[4], 2);
            memory.copyIntoPage(memoryPages[5], 3);
            memory.copyIntoPage(memoryPages[8], 1);

            return memory;
        } else {
            final Memory128K memory = new Memory128K(Model._128K);
            for (int i = 3; i <= 10; i++) {
                memory.copyIntoBank(memoryPages[i], i - 3);
            }

            memory.write(0xfd, 0x7f, lastWriteTo0x7ffd);
            return memory;
        }
    }

    private void read() throws IOException {
        final SnapshotInfo snapshotInfo = extractCommonHeaders();
        borderColour = snapshotInfo.borderColour;
        if (pcValue == 0x0000) {
            final int headerLength = Word.from(inputStream.read(), inputStream.read());
            pcValue = Word.from(inputStream.read(), inputStream.read());

            final HWMode hwMode = HWMode.from(inputStream.read());
            if (hwMode == HWMode.UNSUPPORTED || hwMode == HWMode.HW_128K) {
                throw new IOException("Unsupported machine version. Only 48k snapshots supported.");
            }

            model = Model._48K;

            lastWriteTo0x7ffd = inputStream.read(); // byte 35
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
            model = Model._48K;
            extractV1Memory(snapshotInfo.memoryIsCompressed);
        }
    }

    private void extractV1Memory(final boolean memoryIsCompressed) throws IOException {
        final int[] wholeMemory = new int[0xc000];
        if (memoryIsCompressed) {
            loadMemoryFromCompressedBinary(0x0000, 0xc000, wholeMemory);
            final int[] endMarker = new int[]{
                    inputStream.read(), inputStream.read(), inputStream.read(), inputStream.read()
            };

            if (endMarker[0] != 0x00 || endMarker[1] != 0xed || endMarker[2] != 0xed || endMarker[3] != 0x00) {
                throw new IOException(".z80 file format invalid");
            }
        } else {
            loadMemoryFromBinary(wholeMemory);
        }

        System.arraycopy(wholeMemory, 0x4000, memoryPages[4], 0x0000, 0x4000);
        System.arraycopy(wholeMemory, 0x8000, memoryPages[5], 0x0000, 0x4000);
        System.arraycopy(wholeMemory, 0x0000, memoryPages[8], 0x0000, 0x4000);
    }

    private SnapshotInfo extractCommonHeaders() throws IOException {
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
        return new SnapshotInfo(memoryIsCompressed, borderColour);
    }

    public void setProcessorState(final Processor processor) {
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
    }

    public void setBorderColour(final ULA ula) {
        ula.setBorderColour(borderColour);
    }

    public Model getModel() {
        return model;
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

    private void loadMemoryFromBinary(final int[] memory) throws IOException {
        for (int i = 0; i < 0xc000; i++) {
            memory[i] = inputStream.read();
        }
    }

    private void skipByte() throws IOException {
        skipBytes(1);
    }

    private void skipBytes(final int bytes) throws IOException {
        inputStream.skip(bytes);
    }

    private class SnapshotInfo {
        private boolean memoryIsCompressed;
        private int borderColour;

        public SnapshotInfo(boolean memoryIsCompressed, int borderColour) {
            this.memoryIsCompressed = memoryIsCompressed;
            this.borderColour = borderColour;
        }
    }
}
