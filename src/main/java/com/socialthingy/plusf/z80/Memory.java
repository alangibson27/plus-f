package com.socialthingy.plusf.z80;

public class Memory {
    private Memory() {}

    private static boolean memoryProtectionEnabled = true;

    private static boolean screenChanged = true;

    public static void disableMemoryProtection() {
        memoryProtectionEnabled = false;
    }

    public static void set(final int[] memory, final int addr, final int value) {
        if (!memoryProtectionEnabled || addr >= 0x4000) {
            if (memory[addr] != value) {
                memory[addr] = value;
                screenChanged = screenChanged || addr >= 0x4000 && addr < 0x5b00;
            }
        }
    }

    public static boolean screenChanged() {
        return screenChanged;
    }

    public static void markScreenDrawn() {
        screenChanged = false;
    }
}
