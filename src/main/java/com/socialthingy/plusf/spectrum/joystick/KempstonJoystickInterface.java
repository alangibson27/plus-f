package com.socialthingy.plusf.spectrum.joystick;

import com.socialthingy.plusf.z80.IO;

public class KempstonJoystickInterface extends JoystickInterface implements IO {
    @Override
    public int read(int port, int accumulator) {
        if (joystick.isPresent()) {
            return joystick.get().getActiveControls().stream().mapToInt(c -> c.kempstonValue).reduce(0, (a, b) -> a + b);
        } else {
            return 0;
        }
    }

    @Override
    public void write(int port, int accumulator, int value) {
    }
}
