package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.*;

public class OpLdAddressA extends Operation {

    private final Processor processor;
    private final Memory memory;
    private final Register aReg;

    public OpLdAddressA(final Processor processor, final Clock clock, final Memory memory) {
        super(clock);
        this.processor = processor;
        this.memory = memory;
        this.aReg = processor.register("a");
    }

    @Override
    public void execute() {
        final int destination = Word.from(processor.fetchNextByte(), processor.fetchNextByte());
        memory.set( destination, aReg.get());
    }

    @Override
    public String toString() {
        return "ld (nn), a";
    }
}
