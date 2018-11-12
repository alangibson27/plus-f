package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.util.Word;
import com.socialthingy.plusf.z80.Clock;
import com.socialthingy.plusf.z80.Operation;
import com.socialthingy.plusf.z80.Processor;
import com.socialthingy.plusf.z80.Register;

abstract class RetOperation extends Operation {
    protected final Processor processor;
    protected final Register pcReg;

    protected RetOperation(final Processor processor, final Clock clock) {
        super(clock);
        this.processor = processor;
        this.pcReg = processor.register("pc");
    }

    protected void ret() {
        this.pcReg.set(Word.from(processor.popByte(), processor.popByte()));
    }
}
