package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.z80.IO;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;

public class OpEdGroup implements Operation {

    private final Processor processor;
    private final int[] memory;
    private final IO io;
    private final Operation[] operations = new Operation[0x100];

    public OpEdGroup(final Processor processor, final int[] memory, final IO io) {
        this.processor = processor;
        this.memory = memory;
        this.io = io;

        prepareOperations();
    }

    private void prepareOperations() {
//            operations[0x40] = new OpIn8RegC(processor, io, "b");
//            operations[0x41] = new OpOutC8Reg(processor, io, "b");
//            operations[0x42] = new OpSbcHl16Reg(processor, "bc");
            operations[0x43] = new OpLdAddress16Reg(processor, memory, processor.register("bc"));
//            0x44: op_neg,
//            0x45: op_retn,
//            0x46: op_im0,
//            operations[0x47] = new OpLdIA(processor);
//            operations[0x48] = new OpIn8RegC(processor, io, "c");
//            operations[0x49] = new OpOutC8Reg(processor, io, "c");
//            operations[0x4a] = new OpAdcHl16Reg(processor, "bc");
            operations[0x4b] = new OpLd16RegAddress(processor, memory, processor.register("bc"));
//            0x4c: op_neg,
//            operations[0x4d] = new OpReti(processor);
//            0x4e: op_im0,
//            operations[0x4f] = new OpLdRA(processor);
//
//            operations[0x50] = new OpIn8RegC(processor, io, "d");
//            operations[0x51] = new OpOutC8Reg(processor, io, "d");
//            operations[0x52] = new OpSbcHl16Reg(processor, "de");
            operations[0x53] = new OpLdAddress16Reg(processor, memory, processor.register("de"));
//            0x54: op_neg,
//            0x55: op_retn,
//            operations[0x56] = new OpIm(processor, 1);
//            operations[0x57] = new OpLdAI(processor);
//            operations[0x58] = new OpIn8RegC(processor, io, "e");
//            operations[0x59] = new OpOutC8Reg(processor, io, "e");
//            operations[0x5a] = new OpAdcHl16Reg(processor, "de");
            operations[0x5b] = new OpLd16RegAddress(processor, memory, processor.register("de"));
//            0x5c: op_neg,
//            0x5d: op_retn,
//            operations[0x5e] = new OpIm(processor, 2);
//            operations[0x5f] = new OpLdAR(processor);
//
//            operations[0x60] = new OpIn8RegC(processor, io, "h");
//            operations[0x61] = new OpOutC8Reg(processor, io, "h");
//            operations[0x62] = new OpSbcHl16Reg(processor, "hl");
            operations[0x63] = new OpLdAddress16Reg(processor, memory, processor.register("hl"));
//            0x64: op_neg,
//            0x65: op_retn,
//            operations[0x66] = new OpIm(processor, 0);
            operations[0x67] = new OpRrd(processor, memory);
//            operations[0x68] = new OpIn8RegC(processor, io, "l");
//            operations[0x69] = new OpOutC8Reg(processor, io, "l");
//            operations[0x6a] = new OpAdcHl16Reg(processor, "hl");
            operations[0x6b] = new OpLd16RegAddress(processor, memory, processor.register("hl"));
//            0x6c: op_neg,
//            0x6d: op_retn,
//            0x6e: op_im0,
            operations[0x6f] = new OpRld(processor, memory);
//
//            0x70: op_in_a_c,
//            operations[0x71] = new OpOutCZero(processor, io);
//            operations[0x72] = new OpSbcHl16Reg(processor, "sp");
            operations[0x73] = new OpLdAddress16Reg(processor, memory, processor.register("sp"));
//            0x74: op_neg,
//            0x75: op_retn,
//            operations[0x76] = new OpIm(processor, 1);
//            0x78: op_in_a_c,
//            operations[0x79] = new OpOutC8Reg(processor, io, "a");
//            operations[0x7a] = new OpAdcHl16Reg(processor, "sp");
            operations[0x7b] = new OpLd16RegAddress(processor, memory, processor.register("sp"));
//            0x7c: op_neg,
//            0x7d: op_retn,
//            operations[0x7e] = new OpIm(processor, 2);
//
//            operations[0xa0] = new OpLdi(processor, memory);
//            operations[0xa1] = new OpCpi(processor, memory);
//            operations[0xa2] = new OpIni(processor, memory, io);
//            operations[0xa3] = new OpOuti(processor, memory, io);
//            operations[0xa8] = new OpLdd(processor, memory);
//            operations[0xa9] = new OpCpd(processor, memory);
//            operations[0xaa] = new OpInd(processor, memory, io);
//            operations[0xab] = new OpOutd(processor, memory, io);
//
//            operations[0xb0] = new OpLdir(processor, memory);
//            operations[0xb1] = new OpCpir(processor, memory);
//            operations[0xb2] = new OpInir(processor, memory, io);
//            operations[0xb3] = new OpOtir(processor, memory, io);
//            operations[0xb8] = new OpLddr(processor, memory);
//            operations[0xb9] = new OpCpdr(processor, memory);
//            operations[0xba] = new OpIndr(processor, memory, io);
//            operations[0xbb] = new OpOtdr(processor, memory, io;
    }

    @Override
    public int execute() {
        final Operation operation = operations[processor.fetchNextPC()];
        if (operation == null) {
            throw new IllegalStateException("Unimplemented operation");
        }
        return operation.execute();
    }
}
