package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

abstract class BitOperation implements Operation {
    protected final FlagsRegister flagsRegister;
    protected final int bitPosition;

    BitOperation(final Processor processor, final int bitPosition) {
        this.flagsRegister = processor.flagsRegister();
        this.bitPosition = bitPosition;
    }

    protected boolean checkBit(final int value) {
        final boolean bitSet = (value & (1 << bitPosition)) > 0;
        flagsRegister.set(FlagsRegister.Flag.Z, !bitSet);
        flagsRegister.set(FlagsRegister.Flag.P, !bitSet);
        flagsRegister.set(FlagsRegister.Flag.H, true);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        return bitSet;
    }
}
