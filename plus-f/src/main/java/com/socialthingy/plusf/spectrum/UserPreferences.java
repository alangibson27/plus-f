package com.socialthingy.plusf.spectrum;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class UserPreferences {
    public static final String LAST_LOAD_DIRECTORY = "last-load-directory";
    public static final String MODEL = "initial-model";
    public static final String SOUND_ENABLED = "sound-enabled";
    public static final String TURBO_LOAD = "turbo-load";

    private final File prefsFile = new File(System.getProperty("user.home"), "plusf.properties");
    private final Properties prefs;

    public UserPreferences(final Properties source) {
        prefs = source;
    }

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

    public  void set(final String key, final boolean value) {
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
}
