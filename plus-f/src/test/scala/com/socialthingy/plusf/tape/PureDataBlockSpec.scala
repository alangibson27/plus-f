package com.socialthingy.plusf.tape

import java.time.Duration

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._

class PureDataBlockSpec extends FlatSpec with TapeMatchers with Matchers with TableDrivenPropertyChecks {

  val initialSignals = Table(
    ("level", "firstValue", "secondValue"),
    ("low", low, high),
    ("high", high, low)
  )

  forAll(initialSignals) { (level, firstValue, secondValue) =>
    "a pure data block not followed by a pause" should
      s"have the correct number of tones of the correct pulse length when the signal is initially $level" in {
      val pureDataBlock = new PureDataBlock(Duration.ZERO, Array[Int](0x54, 0xf0), 50, 100, 8)

      val bits = pureDataBlock.getBlockSignal(new SignalState(firstValue)).asScala.toList

      val pulses = bits.splitInto(
        50, 50, 100, 100, 50, 50, 100, 100, 50, 50, 100, 100, 50, 50, 50, 50,
        100, 100, 100, 100, 100, 100, 100, 100, 50, 50, 50, 50, 50, 50, 50, 50
      )

      pulses should have length 32
      pulses(0) should haveLengthAndState(50, firstValue)
      pulses(1) should haveLengthAndState(50, secondValue)

      pulses(2) should haveLengthAndState(100, firstValue)
      pulses(3) should haveLengthAndState(100, secondValue)

      pulses(4) should haveLengthAndState(50, firstValue)
      pulses(5) should haveLengthAndState(50, secondValue)

      pulses(6) should haveLengthAndState(100, firstValue)
      pulses(7) should haveLengthAndState(100, secondValue)

      pulses(8) should haveLengthAndState(50, firstValue)
      pulses(9) should haveLengthAndState(50, secondValue)

      pulses(10) should haveLengthAndState(100, firstValue)
      pulses(11) should haveLengthAndState(100, secondValue)

      pulses(12) should haveLengthAndState(50, firstValue)
      pulses(13) should haveLengthAndState(50, secondValue)

      pulses(14) should haveLengthAndState(50, firstValue)
      pulses(15) should haveLengthAndState(50, secondValue)

      pulses(16) should haveLengthAndState(100, firstValue)
      pulses(17) should haveLengthAndState(100, secondValue)

      pulses(18) should haveLengthAndState(100, firstValue)
      pulses(19) should haveLengthAndState(100, secondValue)

      pulses(20) should haveLengthAndState(100, firstValue)
      pulses(21) should haveLengthAndState(100, secondValue)

      pulses(22) should haveLengthAndState(100, firstValue)
      pulses(23) should haveLengthAndState(100, secondValue)

      pulses(24) should haveLengthAndState(50, firstValue)
      pulses(25) should haveLengthAndState(50, secondValue)

      pulses(26) should haveLengthAndState(50, firstValue)
      pulses(27) should haveLengthAndState(50, secondValue)

      pulses(28) should haveLengthAndState(50, firstValue)
      pulses(29) should haveLengthAndState(50, secondValue)

      pulses(30) should haveLengthAndState(50, firstValue)
      pulses(31) should haveLengthAndState(50, secondValue)
    }
  }

  "a pure data block followed by a pause" should "finish with a low signal for the pause duration" in {
    val pureDataBlock = new PureDataBlock(Duration.ofMillis(10), Array[Int](0x00), 50, 100, 8)

    val bits = pureDataBlock.getBlockSignal(lowSignal).asScala.toList

    val pulses = bits.splitInto(50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 35000 + 3500)

    pulses should have length 17

    pulses(0) should haveLengthAndState(50, low)
    pulses(1) should haveLengthAndState(50, high)

    pulses(2) should haveLengthAndState(50, low)
    pulses(3) should haveLengthAndState(50, high)

    pulses(4) should haveLengthAndState(50, low)
    pulses(5) should haveLengthAndState(50, high)

    pulses(6) should haveLengthAndState(50, low)
    pulses(7) should haveLengthAndState(50, high)

    pulses(8) should haveLengthAndState(50, low)
    pulses(9) should haveLengthAndState(50, high)

    pulses(10) should haveLengthAndState(50, low)
    pulses(11) should haveLengthAndState(50, high)

    pulses(12) should haveLengthAndState(50, low)
    pulses(13) should haveLengthAndState(50, high)

    pulses(14) should haveLengthAndState(50, low)
    pulses(15) should haveLengthAndState(50, high)

    pulses(16) should haveLengthAndState(35000 + 3500, low)
  }

