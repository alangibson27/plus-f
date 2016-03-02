package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.util.Word;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpLd16RegAddress implements Operation {
    private final Processor processor;
    private final int[] memory;
    private final Register dest;

    public OpLd16RegAddress(final Processor processor, final int[] memory, final Register dest) {
        this.processor = processor;
        this.memory = memory;
        this.dest = dest;
    }

    @Override
    public int execute() {
        final int source = processor.fetchNextWord();
        dest.set(Word.from(memory[source], memory[(source + 1) & 0xffff]));
        return 20;
    }
}