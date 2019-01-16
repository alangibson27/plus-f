package com.socialthingy.plusf.snapshot

object EDCompressor {
    fun compress(input: Array<Int>): Array<Int> = compress(input, false)

    fun compress(input: Array<Int>, addEndMarker: Boolean): Array<Int> {
        if (input.isEmpty()) {
            return arrayOf()
        }

        val output = mutableListOf<Int>()

        var lastValue = input[0]
        var lastValueRepetitions = 1
        var afterSingleEd = false
        for (value in input.drop(1).plus(-1)) {
            if (value == lastValue) {
                lastValueRepetitions++
            } else {
                if (lastValueRepetitions == 1) {
                    afterSingleEd = lastValue == 0xed
                    output.add(lastValue)
                } else if (lastValue != 0xed && lastValueRepetitions < 5) {
                    repeat(lastValueRepetitions) { output.add(lastValue) }
                } else {
                    if (afterSingleEd) {
                        output.add(lastValue)
                        lastValueRepetitions--
                    }
                    output.add(0xed)
                    output.add(0xed)
                    output.add(lastValueRepetitions)
                    output.add(lastValue)
                }
                lastValueRepetitions = 1
                lastValue = value
            }
        }

        if (addEndMarker) {
            output.add(0)
            output.add(0xed)
            output.add(0xed)
            output.add(0)
        }

        return output.toTypedArray()
    }

    fun decompress(input: Array<Int>): Array<Int> {
        if (input.isEmpty()) {
            return arrayOf()
        }

        val endMarkerLength = if (input.reversedArray().take(4) == listOf(0, 0xed, 0xed, 0)) 4 else 0

        val output = mutableListOf<Int>()
        var lastValue = -1
        var inCompressionBlock = false
        var repetitions = 0
        for (value in input.dropLast(endMarkerLength)) {
            if (!inCompressionBlock) {
                if (value == 0xed) {
                    inCompressionBlock = lastValue == 0xed
                } else {
                    if (lastValue == 0xed) {
                        output.add(0xed)
                    }
                    output.add(value)
                }
                lastValue = value
            } else {
                if (repetitions == 0) {
                    repetitions = value
                } else {
                    repeat(repetitions) { output.add(value) }
                    lastValue = -1
                }
            }
        }

        return output.toTypedArray()
    }
}