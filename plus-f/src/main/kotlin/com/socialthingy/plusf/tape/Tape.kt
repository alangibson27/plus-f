package com.socialthingy.plusf.tape

data class NavigableBlock(val index: Int, val block: TapeBlock)

class Tape(val version: String, val blocks: List<TapeBlock>) {
    val archiveInfo = blocks.filter { it is ArchiveInfoBlock }
        .flatMap { (it as ArchiveInfoBlock).descriptions.toList() }
        .toList()

    val navigableBlocks: List<NavigableBlock> = navigableBlocks()

    private fun navigableBlocks(): List<NavigableBlock> {
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