package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

abstract class BlockInOperation implements Operation {
    protected final Processor processor;
    protected final int[] memory;
    protected final IO io;
    protected final Register bReg;
    protected final Register cReg;
    protected final Register hlReg;
    protected final FlagsRegister flagsRegister;
    protected final Register pcReg;

    protected BlockInOperation(final Processor processor, final int[] memory, final IO io) {
        this.processor = processor;
        this.memory = memory;
        this.io = io;
        this.cReg = processor.register("c");
        this.flagsRegister = processor.flagsRegister();
        this.hlReg = processor.register("hl");
        this.bReg = processor.register("b");
        this.pcReg = processor.register("pc");
    }

    protected void readThenDecrementB(final int hlDirection) {
        final int bVal = bReg.get();
        final int value = io.read(cReg.get(), bVal);
        final int hlValue = hlReg.get();
        Memory.set(memory, hlValue, value);
        bReg.set((bVal - 1) & 0xff);
        hlReg.set((hlValue + hlDirection) & 0xffff);
    }

    protected int adjustPC() {
        if (bReg.get() != 0x0000) {
            pcReg.set(pcReg.get() - 2);
            return 21;
        } else {
            return 16;
        }
    }
}
