package com.socialthingy.plusf.tape

import java.time.Duration

data class NavigableBlock(val index: Int, val block: TapeBlock)

class OverrideBlock(private val underlying: TapeBlock): TapeBlock() {
    override fun getBlockSignal(signalState: SignalState?): BlockSignal {
        return underlying.getBlockSignal(signalState)
    }

    override fun getDisplayName(): String {
        return underlying.displayName
    }

    override fun isDataBlock(): Boolean {
        return underlying.isDataBlock
    }

    override fun shouldStopTape(): Boolean {
        return underlying.shouldStopTape()
    }
}

enum class OverrideType {
    STOP_TAPE
}

class Tape(val version: String, val signature: String, val blocks: MutableList<TapeBlock>) {
    val archiveInfo = blocks.filter { it is ArchiveInfoBlock }
        .flatMap { (it as ArchiveInfoBlock).descriptions.toList() }
        .toList()

    fun addOverride(overrideType: OverrideType, position: Int) {
        when (overrideType) {
            OverrideType.STOP_TAPE -> blocks.add(position, OverrideBlock(PauseBlock(Duration.ZERO)))
        }
    }

    fun getNavigableBlocks(): List<NavigableBlock> {
        fun loop(idx: Int, navigableBlocks: List<NavigableBlock>, nestingLevel: Int): List<NavigableBlock> {
            if (idx == blocks.size) {
                return navigableBlocks
            } else {
                val nextBlock = blocks[idx]
                when (nextBlock) {
                    is GroupStartBlock ->
                        return if (nestingLevel == 0) {
                            loop(idx + 1, navigableBlocks.plus(NavigableBlock(idx, nextBlock)), nestingLevel + 1)
                        } else {
                            loop(idx + 1, navigableBlocks, nestingLevel + 1)
                        }

                    is GroupEndBlock ->
                        return loop(idx + 1, navigableBlocks, nestingLevel - 1)

                    is LoopStartBlock ->
                        return if (nestingLevel == 0) {
                            loop(idx + 1, navigableBlocks.plus(NavigableBlock(idx, nextBlock)), nestingLevel + 1)
                        } else {
                            loop(idx + 1, navigableBlocks, nestingLevel + 1)
                        }

                    is LoopEndBlock ->
                         return loop(idx + 1, navigableBlocks, nestingLevel - 1)

                    else ->
                        return if (nextBlock.isDataBlock && nestingLevel == 0) {
                            loop(idx + 1, navigableBlocks.plus(NavigableBlock(idx, nextBlock)), 0)
                        } else {
                            loop(idx + 1, navigableBlocks, nestingLevel)
                        }
                }
            }
        }

        return loop(0, mutableListOf(), 0)
    }
}