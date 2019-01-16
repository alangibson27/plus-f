package com.socialthingy.plusf.snapshot

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

  it should "add an end marker if requested" in {
    compress(bytes(1, 2, 3, 4, 5), true) shouldBe bytes(1, 2, 3, 4, 5, 0, 0xed, 0xed, 0)
  }

  it should "decompress a compression sequence correctly" in {
    val sequencesToDecompress = Table(
      ("input sequence", "output sequence"),
      (bytes(), bytes()),
      (bytes(0xed, 0xed, 5, 1), bytes(1, 1, 1, 1, 1)),
      (bytes(0xed, 1, 2, 3, 4), bytes(0xed, 1, 2, 3, 4)),
      (bytes(0xed, 0xed, 2, 0xed), bytes(0xed, 0xed))
    )

    forAll(sequencesToDecompress) { (input, output) =>
      decompress(input) shouldBe output
    }
  }

  it should "discard an end marker if one is supplied" in {
    decompress(bytes(1, 2, 3, 0xed, 0xed, 5, 5, 0, 0xed, 0xed, 0)) shouldBe bytes(1, 2, 3, 5, 5, 5, 5, 5)
  }

  def bytes(bytes: Int*): Array[Integer] = bytes.map(Integer.valueOf).toArray
}
