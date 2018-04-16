package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.p2p.Statistics;
import com.socialthingy.plusf.util.ObservedValue;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class ConnectionMonitor extends JPanel implements Observer {
    private final JLabel statusLabel = new JLabel("Not connected");
    private final ObservedValue<Boolean> connected;
    private final ObservedValue<Statistics> statistics;
    private final ObservedValue<Long> timeSinceLastReceived;

    public ConnectionMonitor(
            final ObservedValue<Boolean> connected,
            final ObservedValue<Statistics> statistics,
            final ObservedValue<Long> timeSinceLastReceived
    ) {
        this.connected = connected;
        this.statistics = statistics;
        this.timeSinceLastReceived = timeSinceLastReceived;

        connected.addObserver(this);
        statistics.addObserver(this);

        setLayout(new BorderLayout());
        add(statusLabel, BorderLayout.CENTER);
    }

    private String textFor(final Statistics statistics) {
        return String.format("Delay: %d ms   Packet size: %d", statistics.latency(), statistics.size());
    }

    private Color colourFor(final Statistics statistics, final long timeSinceLastReceived) {
        if (timeSinceLastReceived > 500 || statistics.latency() > 75) {
            return Color.RED;
        } else if (statistics.latency() > 50) {
            return Color.MAGENTA;
        } else {
            return Color.GREEN;
        }
    }

    @Override
    public void update(final Observable o, final Object arg) {
        if (connected.get()) {
            statusLabel.setForeground(colourFor(statistics.get(), timeSinceLastReceived.get()));
            statusLabel.setText(textFor(statistics.get()));
        } else {
            statusLabel.setForeground(Color.BLACK);
            statusLabel.setText("Not connected");
        }
    }
}
