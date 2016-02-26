package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpExx implements Operation {

    private final Register bcReg;
    private final Register bcPrimeReg;
    private final Register deReg;
    private final Register dePrimeReg;
    private final Register hlReg;
    private final Register hlPrimeReg;

    public OpExx(final Processor processor) {
        this.bcReg = processor.register("bc");
        this.bcPrimeReg = processor.register("bc'");
        this.deReg = processor.register("de");
        this.dePrimeReg = processor.register("de'");
        this.hlReg = processor.register("hl");
        this.hlPrimeReg = processor.register("hl'");
    }

    @Override
    public int execute() {
        exchange(bcReg, bcPrimeReg);
        exchange(deReg, dePrimeReg);
        exchange(hlReg, hlPrimeReg);
        return 4;
    }

    private void exchange(final Register reg1, final Register reg2) {
        final int temp = reg2.get();
        reg2.set(reg1.get());
        reg1.set(temp);
    }
}