  it should "have a short ending high pulse when the final data pulse is low" in {
    val pureDataBlock = new PureDataBlock(Duration.ofMillis(10), Array[Int](0x00), 50, 100, 8)

    val bits = pureDataBlock.getBlockSignal(highSignal).asScala.toList

    val pulses = bits.splitInto(50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 3500, 35000)

    pulses should have length 18

    pulses(0) should haveLengthAndState(50, high)
    pulses(1) should haveLengthAndState(50, low)

    pulses(2) should haveLengthAndState(50, high)
    pulses(3) should haveLengthAndState(50, low)

    pulses(4) should haveLengthAndState(50, high)
    pulses(5) should haveLengthAndState(50, low)

    pulses(6) should haveLengthAndState(50, high)
    pulses(7) should haveLengthAndState(50, low)

    pulses(8) should haveLengthAndState(50, high)
    pulses(9) should haveLengthAndState(50, low)

    pulses(10) should haveLengthAndState(50, high)
    pulses(11) should haveLengthAndState(50, low)

    pulses(12) should haveLengthAndState(50, high)
    pulses(13) should haveLengthAndState(50, low)

    pulses(14) should haveLengthAndState(50, high)
    pulses(15) should haveLengthAndState(50, low)

    pulses(16) should haveLengthAndState(3500, high)
    pulses(17) should haveLengthAndState(35000, low)
  }

  "a pure data block" should "skip correctly within a single bit" in {
    val pureDataBlock = new PureDataBlock(Duration.ofMillis(0), Array[Int](0x00), 4, 2, 8)

    val bits = pureDataBlock.getBlockSignal(lowSignal)

    bits.next() shouldBe false
    bits.skip(2) shouldBe 2
    bits.next() shouldBe false
    bits.next() shouldBe true
  }

  it should "skip correctly across bits" in {
    val pureDataBlock = new PureDataBlock(Duration.ofMillis(0), Array[Int](0x00), 4, 2, 8)

    val bits = pureDataBlock.getBlockSignal(lowSignal)

    bits.next() shouldBe false
    bits.skip(7) shouldBe 7
    bits.next() shouldBe false
    bits.next() shouldBe false
    bits.next() shouldBe false
    bits.next() shouldBe false
    bits.next() shouldBe true
  }

  it should "skip correctly across bytes" in {
    val pureDataBlock = new PureDataBlock(Duration.ofMillis(0), Array[Int](0x00, 0xff), 4, 2, 8)

    val bits = pureDataBlock.getBlockSignal(lowSignal)

    bits.next() shouldBe false
    bits.skip(63) shouldBe 63
    bits.next() shouldBe false
    bits.next() shouldBe false
    bits.next() shouldBe true
    bits.next() shouldBe true
  }

  it should "skip correctly to the end of the block" in {
    val pureDataBlock = new PureDataBlock(Duration.ofMillis(0), Array[Int](0x00, 0xff), 4, 2, 8)

    val bits = pureDataBlock.getBlockSignal(lowSignal)

    bits.next() shouldBe false
    bits.skip(63) shouldBe 63
    bits.next() shouldBe false
    bits.next() shouldBe false
    bits.next() shouldBe true
    bits.next() shouldBe true
    bits.skip(28) shouldBe 28
    bits.hasNext() shouldBe false
  }

  it should "skip correctly beyond the end of the block" in {
    val pureDataBlock = new PureDataBlock(Duration.ofMillis(0), Array[Int](0x00, 0xff), 4, 2, 8)

    val bits = pureDataBlock.getBlockSignal(lowSignal)

    bits.next() shouldBe false
    bits.skip(63) shouldBe 63
    bits.next() shouldBe false
    bits.next() shouldBe false
    bits.next() shouldBe true
    bits.next() shouldBe true
    bits.skip(30) shouldBe 28
    bits.hasNext() shouldBe false
  }

  it should "skip correctly to the end of the block when it only has 7 bits in the final byte" in {
    val pureDataBlock = new PureDataBlock(Duration.ofMillis(0), Array[Int](0x00, 0xff), 4, 2, 7)

    val bits = pureDataBlock.getBlockSignal(lowSignal)

    bits.next() shouldBe false
    bits.skip(63) shouldBe 63
    bits.next() shouldBe false
    bits.next() shouldBe false
    bits.next() shouldBe true
    bits.next() shouldBe true
    bits.skip(24) shouldBe 24
    bits.hasNext() shouldBe false
  }

  it should "skip correctly beyond the end of the block when it only has 7 bits in the final byte" in {
    val pureDataBlock = new PureDataBlock(Duration.ofMillis(0), Array[Int](0x00, 0xff), 4, 2, 7)

    val bits = pureDataBlock.getBlockSignal(lowSignal)

    bits.next() shouldBe false
    bits.skip(63) shouldBe 63
    bits.next() shouldBe false
    bits.next() shouldBe false
    bits.next() shouldBe true
    bits.next() shouldBe true
    bits.skip(25) shouldBe 24
    bits.hasNext() shouldBe false
  }

  val finalByteLengths = Table("finalByteLength", 7, 6, 5, 4, 3, 2, 1)

  forAll(finalByteLengths) { finalByteLength =>
    s"a pure data block with $finalByteLength bits in the final byte" should "be represented correctly" in {
      val pureDataBlock = new PureDataBlock(Duration.ZERO, Array[Int](0x00), 1, 2, finalByteLength)

      val bits = pureDataBlock.getBlockSignal(highSignal).asScala.toList

      val numPulses = finalByteLength * 2
      val pulses = bits.splitInto(Array.fill(numPulses)(1): _*)

      pulses should have length numPulses

      (0 until finalByteLength) foreach { idx =>
        pulses(idx * 2) should haveLengthAndState(1, high)
        pulses((idx * 2) + 1) should haveLengthAndState(1, low)
      }
    }
  }
}
