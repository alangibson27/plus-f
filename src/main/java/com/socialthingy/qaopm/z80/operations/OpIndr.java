package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.FlagsRegister;
import com.socialthingy.qaopm.z80.IO;
import com.socialthingy.qaopm.z80.Processor;

public class OpIndr extends BlockInOperation {
    public OpIndr(final Processor processor, final int[] memory, final IO io) {
        super(processor, memory, io);
    }

    @Override
    public int execute() {
        readThenDecrementB(-1);
        flagsRegister.set(FlagsRegister.Flag.Z, true);
        flagsRegister.set(FlagsRegister.Flag.N, true);

        return adjustPC();
    }
}
