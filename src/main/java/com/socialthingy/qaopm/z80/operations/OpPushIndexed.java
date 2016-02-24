package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.IndexRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;

public class OpPushIndexed implements Operation {
    private final Processor processor;
    private final IndexRegister indexRegister;

    public OpPushIndexed(final Processor processor, final IndexRegister indexRegister) {
        this.processor = processor;
        this.indexRegister = indexRegister;
    }

    @Override
    public int execute() {
        final int value = indexRegister.get();
        processor.pushByte((value & 0xff00) >> 8);
        processor.pushByte(value & 0x00ff);
        return 15;
    }
}
