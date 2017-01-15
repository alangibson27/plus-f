package com.socialthingy.plusf.tape

import java.util.{List => JList}

import scala.collection.JavaConverters._

case class Pair[F, S](first: F, second: S)

object Tape {
  def apply(version: String, blocks: JList[TapeBlock]): Tape =
    new Tape(version, blocks.toArray(Array.ofDim[TapeBlock](blocks.size())))
}

class Tape(val version: String, val blocks: Array[TapeBlock]) {
  lazy val archiveInfo: JList[Pair[String, String]] = {
    blocks.filter(_.isInstanceOf[ArchiveInfoBlock])
      .flatMap(_.asInstanceOf[ArchiveInfoBlock].getDescriptions.asScala.toList)
      .toList.asJava
  }

  lazy val navigableBlocks: JList[NavigableBlock] = {
    def loop(idx: Int, navigableBlocks: List[NavigableBlock], nestingLevel: Int): List[NavigableBlock] = {
      if (idx == blocks.length) {
        navigableBlocks
      } else {
        blocks(idx) match {
          case x: GroupStartBlock if nestingLevel == 0 =>
            loop(idx + 1, NavigableBlock(idx, x) :: navigableBlocks, nestingLevel + 1)

          case x: GroupStartBlock if nestingLevel > 0 =>
            loop(idx + 1, navigableBlocks, nestingLevel + 1)

          case x: GroupEndBlock =>
            loop(idx + 1, navigableBlocks, nestingLevel - 1)

          case x: LoopStartBlock if nestingLevel == 0 =>
            loop(idx + 1, NavigableBlock(idx, x) :: navigableBlocks, nestingLevel + 1)

          case x: LoopStartBlock if nestingLevel > 0 =>
            loop(idx + 1, navigableBlocks, nestingLevel + 1)

          case x: LoopEndBlock =>
            loop(idx + 1, navigableBlocks, nestingLevel - 1)

          case x if isDataBlock(x) && nestingLevel == 0 =>
            loop(idx + 1, NavigableBlock(idx, x) :: navigableBlocks, 0)

          case _ =>
            loop(idx + 1, navigableBlocks, nestingLevel)
        }
      }
    }

    loop(0, List(), 0).reverse.asJava
  }

  private def isDataBlock(block: TapeBlock) =
    block.isInstanceOf[PauseBlock] || block.isInstanceOf[PulseSequenceBlock] || block.isInstanceOf[PureDataBlock] ||
    block.isInstanceOf[PureToneBlock] || block.isInstanceOf[VariableSpeedBlock]
}

case class NavigableBlock(index: Int, block: TapeBlock)