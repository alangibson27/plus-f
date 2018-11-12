package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

abstract class BlockOutOperation extends Operation {
    protected final Processor processor;
    protected final Memory memory;
    protected final IO io;
    protected final Register bReg;
    protected final Register cReg;
    protected final Register hlReg;
    protected final FlagsRegister flagsRegister;
    protected final Register pcReg;

    protected BlockOutOperation(final Processor processor, final Clock clock, final Memory memory, final IO io) {
        super(clock);
        this.processor = processor;
        this.memory = memory;
        this.io = io;
        this.cReg = processor.register("c");
        this.flagsRegister = processor.flagsRegister();
        this.hlReg = processor.register("hl");
        this.bReg = processor.register("b");
        this.pcReg = processor.register("pc");
    }

    protected void decrementBThenWrite(final int hlDirection) {
        final int bVal = (bReg.get() - 1) & 0xff;
        bReg.set(bVal);
        final int hlValue = hlReg.get();
        final int toWrite = memory.get(hlValue);
        clock.tick(1);
        io.write(cReg.get(), bVal, toWrite);
        hlReg.set((hlValue + hlDirection) & 0xffff);
    }

    protected void adjustPC() {
        if (bReg.get() != 0x0000) {
            pcReg.set(pcReg.get() - 2);
            clock.tick(5);
        }
    }
}
