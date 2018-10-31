package com.socialthingy.plusf.spectrum.ui;

import javax.swing.*;
import java.awt.*;

public class Icons {
    public static final Image windowIcon = Toolkit.getDefaultToolkit().createImage(Icons.class.getResource("/icons/plus-f.png"));
    public static final Icon aboutIcon = new ImageIcon(windowIcon.getScaledInstance(100, 100, Image.SCALE_SMOOTH));
    public static final Icon tape = new ImageIcon(Icons.class.getResource("/icons/tape.png"));
    public static final Icon tapePlaying = new ImageIcon(Icons.class.getResource("/icons/tape-playing.png"));
    public static final Icon rewindToStart = new ImageIcon(Icons.class.getResource("/icons/rewind-to-start.png"));
    public static final Icon play = new ImageIcon(Icons.class.getResource("/icons/play.png"));
    public static final Icon stop = new ImageIcon(Icons.class.getResource("/icons/stop.png"));
    public static final Icon fastForward = new ImageIcon(Icons.class.getResource("/icons/fast-forward.png"));
    public static final Icon rewind = new ImageIcon(Icons.class.getResource("/icons/rewind.png"));
}
