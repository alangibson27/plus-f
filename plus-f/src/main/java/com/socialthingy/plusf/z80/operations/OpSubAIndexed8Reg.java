package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpSubAIndexed8Reg extends ArithmeticOperation {
    private final Register register;
    private final String toString;

    public OpSubAIndexed8Reg(final Processor processor, final Clock clock, final Register register, final boolean useCarryFlag) {
        super(processor, clock, useCarryFlag);
        this.register = register;

        if (useCarryFlag) {
            this.toString = "sbc a, " + register.name();
        } else {
            this.toString = "sub a, " + register.name();
        }
    }

    @Override
    public void execute() {
        accumulator.set(sub(register.get(), true));
        flagsRegister.setUndocumentedFlagsFromValue(accumulator.get());
    }

    @Override
    public String toString() {
        return toString;
    }
}