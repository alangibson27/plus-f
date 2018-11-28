package com.socialthingy.plusf.spectrum.joystick;

import com.socialthingy.plusf.spectrum.io.Keyboard;

import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class SinclairJoystickInterface extends JoystickInterface implements Observer {
    private final Keyboard keyboard;

    public SinclairJoystickInterface(final Keyboard keyboard) {
        super(JoystickInterfaceType.SINCLAIR_1);
        this.keyboard = keyboard;
    }

    @Override
    public void connect(final Joystick joystick) {
        super.connect(joystick);
        joystick.addObserver(this);
    }

    @Override
    public boolean disconnectIfConnected(final Joystick joystick) {
        if (super.disconnectIfConnected(joystick)) {
            joystick.deleteObserver(this);
            return true;
        }

        return false;
    }

    @Override
    public void update(final Observable observedJoystick, final Object arg) {
        if (joystick.isPresent() && observedJoystick == joystick.get()) {
            final Set<Control> activeControls = joystick.get().getActiveControls();
            for (Control c : Control.values()) {
                if (activeControls.contains(c)) {
                    keyboard.keyDown(c.sinclair1Key);
                } else {
                    keyboard.keyUp(c.sinclair1Key);
                }
            }
        }
    }
}
