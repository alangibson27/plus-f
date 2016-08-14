package com.socialthingy.plusf.spectrum.input;

import com.socialthingy.plusf.spectrum.joystick.Control;
import com.socialthingy.plusf.spectrum.joystick.Joystick;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashMap;
import java.util.Map;

public class JavaFXJoystick extends Joystick implements EventHandler<KeyEvent> {
    private Map<KeyCode, Control> controlKeyAssignments = new HashMap<>();

    public JavaFXJoystick() {
        controlKeyAssignments.put(KeyCode.Q, Control.UP);
        controlKeyAssignments.put(KeyCode.A, Control.DOWN);
        controlKeyAssignments.put(KeyCode.O, Control.LEFT);
        controlKeyAssignments.put(KeyCode.P, Control.RIGHT);
        controlKeyAssignments.put(KeyCode.M, Control.FIRE);
    }

    @Override
    public void handle(final KeyEvent event) {
        final Control pressed = controlKeyAssignments.get(event.getCode());
        if (pressed != null) {
            event.consume();
            if (event.getEventType() == KeyEvent.KEY_PRESSED) {
                controlActive(pressed);
            } else if (event.getEventType() == KeyEvent.KEY_RELEASED) {
                controlInactive(pressed);
            }
        }
    }
}
