package com.socialthingy.plusf.tape

import java.time.Duration
import java.util.{ List => JList }

import org.scalatest.{FlatSpec, Matchers}

import scala.collection.JavaConverters._
import scala.language.implicitConversions

import kotlin.{ Pair => KPair }

class TapeSpec extends FlatSpec with Matchers {
  "Tape" should "provide all information in all archive info blocks as a list of key-value pairs" in {
    val tape = givenTapeOf(
      new ArchiveInfoBlock(List[KPair[String, String]]("Field 1" -> "Value 1", "Field 2" -> "Value 2").asJava),
      new PauseBlock(Duration.ofSeconds(1)),
      new ArchiveInfoBlock(List[KPair[String, String]]("Field 3" -> "Value 3", "Field 2" -> "Value 2 Again").asJava)
    )

    val expected: JList[KPair[String, String]] = List[KPair[String, String]](
      "Field 1" -> "Value 1",
      "Field 2" -> "Value 2",
      "Field 3" -> "Value 3",
      "Field 2" -> "Value 2 Again"
    ).asJava

    tape.getArchiveInfo shouldBe expected
  }

  it should "return only data blocks as navigable blocks in a tape with no loops or groups" in {
    val tape = givenTapeOf(
      new ArchiveInfoBlock(List[KPair[String, String]]("Field 1" -> "Value 1").asJava),
      new MessageBlock("This is a message", Duration.ofSeconds(1)),
      new PauseBlock(Duration.ofSeconds(1)),
      new PulseSequenceBlock(Array[Int](100, 200, 100, 200)),
      new PureDataBlock(Duration.ofSeconds(1), Array[Int](0x01, 0x02, 0x03), 100, 200, 8),
      PureToneBlock.create(100, 1000),
      new TextDescriptionBlock("This is a tape"),
      new VariableSpeedBlock(Duration.ofSeconds(1), Array[Int](0x01, 0x02, 0x03))
    )

    val navigableBlocks = tape.getNavigableBlocks.asScala

    navigableBlocks should have size 5
    navigableBlocks(0).getIndex shouldBe 2
    navigableBlocks(0).getBlock shouldBe a[PauseBlock]

    navigableBlocks(1).getIndex shouldBe 3
    navigableBlocks(1).getBlock shouldBe a[PulseSequenceBlock]

    navigableBlocks(2).getIndex shouldBe 4
    navigableBlocks(2).getBlock shouldBe a[PureDataBlock]

    navigableBlocks(3).getIndex shouldBe 5
    navigableBlocks(3).getBlock shouldBe a[PulseSequenceBlock]

    navigableBlocks(4).getIndex shouldBe 7
    navigableBlocks(4).getBlock shouldBe a[VariableSpeedBlock]
  }

  it should "return top-level group start blocks as navigable blocks, but not the blocks within them" in {
    val tape = givenTapeOf(
      new ArchiveInfoBlock(List[KPair[String, String]]("Field 1" -> "Value 1").asJava),
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

    val navigableBlocks = tape.getNavigableBlocks.asScala

    navigableBlocks should have size 4

    navigableBlocks(0).getIndex shouldBe 1
    navigableBlocks(0).getBlock shouldBe a[VariableSpeedBlock]

    navigableBlocks(1).getIndex shouldBe 2
    navigableBlocks(1).getBlock shouldBe a[PauseBlock]

    navigableBlocks(2).getIndex shouldBe 3
    navigableBlocks(2).getBlock shouldBe a[GroupStartBlock]

    navigableBlocks(3).getIndex shouldBe 9
    navigableBlocks(3).getBlock shouldBe a[PauseBlock]
  }

  it should "return top-level loop start blocks as navigable blocks, but not the blocks within them" in {
    val tape = givenTapeOf(
      new ArchiveInfoBlock(List[KPair[String, String]]("Field 1" -> "Value 1").asJava),
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

    val navigableBlocks = tape.getNavigableBlocks.asScala

    navigableBlocks should have size 4

    navigableBlocks(0).getIndex shouldBe 1
    navigableBlocks(0).getBlock shouldBe a[VariableSpeedBlock]

    navigableBlocks(1).getIndex shouldBe 2
    navigableBlocks(1).getBlock shouldBe a[PauseBlock]

    navigableBlocks(2).getIndex shouldBe 3
    navigableBlocks(2).getBlock shouldBe a[LoopStartBlock]

    navigableBlocks(3).getIndex shouldBe 9
    navigableBlocks(3).getBlock shouldBe a[PauseBlock]
  }

  implicit def tuple2ToPair(tuple: (String, String)): KPair[String, String] = new KPair(tuple._1, tuple._2)
  private def givenTapeOf(blocks: TapeBlock*): Tape = new Tape("1.0", blocks.toList.asJava)
}
