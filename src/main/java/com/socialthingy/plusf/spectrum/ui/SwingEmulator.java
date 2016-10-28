package com.socialthingy.plusf.spectrum.ui;

import com.codahale.metrics.MetricRegistry;
import com.socialthingy.plusf.spectrum.*;
import com.socialthingy.plusf.spectrum.display.SafePixelMapper;
import com.socialthingy.plusf.spectrum.display.UnsafePixelMapper;
import com.socialthingy.plusf.spectrum.input.HostInputMultiplexer;
import com.socialthingy.plusf.spectrum.io.IOMultiplexer;
import com.socialthingy.plusf.spectrum.io.ULA;
import com.socialthingy.plusf.spectrum.joystick.Joystick;
import com.socialthingy.plusf.spectrum.joystick.KempstonJoystickInterface;
import com.socialthingy.plusf.spectrum.joystick.SinclairJoystickInterface;
import com.socialthingy.plusf.spectrum.network.EmulatorPeerAdapter;
import com.socialthingy.plusf.spectrum.network.EmulatorState;
import com.socialthingy.plusf.spectrum.network.GuestStateType;
import com.socialthingy.plusf.tape.NavigableBlock;
import com.socialthingy.plusf.tape.Tape;
import com.socialthingy.plusf.tape.TapeException;
import com.socialthingy.plusf.tape.TapeFileReader;
import com.socialthingy.plusf.z80.Memory;
import com.socialthingy.plusf.z80.Processor;
import javafx.util.Pair;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

import static com.socialthingy.plusf.spectrum.UserPreferences.LAST_LOAD_DIRECTORY;
import static com.socialthingy.plusf.spectrum.UserPreferences.MODEL;
import static com.socialthingy.plusf.spectrum.ui.MenuUtils.menuItemFor;
import static java.util.Optional.empty;

public class SwingEmulator {
    private final Computer computer;
    private final JFrame mainWindow;
    private final DisplayComponent display;
    private final int[] memory;
    private final UserPreferences prefs = new UserPreferences();
    private final TapePlayer tapePlayer;
    private final HostInputMultiplexer hostInputMultiplexer;
    private final EmulatorPeerAdapter peer;
    private Model currentModel;
    private final SwingKeyboard keyboard;
    private final ScheduledThreadPoolExecutor cycleScheduler;
    private ScheduledFuture<?> cycleTimer;
    private EmulatorSpeed currentSpeed;
    private JLabel speedIndicator;
    private final ULA ula;
    private final Processor processor;
    private long lastRepaint;
    private final Joystick guestJoystick;

    public SwingEmulator() throws IOException {
        currentModel = Model.valueOf(prefs.getOrElse(MODEL, Model._48K.name()));
        memory = new int[0x10000];
        Memory.configure(memory, currentModel);

        final IOMultiplexer ioMux = new IOMultiplexer();
        processor = new Processor(memory, ioMux);
        keyboard = new SwingKeyboard();
        tapePlayer = new TapePlayer();
        ula = new ULA(keyboard, tapePlayer, memory);

        final SwingJoystick hostJoystick = new SwingJoystick();
        final KempstonJoystickInterface kempstonJoystickInterface = new KempstonJoystickInterface();
        final SinclairJoystickInterface sinclair1JoystickInterface = new SinclairJoystickInterface(keyboard);
        guestJoystick = new Joystick();
        hostInputMultiplexer = new HostInputMultiplexer(keyboard, hostJoystick);
        hostInputMultiplexer.deactivateJoystick();
        kempstonJoystickInterface.connect(guestJoystick);
        sinclair1JoystickInterface.connect(hostJoystick);

        ioMux.register(0xfe, ula);
        if (currentModel.ramPageCount > 1) {
            ioMux.register(0xfd, ula);
        }
        ioMux.register(0x1f, kempstonJoystickInterface);

        computer = new Computer(
            processor,
            ula,
            memory,
            Model._48K,
            new MetricRegistry()
        );

        peer = new EmulatorPeerAdapter(gs -> {
            if (gs.getEventType() == GuestStateType.JOYSTICK_STATE.ordinal()) {
                guestJoystick.deserialise(gs.getEventValue());
            }
        });

        if (System.getProperty("safeDisplay") == null) {
            System.out.println("Using unsafe display");
            display = new SwingDoubleSizeDisplay(new UnsafePixelMapper(), memory, ula);
        } else {
            System.out.println("Using safe display");
            display = new SafeSwingDoubleSizeDisplay(new SafePixelMapper(), memory, ula);
        }
        cycleScheduler = new ScheduledThreadPoolExecutor(1);
        speedIndicator = new JLabel("Normal speed");

        mainWindow = new JFrame("+F Spectrum Emulator");
        initialiseUI();
    }

