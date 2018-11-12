package com.socialthingy.plusf

import com.socialthingy.plusf.z80.FlagsRegister.Flag
import com.socialthingy.plusf.z80.{Clock, _}
import org.mockito.Matchers._
import org.mockito.Mockito.{verify, when => mockitoWhen}
import org.mockito.{Matchers => MockitoMatchers}
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{FlatSpec, GivenWhenThen, Matchers}

import scala.util.Random

trait ProcessorSpec extends FlatSpec with GivenWhenThen with Matchers with MockitoSugar {

  trait Machine {
    var instructionPointer = 0x0

    val io = {
      val stub = mock[IO]
      mockitoWhen(stub.read(anyInt(), anyInt())).thenReturn(Random.nextInt(256))
      stub
    }

    val clock = new Clock
    val memory: Memory = new UncontendedMemory(clock)
    val processor = new Processor(memory, io, clock)

    def interruptsAreEnabled(): Unit = {
      processor.setIff(0, true)
      processor.setIff(1, true)
    }

    def interruptsAreDisabled(): Unit = {
      processor.setIff(0, false)
      processor.setIff(1, false)
    }

    def readFromPort(port: Int, accumulator: Int): PortBuilder = new PortBuilder(port, accumulator)

    def verifyPortWrite(port: Int, accumulator: Int, value: Int): Unit = {
      verify(io).write(port, accumulator, value)
    }

    def nextInstructionIs(opCode: Int*): Unit = {
      opCode foreach pokeAtIp
    }

    def registerContainsValue(register: String, value: Int): Unit = {
      processor.register(register.toLowerCase()).set(value)
      if (register.equals("pc")) instructionPointer = value
    }

    def stackPointerIs(value: Int): Unit = {
      processor.register("sp").set(value)
    }

    def programCounterIs(value: Int): Unit = {
      processor.register("pc").set(value)
    }

    def pokeAtIp(value: Int): Unit = {
      memory.set(instructionPointer, value)
      instructionPointer = instructionPointer +% 1
    }

    def flag(name: String): FlagBuilder = new FlagBuilder(name, processor)

    def pcAddress: Int = processor.register("pc").get()

    def spAddress: Int = processor.register("sp").get()

    def registerValue(reg: String): Int = processor.register(reg).get()

    def signedRegisterValue(reg: String): Int = processor.register(reg).asInstanceOf[ByteRegister].signedGet()

    class PortBuilder(port: Int, accumulator: Int) {
      def returns(value: Int): Unit = {
        mockitoWhen(io.read(MockitoMatchers.eq(port), anyInt())).thenReturn(value)
      }
    }
  }

  def randomByte: Int = Random.nextInt(0x100)

  def splitWord(word: Int) = ((word & 0xff00) >> 8, word & 0x00ff)

  def binary(value: String): Int = Integer.parseInt(value, 2)

  implicit class WrappedWord(n: Int) {
    def +%(x: Int): Int = (n + x) & 0xffff
  }
}

class FlagBuilder(name: String, processor: Processor) {
  val flagsRegister = processor.flagsRegister()

  def is(value: Boolean): Unit = set(name, value)

  private def set(name: String, value: Boolean) = name match {
    case "c" => flagsRegister.set(Flag.C, value)
    case "n" => flagsRegister.set(Flag.N, value)
    case "p" => flagsRegister.set(Flag.P, value)
    case "h" => flagsRegister.set(Flag.H, value)
    case "z" => flagsRegister.set(Flag.Z, value)
    case "s" => flagsRegister.set(Flag.S, value)
    case "f3" => flagsRegister.set(Flag.F3, value)
    case "f5" => flagsRegister.set(Flag.F5, value)
  }

  def value(): Boolean = name match {
    case "c" => flagsRegister.get(Flag.C)
    case "n" => flagsRegister.get(Flag.N)
    case "p" => flagsRegister.get(Flag.P)
    case "h" => flagsRegister.get(Flag.H)
    case "z" => flagsRegister.get(Flag.Z)
    case "s" => flagsRegister.get(Flag.S)
    case "f3" => flagsRegister.get(Flag.F3)
    case "f5" => flagsRegister.get(Flag.F5)
  }
}
