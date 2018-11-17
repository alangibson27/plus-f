package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.ContentionModel;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Register;

public class OpExRegister extends Operation {

    private final Register reg1;
    private final Register reg2;

    public OpExRegister(final Register reg1, final Register reg2) {
        this.reg1 = reg1;
        this.reg2 = reg2;
    }

    @Override
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        final int temp = reg2.get();
        reg2.set(reg1.get());
        reg1.set(temp);
    }

    @Override
    public String toString() {
        return "ex " + reg1.name() + ", " + reg2.name();
    }
}
