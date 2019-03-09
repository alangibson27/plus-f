package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.Settings;
import com.socialthingy.plusf.spectrum.UserPreferences;
import com.socialthingy.plusf.spectrum.display.DisplayComponent;
import com.socialthingy.plusf.spectrum.display.PixelMapper;
import com.socialthingy.plusf.spectrum.joystick.JoystickInterfaceType;
import com.socialthingy.plusf.spectrum.network.EmulatorState;
import com.socialthingy.plusf.spectrum.network.GuestPeerAdapter;
import com.socialthingy.plusf.spectrum.network.GuestState;
import com.socialthingy.plusf.spectrum.network.SessionInfo;
import com.socialthingy.plusf.ui.JoystickKeys;
import com.socialthingy.plusf.ui.RedefineKeysPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.socialthingy.plusf.spectrum.UserPreferences.PREVIOUS_SESSION;
import static com.socialthingy.plusf.spectrum.ui.MenuUtils.joystickItem;
import static com.socialthingy.plusf.spectrum.ui.MenuUtils.menuItemFor;
import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;

public class Guest extends PlusFComponent implements Runnable {
    private final SwingJoystick joystick;
    private final GuestPeerAdapter peer;
    private final DisplayComponent display;
    private final PixelMapper pixelMapper;
    private EmulatorState lastHostData;
    private int count = 0;
    private final ScheduledThreadPoolExecutor cycleScheduler;
    private final Frame window;
    private JoystickInterfaceType joystickType = JoystickInterfaceType.KEMPSTON;
    private UserPreferences prefs;
    private final boolean reconnect;

    public Guest(final Frame window, final boolean reconnect) {
        this.window = window;
        this.reconnect = reconnect;
        prefs = new UserPreferences();
        display = new DisplayComponent();
        pixelMapper = new PixelMapper();
        joystick = new SwingJoystick(new JoystickKeys(prefs));

        cycleScheduler = new ScheduledThreadPoolExecutor(1);
        peer = new GuestPeerAdapter(hostData -> lastHostData = hostData);

        initialiseUI();
    }

