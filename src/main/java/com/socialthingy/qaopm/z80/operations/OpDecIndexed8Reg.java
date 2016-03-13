package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpDecIndexed8Reg extends DecOperation {

    private final Register register;

    public OpDecIndexed8Reg(final Processor processor, final Register register) {
        super(processor);
        this.register = register;
    }

    @Override
    public int execute() {
        register.set(decrement(register.get()));
        return 8;
    }
}
