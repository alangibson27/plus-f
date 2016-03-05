package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.FlagsRegister;
import com.socialthingy.qaopm.z80.IO;
import com.socialthingy.qaopm.z80.Processor;

public class OpOutd extends BlockOutOperation {
    public OpOutd(final Processor processor, final int[] memory, final IO io) {
        super(processor, memory, io);
    }

    @Override
    public int execute() {
        decrementBThenWrite(-1);
        flagsRegister.set(FlagsRegister.Flag.Z, bReg.get() == 0);
        flagsRegister.set(FlagsRegister.Flag.N, true);
        return 16;
    }
}
