package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpOuti extends BlockOutOperation {
    public OpOuti(final Processor processor, final Clock clock, final Memory memory, final IO io) {
        super(processor, clock, memory, io);
    }

    @Override
    public void execute() {
        decrementBThenWrite(1);
        flagsRegister.set(FlagsRegister.Flag.Z, bReg.get() == 0);
        flagsRegister.set(FlagsRegister.Flag.N, true);
        clock.tick(8);
    }

    @Override
    public String toString() {
        return "outi";
    }
}
