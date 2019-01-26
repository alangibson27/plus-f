package com.socialthingy.plusf.snapshot;

import com.socialthingy.plusf.sound.AYChip;
import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.spectrum.io.*;
import com.socialthingy.plusf.ui.JoystickKeys;
import com.socialthingy.plusf.z80.Processor;

import java.io.IOException;
import java.io.OutputStream;

public class SnapshotSaver {
    private final SpectrumMemory memory;
    private final AYChip ayChip;
    private final JoystickKeys joystickKeys;
    private final ULA ula;
    private final Processor processor;
    private final Model model;
    private int[][] memoryPages;

    public SnapshotSaver(final Processor processor, final ULA ula, final SpectrumMemory memory, final Model model, final AYChip ayChip, final JoystickKeys joystickKeys) {
        this.processor = processor;
        this.ula = ula;
        this.memory = memory;
        this.model = model;
        this.ayChip = ayChip;
        this.joystickKeys = joystickKeys;
    }

    public void write(final OutputStream target) throws IOException {
        memoryPages = new int[8][];
        if (model == Model._48K) {
            memoryPages[1] = new int[0x4000];
            memoryPages[2] = new int[0x4000];
            memoryPages[5] = new int[0x4000];
            memory.copyFromBank(1, memoryPages[1]);
            memory.copyFromBank(2, memoryPages[2]);
            memory.copyFromBank(5, memoryPages[5]);
        } else {
            for (int i = 0; i < 8; i++) {
                memoryPages[i] = new int[0x4000];
                memory.copyFromBank(i, memoryPages[i]);
            }
        }

        int borderColour = ula.getBorderColour();
        int lastWriteTo0x7ffd;
        int lastWriteTo0x1ffd;
        if (memory instanceof Memory128K) {
            lastWriteTo0x7ffd = ((Memory128K) memory).getLastWriteTo0x7ffd();
        } else {
            lastWriteTo0x7ffd = -1;
        }

        if (memory instanceof MemoryPlus2A) {
            lastWriteTo0x1ffd = ((MemoryPlus2A) memory).getLastWriteTo0x1ffd();
        } else {
            lastWriteTo0x1ffd = -1;
        }

        int lastWriteTo0xfffd;
        if (ayChip != null) {
            lastWriteTo0xfffd = ayChip.getLastWriteTo0xfffd();
        } else {
            lastWriteTo0xfffd = -1;
        }
        int leftKey = joystickKeys.getLeft();
        int rightKey = joystickKeys.getRight();
        int downKey = joystickKeys.getDown();
        int upKey = joystickKeys.getUp();
        int fireKey = joystickKeys.getFire();

        target.write(processor.register("a").get());
        target.write(processor.register("f").get());
        target.write(processor.register("c").get());
        target.write(processor.register("b").get());
        target.write(processor.register("l").get());
        target.write(processor.register("h").get());
        target.write(0);
        target.write(0);
        writeWord(target, processor.register("sp").get());
        target.write(processor.register("i").get());
        target.write(processor.register("r").get());
        target.write(processor.register("r").get() >> 7 | borderColour << 1 | 0x20);
        target.write(processor.register("e").get());
        target.write(processor.register("d").get());
        writeWord(target, processor.register("bc'").get());
        writeWord(target, processor.register("de'").get());
        writeWord(target, processor.register("hl'").get());
        target.write(processor.register("a'").get());
        target.write(processor.register("f'").get());
        writeWord(target, processor.register("iy").get());
        writeWord(target, processor.register("ix").get());
        target.write(processor.getIff(0) ? 1 : 0);
        target.write(processor.getIff(1) ? 1 : 0);
        target.write(processor.getInterruptMode());

        target.write(55);
        target.write(0);

        writeWord(target, processor.register("pc").get());
        target.write(hwModeFrom(model));
        target.write(lastWriteTo0x7ffd < 0 ? 0 : lastWriteTo0x7ffd);
        target.write(0);
        target.write(0);
        target.write(lastWriteTo0xfffd < 0 ? 0 : lastWriteTo0xfffd);
        for (int i = 0; i < 16; i++) {
            target.write(0);
        }
        target.write(0);
        target.write(0);
        target.write(0);
        target.write(0);
        target.write(0);
        target.write(0);
        target.write(0);
        target.write(0);
        target.write(KeyTranslator.keyCodeToCoordinate(leftKey));
        target.write(KeyTranslator.keyCodeToCoordinate(rightKey));
        target.write(KeyTranslator.keyCodeToCoordinate(downKey));
        target.write(KeyTranslator.keyCodeToCoordinate(upKey));
        target.write(KeyTranslator.keyCodeToCoordinate(fireKey));
        target.write(KeyTranslator.keyCodeToDisplayable(leftKey));
        target.write(KeyTranslator.keyCodeToDisplayable(rightKey));
        target.write(KeyTranslator.keyCodeToDisplayable(downKey));
        target.write(KeyTranslator.keyCodeToDisplayable(upKey));
        target.write(KeyTranslator.keyCodeToDisplayable(fireKey));
        target.write(0);
        target.write(0);
        target.write(0);
        target.write(lastWriteTo0x1ffd);

        if (model == Model._48K) {
            writeMemory(target, 1, 4);
            writeMemory(target, 2, 5);
            writeMemory(target, 5, 8);
        } else {
            for (int i = 0; i <= 7; i++) {
                writeMemory(target, i, i + 3);
            }
        }

    }

    private void writeWord(final OutputStream target, final int word) throws IOException {
        target.write(word & 0xff);
        target.write(word >> 8);
    }

    private void writeMemory(final OutputStream fos, final int sourcePage, final int pageNumberInSnapshot) throws IOException {
        final int[] compressed = EDCompressor.INSTANCE.compress(memoryPages[sourcePage]);
        writeWord(fos, compressed.length);
        fos.write(pageNumberInSnapshot);
        for (int value: compressed) {
            fos.write(value);
        }
    }

    private int hwModeFrom(final Model model) {
        switch (model) {
            case _48K:
                return 0;

            case _128K:
            case _128K_SPANISH:
                return 4;

            case PLUS_2:
                return 12;

            default:
                return 13;
        }
    }
}
