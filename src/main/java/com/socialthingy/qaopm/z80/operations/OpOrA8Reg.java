package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpOrA8Reg extends OrOperation {

    private final Register register;

    public OpOrA8Reg(final Processor processor, final Register register) {
        super(processor);
        this.register = register;
    }

    @Override
    public int execute() {
        or(register.get());
        return 4;
    }
}
