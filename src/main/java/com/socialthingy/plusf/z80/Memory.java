package com.socialthingy.plusf.z80;

public class Memory {
    private Memory() {}

    private static boolean romProtectionEnabled = false;

    public static void enableRomProtection() {
        romProtectionEnabled = true;
    }

    public static void disableRomProtection() {
        romProtectionEnabled = false;
    }

    public static void set(final int[] memory, final int addr, final int value) {
        if (romProtectionEnabled && addr < 0x4000) {
            throw new RomProtectionException(String.format(
                    "Write to ROM attempted, suppressing. Address [%d], value [%d].", addr, value
            ));
        }

        memory[addr] = value;
    }
}
