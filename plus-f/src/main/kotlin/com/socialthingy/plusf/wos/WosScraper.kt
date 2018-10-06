package com.socialthingy.plusf.wos

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import java.util.stream.Collectors

data class Title(val name: String, val location: URL) {
    override fun toString() = name
}

data class Archive(val name: String, val location: URL) {
    override fun toString() = name
}

object WosScrapers {
    fun new() = CachedWosScraper(RawWosScraper("www.worldofspectrum.org"))
    fun new(host: String) = CachedWosScraper(RawWosScraper(host))
}

interface WosScraper {
    fun findTitles(searchText: String): Try<List<Title>>
    fun findArchives(title: Title): Try<List<Archive>>
}

fun <T> List<T>.dropUntil(p: (T) -> Boolean): List<T> {
    return this.dropWhile { !p(it) }
}

fun <T> List<T>.takeUntil(p: (T) -> Boolean): List<T> {
    return this.takeWhile { !p(it) }
}

class CachedWosScraper(private val underlying: WosScraper) : WosScraper {
    private val titleCache = mutableMapOf<String, List<Title>>()
    private val archiveCache = mutableMapOf<Title, List<Archive>>()

    override fun findTitles(searchText: String): Try<List<Title>> {
        return findFromCache(searchText, underlying::findTitles, titleCache)
    }

    override fun findArchives(title: Title): Try<List<Archive>> {
        return findFromCache(title, underlying::findArchives, archiveCache)
    }

    private fun <K, V> findFromCache(key: K, underlying: (K) -> Try<List<V>>, cache: MutableMap<K, List<V>>): Try<List<V>> {
        val cached: List<V>? = cache[key]
        return if (cached == null) {
            val results = underlying(key)
            results.ifDefined {
                cache[key] = it
                if (cache.size == 51) {
                    cache.remove(cache.keys.first())
                }
            }

            results
        } else {
            Success(cached)
        }
    }
}

class RawWosScraper(private val host: String) : WosScraper {
    private val formats = listOf("TAP", "TZX")

    override fun findTitles(searchText: String): Try<List<Title>> {
        val titles = mutableListOf<Title>()
        formats.map { getTitlesForFormat(it, searchText) }
            .forEach {
                when (it) {
                    is Failure -> { return it }
                    is Success -> {
                        it.t.filterNot { titles.contains(it) }.forEach { titles.add(it) }
                    }
                }
            }

        return Success(titles.sortedBy { it.name })
    }

    override fun findArchives(title: Title): Try<List<Archive>> {
        val content = scrape(
                title.location,
                { it.contains("""<A HREF="/pub/sinclair/""") },
                { it.startsWith("""<FONT SIZE="+1">""") }
        )

        return content.map {
            it.map(this::extractArchiveFromRow).filter { it.isPresent }.map { it.get() }
        }
    }

    private fun getTitlesForFormat(searchText: String, format: String): Try<List<Title>> {
        val url = urlForHost(String.format("/infoseekadv.cgi?what=1&format=%s&regexp=%s", searchText, format))
        val content = scrape(
                url,
                { it.contains("""<TR><TD><FONT FACE="Arial,Helvetica"><A HREF="/infoseek.cgi""") },
                { it.startsWith("""</TABLE>""") }
        )

        return content.map {
            it.map(this::extractTitleFromRow).filter { it.isPresent }.map { it.get() }
        }
    }

    private fun scrape(url: URL,
                       firstLineMatch: (String) -> Boolean,
                       lastLineMatch: (String) -> Boolean): Try<List<String>> {
        return Try {
            val connection = url.openConnection()
            val contentStream = BufferedReader(InputStreamReader(connection.getInputStream()))
            contentStream.use { cs ->
                val allLines = cs.lines().collect(Collectors.toList<String>())
                allLines.dropUntil(firstLineMatch).takeUntil(lastLineMatch)
            }
        }
    }

    private val titleRegex = """.*<A HREF="(.+)">(.+)</A>.*""".toRegex()

    private fun extractTitleFromRow(row: String): Optional<Title> {
        val result = titleRegex.matchEntire(row)
        return if (result == null) {
            Optional.empty()
        } else {
            Optional.of(Title(result.groupValues[2], urlForHost(result.groupValues[1])))
        }
    }

    private val archiveRegex = """.*<A HREF="(.+\.(?:tap|tzx)\.zip)".*>(.+\.(?:tap|tzx)\.zip)</A>.*""".toRegex()
    private fun extractArchiveFromRow(row: String): Optional<Archive> {
        val result = archiveRegex.matchEntire(row)
        return if (result == null) {
            Optional.empty()
        } else {
            Optional.of(Archive(result.groupValues[2], urlForHost(result.groupValues[1])))
        }
    }

    private fun urlForHost(path: String) = URL(String.format("http://%s%s", host, path))
}