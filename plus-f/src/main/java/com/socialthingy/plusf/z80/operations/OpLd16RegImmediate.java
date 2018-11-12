package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLd16RegImmediate extends Operation {
    private final Register destReg;
    private final Processor processor;

    public OpLd16RegImmediate(final Processor processor, final Clock clock, final Register destReg) {
        super(clock);
        this.processor = processor;
        this.destReg = destReg;
    }

    @Override
    public void execute() {
        final int value = Word.from(processor.fetchNextByte(), processor.fetchNextByte());
        destReg.set(value);
    }

    @Override
    public String toString() {
        return String.format("ld %s, nn", destReg.name());
    }
}
