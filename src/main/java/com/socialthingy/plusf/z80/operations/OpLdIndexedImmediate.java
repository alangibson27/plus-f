package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.IndexRegister;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLdIndexedImmediate implements Operation {
    private final Processor processor;
    private final Register indexRegister;

    public OpLdIndexedImmediate(final Processor processor, final IndexRegister indexRegister) {
        this.processor = processor;
        this.indexRegister = indexRegister;
    }

    @Override
    public int execute() {
        final int value = Word.from(processor.fetchNextPC(), processor.fetchNextPC());
        indexRegister.set(value);
        return 14;
    }
}
