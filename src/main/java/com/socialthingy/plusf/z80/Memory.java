package com.socialthingy.plusf.z80;

public class Memory {
    private Memory() {}

    private static boolean memoryProtectionEnabled = true;

    public static void disableMemoryProtection() {
        memoryProtectionEnabled = false;
    }

    public static void set(final int[] memory, final int addr, final int value) {
        if (!memoryProtectionEnabled || addr >= 0x4000) {
            memory[addr] = value;
        }
    }
}
