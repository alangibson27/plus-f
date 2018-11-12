package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.*;

public class OpLd16RegAddress extends Operation {
    private final Processor processor;
    private final Memory memory;
    private final Register dest;

    public OpLd16RegAddress(final Processor processor, final Clock clock, final Memory memory, final Register dest) {
        super(clock);
        this.processor = processor;
        this.memory = memory;
        this.dest = dest;
    }

    @Override
    public void execute() {
        final int source = processor.fetchNextWord();
        dest.set(Word.from(memory.get(source), memory.get(source + 1)));
    }

    @Override
    public String toString() {
        return String.format("ld %s, (nn)", dest.name());
    }
}
