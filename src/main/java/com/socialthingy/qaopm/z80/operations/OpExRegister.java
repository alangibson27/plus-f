package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpExRegister implements Operation {

    private final Register reg1;
    private final Register reg2;

    public OpExRegister(final Register reg1, final Register reg2) {
        this.reg1 = reg1;
        this.reg2 = reg2;
    }

    @Override
    public int execute() {
        final int temp = reg2.get();
        reg2.set(reg1.get());
        reg1.set(temp);
        return 4;
    }
}
