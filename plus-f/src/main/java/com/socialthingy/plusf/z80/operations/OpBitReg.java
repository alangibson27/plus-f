package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.FlagsRegister;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpBitReg extends BitOperation {

    private final Register register;
    private final String toString;

    public OpBitReg(final Processor processor, final Clock clock, final Register register, final int bitPosition) {
        super(processor, clock, bitPosition);
        this.register = register;

        this.toString = String.format("bit %d, %s", bitPosition, register.name());
    }

    @Override
    public void execute() {
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
