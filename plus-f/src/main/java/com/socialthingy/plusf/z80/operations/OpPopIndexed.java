package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;

public class OpPopIndexed extends Operation {
    private final Processor processor;
    private final IndexRegister indexRegister;

    public OpPopIndexed(final Processor processor, final Clock clock, final IndexRegister indexRegister) {
        super(clock);
        this.processor = processor;
        this.indexRegister = indexRegister;
    }

    @Override
    public void execute() {
        indexRegister.set(Word.from(processor.popByte(), processor.popByte()));
    }

    @Override
    public String toString() {
        return "pop " + indexRegister.name();
    }
}
