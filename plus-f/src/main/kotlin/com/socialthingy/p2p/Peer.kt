package com.socialthingy.p2p

import java.net.*
import java.nio.ByteBuffer
import java.util.concurrent.Executors

import com.codahale.metrics.ExponentiallyDecayingReservoir
import com.codahale.metrics.Histogram
import com.codahale.metrics.Meter
import org.slf4j.LoggerFactory
import java.util.*

enum class State {
    INITIAL, WAITING_FOR_PEER, CONNECTED, CLOSING
}

class Peer(private val bindAddress: InetSocketAddress,
           private val discoveryServiceAddress: InetSocketAddress,
           val callbacks: Callbacks,
           val serialiser: Serialiser,
           val deserialiser: Deserialiser,
           private val timeout: java.time.Duration) {

    val log = LoggerFactory.getLogger(javaClass)!!
    private var socket: Optional<DatagramSocket> = Optional.empty()
    private val socketHandlerExecutor = Executors.newSingleThreadExecutor()!!

    private val latencies = Histogram(ExponentiallyDecayingReservoir())
    private val sizes = Histogram(ExponentiallyDecayingReservoir())
    private val meter = Meter()
    private var outOfOrder = 0

    private var peerConnection: Optional<InetSocketAddress> = Optional.empty()

    private val states = HashMap<State, (ByteBuffer, InetSocketAddress) -> Unit>()

    init {
        states.put(State.INITIAL, this::doNothing)
        states.put(State.WAITING_FOR_PEER, this::waitForResponse)
        states.put(State.CONNECTED, this::connectedToPeer)
    }

    var currentState: State = State.INITIAL
    var currentSessionId: Optional<String> = Optional.empty()
    var lastReceivedTimestamp = -1L

    private fun doNothing(data: ByteBuffer, source: InetSocketAddress) {}

    private fun connectedToPeer(data: ByteBuffer, source: InetSocketAddress) {
        try {
            val decompressed = WrappedData(PacketUtils.decompress(data), deserialiser)
            latencies.update(System.currentTimeMillis() - decompressed.systemTime)
            meter.mark()
            if (decompressed.timestamp >= lastReceivedTimestamp) {
                lastReceivedTimestamp = decompressed.timestamp
                callbacks.data(decompressed.content)
            } else {
                outOfOrder += 1
            }

            peerConnection.ifPresent { x ->
                if (x != source) {
                    log.info("Switching peer connection from {} to {}", x.toString(), source.toString())
                }
            }

            peerConnection = Optional.of(source)

        } catch (ex: Exception) {
            log.error(
                    String.format("Unable to decode received message %s from %s", data.toString(), source.toString()),
                    ex
            )
        }
    }

    private fun waitForResponse(data: ByteBuffer, source: InetSocketAddress) {
        val result = String(data.array(), data.position(), data.remaining())
        val splitMessage = result.split('|')

        when {
            splitMessage.size == 3 && splitMessage[0] == "PEER" -> {
                val peerHost = splitMessage[1]
                val peerPort = splitMessage[2].toInt()
                callbacks.connectedToPeer(peerPort)
                log.info("Connected to peer at {}:{}", peerHost, peerPort)
                currentState = State.CONNECTED
                peerConnection = Optional.of(InetSocketAddress(peerHost, peerPort))
            }

            splitMessage.size == 1 && splitMessage[0] == "WAIT" -> {
                callbacks.waitingForPeer()
                log.info("Waiting for peer to join")
            }

            else -> {
                log.error(
                        "Unrecognised message received from {} with content {}, still waiting for peer",
                        source.toString(),
                        data.toString()
                )
            }
        }
    }

    private fun startIfRequired() {
        if (socket.map { it.isClosed }.orElse(false)) {
            val newSocket = DatagramSocket(bindAddress)
            socket = Optional.of(newSocket)
            newSocket.soTimeout = timeout.toMillis().toInt()

            socketHandlerExecutor.submit({
                val receivedPacket = DatagramPacket(ByteArray(16384), 16384)
                while (!newSocket.isClosed) {
                    try {
                        newSocket.receive(receivedPacket)
                        log.debug("Received packet of size {} from {}", receivedPacket.length, receivedPacket.address.toString())
                        sizes.update(receivedPacket.length)
                        states[currentState]?.invoke(
                                ByteBuffer.wrap(receivedPacket.data, receivedPacket.offset, receivedPacket.length),
                                receivedPacket.socketAddress as InetSocketAddress
                        )
                    } catch (e: SocketTimeoutException) {
                        if (currentState == State.WAITING_FOR_PEER) {
                            callbacks.discoveryTimeout()
                            close()
                        } else if (e.message == "Socket closed") {
                            // do nothing, expected
                        }
                    } catch (e: Exception) {
                        log.error("Failure in receive loop", e)
                    }
                }
                log.info("Socket closed")
            })
        }
    }

    fun join(sessionId: String, fwdPort: Optional<Int> = Optional.empty()) {
        startIfRequired()

        callbacks.discovering()
        val joinCommand = fwdPort.map { "JOIN|" + sessionId + it }.orElse("JOIN|" + sessionId)

        currentState = State.WAITING_FOR_PEER
        currentSessionId = Optional.of(sessionId)
        socket.ifPresent { it.send(PacketUtils.buildPacket(joinCommand, discoveryServiceAddress)) }
    }

    fun send(data: RawData) {
        when {
            !peerConnection.isPresent ->
                log.error("Unable to send data, no peer connection")

            socket.map { it.isClosed }.orElse(false) ->
                log.error("Unable to send data, socket closed")

            else -> {
                val compressed = PacketUtils.compress(data.wrap.pack(serialiser))
                log.debug("Sending packet of size {} to {}", compressed.remaining(), peerConnection.get().toString())
                socket.get().send(DatagramPacket(compressed.array(), compressed.position(), compressed.remaining(), peerConnection.get()))
            }
        }
    }

    fun statistics(): Statistics = Statistics(
            latencies.snapshot.get99thPercentile().toInt(),
            outOfOrder,
            sizes.snapshot.get99thPercentile().toInt(),
            meter.oneMinuteRate
    )

    fun close() {
        if (currentState == State.WAITING_FOR_PEER && currentSessionId.isPresent) {
            socket.ifPresent { it.send(PacketUtils.buildPacket("CANCEL" + currentSessionId.get(), discoveryServiceAddress)) }
            reset()
            callbacks.discoveryCancelled()
        } else {
            reset()
            callbacks.closed()
        }
    }

    private fun reset() {
        if (socket.isPresent) {
            socket.get().close()
        }
        currentSessionId = Optional.empty()
        currentState = State.INITIAL
        peerConnection = Optional.empty()
    }
}

data class Statistics(val latency: Int, val outOfOrder: Int, val size: Int, val oneMinuteRate: Double)
