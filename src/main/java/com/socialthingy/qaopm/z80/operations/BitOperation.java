package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.FlagsRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;

abstract class BitOperation implements Operation {
    protected final FlagsRegister flagsRegister;
    protected final int bitPosition;

    BitOperation(final Processor processor, final int bitPosition) {
        this.flagsRegister = processor.flagsRegister();
        this.bitPosition = bitPosition;
    }

    protected void checkBit(final int value) {
        flagsRegister.set(FlagsRegister.Flag.Z, (value & (1 << bitPosition)) == 0);
        flagsRegister.set(FlagsRegister.Flag.H, true);
        flagsRegister.set(FlagsRegister.Flag.N, false);
    }
}
