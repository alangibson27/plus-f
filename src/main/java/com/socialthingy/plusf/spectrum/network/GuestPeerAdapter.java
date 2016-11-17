package com.socialthingy.plusf.spectrum.network;

import akka.actor.ActorSystem;
import com.socialthingy.plusf.p2p.Callbacks;
import com.socialthingy.plusf.spectrum.Settings;

import java.util.function.Consumer;

public class GuestPeerAdapter extends PeerAdapter<EmulatorState> implements Callbacks {
    public GuestPeerAdapter(final ActorSystem actorSystem, final Consumer<EmulatorState> receiver) {
        super(actorSystem, receiver, Settings.GUEST_PORT, new GuestStateHandler(), new EmulatorStateHandler());
    }
}
