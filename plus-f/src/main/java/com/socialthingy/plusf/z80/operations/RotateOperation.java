package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Bitwise;
import com.socialthingy.plusf.util.UnsafeUtil;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;
import sun.misc.Unsafe;

abstract class RotateOperation implements Operation {
    protected final FlagsRegister flagsRegister;
    protected final Register accumulator;
    protected final Unsafe unsafe = UnsafeUtil.getUnsafe();

    RotateOperation(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
    }

    protected void setCarryAndNegateAfterRotate(final int carryBit) {
        flagsRegister.set(FlagsRegister.Flag.C, carryBit == 1);
        flagsRegister.set(FlagsRegister.Flag.H, false);
        flagsRegister.set(FlagsRegister.Flag.N, false);
    }

    protected int rlcValue(final int value) {
        final int highBit = value >> 7;
        final int result = ((value << 1) & 0xff) | highBit;
        setCarryAndNegateAfterRotate(highBit);
        flagsRegister.setUndocumentedFlagsFromValue(result);
        return result;
    }

    protected void setSignZeroAndParity(final int value) {
        flagsRegister.set(FlagsRegister.Flag.S, (byte) value < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, value == 0);
        flagsRegister.set(FlagsRegister.Flag.P, Bitwise.hasParity(value));
    }

    protected int rlValue(final int value) {
        final int highBit = value >> 7;
        final int result = ((value << 1) & 0xff) | (flagsRegister.get(FlagsRegister.Flag.C) ? 0b1 : 0);
        setCarryAndNegateAfterRotate(highBit);
        flagsRegister.setUndocumentedFlagsFromValue(result);
        return result;
    }

    protected int rrcValue(final int value) {
        final int lowBit = value & 0b1;
        final int result = (value >> 1) | (lowBit * 0b10000000);
        setCarryAndNegateAfterRotate(lowBit);
        flagsRegister.setUndocumentedFlagsFromValue(result);
        return result;
    }

    protected int rrValue(final int value) {
        final int lowBit = value & 0b1;
        final int result = (value >> 1) | (flagsRegister.get(FlagsRegister.Flag.C) ? 0b10000000 : 0);
        setCarryAndNegateAfterRotate(lowBit);
        flagsRegister.setUndocumentedFlagsFromValue(result);
        return result;
    }

    protected int lowNibble(final int value) {
        return value & 0b1111;
    }

    protected int highNibble(final int value) {
        return (value & 0b11110000) >> 4;
    }
}
