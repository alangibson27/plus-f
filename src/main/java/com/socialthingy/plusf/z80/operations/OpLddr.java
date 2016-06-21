package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Processor;

public class OpLddr extends BlockOperation {
    public OpLddr(final Processor processor, final int[] memory) {
        super(processor, memory, -1);
    }

    @Override
    public int execute() {
        blockTransfer();
        flagsRegister.set(FlagsRegister.Flag.P, false);
        return adjustPC();
    }

    @Override
    public String toString() {
        return "lddr";
    }
}
