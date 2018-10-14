package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLdAddressA implements Operation {

    private final Processor processor;
    private final Memory memory;
    private final Register aReg;

    public OpLdAddressA(final Processor processor, final Memory memory) {
        this.processor = processor;
        this.memory = memory;
        this.aReg = processor.register("a");
    }

    @Override
    public int execute() {
        final int destination = Word.from(processor.fetchNextByte(), processor.fetchNextByte());
        memory.set( destination, aReg.get());
        return 13;
    }

    @Override
    public String toString() {
        return "ld (nn), a";
    }
}
