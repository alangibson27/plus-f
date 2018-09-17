package com.socialthingy.plusf.tape

import java.time.Duration
import java.util.{ List => JList }

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConversions._
import scala.language.implicitConversions

class TapeSpec extends FlatSpec with Matchers {
  "Tape" should "provide all information in all archive info blocks as a list of key-value pairs" in {
    val tape = givenTapeOf(
      new ArchiveInfoBlock(List[JPair]("Field 1" -> "Value 1", "Field 2" -> "Value 2")),
      new PauseBlock(Duration.ofSeconds(1)),
      new ArchiveInfoBlock(List[JPair]("Field 3" -> "Value 3", "Field 2" -> "Value 2 Again"))
    )

    val expected: JList[JPair] = List[JPair](
      "Field 1" -> "Value 1",
      "Field 2" -> "Value 2",
      "Field 3" -> "Value 3",
      "Field 2" -> "Value 2 Again"
    )

    tape.archiveInfo shouldBe expected
  }

  it should "return only data blocks as navigable blocks in a tape with no loops or groups" in {
    val tape = givenTapeOf(
      new ArchiveInfoBlock(List[JPair]("Field 1" -> "Value 1")),
      new MessageBlock("This is a message", Duration.ofSeconds(1)),
      new PauseBlock(Duration.ofSeconds(1)),
      new PulseSequenceBlock(Array[Int](100, 200, 100, 200)),
      new PureDataBlock(Duration.ofSeconds(1), Array[Int](0x01, 0x02, 0x03), 100, 200, 8),
      PureToneBlock.create(100, 1000),
      new TextDescriptionBlock("This is a tape"),
      new VariableSpeedBlock(Duration.ofSeconds(1), Array[Int](0x01, 0x02, 0x03))
    )

    val navigableBlocks = tape.navigableBlocks

    navigableBlocks should have size 5
    navigableBlocks(0).index shouldBe 2
    navigableBlocks(0).block shouldBe a[PauseBlock]

    navigableBlocks(1).index shouldBe 3
    navigableBlocks(1).block shouldBe a[PulseSequenceBlock]

    navigableBlocks(2).index shouldBe 4
    navigableBlocks(2).block shouldBe a[PureDataBlock]

    navigableBlocks(3).index shouldBe 5
    navigableBlocks(3).block shouldBe a[PulseSequenceBlock]

    navigableBlocks(4).index shouldBe 7
    navigableBlocks(4).block shouldBe a[VariableSpeedBlock]
  }

  it should "return top-level group start blocks as navigable blocks, but not the blocks within them" in {
    val tape = givenTapeOf(
      new ArchiveInfoBlock(List[JPair]("Field 1" -> "Value 1")),
      new VariableSpeedBlock(Duration.ofSeconds(1), Array.ofDim[Int](0x01)),
      new PauseBlock(Duration.ofSeconds(1)),
      new GroupStartBlock("Group Start"),
      new GroupStartBlock("Inner Group Start"),
      PureToneBlock.create(100, 1000),
      new PureDataBlock(Duration.ofSeconds(1), Array.ofDim[Int](0x01), 100, 200, 8),
      new GroupEndBlock(),
      new GroupEndBlock(),
      new PauseBlock(Duration.ofSeconds(1))
    )

    val navigableBlocks = tape.navigableBlocks

    navigableBlocks should have size 4

    navigableBlocks(0).index shouldBe 1
    navigableBlocks(0).block shouldBe a[VariableSpeedBlock]

    navigableBlocks(1).index shouldBe 2
    navigableBlocks(1).block shouldBe a[PauseBlock]

    navigableBlocks(2).index shouldBe 3
    navigableBlocks(2).block shouldBe a[GroupStartBlock]

    navigableBlocks(3).index shouldBe 9
    navigableBlocks(3).block shouldBe a[PauseBlock]
  }

  it should "return top-level loop start blocks as navigable blocks, but not the blocks within them" in {
    val tape = givenTapeOf(
      new ArchiveInfoBlock(List[JPair]("Field 1" -> "Value 1")),
      new VariableSpeedBlock(Duration.ofSeconds(1), Array.ofDim[Int](0x01)),
      new PauseBlock(Duration.ofSeconds(1)),
      new LoopStartBlock(10),
      new LoopStartBlock(20),
      PureToneBlock.create(100, 1000),
      new PureDataBlock(Duration.ofSeconds(1), Array.ofDim[Int](0x01), 100, 200, 8),
      new LoopEndBlock(),
      new LoopEndBlock(),
      new PauseBlock(Duration.ofSeconds(1))
    )

    val navigableBlocks = tape.navigableBlocks

    navigableBlocks should have size 4

    navigableBlocks(0).index shouldBe 1
    navigableBlocks(0).block shouldBe a[VariableSpeedBlock]

    navigableBlocks(1).index shouldBe 2
    navigableBlocks(1).block shouldBe a[PauseBlock]

    navigableBlocks(2).index shouldBe 3
    navigableBlocks(2).block shouldBe a[LoopStartBlock]

    navigableBlocks(3).index shouldBe 9
    navigableBlocks(3).block shouldBe a[PauseBlock]
  }

  type JPair = Pair[String, String]
  implicit def tuple2ToPair(tuple: (String, String)): JPair = new JPair(tuple._1, tuple._2)
  private def givenTapeOf(blocks: TapeBlock*): Tape = new Tape("1.0", blocks.toArray)
}
