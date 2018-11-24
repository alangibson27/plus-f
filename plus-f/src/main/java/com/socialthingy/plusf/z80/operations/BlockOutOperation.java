package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

abstract class BlockOutOperation extends Operation {
    protected final Processor processor;
    protected final Memory memory;
    protected final IO io;
    protected final Register bcReg;
    protected final Register bReg;
    protected final Register cReg;
    protected final Register hlReg;
    protected final FlagsRegister flagsRegister;
    protected final Register pcReg;

    protected BlockOutOperation(final Processor processor, final Memory memory, final IO io) {
        this.processor = processor;
        this.memory = memory;
        this.io = io;
        this.cReg = processor.register("c");
        this.flagsRegister = processor.flagsRegister();
        this.hlReg = processor.register("hl");
        this.bReg = processor.register("b");
        this.pcReg = processor.register("pc");
        this.bcReg = processor.register("bc");
    }

    protected void decrementBThenWrite(final int lowByte, final int highByte, final int hlDirection) {
        bReg.set(highByte - 1);
        final int hlValue = hlReg.get();
        final int toWrite = memory.get(hlValue);
        io.write(lowByte, bReg.get(), toWrite);
        hlReg.set((hlValue + hlDirection) & 0xffff);
    }

    protected boolean continueLoop() {
        if (bReg.get() != 0x0000) {
            pcReg.set(pcReg.get() - 2);
            return true;
        }

        return false;
    }
}
