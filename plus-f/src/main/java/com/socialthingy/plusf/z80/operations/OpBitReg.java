package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpBitReg extends BitOperation {
    private final Register register;
    private final String toString;

    public OpBitReg(final Processor processor, final Register register, final int bitPosition) {
        super(processor, bitPosition);
        this.register = register;

        this.toString = String.format("bit %d, %s", bitPosition, register.name());
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        final int value = register.get();
        checkBit(value);
        flagsRegister.set(FlagsRegister.Flag.F3, bitSet(value, 3));
        flagsRegister.set(FlagsRegister.Flag.F5, bitSet(value, 5));
    }

    @Override
    public String toString() {
        return toString;
    }
}
