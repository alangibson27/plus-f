package com.socialthingy.plusf.spectrum.remote

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import org.scalatest.{FlatSpec, Matchers}
import org.apache.commons.lang3.tuple.{Pair => JPair}

class GuestStateSpec extends FlatSpec with Matchers {

  "GuestState" should "serialise and deserialise correctly" in {
    val input = new GuestState(1, 2, 3)

    val bytesOut = new ByteArrayOutputStream
    GuestState.serialise(JPair.of(input, bytesOut))
    val serialisedForm = bytesOut.toByteArray

    val bytesIn = new ByteArrayInputStream(serialisedForm)
    val output = GuestState.deserialise(bytesIn)

    output.getPort shouldBe input.getPort
    output.getAccumulator shouldBe input.getAccumulator
    output.getValue shouldBe input.getValue
  }

}