    private void initialiseUI() {
        final JMenuBar menuBar = new JMenuBar();
        final JMenu fileMenu = new JMenu("File");
        fileMenu.add(menuItemFor("Load", this::load, Optional.of(KeyEvent.VK_L)));
        fileMenu.add(menuItemFor("Quit", this::quit, Optional.of(KeyEvent.VK_Q)));
        menuBar.add(fileMenu);

        final JMenu computerMenu = new JMenu("Computer");
        computerMenu.add(menuItemFor("Reset", this::reset, Optional.of(KeyEvent.VK_R)));

        final JCheckBoxMenuItem hostJoystickItem = new JCheckBoxMenuItem("Enable Host Joystick");
        hostJoystickItem.addActionListener(action -> {
            if (hostJoystickItem.isSelected()) {
                hostInputMultiplexer.activateJoystick();
            } else {
                hostInputMultiplexer.deactivateJoystick();
            }
        });
        computerMenu.add(hostJoystickItem);

        final JMenu modelMenu = new JMenu("Model");
        final ButtonGroup modelButtonGroup = new ButtonGroup();
        for (Model model: Model.values()) {
            final JRadioButtonMenuItem modelItem = new JRadioButtonMenuItem(
                model.displayName,
                model == currentModel
            );
            modelItem.addActionListener(item -> {
                if (currentModel != model) {
                    changeModel(model);
                }
            });
            modelMenu.add(modelItem);
            modelButtonGroup.add(modelItem);
        }
        computerMenu.add(modelMenu);

        final JMenu speedMenu = new JMenu("Speed");
        final ButtonGroup speedButtonGroup = new ButtonGroup();
        for (EmulatorSpeed speed: EmulatorSpeed.values()) {
            final JRadioButtonMenuItem speedItem = new JRadioButtonMenuItem(
                speed.displayName,
                speed == EmulatorSpeed.NORMAL
            );
            speedItem.addActionListener(item -> {
                if (currentSpeed != speed) {
                    setSpeed(speed);
                }
            });
            speedItem.setAccelerator(KeyStroke.getKeyStroke(speed.shortcutKey, 0));
            speedMenu.add(speedItem);
            speedButtonGroup.add(speedItem);
        }
        computerMenu.add(speedMenu);
        menuBar.add(computerMenu);

        final JMenu tapeMenu = new JMenu("Tape");
        final JCheckBoxMenuItem playTape = new JCheckBoxMenuItem("Play");
        playTape.setModel(tapePlayer.getPlayButtonModel());
        playTape.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0));
        tapeMenu.add(playTape);

        final JMenuItem stopTape = new JMenuItem("Stop");
        stopTape.setModel(tapePlayer.getStopButtonModel());
        stopTape.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0));
        tapeMenu.add(stopTape);

        final JMenuItem rewindTape = new JMenuItem("Rewind to Start");
        rewindTape.setModel(tapePlayer.getRewindToStartButtonModel());
        rewindTape.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
        tapeMenu.add(rewindTape);

        final JMenuItem tapeInfo = menuItemFor("Tape Information ...", this::tapeInfo, empty());
        tapeInfo.setModel(tapePlayer.getTapePresentModel());
        tapeMenu.add(tapeInfo);

        final JMenuItem jumpToBlock = menuItemFor("Jump to Block ...", this::jumpToTapeBlock, empty());
        jumpToBlock.setModel(tapePlayer.getJumpButtonModel());
        tapeMenu.add(jumpToBlock);

        menuBar.add(tapeMenu);

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

        final TapeControls tapeControls = new TapeControls(tapePlayer);
        final JPanel statusBar = new JPanel(new GridLayout(1, 3));
        speedIndicator.setHorizontalAlignment(SwingConstants.TRAILING);
        statusBar.add(
            new ConnectionMonitor(peer.connectedProperty(), peer.statistics(), peer.timeSinceLastReceived())
        );
        statusBar.add(tapeControls);
        statusBar.add(speedIndicator);

        mainWindow.setJMenuBar(menuBar);
        mainWindow.addKeyListener(hostInputMultiplexer);
        mainWindow.getContentPane().setLayout(new BoxLayout(mainWindow.getContentPane(), BoxLayout.Y_AXIS));
        mainWindow.getContentPane().add(display);
        mainWindow.getContentPane().add(statusBar);
        mainWindow.pack();
        mainWindow.setResizable(false);
        mainWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private void tapeInfo(final ActionEvent actionEvent) {
        final Tape tape = tapePlayer.getTape().get();
        final List<Pair<String, String>> info = tape.archiveInfo()
                .stream()
                .map(p -> new Pair<>(p.getKey().replaceAll("\\s", " "), p.getValue().replaceAll("\\s", " ")))
                .collect(Collectors.toList());

        if (info.isEmpty()) {
            JOptionPane.showMessageDialog(
                mainWindow,
                "No tape information available.",
                "Tape Information",
                JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            final DefaultTableModel tableModel = new DefaultTableModel(0, 2);
            tableModel.setColumnIdentifiers(new String[] {"Name", "Value"});
            info.stream().map(i -> new Object[] {i.getKey(), i.getValue()}).forEach(tableModel::addRow);

            final JTable infoTable = new JTable(tableModel);
            infoTable.getColumnModel().getColumn(0).setMaxWidth(128);
            JOptionPane.showMessageDialog(
                mainWindow,
                new JScrollPane(infoTable),
                "Tape Information",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void jumpToTapeBlock(final ActionEvent actionEvent) {
        final Tape tape = tapePlayer.getTape().get();
        final List<NavigableBlock> blocks = tape.navigableBlocks();

        if (blocks.isEmpty()) {
            JOptionPane.showMessageDialog(
                    mainWindow,
                    "Tape is empty.",
                    "Jump to Block",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } else {
            final DefaultListModel<NavigableBlock> listModel = new DefaultListModel<>();
            blocks.forEach(listModel::addElement);

            final JList<NavigableBlock> blockList = new JList<>(listModel);
            blockList.setCellRenderer(new DefaultListCellRenderer() {
                @Override
                public Component getListCellRendererComponent(
                    final JList<?> list,
                    final Object value,
                    final int index,
                    final boolean isSelected,
                    final boolean cellHasFocus
                ) {
                    final NavigableBlock selected = (NavigableBlock) list.getModel().getElementAt(index);
                    return super.getListCellRendererComponent(list, selected.block().getDisplayName(), index, isSelected, cellHasFocus);
                }
            });
            final int result = JOptionPane.showConfirmDialog(
                    mainWindow,
                    new JScrollPane(blockList),
                    "Jump to Block",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION && blockList.getSelectedValue() != null) {
                try {
                    tapePlayer.jumpToBlock(blockList.getSelectedValue().index());
                } catch (TapeException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void run() throws IOException {
        mainWindow.setVisible(true);
        setSpeed(EmulatorSpeed.NORMAL);
    }

    private void connect(final ActionEvent e) {
        final String codename = JOptionPane.showInputDialog(
                mainWindow,
                "Enter codename of the Guest",
                "Connect to Guest",
                JOptionPane.QUESTION_MESSAGE
        );

        if (codename != null) {
            peer.connect(mainWindow, codename);
        }
    }

    private void disconnect(final ActionEvent e) {
        peer.disconnect();
    }

    private void setSpeed(final EmulatorSpeed newSpeed) {
        speedIndicator.setText(String.format("%s speed", newSpeed.displayName));
        currentSpeed = newSpeed;
        if (cycleTimer != null) {
            cycleTimer.cancel(false);
        }
        if (newSpeed == EmulatorSpeed.TURBO) {
            cycleScheduler.execute(this::singleCycle);
        } else {
            cycleTimer = cycleScheduler.scheduleAtFixedRate(this::singleCycle, 0, newSpeed.period, newSpeed.timeUnit);
        }
    }

    private void singleCycle() {
        try {
            do {
                computer.singleCycle();
                if (peer.isConnected() && currentSpeed == EmulatorSpeed.NORMAL) {
                    final int[] screenBytes = Memory.getScreenBytes(memory);
                    peer.send(new EmulatorState(screenBytes, ula.getBorderChanges(), ula.flashActive()));
                }

                if (shouldRepaint()) {
                    lastRepaint = System.currentTimeMillis();
                    display.updateScreen();
                    display.updateBorder(currentSpeed == EmulatorSpeed.TURBO);
                    SwingUtilities.invokeLater(display::repaint);
                }
            } while (currentSpeed == EmulatorSpeed.TURBO);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean shouldRepaint() {
        return currentSpeed != EmulatorSpeed.TURBO || System.currentTimeMillis() - lastRepaint > 20;
    }

    private void load(final ActionEvent e) {
        whilePaused(() -> {
            final JFileChooser chooser = new JFileChooser("Load .TAP, .TZX or .Z80 file");
            if (prefs.definedFor(LAST_LOAD_DIRECTORY)) {
                chooser.setCurrentDirectory(new File(prefs.get(LAST_LOAD_DIRECTORY)));
            }

            final int result = chooser.showOpenDialog(mainWindow);
            if (result == JFileChooser.APPROVE_OPTION) {
                try {
                    detectAndLoad(chooser.getSelectedFile());
                    prefs.set(LAST_LOAD_DIRECTORY, chooser.getSelectedFile().getAbsolutePath());
                } catch (TapeException | IOException ex) {
                    JOptionPane.showMessageDialog(
                        mainWindow,
                        String.format("An error occurred while loading the file file:\n%s", ex.getMessage()),
                        "Loading Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    ex.printStackTrace();
                }
            }
        });
    }

    private void detectAndLoad(final File selectedFile) throws IOException, TapeException {
        final byte[] prelude = new byte[256];
        try (final InputStream is = new FileInputStream(selectedFile)) {
            is.read(prelude);
        }

        if (selectedFile.getName().toLowerCase().endsWith(".tap")) {
            final Tape tap = new TapeFileReader(selectedFile).readTap();
            tapePlayer.setTape(tap);
        } else if (TapeFileReader.recognises(prelude)) {
            final Tape tzx = new TapeFileReader(selectedFile).readTzx();
            tapePlayer.setTape(tzx);
        } else {
            final int borderColour = computer.loadSnapshot(selectedFile);
            ula.setBorderColour(borderColour);
        }
    }

    private void changeModel(final Model newModel) {
        whilePaused(() -> {
            final int result = JOptionPane.showConfirmDialog(
                mainWindow,
                "This will reset the computer. Do you want to continue?",
                String.format("Change to %s", newModel.displayName),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                prefs.set(MODEL, newModel.name());
                currentModel = newModel;
                resetComputer();
            }
        });
    }

    private void reset(final ActionEvent e) {
        whilePaused(() -> {
            final int result = JOptionPane.showConfirmDialog(
               mainWindow,
               "Do you want to reset the computer?",
               "Reset",
               JOptionPane.YES_NO_OPTION,
               JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                resetComputer();
            }
        });
    }

    private void resetComputer() {
        try {
            Memory.configure(memory, Model.valueOf(prefs.getOrElse(MODEL, Model._48K.name())));
            computer.reset();
            processor.reset();
            ula.reset();
            tapePlayer.ejectTape();
            keyboard.reset();
            cycleTimer.cancel(false);
            setSpeed(EmulatorSpeed.NORMAL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void quit(final ActionEvent e) {
        System.exit(0);
    }

    private void whilePaused(final Runnable action) {
        cycleTimer.cancel(true);
        currentSpeed = EmulatorSpeed.NORMAL;
        keyboard.reset();
        try {
            action.run();
        } finally {
            setSpeed(currentSpeed);
        }
    }

    public static void main(final String ... args) throws IOException {
        final SwingEmulator emulator = new SwingEmulator();
        emulator.run();
    }
}
