package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.WordRegister;

abstract class CallOperation implements Operation {
    protected final Processor processor;
    private final WordRegister pcReg;

    protected CallOperation(final Processor processor) {
        this.processor = processor;
        this.pcReg = WordRegister.class.cast(processor.register("pc"));
    }

    protected void call(final int address) {
        processor.pushByte(pcReg.getHigh());
        processor.pushByte(pcReg.getLow());
        pcReg.set(address);
    }
}
