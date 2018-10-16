package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLd16RegAddress implements Operation {
    private final Processor processor;
    private final Memory memory;
    private final Register dest;

    public OpLd16RegAddress(final Processor processor, final Memory memory, final Register dest) {
        this.processor = processor;
        this.memory = memory;
        this.dest = dest;
    }

    @Override
    public int execute() {
        final int source = processor.fetchNextWord();
        dest.set(Word.from(memory.get(source), memory.get(source + 1)));
        return 20;
    }

    @Override
    public String toString() {
        return String.format("ld %s, (nn)", dest.name());
    }
}
