package com.socialthingy.plusf.spectrum;

import com.socialthingy.plusf.tape.Tape;

public interface TapeListener {
    void tapeChanged(Tape tape);
    void blockChanged(int blockIndex);
}
