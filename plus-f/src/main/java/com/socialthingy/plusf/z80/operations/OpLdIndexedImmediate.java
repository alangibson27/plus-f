package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.*;

public class OpLdIndexedImmediate extends Operation {
    private final Processor processor;
    private final Register indexRegister;

    public OpLdIndexedImmediate(final Processor processor, final Clock clock, final IndexRegister indexRegister) {
        super(clock);
        this.processor = processor;
        this.indexRegister = indexRegister;
    }

    @Override
    public void execute() {
        final int value = Word.from(processor.fetchNextByte(), processor.fetchNextByte());
        indexRegister.set(value);
        clock.tick(6);
    }

    @Override
    public String toString() {
        return String.format("ld %s, nn", indexRegister.name());
    }
}
