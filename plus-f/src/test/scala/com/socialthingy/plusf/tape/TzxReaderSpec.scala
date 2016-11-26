package com.socialthingy.plusf.tape

import org.scalatest.Matchers
import org.scalatest.FlatSpec

class TzxReaderSpec extends FlatSpec with Matchers {

  "TZX reader" should "correctly identify all blocks in a standard-speed TZX file" in {
    // given
    val standardSpeedFile = getClass.getResourceAsStream("/test.tzx")

    // when
    val tzx = new TapeFileReader(standardSpeedFile).readTzx()

    // then
    tzx.version shouldBe "1.10"

    val blocks = tzx.blocks
    blocks should have size 6

    val programHeaderBlock = blocks(0)
    programHeaderBlock shouldBe a[VariableSpeedBlock]
    programHeaderBlock.asInstanceOf[VariableSpeedBlock].getPauseLength.toMillis shouldBe 0x03e8
    programHeaderBlock.asInstanceOf[VariableSpeedBlock].getData should have length 0x0013

    val programCodeBlock = blocks(1)
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
    tzx.version shouldBe "1.10"

    val blocks = tzx.blocks
    blocks should have size 6

    blocks(2) shouldBe a[GroupStartBlock]
    blocks(3) shouldBe a[PulseSequenceBlock]
    blocks(4) shouldBe a[PauseBlock]
    blocks(5) shouldBe a[GroupEndBlock]
  }

  it should "reject a file with a malformed header" in {
    // given
    val malformedFile = getClass.getResourceAsStream("/screenfiller.z80")

    // when
    intercept[TapeException] {
      new TapeFileReader(malformedFile).readTzx()
    }
  }

}
