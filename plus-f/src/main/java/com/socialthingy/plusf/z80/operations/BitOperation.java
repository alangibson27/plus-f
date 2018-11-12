package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

abstract class BitOperation extends Operation {
    protected final FlagsRegister flagsRegister;
    protected final int bitPosition;

    BitOperation(final Processor processor, final Clock clock, final int bitPosition) {
        super(clock);
        this.flagsRegister = processor.flagsRegister();
        this.bitPosition = bitPosition;
    }

    protected boolean checkBit(final int value) {
        final boolean bitSet = bitSet(value, bitPosition);
        flagsRegister.set(FlagsRegister.Flag.Z, !bitSet);
        flagsRegister.set(FlagsRegister.Flag.P, !bitSet);
        flagsRegister.set(FlagsRegister.Flag.H, true);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.set(FlagsRegister.Flag.S, bitPosition == 7 && bitSet);
        return bitSet;
    }

    protected boolean bitSet(final int value, final int position) {
        return (value & (1 << position)) > 0;
    }
}
