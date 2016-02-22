package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.util.Bitwise;
import com.socialthingy.qaopm.z80.FlagsRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpAndA8Reg extends AndOperation {

    private final Register register;

    public OpAndA8Reg(final Processor processor, final Register register) {
        super(processor);
        this.register = register;
    }

    @Override
    public int execute() {
        and(register.get());
        return 4;
    }
}
