package com.socialthingy.plusf.z80.operations;

import com.socialthingy.plusf.z80.*;

public class OpExx extends Operation {

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
    public void execute(ContentionModel contentionModel, int initialPcValue, int irValue) {
        contentionModel.applyContention(initialPcValue, 4);
        exchange(bcReg, bcPrimeReg);
        exchange(deReg, dePrimeReg);
        exchange(hlReg, hlPrimeReg);
    }

    private void exchange(final Register reg1, final Register reg2) {
        final int temp = reg2.get();
        reg2.set(reg1.get());
        reg1.set(temp);
    }

    @Override
    public String toString() {
        return "exx";
    }
}
