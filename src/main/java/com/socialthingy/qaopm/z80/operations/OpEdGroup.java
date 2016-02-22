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
//            operations[0x40] = OpIn8RegC(processor, io, 'b');
//            operations[0x41] = OpOutC8Reg(processor, io, 'b');
//            operations[0x42] = OpSbcHl16Reg(processor, 'bc');
//            operations[0x43] = OpLdAddress16Reg(processor, memory, 'bc');
//            0x44: op_neg,
//            0x45: op_retn,
//            0x46: op_im0,
//            operations[0x47] = OpLdIA(processor);
//            operations[0x48] = OpIn8RegC(processor, io, 'c');
//            operations[0x49] = OpOutC8Reg(processor, io, 'c');
//            operations[0x4a] = OpAdcHl16Reg(processor, 'bc');
//            operations[0x4b] = OpLd16RegAddress(processor, memory, 'bc');
//            0x4c: op_neg,
//            operations[0x4d] = OpReti(processor);
//            0x4e: op_im0,
//            operations[0x4f] = OpLdRA(processor);
//
//            operations[0x50] = OpIn8RegC(processor, io, 'd');
//            operations[0x51] = OpOutC8Reg(processor, io, 'd');
//            operations[0x52] = OpSbcHl16Reg(processor, 'de');
//            operations[0x53] = OpLdAddress16Reg(processor, memory, 'de');
//            0x54: op_neg,
//            0x55: op_retn,
//            operations[0x56] = OpIm(processor, 1);
//            operations[0x57] = OpLdAI(processor);
//            operations[0x58] = OpIn8RegC(processor, io, 'e');
//            operations[0x59] = OpOutC8Reg(processor, io, 'e');
//            operations[0x5a] = OpAdcHl16Reg(processor, 'de');
//            operations[0x5b] = OpLd16RegAddress(processor, memory, 'de');
//            0x5c: op_neg,
//            0x5d: op_retn,
//            operations[0x5e] = OpIm(processor, 2);
//            operations[0x5f] = OpLdAR(processor);
//
//            operations[0x60] = OpIn8RegC(processor, io, 'h');
//            operations[0x61] = OpOutC8Reg(processor, io, 'h');
//            operations[0x62] = OpSbcHl16Reg(processor, 'hl');
//            operations[0x63] = OpLdAddress16Reg(processor, memory, 'hl');
//            0x64: op_neg,
//            0x65: op_retn,
//            operations[0x66] = OpIm(processor, 0);
            operations[0x67] = new OpRrd(processor, memory);
//            operations[0x68] = OpIn8RegC(processor, io, 'l');
//            operations[0x69] = OpOutC8Reg(processor, io, 'l');
//            operations[0x6a] = OpAdcHl16Reg(processor, 'hl');
//            operations[0x6b] = OpLd16RegAddress(processor, memory, 'hl');
//            0x6c: op_neg,
//            0x6d: op_retn,
//            0x6e: op_im0,
            operations[0x6f] = new OpRld(processor, memory);
//
//            0x70: op_in_a_c,
//            operations[0x71] = OpOutCZero(processor, io);
//            operations[0x72] = OpSbcHl16Reg(processor, 'sp');
//            operations[0x73] = OpLdExtSp(processor, memory);
//            0x74: op_neg,
//            0x75: op_retn,
//            operations[0x76] = OpIm(processor, 1);
//            0x78: op_in_a_c,
//            operations[0x79] = OpOutC8Reg(processor, io, 'a');
//            operations[0x7a] = OpAdcHl16Reg(processor, 'sp');
//            operations[0x7b] = OpLdSpExt(processor, memory);
//            0x7c: op_neg,
//            0x7d: op_retn,
//            operations[0x7e] = OpIm(processor, 2);
//
//            operations[0xa0] = OpLdi(processor, memory);
//            operations[0xa1] = OpCpi(processor, memory);
//            operations[0xa2] = OpIni(processor, memory, io);
//            operations[0xa3] = OpOuti(processor, memory, io);
//            operations[0xa8] = OpLdd(processor, memory);
//            operations[0xa9] = OpCpd(processor, memory);
//            operations[0xaa] = OpInd(processor, memory, io);
//            operations[0xab] = OpOutd(processor, memory, io);
//
//            operations[0xb0] = OpLdir(processor, memory);
//            operations[0xb1] = OpCpir(processor, memory);
//            operations[0xb2] = OpInir(processor, memory, io);
//            operations[0xb3] = OpOtir(processor, memory, io);
//            operations[0xb8] = OpLddr(processor, memory);
//            operations[0xb9] = OpCpdr(processor, memory);
//            operations[0xba] = OpIndr(processor, memory, io);
//            operations[0xbb] = OpOtdr(processor, memory, io;
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
