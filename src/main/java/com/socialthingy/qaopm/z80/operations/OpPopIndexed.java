package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.util.Word;
import com.socialthingy.qaopm.z80.IndexRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;

public class OpPopIndexed implements Operation {
    private final Processor processor;
    private final IndexRegister indexRegister;

    public OpPopIndexed(final Processor processor, final IndexRegister indexRegister) {
        this.processor = processor;
        this.indexRegister = indexRegister;
    }

    @Override
    public int execute() {
        indexRegister.set(Word.from(processor.popByte(), processor.popByte()));
        return 14;
    }
}
