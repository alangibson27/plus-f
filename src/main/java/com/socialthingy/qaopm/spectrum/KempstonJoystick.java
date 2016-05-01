package com.socialthingy.qaopm.spectrum;

import java.util.HashSet;
import java.util.Set;

public class KempstonJoystick {
    public enum Button {
        FIRE(0b00010000),
        UP(0b00001000),
        DOWN(0b00000100),
        LEFT(0b00000010),
        RIGHT(0b00000001);

        private int mask;

        Button(int mask) {
            this.mask = mask;
        }
    }

    private Set<Button> buttonsActive = new HashSet<>();

    public void buttonDown(final Button button) {
        buttonsActive.add(button);
    }

    public void buttonUp(final Button button) {
        buttonsActive.remove(button);
    }

//    @Override
    public int read(int port, int accumulator) {
        return buttonsActive.stream().mapToInt(b -> b.mask).reduce(0, (a, b) -> a + b);
    }

//    @Override
    public void write(int port, int accumulator, int value) {

    }
}
