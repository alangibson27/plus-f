package com.socialthingy.plusf.tape

import scala.collection.JavaConverters._
import org.scalatest.{FlatSpec, Matchers}

class PulseSequenceBlockSpec extends FlatSpec with TapeMatchers with Matchers {

  val pulseSequenceBlock = new PulseSequenceBlock(Array[Int](100, 75, 125))

  "a pulse sequence block" should "have the correct number of tones of the correct pulse length when the signal is initially low" in {
    val bits = pulseSequenceBlock.getBitList(lowSignal).asScala.toList

    val pulses = bits.splitInto(100, 75, 125)

    pulses should have length 3
    pulses(0) should haveLengthAndState(100, low)
    pulses(1) should haveLengthAndState(75, high)
    pulses(2) should haveLengthAndState(125, low)
  }

  it should "have the correct number of tones of the correct pulse length when the signal is initially high" in {
    val bits = pulseSequenceBlock.getBitList(highSignal).asScala.toList

    val pulses = bits.splitInto(100, 75, 125)

    pulses should have length 3
    pulses(0) should haveLengthAndState(100, low)
    pulses(1) should haveLengthAndState(75, high)
    pulses(2) should haveLengthAndState(125, low)
  }

  it should "always start with a low signal, even if the signal before the block is high" in {
    val bits = pulseSequenceBlock.getBitList(highSignal).asScala.take(1).toList
    bits.head shouldBe false
  }

  it should "skip correctly within a pulse" in {
    val bits = pulseSequenceBlock.getBitList(lowSignal)
    bits.skip(99)
    bits.next() shouldBe false
    bits.next() shouldBe true
  }

  it should "skip correctly across pulses" in {
    val bits = pulseSequenceBlock.getBitList(lowSignal)
    bits.skip(100)
    bits.next() shouldBe true
  }
}
