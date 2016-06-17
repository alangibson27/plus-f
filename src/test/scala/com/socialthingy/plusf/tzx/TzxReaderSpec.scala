package com.socialthingy.plusf.tzx

import org.scalatest.Matchers
import org.scalatest.FlatSpec

import scala.collection.JavaConverters._

class TzxReaderSpec extends FlatSpec with Matchers {

  "TZX reader" should "correctly identify all blocks in a standard-speed TZX file" in {
    // given
    val standardSpeedFile = getClass.getResourceAsStream("/test.tzx")

    // when
    val tzx = new TzxReader(standardSpeedFile).readTzx()

    // then
    tzx.getVersion shouldBe "1.10"

    val blocks = tzx.getBlocks.asScala.toList
    blocks should have size 2

    val programHeaderBlock = blocks.head
    programHeaderBlock shouldBe a[VariableSpeedBlock]
    programHeaderBlock.asInstanceOf[VariableSpeedBlock].getPauseLength.toMillis shouldBe 0x03e8
    programHeaderBlock.asInstanceOf[VariableSpeedBlock].getData should have length 0x0013

    val programCodeBlock = blocks(1)
    programCodeBlock shouldBe a[VariableSpeedBlock]
    programCodeBlock.asInstanceOf[VariableSpeedBlock].getPauseLength.toMillis shouldBe 0x03e8
    programCodeBlock.asInstanceOf[VariableSpeedBlock].getData should have length 0x0066
  }

  it should "reject a file with a malformed header" in {
    // given
    val malformedFile = getClass.getResourceAsStream("/screenfiller.z80")

    // when
    intercept[TzxException] {
      new TzxReader(malformedFile).readTzx()
    }
  }

}
