package com.socialthingy.plusf.tape

import java.lang.{Boolean => JBoolean}
import java.time.Duration

import com.socialthingy.replist.RepList
import org.scalatest.{FlatSpec, Matchers}

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
    val newBits = variableSpeedBlock.getBitList(new SignalState(false)).iterator()

    val referenceTape = new RepList[JBoolean]()
    referenceBlock.write(referenceTape, false)
    val referenceBits = referenceTape.iterator()

    while (newBits.hasNext && referenceBits.hasNext) {
      val newBit = newBits.next
      val refBit = referenceBits.next

      newBit shouldBe refBit
    }

    newBits.hasNext shouldBe false
    referenceBits.hasNext shouldBe false
  }
}
