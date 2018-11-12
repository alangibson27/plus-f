package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpIndr extends BlockInOperation {
    public OpIndr(final Processor processor, final Clock clock, final Memory memory, final IO io) {
        super(processor, clock, memory, io);
    }

    @Override
    public void execute() {
        readThenDecrementB(-1);
        flagsRegister.set(FlagsRegister.Flag.Z, true);
        flagsRegister.set(FlagsRegister.Flag.N, true);

        adjustPC();
    }

    @Override
    public String toString() {
        return "indr";
    }
}
