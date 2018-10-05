package com.socialthingy.p2p

interface Callbacks {
    fun data(content: Any)
    fun discoveryTimeout()
    fun discoveryCancelled()
    fun waitingForPeer()
    fun connectedToPeer(port: Int)
    fun discovering()
    fun initialising()
    fun closed()
}
