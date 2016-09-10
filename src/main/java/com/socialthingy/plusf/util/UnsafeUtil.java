package com.socialthingy.plusf.util;

import sun.misc.Unsafe;

import java.lang.reflect.Constructor;

public class UnsafeUtil {
    private static Unsafe unsafe;

    public static synchronized Unsafe getUnsafe() {
        if (unsafe == null) {
            try {
                final Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
                unsafeConstructor.setAccessible(true);
                unsafe = unsafeConstructor.newInstance();
            } catch (Exception ex) {
                System.out.println("Unable to access sun.misc.Unsafe class. Exiting");
                System.exit(1);
            }
        }

        return unsafe;
    }
}
