package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpNeg implements Operation {
    private final FlagsRegister flagsRegister;
    private final Register accumulator;

    public OpNeg(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
    }

    @Override
    public int execute() {
        final int[] result = Bitwise.sub(0, accumulator.get());
        accumulator.set(result[0]);
        flagsRegister.set(FlagsRegister.Flag.S, (byte) result[0] < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, result[0] == 0);
        flagsRegister.set(FlagsRegister.Flag.H, result[1] != 0);
        flagsRegister.set(FlagsRegister.Flag.P, result[0] == 0x80);
        flagsRegister.set(FlagsRegister.Flag.N, true);
        flagsRegister.set(FlagsRegister.Flag.C, result[0] != 0);
        return 8;
    }
}
