package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.*;
import com.socialthingy.plusf.z80.FlagsRegister.Flag;

import static com.socialthingy.plusf.util.Bitwise.FULL_CARRY_BIT;
import static com.socialthingy.plusf.util.Bitwise.HALF_CARRY_BIT;

abstract class ArithmeticOperation extends Operation {
    protected final Register accumulator;
    protected final FlagsRegister flagsRegister;
    protected final Processor processor;
    protected final boolean useCarryFlag;

    ArithmeticOperation(final Processor processor, final Clock clock, final boolean useCarryFlag) {
        super(clock);
        this.processor = processor;
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
        this.useCarryFlag = useCarryFlag;
    }

    protected void add(int value) {
        final byte signedAccumulator = (byte) accumulator.get();

        if (useCarryFlag && flagsRegister.get(Flag.C)) {
            value = value + 1;
        }

        final int result = Bitwise.add(accumulator.get(), value);
        final int answer = result & 0xff;
        accumulator.set(answer);
        flagsRegister.set(Flag.N, false);
        setCommonFlags(signedAccumulator, result);
        flagsRegister.setUndocumentedFlagsFromValue(answer);
    }

    protected int sub(int value, final boolean setUndocumentedFlagsFromResult) {
        final byte signedAccumulator = (byte) accumulator.get();

        if (useCarryFlag && flagsRegister.get(Flag.C)) {
            value = value + 1;
        }

        final int result = Bitwise.sub(accumulator.get(), value);
        final int answer = result & 0xff;
        flagsRegister.set(Flag.N, true);
        setCommonFlags(signedAccumulator, result);
        if (setUndocumentedFlagsFromResult) {
            flagsRegister.setUndocumentedFlagsFromValue(answer);
        } else {
            flagsRegister.setUndocumentedFlagsFromValue(value);
        }
        return answer;
    }

    protected void setCommonFlags(final byte signedAccumulator, final int result) {
        final byte signedResult = (byte) (result & 0xff);
        flagsRegister.set(Flag.S, signedResult < 0);
        flagsRegister.set(Flag.Z, (result & 0xff) == 0);
        flagsRegister.set(Flag.H, (result & HALF_CARRY_BIT) != 0);
        flagsRegister.set(Flag.P, (signedAccumulator < 0) != (signedResult < 0));
        flagsRegister.set(Flag.C, (result & FULL_CARRY_BIT) != 0);
    }
}
