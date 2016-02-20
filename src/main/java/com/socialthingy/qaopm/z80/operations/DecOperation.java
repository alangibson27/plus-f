package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.util.Bitwise;
import com.socialthingy.qaopm.z80.FlagsRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;

abstract class DecOperation implements Operation {

    private final FlagsRegister flagsRegister;

    protected DecOperation(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
    }

    protected int decrement(final int initialValue) {
        final int[] result = Bitwise.sub(initialValue, 1);

        flagsRegister.set(FlagsRegister.Flag.P, initialValue == 0x80);
        flagsRegister.set(FlagsRegister.Flag.S, (byte) result[0] < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, result[0] == 0);
        flagsRegister.set(FlagsRegister.Flag.H, result[1] == 1);
        flagsRegister.set(FlagsRegister.Flag.N, true);

        return result[0];
    }
}
