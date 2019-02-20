package com.socialthingy.plusf.tape

import java.io.IOException

import org.scalatest.Matchers
import org.scalatest.FlatSpec

class TzxReaderSpec extends FlatSpec with Matchers {

  "TZX reader" should "correctly identify all blocks in a standard-speed TZX file" in {
    // given
    val standardSpeedFile = getClass.getResourceAsStream("/test.tzx")

    // when
    val tzx = new TapeFileReader(standardSpeedFile).readTzx()

    // then
    tzx.getVersion shouldBe "1.10"

    val blocks = tzx.getBlocks
    blocks should have size 7

    val programHeaderBlock = blocks.get(0)
    programHeaderBlock shouldBe a[VariableSpeedBlock]
    programHeaderBlock.asInstanceOf[VariableSpeedBlock].getPauseLength.toMillis shouldBe 0x03e8
    programHeaderBlock.asInstanceOf[VariableSpeedBlock].getData should have length 0x0013

    val programCodeBlock = blocks.get(1)
    programCodeBlock shouldBe a[VariableSpeedBlock]
    programCodeBlock.asInstanceOf[VariableSpeedBlock].getPauseLength.toMillis shouldBe 0x03e8
    programCodeBlock.asInstanceOf[VariableSpeedBlock].getData should have length 0x0066
  }

  it should "add standard suffix blocks to the end of a TZX file" in {
    // given
    val standardSpeedFile = getClass.getResourceAsStream("/test.tzx")

    // when
    val tzx = new TapeFileReader(standardSpeedFile).readTzx()

    // then
    tzx.getVersion shouldBe "1.10"

    val blocks = tzx.getBlocks
    blocks should have size 7

    blocks.get(2) shouldBe a[GroupStartBlock]
    blocks.get(3) shouldBe a[PulseSequenceBlock]
    blocks.get(4) shouldBe a[PauseBlock]
    blocks.get(5) shouldBe a[PauseBlock]
    blocks.get(5).asInstanceOf[PauseBlock].shouldStopTape shouldBe true
    blocks.get(6) shouldBe a[GroupEndBlock]
  }

  it should "reject a file with a malformed header" in {
    // given
    val malformedFile = getClass.getResourceAsStream("/screenfiller.z80")

    // when
    intercept[IOException] {
      new TapeFileReader(malformedFile).readTzx()
    }
  }

}
