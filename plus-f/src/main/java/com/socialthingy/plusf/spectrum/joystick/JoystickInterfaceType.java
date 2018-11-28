package com.socialthingy.plusf.spectrum.joystick;

public enum JoystickInterfaceType {
    NONE("None"), KEMPSTON("Kempston"), SINCLAIR_1("Sinclair 1"), SINCLAIR_2("Sinclair 2");

    public final String displayName;

    JoystickInterfaceType(final String displayName) {
        this.displayName = displayName;
    }

    public static JoystickInterfaceType from(final int ordinal) {
        for (JoystickInterfaceType type: JoystickInterfaceType.values()) {
            if (type.ordinal() == ordinal) {
                return type;
            }
        }

        throw new IllegalArgumentException(String.valueOf(ordinal));
    }
}
