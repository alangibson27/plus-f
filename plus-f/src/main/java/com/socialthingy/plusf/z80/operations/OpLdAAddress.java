package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.*;

public class OpLdAAddress extends Operation {

    private final Processor processor;
    private final Register aReg;
    private final Memory memory;

    public OpLdAAddress(final Processor processor, final Clock clock, final Memory memory) {
        super(clock);
        this.processor = processor;
        this.memory = memory;
        this.aReg = processor.register("a");
    }

    @Override
    public void execute() {
        final int address = Word.from(processor.fetchNextByte(), processor.fetchNextByte());
        aReg.set(memory.get(address));
        clock.tick(9);
    }

    @Override
    public String toString() {
        return "ld a, (nn)";
    }
}
