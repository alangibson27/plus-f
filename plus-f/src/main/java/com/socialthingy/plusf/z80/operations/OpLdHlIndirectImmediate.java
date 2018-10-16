package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLdHlIndirectImmediate implements Operation {

    private final Processor processor;
    private final Register destReference;
    private final Memory memory;

    public OpLdHlIndirectImmediate(final Processor processor, final Memory memory) {
        this.processor = processor;
        this.destReference = processor.register("hl");
        this.memory = memory;
    }

    @Override
    public int execute() {
        memory.set( destReference.get(), processor.fetchNextByte());
        return 10;
    }

    @Override
    public String toString() {
        return "ld (hl), n";
    }
}
