package com.socialthingy.plusf.wos

import java.io.{File, InputStream}
import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING

import com.socialthingy.plusf.wos.ZipUtils.{findFiles, isZipFile, unzipFile}
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source
import scala.collection.JavaConverters._
import scala.language.implicitConversions

class ZipUtilsSpec extends FlatSpec with Matchers {
  "ZipUtils" should "recognise a valid zip file" in {
    isZipFile(getClass.getResourceAsStream("/valid.zip")) shouldBe true
  }

  it should "recognise an invalid zip file" in {
    isZipFile(getClass.getResourceAsStream("/screenfiller.z80")) shouldBe false
  }

  it should "identify TAP and TZX files in a zip file" in {
    findFiles(getClass.getResourceAsStream("/valid.zip")).asScala.map(_.getName) should contain only ("file.tap", "file.tzx")
  }

  it should "unzip a file in a zip file" in {
    val file: File = getClass.getResourceAsStream("/valid.zip")
    val inZip = findFiles(file).get(0)
    val unzipped = unzipFile(file, inZip)
    Source.fromFile(unzipped.get).getLines().toList should contain only "hello-tap"
  }

  implicit def fileFromStream(stream: InputStream): File = {
    val tempFile = File.createTempFile("plusf", ".zip")
    tempFile.deleteOnExit()
    Files.copy(stream, tempFile.toPath, REPLACE_EXISTING)
    tempFile
  }
}
