package com.socialthingy.plusf.tape

import java.lang.{Boolean => JBoolean}
import java.time.Duration

import com.socialthingy.replist.RepList
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class VariableSpeedBlockSpec extends FlatSpec with Matchers with TapeMatchers with TableDrivenPropertyChecks {
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

  "a header block" should "report the block type and name in its description" in {
    val blockTypes = Table(
      ("marker", "name"),
      (0x00, "Program"),
      (0x01, "Number array"),
      (0x02, "Character array"),
      (0x03, "Bytes"))

    forAll(blockTypes) { (marker, name) =>
      val variableSpeedBlock = new VariableSpeedBlock(
        Duration.ofMillis(10),
        Array(0x00, marker, 0x52, 0x4f, 0x4d, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x00, 0x00, 0x00, 0x00, 0xf1)
      )

      variableSpeedBlock.getDisplayName shouldBe s"$name: ROM"
    }
  }

  it should "strip non-ascii characters from a header block name in its description" in {
    val variableSpeedBlock = new VariableSpeedBlock(
      Duration.ofMillis(10),
      Array(0x00, 0x00, 0xff, 0x52, 0x4f, 0x4d, 0x01, 0x20, 0x20, 0x20, 0x20, 0x20, 0x00, 0x00, 0x00, 0x00, 0xf1)
    )

    variableSpeedBlock.getDisplayName shouldBe "Program: ROM"
  }

  it should "use a standard description if the block type is unrecognised" in {
    val variableSpeedBlock = new VariableSpeedBlock(
      Duration.ofMillis(10),
      Array(0x00, 0xdd, 0xff, 0x52, 0x4f, 0x4d, 0x01, 0x20, 0x20, 0x20, 0x20, 0x20, 0x00, 0x00, 0x00, 0x00, 0xf1)
    )

    variableSpeedBlock.getDisplayName shouldBe "Standard speed header block"
  }

  "a data block" should "have a standard description" in {
    val variableSpeedBlock = new VariableSpeedBlock(
      Duration.ofMillis(10),
      Array(0xff, 0x10)
    )

    variableSpeedBlock.getDisplayName shouldBe "Standard speed data block"
  }

  "an unrecognised block" should "have a generic description" in {
    val variableSpeedBlock = new VariableSpeedBlock(
      Duration.ofMillis(10),
      Array(0x01, 0x10)
    )

    variableSpeedBlock.getDisplayName shouldBe "Standard speed block"
  }

  private def checkSameContent(variableSpeedBlock: VariableSpeedBlock, referenceBlock: ReferenceVariableSpeedBlock): Unit = {
    val newBits = variableSpeedBlock.getBlockSignal(new SignalState(false))

    val referenceTape = new RepList[JBoolean]()
    referenceBlock.write(referenceTape, false)
    val referenceBits = referenceTape.iterator()

    var position = 0
    while (newBits.hasNext && referenceBits.hasNext) {
      val newBit = newBits.next
      val refBit = referenceBits.next

      if (newBit != refBit) {
        println(s"mismatch at position ${position}")
      }
      newBit shouldBe refBit
      position += 1
    }

    newBits.hasNext shouldBe false
    referenceBits.hasNext shouldBe false
  }
}
