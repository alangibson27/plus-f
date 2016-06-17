package com.socialthingy.plusf.tzx;

import com.socialthingy.plusf.RepeatingList;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PlayableTzx implements Iterator<TzxBlock.Bit> {
    public enum PlayerState { PLAYING, STOPPED }

    private final Iterator<TzxBlock.Bit> tape;
    private PlayerState playerState = PlayerState.STOPPED;

    public PlayableTzx(final Iterator<TzxBlock.Bit> tape) {
        this.tape = tape;
    }

    public void stop() {
        playerState = PlayerState.STOPPED;
    }

    public void play() {
        playerState = PlayerState.PLAYING;
    }

    public PlayerState getState() {
        return playerState;
    }

    @Override
    public boolean hasNext() {
        if (playerState == PlayerState.STOPPED) {
            return false;
        } else {
            return tape.hasNext();
        }
    }

    @Override
    public TzxBlock.Bit next() {
        if (playerState == PlayerState.STOPPED) {
            throw new NoSuchElementException("Tape stopped");
        } else {
            return tape.next();
        }
    }
}
