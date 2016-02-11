package com.socialthingy.qaopm.util;

public class Word {
    public static int from(int low, int high) {
        return (high << 8) + low;
    }
}
