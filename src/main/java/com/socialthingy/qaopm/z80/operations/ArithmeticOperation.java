package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.FlagsRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

abstract class ArithmeticOperation implements Operation {
    protected final Register accumulator;
    protected final FlagsRegister flagsRegister;
    protected final Processor processor;
    protected final boolean useCarryFlag;

    ArithmeticOperation(final Processor processor, final boolean useCarryFlag) {
        this.processor = processor;
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
        this.useCarryFlag = useCarryFlag;
    }

    protected void add(int value) {
        final byte signedAccumulator = (byte) accumulator.get();

        if (useCarryFlag && flagsRegister.getC()) {
            value = (value + 1) & 0xff;
        }

        final int[] result = bitwiseAdd(accumulator.get(), value);
        accumulator.set(result[0]);
        flagsRegister.setN(false);
        setCommonFlags(signedAccumulator, result);
    }

    protected int sub(int value) {
        final byte signedAccumulator = (byte) accumulator.get();

        if (useCarryFlag && flagsRegister.getC()) {
            value = (value + 1) & 0xff;
        }

        final int[] result = bitwiseSub(accumulator.get(), value);
        flagsRegister.setN(true);
        setCommonFlags(signedAccumulator, result);
        return result[0];
    }

    protected void setCommonFlags(final byte signedAccumulator, final int[] result) {
        final byte signedResult = (byte) result[0];
        flagsRegister.setS(signedResult < 0);
        flagsRegister.setZ(result[0] == 0);
        flagsRegister.setH(result[1] == 1);
        flagsRegister.setP((signedAccumulator < 0) != (signedResult < 0));
        flagsRegister.setC(result[2] == 1);
    }

    private int[] bitwiseAdd(final int v1, final int v2) {
        final int v1Low = v1 & 0x0f;
        final int v2Low = v2 & 0x0f;

        final int resultLow = v1Low + v2Low;
        final int result = v1 + v2;
        return new int[] {result & 0xff, resultLow > 0x0f ? 1 : 0, result > 0xff ? 1 : 0};
    }

    private int[] bitwiseSub(final int v1, final int v2) {
        final int v1Low = v1 & 0x0f;
        final int v2Low = v2 & 0x0f;

        final int resultLow = v1Low - v2Low;
        final int result = v1 - v2;
        return new int[] {result & 0xff, resultLow < 0x00 ? 1 : 0, result < 0x00 ? 1 : 0};
    }
}
