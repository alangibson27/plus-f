package com.socialthingy.plusf.tape

import com.socialthingy.plusf.tape.TapeBlock.Bit

import scala.collection.JavaConverters._
import org.scalatest.{FlatSpec, Matchers}

class PulseSequenceBlockSpec extends FlatSpec with TapeMatchers with Matchers {

  val pulseSequenceBlock = new PulseSequenceBlock(3, Array[Int](100, 75, 125))

  "a pulse sequence block" should "have the correct number of tones of the correct pulse length when the signal is initially low" in {
    val bits: List[Bit] = pulseSequenceBlock.bits(lowSignal).asScala.toList

    val pulses = bits.splitInto(100, 75, 125)

    pulses should have length 3
    pulses(0) should haveLengthAndState(100, low)
    pulses(1) should haveLengthAndState(75, high)
    pulses(2) should haveLengthAndState(125, low)
  }

  it should "have the correct number of tones of the correct pulse length when the signal is initially high" in {
    val bits: List[Bit] = pulseSequenceBlock.bits(highSignal).asScala.toList

    val pulses = bits.splitInto(100, 75, 125)

    pulses should have length 3
    pulses(0) should haveLengthAndState(100, low)
    pulses(1) should haveLengthAndState(75, high)
    pulses(2) should haveLengthAndState(125, low)
  }
}
