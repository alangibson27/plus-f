package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpCpl implements Operation {
    private final FlagsRegister flagsRegister;
    private final Register accumulator;

    public OpCpl(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
    }

    @Override
    public int execute() {
        accumulator.set(0xff - accumulator.get());
        flagsRegister.set(FlagsRegister.Flag.H, true);
        flagsRegister.set(FlagsRegister.Flag.N, true);
        return 4;
    }

    @Override
    public String toString() {
        return "cpl";
    }
}
