package com.socialthingy.plusf.spectrum.network;

import akka.actor.ActorSystem;
import com.socialthingy.plusf.p2p.Callbacks;
import com.socialthingy.plusf.spectrum.Settings;

import java.util.function.Consumer;

public class EmulatorPeerAdapter extends PeerAdapter<GuestState> implements Callbacks {
    public EmulatorPeerAdapter(final ActorSystem actorSystem, final Consumer<GuestState> receiver) {
        super(actorSystem, receiver, Settings.COMPUTER_PORT, new EmulatorStateHandler(), new GuestStateHandler());
    }
}
