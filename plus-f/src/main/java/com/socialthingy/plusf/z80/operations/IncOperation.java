package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

import static com.socialthingy.plusf.util.Bitwise.HALF_CARRY_BIT;

abstract class IncOperation extends Operation {

    private final FlagsRegister flagsRegister;

    protected IncOperation(final Processor processor, final Clock clock) {
        super(clock);
        this.flagsRegister = processor.flagsRegister();
    }

    protected int increment(final int initialValue) {
        final int result = Bitwise.add(initialValue, 1);
        final int answer = result & 0xff;

        flagsRegister.set(FlagsRegister.Flag.P, initialValue == 0x7f);
        flagsRegister.set(FlagsRegister.Flag.S, (byte) answer < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, answer == 0);
        flagsRegister.set(FlagsRegister.Flag.H, (result & HALF_CARRY_BIT) != 0);
        flagsRegister.set(FlagsRegister.Flag.N, false);
        flagsRegister.setUndocumentedFlagsFromValue(answer);

        return answer;
    }
}
