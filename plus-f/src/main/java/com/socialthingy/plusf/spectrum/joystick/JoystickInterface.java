package com.socialthingy.plusf.spectrum.joystick;

import java.util.Optional;

public class JoystickInterface {
    protected Optional<Joystick> joystick = Optional.empty();
    private final JoystickInterfaceType type;

    protected JoystickInterface(final JoystickInterfaceType type) {
        this.type = type;
    }

    public boolean isConnected(final Joystick joystick) {
        return this.joystick.isPresent() && this.joystick.get().equals(joystick);
    }

    public void connect(final Joystick joystick) {
        this.joystick = Optional.of(joystick);
    }

    public boolean disconnectIfConnected(final Joystick joystick) {
        if (this.joystick.isPresent() && this.joystick.get().equals(joystick)) {
            this.joystick = Optional.empty();
            return true;
        }

        return false;
    }

    public JoystickInterfaceType getType() {
        return type;
    }
}
