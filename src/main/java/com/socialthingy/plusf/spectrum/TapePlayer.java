package com.socialthingy.plusf.spectrum;

import com.socialthingy.plusf.tape.Tape;
import com.socialthingy.plusf.tape.TapeBlock;
import com.socialthingy.plusf.tape.TapeException;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

public class TapePlayer implements Iterator<TapeBlock.Bit> {
    private Optional<Tape> tape = Optional.empty();
    private Iterator<TapeBlock.Bit> playableTape = Collections.emptyIterator();
    private SimpleBooleanProperty isPlaying = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty playAvailable = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty stopAvailable = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty seekAvailable = new SimpleBooleanProperty(false);

    public SimpleBooleanProperty playAvailableProperty() {
        return playAvailable;
    }

    public SimpleBooleanProperty stopAvailableProperty() {
        return stopAvailable;
    }

    public SimpleBooleanProperty seekAvailableProperty() {
        return seekAvailable;
    }

    public SimpleBooleanProperty playingProperty() {
        return isPlaying;
    }

    public void ejectTape() {
        tape = Optional.empty();
        playableTape = Collections.emptyIterator();
        playAvailable.set(false);
        stopAvailable.set(false);
        seekAvailable.set(false);
    }

    public void setTape(final Tape tape) throws TapeException {
        this.tape = Optional.of(tape);
        this.playableTape = tape.getPlayableTape();
        playAvailable.set(true);
        stopAvailable.set(false);
        seekAvailable.set(false);
    }

    public void stop() {
        if (tape.isPresent()) {
            isPlaying.set(false);
            playAvailable.set(true);
            stopAvailable.set(false);
            seekAvailable.set(true);
        }
    }

    public void play() {
        if (tape.isPresent()) {
            isPlaying.set(true);
            playAvailable.set(false);
            stopAvailable.set(true);
            seekAvailable.set(true);
        }
    }

    public void rewindToStart() throws TapeException {
        if (tape.isPresent()) {
            isPlaying.set(false);
            playableTape = tape.get().getPlayableTape();
            playAvailable.set(true);
            stopAvailable.set(false);
            seekAvailable.set(true);
        }
    }

    @Override
    public boolean hasNext() {
        if (!isPlaying.get()) {
            return false;
        } else {
            final boolean hasNext = playableTape.hasNext();
            if (!hasNext) {
                stop();
            }
            return hasNext;
        }
    }

    @Override
    public TapeBlock.Bit next() {
        if (!isPlaying.get()) {
            throw new NoSuchElementException("Tape stopped");
        } else {
            return playableTape.next();
        }
    }

}
