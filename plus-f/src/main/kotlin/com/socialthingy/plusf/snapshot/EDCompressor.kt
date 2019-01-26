package com.socialthingy.plusf.snapshot

import java.io.InputStream

object EDCompressor {
    fun compress(input: IntArray): IntArray {
        if (input.isEmpty()) {
            return IntArray(0)
        }

        val output = mutableListOf<Int>()

        var lastValue = input[0]
        var lastValueRepetitions = 1
        var afterSingleEd = false
        for (value in input.drop(1).plus(-1)) {
            if (value == lastValue) {
                lastValueRepetitions++
                if (lastValueRepetitions === 255) {
                    output.add(0xed)
                    output.add(0xed)
                    output.add(0xff)
                    output.add(lastValue)
                    lastValueRepetitions = 0
                }
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

        return output.toIntArray()
    }

    fun decompress(inputStream: InputStream, compressedLength: Int): IntArray {
        val out = mutableListOf<Int>()

        var count = 0
        while (count < compressedLength) {
            val nextByte = inputStream.read()
            count++
            if (nextByte == 0xed) {
                val nextByte2 = inputStream.read()
                count++
                if (nextByte2 == 0xed) {
                    val repetitions = inputStream.read()
                    count++
                    val value = inputStream.read()
                    count++

                    for (i in 0 until repetitions) {
                        out.add(value)
                    }
                } else {
                    out.add(nextByte)
                    out.add(nextByte2)
                }
            } else {
                out.add(nextByte)
            }
        }

        return out.toIntArray()
    }
}