package com.socialthingy.plusf.spectrum.ui;

import com.socialthingy.plusf.p2p.Statistics;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import javax.swing.*;
import java.awt.*;

public class ConnectionMonitor extends JPanel implements ChangeListener {
    private final JLabel statusLabel = new JLabel("Not connected");
    private final BooleanProperty connected;
    private final ObjectProperty<Statistics> statistics;
    private final LongProperty timeSinceLastReceived;

    public ConnectionMonitor(
            final BooleanProperty connected,
            final ObjectProperty<Statistics> statistics,
            final LongProperty timeSinceLastReceived
    ) {
        this.connected = connected;
        this.statistics = statistics;
        this.timeSinceLastReceived = timeSinceLastReceived;

        connected.addListener(this);
        statistics.addListener(this);

        setLayout(new BorderLayout());
        add(statusLabel, BorderLayout.CENTER);
    }

    private String textFor(final Statistics statistics) {
        return String.format("Delay: %d ms", statistics.medianLatency());
    }

    private Color colourFor(final Statistics statistics, final long timeSinceLastReceived) {
        if (timeSinceLastReceived > 500) {
            return Color.RED;
        } else if (statistics.medianLatency() > 40) {
            return Color.YELLOW;
        } else {
            return Color.GREEN;
        }
    }

    @Override
    public void changed(final ObservableValue observable, final Object oldValue, final Object newValue) {
        if (connected.get()) {
            statusLabel.setForeground(colourFor(statistics.get(), timeSinceLastReceived.get()));
            statusLabel.setText(textFor(statistics.get()));
        } else {
            statusLabel.setForeground(Color.BLACK);
            statusLabel.setText("Not connected");
        }
    }
}
