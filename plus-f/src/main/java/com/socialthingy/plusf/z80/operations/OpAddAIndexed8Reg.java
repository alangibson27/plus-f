package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpAddAIndexed8Reg extends ArithmeticOperation {

    private final Register register;
    private final String toString;

    public OpAddAIndexed8Reg(final Processor processor, final Register register, final boolean useCarryFlag) {
        super(processor, useCarryFlag);
        this.register = register;

        if (useCarryFlag) {
            this.toString = "adc a, " + register.name();
        } else {
            this.toString = "add a, " + register.name();
        }
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        add(register.get());
    }

    @Override
    public String toString() {
        return toString;
    }
}