package com.socialthingy.plusf.wos

import java.io.{BufferedReader, InputStreamReader}
import java.net.URL
import java.util.stream.Collectors

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

case class Title(name: String, location: URL) {
  override def toString = name
}
case class Archive(name: String, location: URL) {
  override def toString = name
}
object WosScraper {
  def apply() = new WosScraper("www.worldofspectrum.org")
  def apply(host: String) = new WosScraper(host)
}

class WosScraper(host: String) {
  private val formats = List("TAP", "TZX")

  def findTitles(searchText: String): Seq[Title] = {
    def collectTitles(acc: Set[Title], maybeTitles: Try[List[Title]]) = maybeTitles match {
      case Success(newTitles) => acc ++ newTitles
      case Failure(_) => acc
    }

    val titlesPerFormat = formats.map(getTitlesForFormat(searchText))
    titlesPerFormat.foldLeft(Set[Title]())(collectTitles).toList.sortBy(_.name)
  }

  def findArchives(title: Title): Seq[Archive] = {
    val content = scrape(
      title.location,
      l => l.contains("""<A HREF="/pub/sinclair/"""),
      l => l.startsWith("""<FONT SIZE="+1">""")
    )
    content.map(extractArchiveFromRow).filter(_.isDefined).map(_.get)
  }

  private def getTitlesForFormat(searchText: String)(format: String) = Try {
    val url = urlForHost(s"/infoseekadv.cgi?what=1&regexp=$searchText&format=$format")
    val content = scrape(
      url,
      l => l.contains("""<TR><TD><FONT FACE="Arial,Helvetica"><A HREF="/infoseek.cgi"""),
      l => l.startsWith("""</TABLE>""")
    )
    content.map(extractTitleFromRow).filter(_.isDefined).map(_.get)
  }

  private def scrape(url: URL,
                     firstLineMatch: String => Boolean,
                     lastLineMatch: String => Boolean) = {
    val connection = url.openConnection()
    val contentStream = new BufferedReader(new InputStreamReader(connection.getInputStream))
    try {
      val allLines = contentStream.lines().collect(Collectors.toList[String]).asScala.toList
      allLines.dropUntil(firstLineMatch).takeUntil(lastLineMatch)
    } finally {
      contentStream.close()
    }
  }

  implicit class ListOps[T](list: List[T]) {
    def dropUntil(p: T => Boolean) = list.dropWhile { x => !p(x) }
    def takeUntil(p: T => Boolean) = list.takeWhile { x => !p(x) }
  }

  private val titleRegex = """.*<A HREF="(.+)">(.+)<\/A>.*""".r
  private def extractTitleFromRow(row: String): Option[Title] = row match {
    case titleRegex(url, name) => Some(Title(name, urlForHost(url)))
    case _ => None
  }

  private val archiveRegex = """.*<A HREF="(.+\.(?:tap|tzx)\.zip)".*>(.+\.(?:tap|tzx)\.zip)<\/A>.*""".r
  private def extractArchiveFromRow(row: String): Option[Archive] = row match {
    case archiveRegex(url, name) => Some(Archive(name, urlForHost(url)))
    case _ => None
  }

  private def urlForHost(path: String) = new URL(s"http://$host$path")
}