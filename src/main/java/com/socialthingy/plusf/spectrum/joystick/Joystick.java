package com.socialthingy.plusf.spectrum.joystick;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

public class Joystick extends Observable {
    private Set<Control> activeControls = new HashSet<>();

    public void controlActive(final Control control) {
        activeControls.add(control);
        setChanged();
        notifyObservers();
    }

    public void controlInactive(final Control control) {
        activeControls.remove(control);
        setChanged();
        notifyObservers();
    }

    public Set<Control> getActiveControls() {
        return this.activeControls;
    }

    public int serialise() {
        return activeControls.stream().mapToInt(c -> c.kempstonValue).reduce(0, (a, b) -> a + b);
    }

    public void deserialise(final int serialised) {
        activeControls.clear();
        for (Control available: Control.values()) {
            if ((available.kempstonValue & serialised) > 0) {
                activeControls.add(available);
            }
        }
    }
}
