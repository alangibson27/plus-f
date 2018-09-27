package com.socialthingy.plusf.wos

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.Optional
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

object ZipUtils {
    fun isZipFile(file: File): Boolean {
        return try {
            val zf = ZipFile(file)
            zf.close()
            true
        } catch (ex: Exception) {
            false
        }
    }

    fun findFiles(file: File): List<ZipEntry> {
        return withZipFile(file, listOf(), {
            val entriesInFile = it.entries()
            val matchingEntries = mutableListOf<ZipEntry>()
            while (entriesInFile.hasMoreElements()) {
                val next = entriesInFile.nextElement()
                val name = next.name.toLowerCase()
                if (name.endsWith(".tap") || name.endsWith(".tzx")) {
                    matchingEntries.add(next)
                }
            }
            matchingEntries
        })
    }

    fun unzipFile(zipFile: File, entry: ZipEntry): Optional<File> {
        return withZipFile(zipFile, Optional.empty(), {
            val unzipped = File.createTempFile("plusf", entry.name)
            unzipped.deleteOnExit()
            val stream = it.getInputStream(entry)
            stream.use {
                Files.copy(it, unzipped.toPath(), REPLACE_EXISTING)
                Optional.of(unzipped)
            }
        })
    }

    private fun <T> withZipFile(file: File, onError: T, action: (ZipFile) -> T): T {
        val zf = ZipFile(file)
        return try {
            action(zf)
        } catch (ex: Exception) {
            onError
        } finally {
            zf.close()
        }
    }
}
