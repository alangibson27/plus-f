package com.socialthingy.plusf.spectrum.joystick;

import com.socialthingy.plusf.z80.IO;

public class KempstonJoystickInterface extends JoystickInterface implements IO {
    @Override
    public boolean recognises(int low, int high) {
        return low == 0x1f;
    }

    @Override
    public int read(int low, int high) {
        if (joystick.isPresent()) {
            return joystick.get().getActiveControls().stream().mapToInt(c -> c.kempstonValue).reduce(0, (a, b) -> a + b);
        } else {
            return 0;
        }
    }

    @Override
    public void write(int low, int high, int value) {
    }
}
