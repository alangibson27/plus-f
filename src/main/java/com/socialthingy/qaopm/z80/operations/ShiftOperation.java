package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.util.Bitwise;
import com.socialthingy.qaopm.z80.FlagsRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;

abstract class ShiftOperation implements Operation {
    private final FlagsRegister flagsRegister;

    protected ShiftOperation(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
    }

    protected void updateFlags(final int result, final int carry) {
        flagsRegister.set(FlagsRegister.Flag.C, carry == 1);
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.S, (byte) result < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, result == 0);
        flagsRegister.set(FlagsRegister.Flag.P, Bitwise.hasParity(result));
    }
}
