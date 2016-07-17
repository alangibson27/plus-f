package com.socialthingy.plusf.tape

import org.scalatest.prop.TableDrivenPropertyChecks

import scala.collection.JavaConverters._
import org.scalatest.{FlatSpec, Inspectors, Matchers}

class PureToneBlockSpec extends FlatSpec with TapeMatchers with Matchers with Inspectors with TableDrivenPropertyChecks {

  val pureToneBlock = new PureToneBlock(100, 3)

  "a single tone" should "have the correct number of bits" in {
    val singleTone = new PureToneBlock(5, 1)

    val signal = lowSignal
    val bits = singleTone.bits(signal).asScala.toList

    bits should have length 5
    forAll(bits) { bit =>
      bit.getState shouldBe low
    }
  }

  val startAndEndSignals = Table(
    ("pulses", "pulseCount", "start", "startValue", "end", "endValue"),
    ("odd", 3, "high", true, "high", true),
    ("odd", 3, "low", false, "high", true),
    ("even", 2, "high", true, "low", false),
    ("even", 2, "low", false, "low", false)
  )

  forAll(startAndEndSignals) { (pulses, pulseCount, start, startValue, end, endValue) =>
    s"a tone of an $pulses number of pulses" should s"have a $end signal at the end of the tone with the initial signal is $start" in {
      val tripleTone = new PureToneBlock(5, pulseCount)

      val signal = new SignalState(startValue)

      tripleTone.bits(signal).asScala.toList should have length (pulseCount * 5)

      signal.get shouldBe endValue
    }
  }

  "a pure tone block" should "have the correct number of tones of the correct pulse length when the signal is initially low" in {
    val bits = pureToneBlock.bits(lowSignal).asScala.toList

    val pulses = bits.splitInto(100, 100)

    pulses should have length 3
    pulses(0) should haveLengthAndState(100, low)
    pulses(1) should haveLengthAndState(100, high)
    pulses(2) should haveLengthAndState(100, low)
  }

  it should "always begin with a low pulse even if the signal state is high before the block begins" in {
    val initialPulse = pureToneBlock.bits(highSignal).asScala.take(1).toList
    initialPulse.head.getState shouldBe false
  }

  it should "have the correct number of tones of the correct pulse length when the signal is initially high" in {
    val bits = pureToneBlock.bits(highSignal).asScala.toList

    val pulses = bits.splitInto(100, 100)

    pulses should have length 3
    pulses(0) should haveLengthAndState(100, low)
    pulses(1) should haveLengthAndState(100, high)
    pulses(2) should haveLengthAndState(100, low)
  }
}
