package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.util.Bitwise;
import com.socialthingy.qaopm.util.Word;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

abstract class RetOperation implements Operation {
    protected final Processor processor;
    protected final Register pcReg;

    protected RetOperation(final Processor processor) {
        this.processor = processor;
        this.pcReg = processor.register("pc");
    }

    protected void ret() {
        this.pcReg.set(Word.from(processor.popByte(), processor.popByte()));
    }
}
