package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

abstract class ShiftOperation extends Operation {
    private final FlagsRegister flagsRegister;

    protected ShiftOperation(final Processor processor, final Clock clock) {
        super(clock);
        this.flagsRegister = processor.flagsRegister();
    }

    protected void updateFlags(final int result, final int carry) {
        flagsRegister.set(FlagsRegister.Flag.C, carry == 1);
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.S, (byte) result < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, result == 0);
        flagsRegister.set(FlagsRegister.Flag.P, Bitwise.hasParity(result));
        flagsRegister.setUndocumentedFlagsFromValue(result);
    }
}
