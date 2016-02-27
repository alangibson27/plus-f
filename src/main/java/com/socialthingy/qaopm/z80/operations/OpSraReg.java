package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpSraReg extends SraOperation {
    private final Register register;

    public OpSraReg(final Processor processor, final Register register) {
        super(processor);
        this.register = register;
    }

    @Override
    public int execute() {
        register.set(shift(register.get()));
        return 8;
    }
}
