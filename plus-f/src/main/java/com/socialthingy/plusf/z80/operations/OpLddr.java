package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;

public class OpLddr extends BlockOperation {
    public OpLddr(final Processor processor, final Clock clock, final Memory memory) {
        super(processor, clock, memory, -1);
    }

    @Override
    public void execute() {
        blockTransfer();
        flagsRegister.set(FlagsRegister.Flag.P, false);
        clock.tick(adjustPC());
    }

    @Override
    public String toString() {
        return "lddr";
    }
}
