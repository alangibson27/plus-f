package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.FlagsRegister;
import com.socialthingy.qaopm.z80.IO;
import com.socialthingy.qaopm.z80.Processor;

public class OpOtdr extends BlockOutOperation {
    public OpOtdr(final Processor processor, final int[] memory, final IO io) {
        super(processor, memory, io);
    }

    @Override
    public int execute() {
        decrementBThenWrite(-1);
        flagsRegister.set(FlagsRegister.Flag.Z, true);
        flagsRegister.set(FlagsRegister.Flag.N, true);
        return adjustPC();
    }
}
