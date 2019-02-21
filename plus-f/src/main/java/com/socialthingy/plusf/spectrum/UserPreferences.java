package com.socialthingy.plusf.spectrum;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class UserPreferences {
    public static final String LAST_LOAD_DIRECTORY = "last-load-directory";
    public static final String MODEL = "initial-model";
    public static final String SOUND_ENABLED = "sound-enabled";
    public static final String TURBO_LOAD = "turbo-load";
    public static final String JOYSTICK_UP = "joystick-up";
    public static final String JOYSTICK_DOWN = "joystick-down";
    public static final String JOYSTICK_LEFT = "joystick-left";
    public static final String JOYSTICK_RIGHT = "joystick-right";
    public static final String JOYSTICK_FIRE = "joystick-fire";
    public static final String EXTEND_BORDER = "extend-border";

    private final File prefsFile = new File(System.getProperty("user.home"), "plusf.properties");
    private final Properties prefs;

    public UserPreferences() {
        prefs = new Properties();
        if (prefsFile.exists()) {
            try (final FileReader fr = new FileReader(prefsFile)) {
                prefs.load(fr);
            } catch (IOException ex) {
                System.out.println(
                        String.format("Unable to read preferences file %s. Saved preferences will not be used.", prefsFile.getAbsolutePath())
                );
            }
        }

    }

    public boolean definedFor(final String key) {
        return prefs.containsKey(key);
    }

    public String get(final String key) {
        return prefs.getProperty(key);
    }

    public void set(final String key, final boolean value) {
        set(key, String.valueOf(value));
    }

    public void set(final String key, final String value) {
        prefs.setProperty(key, value);
        try (final FileWriter fw = new FileWriter(prefsFile)) {
            prefs.store(fw, "Plus-F user preferences");
        } catch (IOException ex) {
            System.out.println(
                    String.format("Unable to read preferences file %s. User preferences will not be saved.", prefsFile.getAbsolutePath())
            );
        }
    }

    public String getOrElse(final String key, final String ifUndefined) {
        return prefs.getProperty(key, ifUndefined);
    }

    public boolean getOrElse(final String key, final boolean ifUndefined) {
        final String value = getOrElse(key, String.valueOf(ifUndefined));
        return Boolean.valueOf(value);
    }

    public int getOrElse(final String key, final int ifUndefinedOrInvalid) {
        try {
            if (definedFor(key)) {
                return Integer.parseInt(get(key));
            } else {
                return ifUndefinedOrInvalid;
            }
        } catch (NumberFormatException nfe) {
            return ifUndefinedOrInvalid;
        }
    }

    public List<Integer> getOverridePositionsFor(final String signature) {
        final String overridePropertyName = overridePropertyName(signature);
        if (definedFor(overridePropertyName)) {
            final String overrideList = get(overridePropertyName).trim();
            return Arrays.stream(overrideList.split(","))
                    .mapToInt(Integer::parseInt).boxed()
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }

    public void saveOverridePositions(final String signature, final List<Integer> overridePositions) {
        set(overridePropertyName(signature),
            overridePositions.stream().map(Object::toString).collect(Collectors.joining(",")));
    }

    private String overridePropertyName(final String signature) {
        return String.format("tapeoverrides.%s", signature.replaceAll("=", ""));
    }
}
