package com.socialthingy.plusf.tape

import java.time.Duration

import com.socialthingy.plusf.RepeatingList
import com.socialthingy.plusf.tape.TapeBlock.Bit
import com.socialthingy.plusf.util.Bitwise._
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

class VariableSpeedBlockSpec extends FlatSpec with Matchers with TapeMatchers {
  "a variable speed block with an odd number of pilot tones" should "play correctly" in {
    val variableSpeedBlock = new VariableSpeedBlock(Duration.ofMillis(10), Array(0x84), 10, 6, 5, 5, 10, 3, 8)
    val referenceBlock = new ReferenceVariableSpeedBlock(Duration.ofMillis(10), Array(0x84), 10, 6, 5, 5, 10, 3, 8)

    checkSameContent(variableSpeedBlock, referenceBlock)
  }

  "a variable speed block with an even number of pilot tones" should "play correctly" in {
    val variableSpeedBlock = new VariableSpeedBlock(Duration.ofMillis(10), Array(0x84), 10, 6, 5, 5, 10, 2, 8)
    val referenceBlock = new ReferenceVariableSpeedBlock(Duration.ofMillis(10), Array(0x84), 10, 6, 5, 5, 10, 2, 8)

    checkSameContent(variableSpeedBlock, referenceBlock)
  }

  private def checkSameContent(variableSpeedBlock: VariableSpeedBlock, referenceBlock: ReferenceVariableSpeedBlock): Unit = {
    val newBits = variableSpeedBlock.bits(new SignalState(false)).asScala.toList

    val referenceTape = new RepeatingList[Bit]()
    referenceBlock.write(referenceTape, false)
    val referenceBits = referenceTape.iterator().asScala.toList

    referenceBits.size shouldBe newBits.size
    referenceBits.indices foreach { idx =>
      if (idx % 500 == 0) {
        print(".")
      }
      withClue(s"bit $idx (${referenceBits(idx).getStage})") {
        referenceBits(idx).getState shouldBe newBits(idx).getState
      }
    }
  }

  private def referenceTapeFor(block: VariableSpeedBlock) = {
    import block._
    val tape = ListBuffer[Bit]()

    // pilot tone
    var state = false
    pilotToneLength times {
      pilotPulseLength times { tape += new TapeBlock.Bit(state, "pilot") }
    }

    // sync 1 - on pulse
    sync1PulseLength times { tape += new TapeBlock.Bit(state, "sync 1") }
    state = !state

    // sync 2 - off pulse
    sync2PulseLength times { tape += new TapeBlock.Bit(state, "sync 2") }
    state = !state

    // data
    data.indices foreach { i =>
      val (b, lastBit) = if (i == data.length - 1) {
        (data(i) & finalByteMask(finalByteBitsUsed - 1), 8 - finalByteBitsUsed)
      } else {
        (data(i), 0)
      }

      (7 to lastBit by -1) foreach { bit =>
        val high = (b & (1 << bit)) != 0
        val pulseLen = if (high) onePulseLength else zeroPulseLength
        pulseLen times { tape += new TapeBlock.Bit(state, "data") }
        state = !state
        pulseLen times { tape += new TapeBlock.Bit(state, "data") }
        state = !state
      }
    }

    if (!pauseLength.isZero) {
      3500 times { tape += new TapeBlock.Bit(state, "end") }
      3500 * pauseLength.toMillis.toInt times { tape += new TapeBlock.Bit(false, "pause") }
      state = false
    }

    tape.map(_.getState).toList
  }

  private val finalByteMask = Array[Int](
    binary("10000000"),
    binary("11000000"),
    binary("11100000"),
    binary("11110000"),
    binary("11111000"),
    binary("11111100"),
    binary("11111110"),
    binary("11111111")
  )

  implicit class IntOps(n: Int) {
    def times(action: => Unit) = (0 until n) foreach (_ => action)
  }
}
