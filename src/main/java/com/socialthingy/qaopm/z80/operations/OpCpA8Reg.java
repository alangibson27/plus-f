package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpCpA8Reg extends ArithmeticOperation {

    private final Register register;

    public OpCpA8Reg(final Processor processor, final Register register) {
        super(processor, false);
        this.register = register;
    }

    @Override
    public int execute() {
        sub(register.get());
        return 4;
    }
}
