package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpAndAIndexed8Reg extends AndOperation {

    private final Register register;

    public OpAndAIndexed8Reg(final Processor processor, final Register register) {
        super(processor);
        this.register = register;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(initialPcValue + 1, 4);
        and(register.get());
    }

    @Override
    public String toString() {
        return "and " + register.name();
    }
}
