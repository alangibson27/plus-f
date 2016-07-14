package com.socialthingy.plusf.tape

import scala.collection.JavaConverters._
import org.scalatest.{FlatSpec, Matchers}

class PureToneBlockSpec extends FlatSpec with TapeMatchers with Matchers {

  val pureToneBlock = new PureToneBlock(100, 3)

  "a pure tone block" should "have the correct number of tones of the correct pulse length when the signal is initially low" in {
    val bits = pureToneBlock.bits(lowSignal).asScala.toList

    val pulses = bits.splitInto(100, 100)

    pulses should have length 3
    pulses(0) should haveLengthAndState(100, low)
    pulses(1) should haveLengthAndState(100, high)
    pulses(2) should haveLengthAndState(100, low)
  }

  it should "have the correct number of tones of the correct pulse length when the signal is initially high" in {
    val bits = pureToneBlock.bits(highSignal).asScala.toList

    val pulses = bits.splitInto(100, 100)

    pulses should have length 3
    pulses(0) should haveLengthAndState(100, high)
    pulses(1) should haveLengthAndState(100, low)
    pulses(2) should haveLengthAndState(100, high)
  }
}
