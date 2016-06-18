package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

public class OpLdAAddress implements Operation {

    private final Processor processor;
    private final Register aReg;
    private final int[] memory;

    public OpLdAAddress(final Processor processor, final int[] memory) {
        this.processor = processor;
        this.memory = memory;
        this.aReg = processor.register("a");
    }

    @Override
    public int execute() {
        final int address = Word.from(processor.fetchNextByte(), processor.fetchNextByte());
        aReg.set(memory[address]);
        return 13;
    }

    @Override
    public String toString() {
        return "ld a, (nn)";
    }
}
