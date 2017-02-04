package com.socialthingy.plusf.wos

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.{Collections, Optional}
import java.util.zip.{ZipEntry, ZipFile}

import scala.collection.mutable.ListBuffer
import scala.util.Try
import scala.util.control.NonFatal
import scala.collection.JavaConverters._

object ZipUtils {
  def isZipFile(file: File): Boolean = {
    val isZip = Try {
      val zf = new ZipFile(file)
      zf.close()
    }

    isZip.isSuccess
  }

  def findFiles(file: File): java.util.List[ZipEntry] = withZipFile(file)(Collections.emptyList[ZipEntry]()) { zf =>
    val entriesInFile = zf.entries()
    val matchingEntries = ListBuffer[ZipEntry]()
    while (entriesInFile.hasMoreElements) {
      val next = entriesInFile.nextElement
      val name = next.getName.toLowerCase()
      if (name.endsWith(".tap") || name.endsWith(".tzx")) {
        matchingEntries.append(next)
      }
    }
    matchingEntries.asJava
  }

  def unzipFile(zipFile: File, entry: ZipEntry): Optional[File] = withZipFile(zipFile)(Optional.empty[File]) { zf =>
    val unzipped = File.createTempFile("plusf", entry.getName)
    unzipped.deleteOnExit()
    val stream = zf.getInputStream(entry)
    try {
      Files.copy(stream, unzipped.toPath, REPLACE_EXISTING)
      Optional.of(unzipped)
    } finally {
      stream.close()
    }
  }

  private def withZipFile[T](file: File)(onError: => T)(fn: ZipFile => T): T = try {
    val zf = new ZipFile(file)
    try {
      fn(zf)
    } finally {
      zf.close()
    }
  } catch {
    case NonFatal(e) => onError
  }
}
