package com.socialthingy.plusf.spectrum.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AboutDialog {
    public static void aboutDialog(final Component parentComponent) {
        final Properties versionFile = new Properties();
        String version;
        try (final InputStream is = AboutDialog.class.getResourceAsStream("/version.properties")) {
            versionFile.load(is);
            version = String.format("Plus-F version %s", versionFile.getProperty("version"));
        } catch (IOException ex) {
            version = "Plus-F";
        }
        JOptionPane.showMessageDialog(
                parentComponent,
                version,
                "Plus-F",
                JOptionPane.INFORMATION_MESSAGE,
                Icons.aboutIcon
        );
    }
}
