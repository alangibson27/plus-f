package com.socialthingy.plusf.snapshot

import java.io.ByteArrayInputStream

import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class EDCompressorSpec extends FlatSpec with Matchers with TableDrivenPropertyChecks {

  import EDCompressor.INSTANCE.{compress, decompress}

  "EDCompressor" should "compress a sequence of five repeated values to a compression sequence" in {
    val sequencesToCompress = Table(
      ("input sequence", "output sequence"),
      (bytes(1, 1, 1, 1, 1), bytes(0xed, 0xed, 5, 1)),
      (bytes(1, 2, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4), bytes(1, 2, 0xed, 0xed, 6, 3, 4, 4, 4, 4)),
      (bytes(), bytes()),
      (bytes(1), bytes(1)),
      (bytes(0xed, 0xed), bytes(0xed, 0xed, 2, 0xed)),
      (bytes(0xed, 0, 0, 0, 0, 0, 0), bytes(0xed, 0, 0xed, 0xed, 5, 0)),
      (bytes(0xed, 0xed, 0xed, 0xed, 0xed, 0xed), bytes(0xed, 0xed, 6, 0xed)),
      (bytes(1, 2, 3, 4, 5), bytes(1, 2, 3, 4, 5))
    )

    forAll(sequencesToCompress) { (input, output) =>
      compress(input) shouldBe output
    }
  }

  it should "decompress a compression sequence correctly" in {
    val sequencesToDecompress = Table(
      ("input sequence", "output sequence"),
      (stream(), bytes()),
      (stream(0xed, 0xed, 5, 1), bytes(1, 1, 1, 1, 1)),
      (stream(0xed, 1, 2, 3, 4), bytes(0xed, 1, 2, 3, 4)),
      (stream(0xed, 0xed, 2, 0xed), bytes(0xed, 0xed))
    )

    forAll(sequencesToDecompress) { (input, output) =>
      decompress(input, input.available()) shouldBe output
    }
  }

  def bytes(bytes: Int*): Array[Integer] = bytes.map(Integer.valueOf).toArray

  def stream(bytes: Int*): ByteArrayInputStream = new ByteArrayInputStream(bytes.map(x => x.asInstanceOf[Byte]).toArray)
}
