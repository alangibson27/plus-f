package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.*;

public class OpIni extends BlockInOperation {
    public OpIni(final Processor processor, final int[] memory, final IO io) {
        super(processor, memory, io);
    }

    @Override
    public int execute() {
        readThenDecrementB(1);
        flagsRegister.set(FlagsRegister.Flag.Z, bReg.get() == 0);
        flagsRegister.set(FlagsRegister.Flag.N, true);
        return 16;
    }
}
