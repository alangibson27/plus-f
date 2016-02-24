package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.BytePairRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpPush16Reg implements Operation {
    private final BytePairRegister register;
    private final Processor processor;

    public OpPush16Reg(final Processor processor, final Register register) {
        this.processor = processor;
        this.register = BytePairRegister.class.cast(register);
    }

    @Override
    public int execute() {
        processor.pushByte(register.getHigh());
        processor.pushByte(register.getLow());
        return 11;
    }
}
