package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.IO;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;

public class GroupEd implements Operation {

    private final Processor processor;
    private final int[] memory;
    private final IO io;
    private final Operation[] operations = new Operation[0x100];

    public GroupEd(final Processor processor, final int[] memory, final IO io) {
        this.processor = processor;
        this.memory = memory;
        this.io = io;

        prepareOperations();
    }

    private void prepareOperations() {
        final Operation opNeg = new OpNeg(processor);
        final OpRetn opRetn = new OpRetn(processor);
        final OpIm opIm1 = new OpIm(processor, 1);
        final OpIm opIm2 = new OpIm(processor, 2);
        final OpIm opIm0 = new OpIm(processor, 0);
        final OpIn8RegC opInAC = new OpIn8RegC(processor, io, processor.register("a"));

        operations[0x40] = new OpIn8RegC(processor, io, processor.register("b"));
        operations[0x41] = new OpOutC8Reg(processor, io, processor.register("b"));
        operations[0x42] = new OpSbcHl16Reg(processor, processor.register("bc"));
        operations[0x43] = new OpLdAddress16Reg(processor, memory, processor.register("bc"));
        operations[0x44] = opNeg;
        operations[0x45] = opRetn;
        operations[0x46] = opIm0;
        operations[0x47] = new OpLdIA(processor);
        operations[0x48] = new OpIn8RegC(processor, io, processor.register("c"));
        operations[0x49] = new OpOutC8Reg(processor, io, processor.register("c"));
        operations[0x4a] = new OpAdcHl16Reg(processor, processor.register("bc"));
        operations[0x4b] = new OpLd16RegAddress(processor, memory, processor.register("bc"));
        operations[0x4c] = opNeg;
        operations[0x4d] = new OpReti(processor);
        operations[0x4e] = opIm0;
        operations[0x4f] = new OpLdRA(processor);

        operations[0x50] = new OpIn8RegC(processor, io, processor.register("d"));
        operations[0x51] = new OpOutC8Reg(processor, io, processor.register("d"));
        operations[0x52] = new OpSbcHl16Reg(processor, processor.register("de"));
        operations[0x53] = new OpLdAddress16Reg(processor, memory, processor.register("de"));
        operations[0x54] = opNeg;
        operations[0x55] = opRetn;
        operations[0x56] = opIm1;
        operations[0x57] = new OpLdAI(processor);
        operations[0x58] = new OpIn8RegC(processor, io, processor.register("e"));
        operations[0x59] = new OpOutC8Reg(processor, io, processor.register("e"));
        operations[0x5a] = new OpAdcHl16Reg(processor, processor.register("de"));
        operations[0x5b] = new OpLd16RegAddress(processor, memory, processor.register("de"));
        operations[0x5c] = opNeg;
        operations[0x5d] = opRetn;
        operations[0x5e] = opIm2;
        operations[0x5f] = new OpLdAR(processor);

        operations[0x60] = new OpIn8RegC(processor, io, processor.register("h"));
        operations[0x61] = new OpOutC8Reg(processor, io, processor.register("h"));
        operations[0x62] = new OpSbcHl16Reg(processor, processor.register("hl"));
        operations[0x63] = new OpLdAddress16Reg(processor, memory, processor.register("hl"));
        operations[0x64] = opNeg;
        operations[0x65] = opRetn;
        operations[0x66] = opIm0;
        operations[0x67] = new OpRrd(processor, memory);
        operations[0x68] = new OpIn8RegC(processor, io, processor.register("l"));
        operations[0x69] = new OpOutC8Reg(processor, io, processor.register("l"));
        operations[0x6a] = new OpAdcHl16Reg(processor, processor.register("hl"));
        operations[0x6b] = new OpLd16RegAddress(processor, memory, processor.register("hl"));
        operations[0x6c] = opNeg;
        operations[0x6d] = opRetn;
        operations[0x6e] = opIm0;
        operations[0x6f] = new OpRld(processor, memory);

        operations[0x70] = opInAC;
        operations[0x71] = new OpOutCZero(processor, io);
        operations[0x72] = new OpSbcHl16Reg(processor, processor.register("sp"));
        operations[0x73] = new OpLdAddress16Reg(processor, memory, processor.register("sp"));
        operations[0x74] = opNeg;
        operations[0x75] = opRetn;
        operations[0x76] = opIm1;
        operations[0x78] = opInAC;
        operations[0x79] = new OpOutC8Reg(processor, io, processor.register("a"));
        operations[0x7a] = new OpAdcHl16Reg(processor, processor.register("sp"));
        operations[0x7b] = new OpLd16RegAddress(processor, memory, processor.register("sp"));
        operations[0x7c] = opNeg;
        operations[0x7d] = opRetn;
        operations[0x7e] = opIm2;

        operations[0xa0] = new OpLdi(processor, memory);
        operations[0xa1] = new OpCpi(processor, memory);
        operations[0xa2] = new OpIni(processor, memory, io);
        operations[0xa3] = new OpOuti(processor, memory, io);
        operations[0xa8] = new OpLdd(processor, memory);
        operations[0xa9] = new OpCpd(processor, memory);
        operations[0xaa] = new OpInd(processor, memory, io);
        operations[0xab] = new OpOutd(processor, memory, io);

        operations[0xb0] = new OpLdir(processor, memory);
        operations[0xb1] = new OpCpir(processor, memory);
        operations[0xb2] = new OpInir(processor, memory, io);
        operations[0xb3] = new OpOtir(processor, memory, io);
        operations[0xb8] = new OpLddr(processor, memory);
        operations[0xb9] = new OpCpdr(processor, memory);
        operations[0xba] = new OpIndr(processor, memory, io);
        operations[0xbb] = new OpOtdr(processor, memory, io);
    }

    @Override
    public int execute() {
        final Operation operation = operations[processor.fetchNextPC()];
        processor.setLastOp(operation);
        if (operation == null) {
            throw new IllegalStateException("Unimplemented operation");
        }
        return operation.execute();
    }
}
