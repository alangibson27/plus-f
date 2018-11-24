package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

import static com.socialthingy.plusf.util.Bitwise.HALF_CARRY_BIT;

abstract class DecOperation extends Operation {

    private final FlagsRegister flagsRegister;

    protected DecOperation(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
    }

    protected int decrement(final int initialValue) {
        final int result = Bitwise.sub(initialValue, 1);
        final int answer = result & 0xff;

        flagsRegister.set(FlagsRegister.Flag.P, initialValue == 0x80);
        flagsRegister.set(FlagsRegister.Flag.S, (byte) answer < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, answer == 0);
        flagsRegister.set(FlagsRegister.Flag.H, (result & HALF_CARRY_BIT) != 0);
        flagsRegister.set(FlagsRegister.Flag.N, true);
        flagsRegister.setUndocumentedFlagsFromValue(answer);
        return answer;
    }
}