    private void initialiseUI() {
        final JPanel statusBar = new JPanel(new GridLayout(1, 1));
        statusBar.add(
                new ConnectionMonitor(peer.connectedProperty(), peer.statistics(), peer.timeSinceLastReceived())
        );

        final Insets insets = new Insets(1, 1, 1, 1);
        setLayout(new GridBagLayout());
        add(
            display,
            new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, CENTER, BOTH, insets, 0, 0)
        );
        add(
            statusBar,
            new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, CENTER, HORIZONTAL, insets, 0, 0)
        );
    }

    public void run() {
        setVisible(true);
        setMinimumSize(getSize());
        if (reconnect) {
            reconnect();
        } else {
            connect();
        }
        cycleScheduler.scheduleAtFixedRate(this::refresh, 0, 20, TimeUnit.MILLISECONDS);
    }

    private void connect() {
        final JPanel connectDetailsPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        connectDetailsPanel.add(new JLabel("Name of Session to join"));
        final JCheckBox portForwardingEnabled = new JCheckBox("I have enabled port forwarding", true);
        connectDetailsPanel.add(portForwardingEnabled);
        final String codename = JOptionPane.showInputDialog(
            this,
            connectDetailsPanel,
            "Join Session",
            JOptionPane.QUESTION_MESSAGE
        );

        if (codename != null) {
            final Optional<Integer> port;
            if (portForwardingEnabled.isSelected()) {
                port = Optional.of(Settings.GUEST_PORT);
            } else {
                port = Optional.empty();
            }
            peer.connect(window, codename, port);
        } else {
            notifyWillClose(new CloseEvent(false, this, false));
        }
    }

    private void refresh() {
        if (lastHostData != null) {
            EventQueue.invokeLater(() -> {
                display.updateScreen(pixelMapper.getPixels(lastHostData.getMemory(), lastHostData.isFlashActive()));
                display.updateBorder(lastHostData.getBorderColours());
                display.repaint();
            });
        }

        if (count % 20 == 0 && peer.isConnected()) {
            peer.send(new GuestState(joystick.serialise(), joystickType.ordinal()));
        }

        count++;
    }

    private void quit(final ActionEvent e) {
        stop();
        System.exit(0);
    }

    @Override
    void stop() {
        peer.shutdown();
    }

    @Override
    JMenuBar getMenuBar() {
        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");
        fileMenu.add(menuItemFor("Quit", this::quit, Optional.of(KeyEvent.VK_Q)));
        menuBar.add(fileMenu);

        final JMenu displayMenu = new JMenu("Display");
        final JCheckBoxMenuItem smoothRendering = new JCheckBoxMenuItem("Smooth Display Rendering");
        smoothRendering.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_MASK));
        smoothRendering.addActionListener(e -> display.setSmoothRendering(smoothRendering.isSelected()));
        smoothRendering.doClick();
        displayMenu.add(smoothRendering);

        final JCheckBoxMenuItem extendBorder = new JCheckBoxMenuItem("Extend Border");
        extendBorder.addActionListener(e -> display.setExtendBorder(extendBorder.isSelected()));
        displayMenu.add(extendBorder);
        menuBar.add(displayMenu);

        final ButtonGroup joystickButtonGroup = new ButtonGroup();
        final JMenu joystickMenu = new JMenu("Joystick");
        menuBar.add(joystickMenu);
        final JMenuItem redefineKeysItem = menuItemFor("Redefine", this::redefineKeys, Optional.empty());
        joystickMenu.add(redefineKeysItem);
        final JMenuItem kempstonJoystick = joystickItem(joystickButtonGroup, "Kempston", true);
        kempstonJoystick.addActionListener(e -> joystickType = JoystickInterfaceType.KEMPSTON);
        joystickMenu.add(kempstonJoystick);
        final JMenuItem sinclairJoystick = joystickItem(joystickButtonGroup, "Sinclair", false);
        sinclairJoystick.addActionListener(e -> joystickType = JoystickInterfaceType.SINCLAIR_1);
        joystickMenu.add(sinclairJoystick);

        final JMenu networkMenu = new JMenu("Network");
        final JMenuItem disconnectItem = menuItemFor("End Session", e -> notifyWillClose(new CloseEvent(true, this, false)), Optional.of(KeyEvent.VK_D));
        networkMenu.add(disconnectItem);
        menuBar.add(networkMenu);

        final JMenu aboutMenu = new JMenu("About");
        final JMenuItem aboutItem = menuItemFor("About Plus-F", e -> AboutDialog.aboutDialog(this), Optional.empty());
        aboutMenu.add(aboutItem);
        menuBar.add(aboutMenu);
        return menuBar;
    }

    @Override
    KeyListener getKeyListener() {
        return new JoystickHandler();
    }

    private void redefineKeys(final ActionEvent e) {
        final RedefineKeysPanel panel = new RedefineKeysPanel(joystick.getKeys());
        final int result = JOptionPane.showConfirmDialog(
                Guest.this,
                panel,
                "Redefine Keys",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result == JOptionPane.OK_OPTION) {
            final JoystickKeys keys = panel.getKeys();
            joystick.setKeys(keys);
            keys.save(prefs);
        }
    }

    public void reconnect() {
        final SessionInfo sessionInfo = new SessionInfo(prefs.get(PREVIOUS_SESSION));
        peer.reconnect(sessionInfo.getDestination());
    }

    private class JoystickHandler implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            joystick.keyPressed(e);
            if (e.isConsumed() && peer.isConnected()) {
                peer.send(new GuestState(joystick.serialise(), joystickType.ordinal()));
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            joystick.keyReleased(e);
            if (e.isConsumed() && peer.isConnected()) {
                peer.send(new GuestState(joystick.serialise(), joystickType.ordinal()));
            }
        }
    }
}