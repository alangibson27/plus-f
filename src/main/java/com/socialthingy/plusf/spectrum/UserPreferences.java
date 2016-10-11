package com.socialthingy.plusf.spectrum;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class UserPreferences {
    public static final String LAST_LOAD_DIRECTORY = "last-load-directory";
    public static final String MODEL = "initial-model";

    private final File prefsFile = new File(System.getProperty("user.home"), "plusf.properties");
    private final Properties prefs = new Properties();

    public UserPreferences() {
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
}
