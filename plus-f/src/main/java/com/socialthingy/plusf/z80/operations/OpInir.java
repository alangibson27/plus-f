package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpInir extends BlockInOperation {
    public OpInir(final Processor processor, final Clock clock, final Memory memory, final IO io) {
        super(processor, clock, memory, io);
    }

    @Override
    public void execute() {
        readThenDecrementB(1);
        flagsRegister.set(FlagsRegister.Flag.Z, true);
        flagsRegister.set(FlagsRegister.Flag.N, true);

        clock.tick(adjustPC());
    }

    @Override
    public String toString() {
        return "inir";
    }
}
