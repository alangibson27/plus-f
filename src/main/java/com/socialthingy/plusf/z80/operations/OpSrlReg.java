package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpSrlReg extends SrlOperation {
    private final Register register;

    public OpSrlReg(final Processor processor, final Register register) {
        super(processor);
        this.register = register;
    }

    @Override
    public int execute() {
        register.set(shift(register.get()));
        return 8;
    }

    @Override
    public String toString() {
        return "srl " + register.name();
    }
}
