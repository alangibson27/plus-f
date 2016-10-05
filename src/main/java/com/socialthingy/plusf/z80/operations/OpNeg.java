package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

import static com.socialthingy.plusf.util.Bitwise.HALF_CARRY_BIT;

public class OpNeg implements Operation {
    private final FlagsRegister flagsRegister;
    private final Register accumulator;

    public OpNeg(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
    }

    @Override
    public int execute() {
        final int result = Bitwise.sub(0, accumulator.get());
        final int answer = result & 0xff;
        accumulator.set(answer);
        flagsRegister.set(FlagsRegister.Flag.S, (byte) answer < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, answer == 0);
        flagsRegister.set(FlagsRegister.Flag.H, (result & HALF_CARRY_BIT) != 0);
        flagsRegister.set(FlagsRegister.Flag.P, answer == 0x80);
        flagsRegister.set(FlagsRegister.Flag.N, true);
        flagsRegister.set(FlagsRegister.Flag.C, answer != 0);
        flagsRegister.setUndocumentedFlagsFromValue(answer);
        return 8;
    }

    @Override
    public String toString() {
        return "neg";
    }
}
