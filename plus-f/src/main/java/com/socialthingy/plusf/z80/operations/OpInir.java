package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.IO;
import com.socialthingy.plusf.z80.Processor;

public class OpInir extends BlockInOperation {
    public OpInir(final Processor processor, final int[] memory, final IO io) {
        super(processor, memory, io);
    }

    @Override
    public int execute() {
        readThenDecrementB(1);
        flagsRegister.set(FlagsRegister.Flag.Z, true);
        flagsRegister.set(FlagsRegister.Flag.N, true);

        return adjustPC();
    }

    @Override
    public String toString() {
        return "inir";
    }
}