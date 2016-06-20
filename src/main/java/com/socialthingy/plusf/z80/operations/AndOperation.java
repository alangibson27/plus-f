package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

abstract class AndOperation implements Operation {
    protected final FlagsRegister flagsRegister;
    protected final Register accumulator;

    AndOperation(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
    }

    protected void and(final int value) {
        final byte signedAccumulator = (byte) accumulator.get();
        final int result = accumulator.set(accumulator.get() & value);
        final byte signedResult = (byte) result;
        flagsRegister.set(FlagsRegister.Flag.S, (result & 0b10000000) > 0);
        flagsRegister.set(FlagsRegister.Flag.Z, result == 0);
        flagsRegister.set(FlagsRegister.Flag.H, true);
        flagsRegister.set(FlagsRegister.Flag.P, (signedAccumulator < 0) != (signedResult < 0));
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.C, false);
    }
}
