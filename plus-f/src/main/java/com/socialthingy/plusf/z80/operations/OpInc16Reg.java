package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Register;

public class OpInc16Reg extends Operation {
    private final Register register;

    public OpInc16Reg(final Register register) {
        this.register = register;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        contentionModel.applyContention(irValue, 1);
        contentionModel.applyContention(irValue, 1);
        register.set(register.get() + 1);
    }

    public String toString() {
        return "inc " + register.name();
    }
}
