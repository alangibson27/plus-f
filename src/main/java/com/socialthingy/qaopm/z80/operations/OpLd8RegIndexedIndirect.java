package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpLd8RegIndexedIndirect implements Operation {

    private final Processor processor;
    private final int[] memory;
    private final Register dest;
    private final Register indexRegister;

    public OpLd8RegIndexedIndirect(final Processor processor, final int[] memory, final Register dest, final Register indexRegister) {
        this.processor = processor;
        this.memory = memory;
        this.dest = dest;
        this.indexRegister = indexRegister;
    }

    @Override
    public int execute() {
        final byte offset = (byte) processor.fetchNextPC();
        dest.set(memory[(indexRegister.get() + offset) & 0xffff]);
        return 19;
    }
}
