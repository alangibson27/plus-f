package com.socialthingy.qaopm.z80.operations;

import com.socialthingy.qaopm.util.Bitwise;
import com.socialthingy.qaopm.z80.FlagsRegister;
import com.socialthingy.qaopm.z80.Operation;
import com.socialthingy.qaopm.z80.Processor;
import com.socialthingy.qaopm.z80.Register;

public class OpDaa implements Operation {
    private final FlagsRegister flagsRegister;
    private final Register accumulator;

    public OpDaa(final Processor processor) {
        this.flagsRegister = processor.flagsRegister();
        this.accumulator = processor.register("a");
    }

    @Override
    public int execute() {
        final boolean fullCarry = flagsRegister.get(FlagsRegister.Flag.C);
        final boolean halfCarry = flagsRegister.get(FlagsRegister.Flag.H);
        final char[] digits = String.format("%02X", accumulator.get()).toCharArray();
        digits[0] -= '0';
        digits[1] -= '0';
        if (flagsRegister.get(FlagsRegister.Flag.N)) {
            daaAfterSub(fullCarry, halfCarry, digits);
        } else {
            daaAfterAdd(fullCarry, halfCarry, digits);
        }
        return 4;
    }

    private void daaAfterAdd(final boolean fullCarry, final boolean halfCarry, final char[] digits) {
        if (!fullCarry && !halfCarry) {
            if (digits[0] <= 0x9 && digits[1]<=0x9) {
                // NOP
            } else if (digits[0] <= 0x8 && digits[1] >= 0xa) {
                accumulator.set(accumulator.get() + 0x6);
            } else if (digits[0] >= 0xa && digits[1] <= 0x9) {
                accumulator.set(accumulator.get() + 0x60);
                flagsRegister.set(FlagsRegister.Flag.C, true);
            } else if (digits[0] >= 0x9 && digits[1] >= 0xa) {
                accumulator.set(accumulator.get() + 0x66);
                flagsRegister.set(FlagsRegister.Flag.C, true);
            }
        } else if (!fullCarry && halfCarry) {
            if (digits[0] <= 0x9 && digits[1] <= 0x3) {
                accumulator.set(accumulator.get() + 0x6);
            } else if (digits[0] >= 0xa && digits[1] <= 0x3) {
                accumulator.set(accumulator.get() + 0x66);
                flagsRegister.set(FlagsRegister.Flag.C, true);
            }
        } else if (fullCarry && !halfCarry) {
            if (digits[0] <= 0x2 && digits[1] <= 0x9) {
                accumulator.set(accumulator.get() + 0x60);
                flagsRegister.set(FlagsRegister.Flag.C, true);
            } else if (digits[0] <= 0x2 && digits[1] >= 0xa) {
                accumulator.set(accumulator.get() + 0x66);
                flagsRegister.set(FlagsRegister.Flag.C, true);
            }
        } else {
            if (digits[0] <= 0x3 && digits[1] <= 0x3) {
                accumulator.set(accumulator.get() + 0x66);
                flagsRegister.set(FlagsRegister.Flag.C, true);
            }
        }

        final int result = accumulator.set(accumulator.get() & 0xff);
        flagsRegister.set(FlagsRegister.Flag.S, (byte) result < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, result == 0);
        flagsRegister.set(FlagsRegister.Flag.P, Bitwise.hasParity(result));
    }

    private void daaAfterSub(final boolean fullCarry, final boolean halfCarry, final char[] digits) {
        if (!fullCarry && !halfCarry) {
            if (digits[0] <= 0x9 && digits[1] <= 0x9) {
                // NOP
            }
        } else if (!fullCarry && halfCarry) {
            if (digits[0] <= 0x8 && digits[1] >= 0x6) {
                accumulator.set(accumulator.get() + 0xfa);
            }
        } else if (fullCarry && !halfCarry) {
            if (digits[0] >= 0x7 && digits[1] <= 0x9) {
                accumulator.set(accumulator.get() + 0xa0);
                flagsRegister.set(FlagsRegister.Flag.C, true);
            }
        } else if (fullCarry && halfCarry) {
            if (digits[0] >= 0x6 && digits[1] >= 0x6) {
                accumulator.set(accumulator.get() + 0x9a);
                flagsRegister.set(FlagsRegister.Flag.C, true);
            }
        }

        final int result = accumulator.set(accumulator.get() & 0xff);
        flagsRegister.set(FlagsRegister.Flag.S, (byte) result < 0);
        flagsRegister.set(FlagsRegister.Flag.Z, result == 0);
        flagsRegister.set(FlagsRegister.Flag.P, Bitwise.hasParity(result));
    }
}
