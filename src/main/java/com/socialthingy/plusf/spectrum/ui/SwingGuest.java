package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.spectrum.Model;
import com.socialthingy.plusf.spectrum.display.Screen;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.spectrum.network.EmulatorState;
import com.socialthingy.plusf.spectrum.network.GuestPeerAdapter;
import com.socialthingy.plusf.spectrum.network.GuestState;
import com.socialthingy.plusf.spectrum.network.GuestStateType;
import com.socialthingy.plusf.z80.Memory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.socialthingy.plusf.spectrum.display.Screen.BOTTOM_BORDER_HEIGHT;
import static com.socialthingy.plusf.spectrum.display.Screen.TOP_BORDER_HEIGHT;
import static com.socialthingy.plusf.spectrum.ui.MenuUtils.menuItemFor;

public class SwingGuest {

    private final JFrame mainWindow;
    private final SwingJoystick joystick;
    private final GuestPeerAdapter peer;
    private final SwingDoubleSizeDisplay display;
    private final GuestULA ula;
    private EmulatorState lastHostData;
    private final int[] memory;
    private int count = 0;
    private final ScheduledThreadPoolExecutor cycleScheduler;

    public SwingGuest() throws IOException {
        memory = new int[0x10000];
        Memory.configure(memory, Model._48K);
        final Screen screen = new Screen(TOP_BORDER_HEIGHT, BOTTOM_BORDER_HEIGHT);
        ula = new GuestULA();
        display = new SwingDoubleSizeDisplay(screen, memory, ula);
        joystick = new SwingJoystick();

        cycleScheduler = new ScheduledThreadPoolExecutor(1);
        mainWindow = new JFrame("+F Spectrum Guest");
        peer = new GuestPeerAdapter(hostData -> lastHostData = hostData);

        initialiseUI();
    }

    private void initialiseUI() {
        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");
        fileMenu.add(menuItemFor("Quit", this::quit, Optional.of(KeyEvent.VK_Q)));
        menuBar.add(fileMenu);

        final JMenu networkMenu = new JMenu("Network");
        final JMenuItem connectItem = menuItemFor("Connect", this::connect, Optional.of(KeyEvent.VK_C));
        networkMenu.add(connectItem);
        final JMenuItem disconnectItem = menuItemFor("Disconnect", this::disconnect, Optional.of(KeyEvent.VK_D));
        networkMenu.add(disconnectItem);
        menuBar.add(networkMenu);

        connectItem.setEnabled(true);
        disconnectItem.setEnabled(false);
        peer.connectedProperty().addListener((observable, oldValue, newValue) -> {
            connectItem.setEnabled(!newValue);
            disconnectItem.setEnabled(newValue);
        });

        final JPanel statusBar = new JPanel(new GridLayout(1, 1));
        statusBar.add(
            new ConnectionMonitor(peer.connectedProperty(), peer.statistics(), peer.timeSinceLastReceived())
        );

        mainWindow.setJMenuBar(menuBar);
        mainWindow.addKeyListener(new JoystickHandler());
        mainWindow.getContentPane().setLayout(new BoxLayout(mainWindow.getContentPane(), BoxLayout.Y_AXIS));
        mainWindow.getContentPane().add(display);
        mainWindow.getContentPane().add(statusBar);
        mainWindow.pack();
        mainWindow.setResizable(false);
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void start() {
        mainWindow.setVisible(true);
        cycleScheduler.scheduleAtFixedRate(this::refresh, 0, 20, TimeUnit.MILLISECONDS);
    }

    private void connect(final ActionEvent e) {
        final String codename = JOptionPane.showInputDialog(
                mainWindow,
                "Enter codename of the Emulator",
                "Connect to Emulator",
                JOptionPane.QUESTION_MESSAGE
        );

        if (codename != null) {
            peer.connect(mainWindow, codename);
        }
    }

    private void disconnect(final ActionEvent e) {
        peer.disconnect();
    }

    private void refresh() {
        if (lastHostData != null) {
            System.arraycopy(lastHostData.getMemory(), 0x4000, memory, 0x4000, 0x1b00);
            ula.setBorderChanges(lastHostData.getBorderChanges());
            EventQueue.invokeLater(() -> {
                display.updateScreen();
                display.updateBorder(true);
                display.repaint();
            });
        }

        if (count % 5 == 0 && peer.isConnected()) {
            peer.send(new GuestState(GuestStateType.JOYSTICK_STATE.ordinal(), joystick.serialise()));
        }

        count++;
    }

    private void quit(final ActionEvent e) {
        peer.shutdown();
        System.exit(0);
    }

    public static void main(final String ... args) throws IOException {
        final SwingGuest guest = new SwingGuest();
        guest.start();
    }

    private class JoystickHandler implements KeyListener {
        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            joystick.keyPressed(e);
            if (e.isConsumed() && peer.isConnected()) {
                peer.send(new GuestState(GuestStateType.JOYSTICK_STATE.ordinal(), joystick.serialise()));
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            joystick.keyReleased(e);
            if (e.isConsumed() && peer.isConnected()) {
                peer.send(new GuestState(GuestStateType.JOYSTICK_STATE.ordinal(), joystick.serialise()));
            }
        }
    }
}

class GuestULA extends ULA {
    public GuestULA() {
        super(null, null, null);
    }

    public void setBorderChanges(final List<Long> borderChanges) {
        getBorderChanges().clear();
        getBorderChanges().addAll(borderChanges);
    }

    @Override
    public boolean borderNeedsRedrawing() {
        return true;
    }
}