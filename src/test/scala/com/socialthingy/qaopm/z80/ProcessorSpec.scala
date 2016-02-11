package com.socialthingy.qaopm.z80

import org.mockito.Mockito.{when => mockitoWhen}
import org.mockito.Matchers._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{Matchers, GivenWhenThen, FlatSpec}

import scala.util.Random

trait ProcessorSpec extends FlatSpec with GivenWhenThen with Matchers with MockitoSugar {

  trait Machine {
    var instructionPointer = 0x0
    val memory = new Array[Int](0x10000)
    val io = {
      val stub = mock[IO]
      mockitoWhen(stub.read(anyInt(), anyInt())).thenReturn(Random.nextInt(256))
      stub
    }

    val processor = new Processor(memory, io)

    def nextInstructionIs(opCode: Int*): Unit = {
      opCode foreach(pokeAtIp(_))
    }

    def registerContainsValue(register: String, value: Int): Unit = {
      processor.register(register.toLowerCase()).set(value)
    }

    def stackPointerIs(value: Int): Unit = {
      processor.register("sp").set(value)
    }

    def programCounterIs(value: Int): Unit = {
      processor.register("pc").set(value)
    }

    def pokeAtIp(value: Int): Unit = {
      memory(instructionPointer) = value
      instructionPointer = instructionPointer +% 1
    }

    def flag(name: String): FlagBuilder = new FlagBuilder(name, processor)

    def pcAddress: Int = processor.register("pc").get()

    def spAddress: Int = processor.register("sp").get()

    def registerValue(reg: String): Int = processor.register(reg).get()

    def signedRegisterValue(reg: String): Int = processor.register(reg).asInstanceOf[ByteRegister].signedGet()
  }

  def randomByte: Int = Random.nextInt(0x100)

  def splitWord(word: Int) = ((word & 0xff00) >> 8, word & 0x00ff)

  implicit class WrappedWord(n: Int) {
    def +%(x: Int): Int = (n + x) & 0xffff
  }
}

class FlagBuilder(name: String, processor: Processor) {
  def isSet(): Unit = {
    processor.setFlag(name, true)
  }

  def isReset(): Unit = {
    processor.setFlag(name, false)
  }

  def value(): Boolean = processor.getFlag(name)
}
