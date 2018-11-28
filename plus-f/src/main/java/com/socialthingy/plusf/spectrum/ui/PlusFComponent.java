package com.socialthingy.plusf.spectrum.ui;

import javax.swing.*;
import java.awt.event.KeyListener;

abstract class PlusFComponent extends JComponent implements Runnable {
    abstract void stop();
    abstract JMenuBar getMenuBar();
    abstract KeyListener getKeyListener();
}
