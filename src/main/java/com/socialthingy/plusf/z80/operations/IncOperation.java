package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

abstract class IncOperation implements Operation {

    private final FlagsRegister flagsRegister;

    protected IncOperation(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
    }

    protected int increment(final int initialValue) {
        final int[] result = Bitwise.add(initialValue, 1);

        flagsRegister.set(FlagsRegister.Flag.P, initialValue == 0x7f);
        flagsRegister.set(FlagsRegister.Flag.S, (byte) result[0] < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, result[0] == 0);
        flagsRegister.set(FlagsRegister.Flag.H, result[1] == 1);
        flagsRegister.set(FlagsRegister.Flag.N, false);

        return result[0];
    }
}
