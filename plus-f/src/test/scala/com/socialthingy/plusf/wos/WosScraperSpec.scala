package com.socialthingy.plusf.wos

import java.net.URL

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, GivenWhenThen, Matchers}

class WosScraperSpec extends FlatSpec with Matchers with BeforeAndAfterAll with GivenWhenThen {
  import TestData._

  val wiremock = new WireMockServer()
  lazy val host = s"localhost:${wiremock.port()}"

  override def beforeAll(): Unit = {
    wiremock.start()
  }

  override def afterAll(): Unit = {
    wiremock.stop()
  }

  "WosSearcher.findTitles" should "return all titles from a WOS search" in {
    Given("WOS has a number of TZX titles containing the word 'Chuckie'")
    WOS hasTitlesMatching "Chuckie" ofFormat "TZX" returning tzxChuckie

    And("it also has a number of TAP titles containing the word 'Chuckie'")
    WOS hasTitlesMatching "Chuckie" ofFormat "TAP" returning tapChuckie

    When("I search for titles containing the word 'Chuckie'")
    val searcher = new RawWosScraper(host)
    val titles = searcher.findTitles("Chuckie")

    Then("I should get a de-duped list of titles, ordered alphabetically")
    titles should have size 4
    titles should contain inOrderOnly(
      new Title("Chuckie Designer", new URL("http://" + host + "/infoseek.cgi?regexp=^Chuckie+Designer$&pub=^P%26M+Software$&loadpics=1")),
      new Title("Chuckie Egg", new URL("http://" + host + "/infoseek.cgi?regexp=^Chuckie+Egg$&pub=^A%27n%27F+Software$&loadpics=1")),
      new Title("Chuckie Egg 2", new URL("http://" + host + "/infoseek.cgi?regexp=^Chuckie+Egg+2$&pub=^A%27n%27F+Software$&loadpics=1")),
      new Title("Chuckie Egg Editor", new URL("http://" + host + "/infoseek.cgi?regexp=^Chuckie+Egg+Editor$&pub=^Mercury+Software$&loadpics=1"))
    )
  }

  "WosSearcher.findArchives" should "return all archives for a title" in {
    Given("WOS has archives for the title Chuckie Egg")
    val chuckieEgg = new Title("Chuckie Egg", new URL("http://" + host + "/infoseek.cgi"))
    WOS hasArchivesFor chuckieEgg returning chuckieEggArchives

    When("I ask for the archives for Chuckie Egg")
    val searcher = new RawWosScraper(host)
    val archives = searcher.findArchives(chuckieEgg)

    Then("I should get the archives")
    archives should have size 3
    archives should contain only (
      new Archive("ChuckieEgg.tzx.zip", new URL("http://" + host + "/pub/sinclair/games/c/ChuckieEgg.tzx.zip")),
      new Archive("ChuckieEgg.tap.zip", new URL("http://" + host + "/pub/sinclair/games/c/ChuckieEgg.tap.zip")),
      new Archive("ChuckieEgg(PickChoose).tzx.zip", new URL("http://" + host + "/pub/sinclair/games/c/ChuckieEgg(PickChoose).tzx.zip"))
    )
  }

  object WOS {
    def hasTitlesMatching(regex: String) = new {
      def ofFormat(format: String) = new {
        def returning(response: String) = {
          stubFor(get(urlPathEqualTo("/infoseekadv.cgi"))
            .withQueryParam("what", equalTo("1"))
            .withQueryParam("regexp", equalTo(regex))
            .withQueryParam("format", equalTo(format))
            .willReturn(aResponse().withHeader("Content-Type", "text/html; charset=iso-8859-1")
              .withBody(response)))
        }
      }
    }

    def hasArchivesFor(title: Title) = new {
      def returning(response: String) = {
        stubFor(get(urlPathEqualTo(title.getLocation.getPath))
          .willReturn(aResponse().withHeader("Content-Type", "text/html; charset=iso-8859-1")
          .withBody(response)))
      }
    }
  }
}

object TestData {
  val tzxChuckie: String =
    """
      |<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
      |<HTML>
      |<HEAD>
      |<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=iso-8859-1">
      |<BASE HREF="http://www.worldofspectrum.org/">
      |<META NAME="robots" CONTENT="noindex,nofollow">
      |<META NAME="generator" CONTENT="infoseek/13.1">
      |<TITLE>World of Spectrum - Sinclair Infoseek - Search Results</TITLE>
      |<SCRIPT TYPE="text/javascript" SRC="http://ajax.googleapis.com/ajax/libs/jquery/1.5.2/jquery.min.js"></SCRIPT>
      |<SCRIPT TYPE="text/javascript" SRC="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.11/jquery-ui.min.js"></SCRIPT>
      |<LINK REL="stylesheet" HREF="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.11/themes/base/jquery-ui.css">
      |<SCRIPT TYPE="text/javascript">/* <![CDATA[ */
      |  $(function() {
      |    var modelField = $('form[name=iseek] select[name=model]');
      |    $('form[name=iseek] input[name=regexp]').autocomplete({
      |      source: function(request, response) {
      |        $.getJSON('/api/infoseek_livesearch_json.cgi?callback=?',
      |          {'title': '^' + request.term, 'perpage': 10, 'model': modelField.val()},
      |          function(results) {
      |            resultTitles = $.map(results.matches, function(result) { return result.title });
      |            response(resultTitles);
      |          }
      |        )
      |      }
      |    })
      |  })
      |/* ]]> */</SCRIPT>
      |</HEAD>
      |<BODY BGCOLOR="#F0F0FF" TEXT="#000000" LINK="#4040C0" VLINK="#303080" ALINK="#F00000" onLoad="document.iseek.regexp.focus()">
      |<FONT FACE="Arial,Helvetica">
      |<HR><CENTER><TABLE WIDTH="100%" BORDER=0><TR><TD BGCOLOR="#000080">
      |<CENTER><FONT FACE="Arial,Helvetica" COLOR="#FFFFFF" SIZE="+1"><B>SINCLAIR INFOSEEK - SEARCH RESULTS</B></FONT></CENTER>
      |</TD></TR></TABLE></CENTER><HR>
      |
      |<TABLE WIDTH="100%" BORDER=0>
      |<TR><TD BGCOLOR="#E0E0E0">
      |<CENTER><FONT FACE="Arial,Helvetica"><BR>
      |<FORM NAME="iseek" ACTION="http://www.worldofspectrum.org/infoseek.cgi" METHOD=POST>
      |[ <A HREF="http://www.worldofspectrum.org/infoseek.cgi">Help!</A> | <A HREF="http://www.worldofspectrum.org/infoseekconfig.cgi">Configure</A> | <A HREF="http://www.worldofspectrum.org/infoseekadv.cgi">Advanced search</A> ]<BR><BR>
      |Search expression
      |<INPUT TYPE=TEXT NAME="regexp" SIZE=30 MAXLENGTH=127 VALUE="">
      | for
      |<SELECT NAME="model">
      |<OPTION VALUE="spectrum"> Software - ZX Spectrum
      |<OPTION VALUE="timex"> Software - Timex
      |<OPTION VALUE="zx81"> Software - ZX81
      |<OPTION VALUE="hardware"> Hardware
      |<OPTION VALUE="books"> Books
      |<OPTION VALUE="any" SELECTED> ANYthing
      |</SELECT><BR>
      |show pictures <SELECT NAME="loadpics"><OPTION VALUE="0"> (none)
      |<OPTION VALUE="1" SELECTED> Screenshots
      |<OPTION VALUE="2"> Inlays
      |<OPTION VALUE="3"> Screenshots and inlays
      |</SELECT> <INPUT TYPE=CHECKBOX NAME="fast"> list only <BR><BR>
      |<INPUT TYPE=SUBMIT VALUE="Search!"><BR>
      |</FORM>
      |</FONT></CENTER>
      |</TABLE>
      |<HR><CENTER><TABLE WIDTH="100%" BORDER=0><TR><TD BGCOLOR="#000080">
      |<CENTER><FONT FACE="Arial,Helvetica" COLOR="#FFFFFF" SIZE="+1"><B>ALL MATCHES</B></FONT></CENTER>
      |</TD></TR></TABLE></CENTER><HR>
      |<CENTER>[ <A HREF="/infoseekadv.cgi?what=1&regexp=chuckie&format=TZX&edit">Refine query</A> ]</CENTER><BR>
      |<TABLE BORDER=0 CELLSPACING=5>
      |<TR><TD><FONT FACE="Arial,Helvetica"><B>Title</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Year</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Publisher</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Score</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Language</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Type</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Model</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Status</B></FONT>
      |<TR><TD><FONT FACE="Arial,Helvetica"><A HREF="/infoseek.cgi?regexp=^Chuckie+Designer$&pub=^P%26M+Software$&loadpics=1">Chuckie Designer</A></FONT><TD><FONT FACE="Arial,Helvetica">1984</FONT><TD><FONT FACE="Arial,Helvetica">P&amp;M Software</FONT><TD ALIGN=CENTER><FONT FACE="Arial,Helvetica">9.67</FONT><TD><FONT FACE="Arial,Helvetica">English</FONT><TD><FONT FACE="Arial,Helvetica">Utility: Game Editor</FONT><TD><FONT FACE="Arial,Helvetica">Spectrum</FONT><TD><FONT FACE="Arial,Helvetica">Available</FONT>
      |<TR><TD><FONT FACE="Arial,Helvetica"><A HREF="/infoseek.cgi?regexp=^Chuckie+Egg$&pub=^A%27n%27F+Software$&loadpics=1">Chuckie Egg</A></FONT><TD><FONT FACE="Arial,Helvetica">1984</FONT><TD><FONT FACE="Arial,Helvetica">A'n'F Software</FONT><TD ALIGN=CENTER><FONT FACE="Arial,Helvetica">8.26</FONT><TD><FONT FACE="Arial,Helvetica">English</FONT><TD><FONT FACE="Arial,Helvetica">Arcade: Platform</FONT><TD><FONT FACE="Arial,Helvetica">Spectrum</FONT><TD><FONT FACE="Arial,Helvetica">Available</FONT>
      |<TR><TD><FONT FACE="Arial,Helvetica"><A HREF="/infoseek.cgi?regexp=^Chuckie+Egg+2$&pub=^A%27n%27F+Software$&loadpics=1">Chuckie Egg 2</A><BR><FONT SIZE="-1">&nbsp;&nbsp;aka. <I>Choccy Egg</I></FONT>
      |</FONT><TD><FONT FACE="Arial,Helvetica">1985</FONT><TD><FONT FACE="Arial,Helvetica">A'n'F Software</FONT><TD ALIGN=CENTER><FONT FACE="Arial,Helvetica">8.19</FONT><TD><FONT FACE="Arial,Helvetica">English</FONT><TD><FONT FACE="Arial,Helvetica">Arcade: Platform</FONT><TD><FONT FACE="Arial,Helvetica">Spectrum</FONT><TD><FONT FACE="Arial,Helvetica">Available</FONT>
      |<TR><TD><FONT FACE="Arial,Helvetica"><A HREF="/infoseek.cgi?regexp=^Chuckie+Egg+Editor$&pub=^Mercury+Software$&loadpics=1">Chuckie Egg Editor</A></FONT><TD><FONT FACE="Arial,Helvetica">1985</FONT><TD><FONT FACE="Arial,Helvetica">Mercury Software</FONT><TD ALIGN=CENTER><FONT FACE="Arial,Helvetica"></FONT><TD><FONT FACE="Arial,Helvetica">English</FONT><TD><FONT FACE="Arial,Helvetica">Utility: Game Editor</FONT><TD><FONT FACE="Arial,Helvetica">Spectrum</FONT><TD><FONT FACE="Arial,Helvetica">Available</FONT>
      |</TABLE>
      |<TABLE BORDER=0 WIDTH="100%">
      |<TR><TD><FONT FACE="Arial,Helvetica" SIZE="-1"><I>(4 titles found)</I></FONT>
      |</TABLE>
      |
      |<BR><CENTER><IMG SRC="pics/zx-hline.gif" WIDTH=540 HEIGHT=8 ALT=""></CENTER>
      |<CENTER>
      |  <FONT SIZE="-1"><I>
      |  Sinclair Infoseek (v13.1) is written by Martijn van der Heide and &copy; 1999-2014 ThunderWare Research Center
      |  </I></FONT><BR>
      |  <FONT SIZE="-1"><A HREF="/archivenote.html" TITLE="Acceptable Use Policy">Acceptable Use Policy</A></FONT><BR>
      |  <FONT SIZE="-2">Generated in 0.020 seconds</FONT>
      |  <BR><TABLE CELLPADDING=5>
      |  <TR>
      |    <TD><A HREF="http://www.worldofspectrum.org/email.html"><IMG SRC="pics/m-mail.gif" WIDTH=80 HEIGHT=40 BORDER=0 ALT="[Mail]"></A>
      |    <TD><A HREF="http://www.worldofspectrum.org/index.html"><IMG SRC="pics/m-index.gif" WIDTH=80 HEIGHT=40 BORDER=0 ALT="[Index]"></A>
      |  </TABLE>
      |</CENTER>
      |</FONT>
      |</BODY>
      |</HTML>
    """.stripMargin

  val tapChuckie: String =
    """
      |<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
      |<HTML>
      |<HEAD>
      |<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=iso-8859-1">
      |<BASE HREF="http://www.worldofspectrum.org/">
      |<META NAME="robots" CONTENT="noindex,nofollow">
      |<META NAME="generator" CONTENT="infoseek/13.1">
      |<TITLE>World of Spectrum - Sinclair Infoseek - Search Results</TITLE>
      |<SCRIPT TYPE="text/javascript" SRC="http://ajax.googleapis.com/ajax/libs/jquery/1.5.2/jquery.min.js"></SCRIPT>
      |<SCRIPT TYPE="text/javascript" SRC="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.11/jquery-ui.min.js"></SCRIPT>
      |<LINK REL="stylesheet" HREF="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.11/themes/base/jquery-ui.css">
      |<SCRIPT TYPE="text/javascript">/* <![CDATA[ */
      |  $(function() {
      |    var modelField = $('form[name=iseek] select[name=model]');
      |    $('form[name=iseek] input[name=regexp]').autocomplete({
      |      source: function(request, response) {
      |        $.getJSON('/api/infoseek_livesearch_json.cgi?callback=?',
      |          {'title': '^' + request.term, 'perpage': 10, 'model': modelField.val()},
      |          function(results) {
      |            resultTitles = $.map(results.matches, function(result) { return result.title });
      |            response(resultTitles);
      |          }
      |        )
      |      }
      |    })
      |  })
      |/* ]]> */</SCRIPT>
      |</HEAD>
      |<BODY BGCOLOR="#F0F0FF" TEXT="#000000" LINK="#4040C0" VLINK="#303080" ALINK="#F00000" onLoad="document.iseek.regexp.focus()">
      |<FONT FACE="Arial,Helvetica">
      |<HR><CENTER><TABLE WIDTH="100%" BORDER=0><TR><TD BGCOLOR="#000080">
      |<CENTER><FONT FACE="Arial,Helvetica" COLOR="#FFFFFF" SIZE="+1"><B>SINCLAIR INFOSEEK - SEARCH RESULTS</B></FONT></CENTER>
      |</TD></TR></TABLE></CENTER><HR>
      |
      |<TABLE WIDTH="100%" BORDER=0>
      |<TR><TD BGCOLOR="#E0E0E0">
      |<CENTER><FONT FACE="Arial,Helvetica"><BR>
      |<FORM NAME="iseek" ACTION="http://www.worldofspectrum.org/infoseek.cgi" METHOD=POST>
      |[ <A HREF="http://www.worldofspectrum.org/infoseek.cgi">Help!</A> | <A HREF="http://www.worldofspectrum.org/infoseekconfig.cgi">Configure</A> | <A HREF="http://www.worldofspectrum.org/infoseekadv.cgi">Advanced search</A> ]<BR><BR>
      |Search expression
      |<INPUT TYPE=TEXT NAME="regexp" SIZE=30 MAXLENGTH=127 VALUE="">
      | for
      |<SELECT NAME="model">
      |<OPTION VALUE="spectrum"> Software - ZX Spectrum
      |<OPTION VALUE="timex"> Software - Timex
      |<OPTION VALUE="zx81"> Software - ZX81
      |<OPTION VALUE="hardware"> Hardware
      |<OPTION VALUE="books"> Books
      |<OPTION VALUE="any" SELECTED> ANYthing
      |</SELECT><BR>
      |show pictures <SELECT NAME="loadpics"><OPTION VALUE="0"> (none)
      |<OPTION VALUE="1" SELECTED> Screenshots
      |<OPTION VALUE="2"> Inlays
      |<OPTION VALUE="3"> Screenshots and inlays
      |</SELECT> <INPUT TYPE=CHECKBOX NAME="fast"> list only <BR><BR>
      |<INPUT TYPE=SUBMIT VALUE="Search!"><BR>
      |</FORM>
      |</FONT></CENTER>
      |</TABLE>
      |<HR><CENTER><TABLE WIDTH="100%" BORDER=0><TR><TD BGCOLOR="#000080">
      |<CENTER><FONT FACE="Arial,Helvetica" COLOR="#FFFFFF" SIZE="+1"><B>ALL MATCHES</B></FONT></CENTER>
      |</TD></TR></TABLE></CENTER><HR>
      |<CENTER>[ <A HREF="/infoseekadv.cgi?what=1&regexp=chuckie&format=TAP&edit">Refine query</A> ]</CENTER><BR>
      |<TABLE BORDER=0 CELLSPACING=5>
      |<TR><TD><FONT FACE="Arial,Helvetica"><B>Title</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Year</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Publisher</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Score</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Language</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Type</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Model</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Status</B></FONT>
      |<TR><TD><FONT FACE="Arial,Helvetica"><A HREF="/infoseek.cgi?regexp=^Chuckie+Egg$&pub=^A%27n%27F+Software$&loadpics=1">Chuckie Egg</A></FONT><TD><FONT FACE="Arial,Helvetica">1984</FONT><TD><FONT FACE="Arial,Helvetica">A'n'F Software</FONT><TD ALIGN=CENTER><FONT FACE="Arial,Helvetica">8.26</FONT><TD><FONT FACE="Arial,Helvetica">English</FONT><TD><FONT FACE="Arial,Helvetica">Arcade: Platform</FONT><TD><FONT FACE="Arial,Helvetica">Spectrum</FONT><TD><FONT FACE="Arial,Helvetica">Available</FONT>
      |<TR><TD><FONT FACE="Arial,Helvetica"><A HREF="/infoseek.cgi?regexp=^Chuckie+Egg+2$&pub=^A%27n%27F+Software$&loadpics=1">Chuckie Egg 2</A><BR><FONT SIZE="-1">&nbsp;&nbsp;aka. <I>Choccy Egg</I></FONT>
      |</FONT><TD><FONT FACE="Arial,Helvetica">1985</FONT><TD><FONT FACE="Arial,Helvetica">A'n'F Software</FONT><TD ALIGN=CENTER><FONT FACE="Arial,Helvetica">8.19</FONT><TD><FONT FACE="Arial,Helvetica">English</FONT><TD><FONT FACE="Arial,Helvetica">Arcade: Platform</FONT><TD><FONT FACE="Arial,Helvetica">Spectrum</FONT><TD><FONT FACE="Arial,Helvetica">Available</FONT>
      |</TABLE>
      |<TABLE BORDER=0 WIDTH="100%">
      |<TR><TD><FONT FACE="Arial,Helvetica" SIZE="-1"><I>(2 titles found)</I></FONT>
      |</TABLE>
      |
      |<BR><CENTER><IMG SRC="pics/zx-hline.gif" WIDTH=540 HEIGHT=8 ALT=""></CENTER>
      |<CENTER>
      |  <FONT SIZE="-1"><I>
      |  Sinclair Infoseek (v13.1) is written by Martijn van der Heide and &copy; 1999-2014 ThunderWare Research Center
      |  </I></FONT><BR>
      |  <FONT SIZE="-1"><A HREF="/archivenote.html" TITLE="Acceptable Use Policy">Acceptable Use Policy</A></FONT><BR>
      |  <FONT SIZE="-2">Generated in 0.014 seconds</FONT>
      |  <BR><TABLE CELLPADDING=5>
      |  <TR>
      |    <TD><A HREF="http://www.worldofspectrum.org/email.html"><IMG SRC="pics/m-mail.gif" WIDTH=80 HEIGHT=40 BORDER=0 ALT="[Mail]"></A>
      |    <TD><A HREF="http://www.worldofspectrum.org/index.html"><IMG SRC="pics/m-index.gif" WIDTH=80 HEIGHT=40 BORDER=0 ALT="[Index]"></A>
      |  </TABLE>
      |</CENTER>
      |</FONT>
      |</BODY>
      |</HTML>
    """.stripMargin

  val chuckieEggArchives =
    """
      |
      |<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
      |<HTML>
      |<HEAD>
      |<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=iso-8859-1">
      |<BASE HREF="http://www.worldofspectrum.org/">
      |<META NAME="robots" CONTENT="nofollow">
      |<META NAME="generator" CONTENT="infoseek/13.1">
      |<TITLE>Chuckie Egg - World of Spectrum</TITLE>
      |<SCRIPT TYPE="text/javascript" SRC="http://ajax.googleapis.com/ajax/libs/jquery/1.5.2/jquery.min.js"></SCRIPT>
      |<SCRIPT TYPE="text/javascript" SRC="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.11/jquery-ui.min.js"></SCRIPT>
      |<LINK REL="stylesheet" HREF="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.11/themes/base/jquery-ui.css">
      |<SCRIPT TYPE="text/javascript">/* <![CDATA[ */
      |  $(function() {
      |    var modelField = $('form[name=iseek] select[name=model]');
      |    $('form[name=iseek] input[name=regexp]').autocomplete({
      |      source: function(request, response) {
      |        $.getJSON('/api/infoseek_livesearch_json.cgi?callback=?',
      |          {'title': '^' + request.term, 'perpage': 10, 'model': modelField.val()},
      |          function(results) {
      |            resultTitles = $.map(results.matches, function(result) { return result.title });
      |            response(resultTitles);
      |          }
      |        )
      |      }
      |    })
      |  })
      |/* ]]> */</SCRIPT>
      |</HEAD>
      |<BODY BGCOLOR="#F0F0FF" TEXT="#000000" LINK="#4040C0" VLINK="#303080" ALINK="#F00000" onLoad="document.iseek.regexp.focus()">
      |<FONT FACE="Arial,Helvetica">
      |<HR><CENTER><TABLE WIDTH="100%" BORDER=0><TR><TD BGCOLOR="#000080">
      |<CENTER><FONT FACE="Arial,Helvetica" COLOR="#FFFFFF" SIZE="+1"><B>SINCLAIR INFOSEEK - SEARCH RESULTS</B></FONT></CENTER>
      |</TD></TR></TABLE></CENTER><HR>
      |
      |<TABLE WIDTH="100%" BORDER=0>
      |<TR><TD BGCOLOR="#E0E0E0">
      |<CENTER><FONT FACE="Arial,Helvetica"><BR>
      |<FORM NAME="iseek" ACTION="http://www.worldofspectrum.org/infoseek.cgi" METHOD=POST>
      |[ <A HREF="http://www.worldofspectrum.org/infoseek.cgi">Help!</A> | <A HREF="http://www.worldofspectrum.org/infoseekconfig.cgi">Configure</A> | <A HREF="http://www.worldofspectrum.org/infoseekadv.cgi">Advanced search</A> ]<BR><BR>
      |Search expression
      |<INPUT TYPE=TEXT NAME="regexp" SIZE=30 MAXLENGTH=127 VALUE="">
      | for
      |<SELECT NAME="model">
      |<OPTION VALUE="spectrum"> Software - ZX Spectrum
      |<OPTION VALUE="timex"> Software - Timex
      |<OPTION VALUE="zx81"> Software - ZX81
      |<OPTION VALUE="hardware"> Hardware
      |<OPTION VALUE="books"> Books
      |<OPTION VALUE="any" SELECTED> ANYthing
      |</SELECT><BR>
      |show pictures <SELECT NAME="loadpics"><OPTION VALUE="0"> (none)
      |<OPTION VALUE="1" SELECTED> Screenshots
      |<OPTION VALUE="2"> Inlays
      |<OPTION VALUE="3"> Screenshots and inlays
      |</SELECT> <INPUT TYPE=CHECKBOX NAME="fast"> list only <BR><BR>
      |<INPUT TYPE=SUBMIT VALUE="Search!"><BR>
      |</FORM>
      |</FONT></CENTER>
      |</TABLE>
      |<HR><CENTER><TABLE WIDTH="100%" BORDER=0><TR><TD BGCOLOR="#000080">
      |<CENTER><FONT FACE="Arial,Helvetica" COLOR="#FFFFFF" SIZE="+1"><B>MATCHES &amp; DOWNLOADS</B></FONT></CENTER>
      |</TD></TR></TABLE></CENTER><HR>
      |<CENTER><TABLE WIDTH="100%" BORDER=0><TR><TD BGCOLOR="#E0E0E0" WIDTH="100%">
      |<CENTER><FONT FACE="Arial,Helvetica" COLOR="#000000" SIZE="+1"><B>Chuckie Egg</B></FONT></CENTER></TD><TD BGCOLOR="#E0E0E0" WIDTH="0%"><A HREF="/infoseekid.cgi?id=0000958&loadpics=0"><IMG SRC="/pics/pics-none.png" WIDTH=36 HEIGHT=12 BORDER=0 ALT="[none]" TITLE="Change picture display to: (none)"></A></TD><TD BGCOLOR="#E0E0E0" WIDTH="0%"><A HREF="/infoseekid.cgi?id=0000958&loadpics=1"><IMG SRC="/pics/pics-screen.png" WIDTH=36 HEIGHT=12 BORDER=0 ALT="[screens]" TITLE="Change picture display to: Screenshots"></A></TD><TD BGCOLOR="#E0E0E0" WIDTH="0%"><A HREF="/infoseekid.cgi?id=0000958&loadpics=2"><IMG SRC="/pics/pics-inlay.png" WIDTH=36 HEIGHT=12 BORDER=0 ALT="[inlays]" TITLE="Change picture display to: Inlays"></A></TD><TD BGCOLOR="#E0E0E0" WIDTH="0%"><A HREF="/infoseekid.cgi?id=0000958&loadpics=3"><IMG SRC="/pics/pics-both.png" WIDTH=36 HEIGHT=12 BORDER=0 ALT="[both]" TITLE="Change picture display to: Screenshots and inlays"></A></TD><TD BGCOLOR="#E0E0E0" WIDTH="0%"><FONT FACE="Arial,Helvetica" COLOR="#000000" SIZE="-1">[<A HREF="http://www.worldofspectrum.org/infoseek.cgi?regexp=^Chuckie+Egg$&pub=^A%27n%27F+Software$&loadpics=1&tzxblock" TITLE="Fetch TZX Archive Info block for this match">tzx</A>]</FONT></TD></TR></TABLE></CENTER><HR>
      |<TABLE BORDER=0 ALIGN=RIGHT>
      |<TR><TD><IMG SRC="/pub/sinclair/screens/load/c/gif/ChuckieEgg.gif" BORDER=1 WIDTH=256 HEIGHT=192 ALIGN=RIGHT ALT="[Loading screen]" TITLE="Loading screen">
      |<TR><TD><IMG SRC="/pub/sinclair/screens/in-game/c/ChuckieEgg.gif" BORDER=1 WIDTH=256 HEIGHT=192 ALIGN=RIGHT ALT="[In-game screen]" TITLE="In-game screen">
      |</TABLE>
      |<TABLE BORDER=0>
      |<TR><TD NOWRAP WIDTH="0%"><FONT FACE="Arial,Helvetica" COLOR="#000080">Full title</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica"><A HREF="/infoseekid.cgi?id=0000958" TITLE="Get direct link to this entry">Chuckie Egg</A></FONT>
      |<TR><TD NOWRAP WIDTH="0%"><FONT FACE="Arial,Helvetica" COLOR="#000080">Year of release</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica">1984</FONT>
      |<TR><TD NOWRAP WIDTH="0%"><FONT FACE="Arial,Helvetica" COLOR="#000080">Publisher</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica"><A HREF="http://www.worldofspectrum.org/infoseekpub.cgi?regexp=^A%27n%27F+Software$&loadpics=1" TITLE="Find other titles from this publisher">A'n'F Software</A> <FONT SIZE="-1"><I>(UK)</I></FONT></FONT>
      |<TR><TD NOWRAP WIDTH="0%" VALIGN=TOP><FONT FACE="Arial,Helvetica" COLOR="#000080">Re-released by</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica"><A HREF="http://www.worldofspectrum.org/infoseekpub.cgi?regexp=^Pick+%26+Choose$&loadpics=1" TITLE="Find other titles from this publisher">Pick &amp; Choose</A> <FONT SIZE="-1"><I>(UK)</I></FONT></FONT>
      |<TR><TD NOWRAP WIDTH="0%" VALIGN=TOP><FONT FACE="Arial,Helvetica" COLOR="#000080">Author(s)</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica"><A HREF="http://www.worldofspectrum.org/infoseekpub.cgi?regexp=^Nigel+Alderton$&loadpics=1" TITLE="Find other titles by this author">Nigel Alderton</A></FONT>
      |<TR><TD NOWRAP WIDTH="0%"><FONT FACE="Arial,Helvetica" COLOR="#000080">Machine type</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica">ZX Spectrum 48K</FONT>
      |<TR><TD NOWRAP WIDTH="0%"><FONT FACE="Arial,Helvetica" COLOR="#000080">Number of players</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica">4 - alternating</FONT>
      |<TR><TD NOWRAP WIDTH="0%"><FONT FACE="Arial,Helvetica" COLOR="#000080">Controls</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica">Redefineable keys</FONT>
      |<TR><TD NOWRAP WIDTH="0%"><FONT FACE="Arial,Helvetica" COLOR="#000080">Type</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica">Arcade: Platform</FONT>
      |<TR><TD NOWRAP WIDTH="0%"><FONT FACE="Arial,Helvetica" COLOR="#000080">Message language</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica">English</FONT>
      |<TR><TD NOWRAP WIDTH="0%" VALIGN=TOP><FONT FACE="Arial,Helvetica" COLOR="#000080">Original publication</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica">Commercial</FONT>
      |<TR><TD NOWRAP WIDTH="0%" VALIGN=TOP><FONT FACE="Arial,Helvetica" COLOR="#000080">Original price</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica">&pound;6.90</FONT>
      |<TR><TD NOWRAP WIDTH="0%"><FONT FACE="Arial,Helvetica" COLOR="#000080">Availability</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica">Available as both Perfect TZX and non-TZX</FONT>
      |<TR><TD NOWRAP WIDTH="0%"><FONT FACE="Arial,Helvetica" COLOR="#000080">Protection scheme</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica">None</FONT>
      |<TR><TD NOWRAP WIDTH="0%" VALIGN=TOP><FONT FACE="Arial,Helvetica" COLOR="#000080">Additional info</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica">Appeared on tape 1, side A of the compilation <A HREF="http://www.worldofspectrum.org/infoseek.cgi?regexp=^10+Computer+Hits+1$&pub=^Beau%2dJolly+Ltd$&loadpics=1" TITLE="Lookup this compilation for its full contents">10 Computer Hits 1</A> (Beau-Jolly Ltd)
      |<BR>
      |Appeared on side B of covertape <A HREF="http://www.worldofspectrum.org/infoseek.cgi?regexp=^Stars+Spectrum+issue+02+%2d+Juegos$&pub=^Stars+Spectrum$&loadpics=1" TITLE="Lookup this covertape for its full contents">Stars Spectrum issue 02 - Juegos</A> (Stars Spectrum), as '<A HREF="http://www.worldofspectrum.org/infoseek.cgi?regexp=^Corral%2c+El$">Corral, El</A>'
      |<BR>
      |Also listed on <A HREF="http://en.wikipedia.org/wiki/Chuckie_Egg" TITLE="Lookup in Wikipedia - remote">Wikipedia</A> and <A HREF="http://zxspectrum.freebase.com/view/base/zxspectrum/wos/0000958" TITLE="Lookup in Freebase - remote">Freebase</A></FONT>
      |<TR><TD NOWRAP WIDTH="0%" VALIGN=TOP><FONT FACE="Arial,Helvetica" COLOR="#000080">Remarks</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica">There also were a <A HREF="http://www.worldofspectrum.org/infoseek.cgi?regexp=^Chuckie+Designer$&pub=^P%26M+Software$&loadpics=1">Chuckie Designer</A>, <A HREF="http://www.worldofspectrum.org/infoseek.cgi?regexp=^Chuckie+Egg+Editor$&pub=^Mercury+Software$&loadpics=1">Chuckie Egg Editor</A> and <A HREF="http://www.worldofspectrum.org/infoseek.cgi?regexp=^Chuckie+Egg+Screen+Designer$&pub=^D%2e+Boocock$&loadpics=1">Chuckie Egg Screen Designer</A>.</FONT>
      |
      |<TR><TD NOWRAP WIDTH="0%" VALIGN=TOP><FONT FACE="Arial,Helvetica" COLOR="#000080">Series</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica">This game belongs in the following series:
      |<BR>
      |1. Chuckie Egg<BR>
      |2. <A HREF="http://www.worldofspectrum.org/infoseek.cgi?regexp=^Chuckie+Egg+2$&pub=^A%27n%27F+Software$&loadpics=1">Chuckie Egg 2</A></FONT>
      |<TR><TD NOWRAP WIDTH="0%" VALIGN=TOP><FONT FACE="Arial,Helvetica" COLOR="#000080">Other systems</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica">This title was also advertised for and/or published on the <A HREF="http://www.cpc-power.com/index.php?page=detail&num=2386" TITLE="Lookup Amstrad CPC release - remote" TARGET="external-details">Amstrad CPC</A>, <A HREF="http://www.atarimania.com/detail_soft.php?MENU=8&VERSION_ID=1055" TITLE="Lookup Atari 8-bit release - remote" TARGET="external-details">Atari 8-bit</A>, BBC Micro, <A HREF="http://www.lemon64.com/?game_id=485" TITLE="Lookup Commodore 64 release - remote" TARGET="external-details">Commodore 64</A> and <A HREF="http://www.generation-msx.nl/msxdb/softwareinfo/2660" TITLE="Lookup MSX release - remote" TARGET="external-details">MSX</A>
      |</FONT>
      |<TR></TR><TR><TD NOWRAP WIDTH="0%"><FONT FACE="Arial,Helvetica" COLOR="#000080">Score</FONT><TD WIDTH="100%"><FONT FACE="Arial,Helvetica">
      |8.26 <FONT SIZE="-1"><I>(408 votes)</I>&nbsp;&nbsp;&nbsp;<A HREF="/infoseekscore.cgi?id=0000958"><IMG SRC="icons/vote.gif" WIDTH=33 HEIGHT=12 BORDER=0 ALT="See scores and vote!"></A></FONT></FONT>
      |</TABLE><BR CLEAR=ALL>
      |<FONT SIZE="+1"><B><I><U>Download and play links</U></I></B></FONT>
      |<TABLE BORDER=0 CELLSPACING=5>
      |<TR><TD><TD><TD><FONT FACE="Arial,Helvetica"><B>Filename</B></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica"><B>Size</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Type</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Origin</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Code</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Barcode</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>D.L.</B></FONT>
      |<TR><TD><A HREF="http://www.worldofspectrum.org/infoseekplay.cgi?title=Chuckie+Egg&pub=A%27n%27F+Software&year=1984&id=0000958&joy=curs&game=/games/c/ChuckieEgg.tzx.zip&emu=3" TARGET="iseekplay" TITLE="Click to run on-line with a 48K Java Spectrum emulator"><IMG SRC="pics/iseekplay48m.gif" WIDTH=64 HEIGHT=32 ALT="Run on-line with a 48K Java Spectrum emulator" BORDER=0></A>
      |<TD><IMG SRC="icons/perfect.gif" WIDTH=31 HEIGHT=21 ALT=""><TD><FONT FACE="Arial,Helvetica"><A HREF="/pub/sinclair/games/c/ChuckieEgg.tzx.zip" TITLE="Download to play off-line in an emulator">ChuckieEgg.tzx.zip</A></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica">10,021</FONT><TD><FONT FACE="Arial,Helvetica">(Perfect TZX tape image)</FONT><TD><FONT FACE="Arial,Helvetica">Original release</FONT><TD><FONT FACE="Arial,Helvetica"></FONT><TD><FONT FACE="Arial,Helvetica"></FONT><TD><FONT FACE="Arial,Helvetica"></FONT><TR><TD><A HREF="http://www.worldofspectrum.org/infoseekplay.cgi?title=Chuckie+Egg&pub=A%27n%27F+Software&year=1984&id=0000958&joy=curs&game=/games/c/ChuckieEgg.tap.zip&emu=3" TARGET="iseekplay" TITLE="Click to run on-line with a 48K Java Spectrum emulator"><IMG SRC="pics/iseekplay48.gif" WIDTH=64 HEIGHT=32 ALT="Run on-line with a 48K Java Spectrum emulator" BORDER=0></A>
      |<TD><IMG SRC="icons/perfect.gif" WIDTH=31 HEIGHT=21 ALT=""><TD><FONT FACE="Arial,Helvetica"><A HREF="/pub/sinclair/games/c/ChuckieEgg.dsk.zip" TITLE="Download to play off-line in an emulator">ChuckieEgg.dsk.zip</A></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica">10,021</FONT><TD><FONT FACE="Arial,Helvetica">(Perfect TZX tape image)</FONT><TD><FONT FACE="Arial,Helvetica">Original release</FONT><TD><FONT FACE="Arial,Helvetica"></FONT><TD><FONT FACE="Arial,Helvetica"></FONT><TD><FONT FACE="Arial,Helvetica"></FONT><TR><TD><A HREF="http://www.worldofspectrum.org/infoseekplay.cgi?title=Chuckie+Egg&pub=A%27n%27F+Software&year=1984&id=0000958&joy=curs&game=/games/c/ChuckieEgg.dsk.zip&emu=3" TARGET="iseekplay" TITLE="Click to run on-line with a 48K Java Spectrum emulator"><IMG SRC="pics/iseekplay48.gif" WIDTH=64 HEIGHT=32 ALT="Run on-line with a 48K Java Spectrum emulator" BORDER=0></A>
      |<TD><IMG SRC="icons/tap.gif" WIDTH=31 HEIGHT=21 ALT=""><TD><FONT FACE="Arial,Helvetica"><A HREF="/pub/sinclair/games/c/ChuckieEgg.tap.zip" TITLE="Download to play off-line in an emulator">ChuckieEgg.tap.zip</A></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica">9,939</FONT><TD><FONT FACE="Arial,Helvetica">((non-TZX) TAP tape image)</FONT><TD><FONT FACE="Arial,Helvetica"></FONT><TD><FONT FACE="Arial,Helvetica"></FONT><TD><FONT FACE="Arial,Helvetica"></FONT><TD><FONT FACE="Arial,Helvetica"></FONT><TR><TD><A HREF="http://www.worldofspectrum.org/infoseekplay.cgi?title=Chuckie+Egg&pub=A%27n%27F+Software&year=1984&id=0000958&joy=curs&game=/games/c/ChuckieEgg(PickChoose).tzx.zip&emu=3" TARGET="iseekplay" TITLE="Click to run on-line with a 48K Java Spectrum emulator"><IMG SRC="pics/iseekplay48m.gif" WIDTH=64 HEIGHT=32 ALT="Run on-line with a 48K Java Spectrum emulator" BORDER=0></A>
      |<TD><IMG SRC="icons/perfect.gif" WIDTH=31 HEIGHT=21 ALT=""><TD><FONT FACE="Arial,Helvetica"><A HREF="/pub/sinclair/games/c/ChuckieEgg(PickChoose).tzx.zip" TITLE="Download to play off-line in an emulator">ChuckieEgg(PickChoose).tzx.zip</A></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica">10,027</FONT><TD><FONT FACE="Arial,Helvetica">(Perfect TZX tape image)</FONT><TD><FONT FACE="Arial,Helvetica">Re-release</FONT><TD><FONT FACE="Arial,Helvetica"></FONT><TD><FONT FACE="Arial,Helvetica">5018647000012</FONT><TD><FONT FACE="Arial,Helvetica"></FONT></TABLE><BR>
      |<FONT SIZE="+1"><B><I><U>Additional material</U></I></B></FONT><BR>
      |<TABLE BORDER=0 CELLSPACING=5>
      |<TR><TD><TD><FONT FACE="Arial,Helvetica"><B>Filename</B></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica"><B>Size</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Type</B></FONT>
      |<TR><TD><IMG SRC="icons/screen.gif" WIDTH=31 HEIGHT=21 ALT=""><TD><FONT FACE="Arial,Helvetica"><A HREF="/pub/sinclair/screens/load/c/gif/ChuckieEgg.gif" TITLE="Loading screen">ChuckieEgg.gif</A></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica">6,270</FONT><TD><FONT FACE="Arial,Helvetica">(Loading screen)</FONT>
      |<TR><TD><IMG SRC="icons/screen.gif" WIDTH=31 HEIGHT=21 ALT=""><TD><FONT FACE="Arial,Helvetica"><A HREF="/pub/sinclair/screens/load/c/scr/ChuckieEgg.scr" TITLE="Loading screen dump">ChuckieEgg.scr</A></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica">6,912</FONT><TD><FONT FACE="Arial,Helvetica">(Loading screen dump)</FONT>
      |<TR><TD><TD><FONT FACE="Arial,Helvetica"><A HREF="/pub/sinclair/screens/in-game/c/ChuckieEgg.gif" TITLE="In-game screen">ChuckieEgg.gif</A></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica">2,892</FONT><TD><FONT FACE="Arial,Helvetica">(In-game screen)</FONT>
      |<TR><TD>
      |<TR><TD><IMG SRC="icons/info.gif" WIDTH=31 HEIGHT=21 ALT=""><TD><FONT FACE="Arial,Helvetica"><A HREF="/pub/sinclair/games-info/c/ChuckieEgg.txt" TITLE="English instructions">ChuckieEgg.txt</A></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica">536</FONT><TD><FONT FACE="Arial,Helvetica">(English instructions)</FONT>
      |<TR><TD><IMG SRC="icons/info.gif" WIDTH=31 HEIGHT=21 ALT=""><TD><FONT FACE="Arial,Helvetica"><A HREF="/pub/sinclair/games-maps/c/ChuckieEgg.png" TITLE="Game map">ChuckieEgg.png</A></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica">119,176</FONT><TD><FONT FACE="Arial,Helvetica">(Game map)</FONT>
      |<TR><TD><TD><FONT FACE="Arial,Helvetica"><A HREF="/pub/sinclair/games-inlays/c/ChuckieEgg.jpg" TITLE="Cassette inlay">ChuckieEgg.jpg</A></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica">94,493</FONT><TD><FONT FACE="Arial,Helvetica">(Cassette inlay)</FONT>
      |<TR><TD><TD><FONT FACE="Arial,Helvetica"><A HREF="/pub/sinclair/games-inlays/c/ChuckieEgg_2.jpg" TITLE="Cassette inlay">ChuckieEgg_2.jpg</A></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica">75,257</FONT><TD><FONT FACE="Arial,Helvetica">(Cassette inlay)</FONT>
      |<TR><TD><TD><FONT FACE="Arial,Helvetica"><A HREF="/pub/sinclair/games-inlays/c/ChuckieEgg_3.jpg" TITLE="Cassette inlay">ChuckieEgg_3.jpg</A></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica">89,543</FONT><TD><FONT FACE="Arial,Helvetica">(Cassette inlay)</FONT>
      |<TR><TD><TD><FONT FACE="Arial,Helvetica"><A HREF="/pub/sinclair/games-inlays/Rereleases/c/ChuckieEgg(PickChoose).jpg" TITLE="Re-release cassette inlay">ChuckieEgg(PickChoose).jpg</A></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica">62,749</FONT><TD><FONT FACE="Arial,Helvetica">(Re-release cassette inlay)</FONT>
      |<TR><TD><TD><FONT FACE="Arial,Helvetica"><A HREF="/pub/sinclair/games-adverts/c/ChuckieEgg.jpg" TITLE="Advertisement">ChuckieEgg.jpg</A></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica">135,025</FONT><TD><FONT FACE="Arial,Helvetica">(Advertisement)</FONT>
      |<TR><TD><IMG SRC="icons/music.gif" WIDTH=31 HEIGHT=21 ALT=""><TD><FONT FACE="Arial,Helvetica"><A HREF="/pub/sinclair/music/ay/games/c/ChuckieEgg.ay.zip" TITLE="Ripped in-game and theme music in AY format">ChuckieEgg.ay.zip</A></FONT><TD ALIGN=RIGHT><FONT FACE="Arial,Helvetica">534</FONT><TD><FONT FACE="Arial,Helvetica">(Ripped in-game and theme music in AY format)</FONT>
      |</TABLE><BR>
      |<TABLE BORDER=0 CELLSPACING=5>
      |<TR><TD><A HREF="http://www.the-tipshop.co.uk/cgi-bin/info.pl?wosid=0000958"><IMG SRC="pics/tipshop.gif" WIDTH=87 HEIGHT=30 BORDER=0 ALT="Search The Tipshop" TITLE="Search The Tipshop"></A>
      |<TD><FONT FACE="Arial,Helvetica">The Tipshop has hints, cheats and/or POKEs for this game!</FONT>
      |</TABLE><BR>
      |<FONT SIZE="+1"><B><I><U>Remakes</U></I></B></FONT><BR>
      |<TABLE BORDER=0 CELLSPACING=5>
      |<TR><TD><FONT FACE="Arial,Helvetica"><B>Title</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Author</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Year</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Platform</B></FONT><TD><FONT FACE="Arial,Helvetica"><B>Status</B></FONT>
      |<TR><TD><FONT FACE="Arial,Helvetica"><A HREF="http://www.remakes.org/">Dos Egg</A></FONT>
      |<TD><FONT FACE="Arial,Helvetica">Steve McCrea, Mark Rendle</FONT>
      |<TD><FONT FACE="Arial,Helvetica">1999</FONT>
      |<TD><FONT FACE="Arial,Helvetica">PC/DOS</FONT>
      |<TD><FONT FACE="Arial,Helvetica">available</FONT>
      |<TR><TD><FONT FACE="Arial,Helvetica"><A HREF="http://www.remakes.org/">Chuckie Egg 99</A></FONT>
      |<TD><FONT FACE="Arial,Helvetica">John Blythe</FONT>
      |<TD><FONT FACE="Arial,Helvetica">1999</FONT>
      |<TD><FONT FACE="Arial,Helvetica">PC/Windows</FONT>
      |<TD><FONT FACE="Arial,Helvetica">available (v0.11a)</FONT>
      |<TR><TD><FONT FACE="Arial,Helvetica"><A HREF="http://www.gershwin.force9.co.uk/chuckie/">Chuckie Egg for Windows 95</A></FONT>
      |<TD><FONT FACE="Arial,Helvetica">Mike Elson</FONT>
      |<TD><FONT FACE="Arial,Helvetica">2001</FONT>
      |<TD><FONT FACE="Arial,Helvetica">PC/Windows</FONT>
      |<TD><FONT FACE="Arial,Helvetica">available (link dead?)</FONT>
      |<TR><TD><FONT FACE="Arial,Helvetica"><A HREF="http://retrospec.sgn.net/game.php?link=chuckie">Chuckie Egg: The Next Batch</A></FONT>
      |<TD><FONT FACE="Arial,Helvetica">John Blythe</FONT>
      |<TD><FONT FACE="Arial,Helvetica">2001</FONT>
      |<TD><FONT FACE="Arial,Helvetica">PC/Windows</FONT>
      |<TD><FONT FACE="Arial,Helvetica">available (v1.2)</FONT>
      |<TR><TD><FONT FACE="Arial,Helvetica"><A HREF="http://www.gamesforge.com/play-5764-Chuckie_Egg.html">Chuckie Egg</A></FONT>
      |<TD><FONT FACE="Arial,Helvetica">Gamesforge.com</FONT>
      |<TD><FONT FACE="Arial,Helvetica">2006</FONT>
      |<TD><FONT FACE="Arial,Helvetica">Flash</FONT>
      |<TD><FONT FACE="Arial,Helvetica">available</FONT>
      |</TABLE><BR>
      |<FONT SIZE="+1"><B><I><U>Player reviews</U></I></B></FONT><BR>
      |<TABLE BORDER=0 CELLSPACING=5>
      |<TR><TD><FONT FACE="Arial,Helvetica">13 player reviews [ <A HREF="http://spectrum20.org/games/958" TITLE="View the player reviews - remote" TARGET="external-details">View</A> | <A HREF="http://spectrum20.org/reviews/new/958" TITLE="Write your own review - remote" TARGET="external-details">Write one</A> ]</FONT>
      |</TABLE><BR>
      |<FONT SIZE="+1"><B><I><U>Magazine references</U></I></B></FONT><BR>
      |<TABLE BORDER=0>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica"><B>Magazine</B></FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica"><B>Issue</B></FONT><TD NOWRAP><TD><FONT FACE="Arial,Helvetica"><B>Type</B></FONT><TD NOWRAP>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Popular Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">10.83</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 315</FONT><TD><FONT FACE="Arial,Helvetica">(Review)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PopularComputingWeekly/Issue831020/Pages/PopularComputingWeekly83102000015.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Home Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">11.83</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 310</FONT><TD><FONT FACE="Arial,Helvetica">(Review)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=HomeComputingWeekly/Issue037/Pages/HomeComputingWeekly03700010.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Sinclair User</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">1.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 42</FONT><TD><FONT FACE="Arial,Helvetica">(<A HREF="http://www.sincuser.f9.co.uk/022/softwre.htm">Review</A>)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=SinclairUser/Issue022/Pages/SinclairUser02200042.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Crash</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">3.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 14</FONT><TD><FONT FACE="Arial,Helvetica">(Review)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=Crash/Issue02/Pages/Crash0200014.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Personal Computer Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">3.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 62</FONT><TD><FONT FACE="Arial,Helvetica">(Review)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PersonalComputerGames/Issue04/Pages/PersonalComputerGames0400062.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Sinclair User</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">3.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 27</FONT><TD><FONT FACE="Arial,Helvetica">(<A HREF="http://www.sincuser.f9.co.uk/024/letters.htm">Letter</A>)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=SinclairUser/Issue024/Pages/SinclairUser02400027.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Crash</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">4.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 13</FONT><TD><FONT FACE="Arial,Helvetica">(<A HREF="http://www.crashonline.org.uk/03/letters.htm">Letter</A>)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=Crash/Issue03/Pages/Crash0300013.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Crash</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">4.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 70</FONT><TD><FONT FACE="Arial,Helvetica">(<A HREF="http://www.crashonline.org.uk/03/lguide07.htm">LivingGuide</A>)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=Crash/Issue03/Pages/Crash0300070.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Personal Computer Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">5.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 36</FONT><TD><FONT FACE="Arial,Helvetica">(Feature - Challenge Chamber)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PersonalComputerGames/Issue06/Pages/PersonalComputerGames0600036.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Crash</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">6.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 52</FONT><TD><FONT FACE="Arial,Helvetica">(Tips)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=Crash/Issue05/Pages/Crash0500052.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Personal Computer Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">7.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 94</FONT><TD><FONT FACE="Arial,Helvetica">(Feature - Challenge Chamber)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PersonalComputerGames/Issue08/Pages/PersonalComputerGames0800094.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Popular Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">7.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 336</FONT><TD><FONT FACE="Arial,Helvetica">(News/Note)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PopularComputingWeekly/Issue840719/Pages/PopularComputingWeekly84071900036.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Personal Computer Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">8.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 21</FONT><TD><FONT FACE="Arial,Helvetica">(Tips)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PersonalComputerGames/Issue09/Pages/PersonalComputerGames0900021.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Personal Computer Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">8.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 92</FONT><TD><FONT FACE="Arial,Helvetica">(Feature - Challenge Chamber)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PersonalComputerGames/Issue09/Pages/PersonalComputerGames0900092.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Personal Computer Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">9.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 100</FONT><TD><FONT FACE="Arial,Helvetica">(Feature - Challenge Chamber)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PersonalComputerGames/Issue10/Pages/PersonalComputerGames1000100.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Sinclair User</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">9.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 16</FONT><TD><FONT FACE="Arial,Helvetica">(<A HREF="http://www.sincuser.f9.co.uk/030/letters.htm">Letter</A>)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=SinclairUser/Issue030/Pages/SinclairUser03000016.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Crash</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">10.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 86</FONT><TD><FONT FACE="Arial,Helvetica">(Tips)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=Crash/Issue09/Pages/Crash0900086.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Personal Computer Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">10.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 94</FONT><TD><FONT FACE="Arial,Helvetica">(Tips)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PersonalComputerGames/Issue11/Pages/PersonalComputerGames1100094.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Your Spectrum</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">10.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 5</FONT><TD><FONT FACE="Arial,Helvetica">(<A HREF="http://www.users.globalnet.co.uk/~jg27paw4/yr08/yr08_05.htm">Hack/Poke</A>)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=YourSpectrum/Issue08/Pages/YourSpectrum0800005.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Personal Computer Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">11.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 121</FONT><TD><FONT FACE="Arial,Helvetica">(Feature - Challenge Chamber)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PersonalComputerGames/Issue12/Pages/PersonalComputerGames1200121.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Your Spectrum</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">12.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 4</FONT><TD><FONT FACE="Arial,Helvetica">(<A HREF="http://www.users.globalnet.co.uk/~jg27paw4/yr10/yr10_04.htm">Hack/Poke</A>)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=YourSpectrum/Issue10/Pages/YourSpectrum1000004.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">ZX Computing</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">12.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 107</FONT><TD><FONT FACE="Arial,Helvetica">(Review)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=ZXComputing/Issue8412/Pages/ZXComputing841200107.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Personal Computer Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">1.85</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 121</FONT><TD><FONT FACE="Arial,Helvetica">(Feature - Challenge Chamber)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PersonalComputerGames/Issue14/Pages/PersonalComputerGames1400121.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Crash</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">3.85</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 59</FONT><TD><FONT FACE="Arial,Helvetica">(Hack/Poke)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=Crash/Issue14/Pages/Crash1400059.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Your Spectrum</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">3.85</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 19</FONT><TD><FONT FACE="Arial,Helvetica">(<A HREF="http://www.users.globalnet.co.uk/~jg27paw4/yr12/yr12_17.htm">Hack/Poke</A>)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=YourSpectrum/Issue12/Pages/YourSpectrum1200019.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Your Spectrum</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">3.85</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 19</FONT><TD><FONT FACE="Arial,Helvetica">(<A HREF="http://www.users.globalnet.co.uk/~jg27paw4/yr12/yr12_17.htm">Letter</A>)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=YourSpectrum/Issue12/Pages/YourSpectrum1200019.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Home Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">6.85</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 246</FONT><TD><FONT FACE="Arial,Helvetica">(Letter)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=HomeComputingWeekly/Issue116/Pages/HomeComputingWeekly11600046.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Home Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">7.85</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 546</FONT><TD><FONT FACE="Arial,Helvetica">(Letter)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=HomeComputingWeekly/Issue123/Pages/HomeComputingWeekly12300046.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer & Video Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">9.85</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 43</FONT><TD><FONT FACE="Arial,Helvetica">(Hack/Poke)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=C+VG/Issue047/Pages/CVG04700043.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Home Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">10.85</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 238</FONT><TD><FONT FACE="Arial,Helvetica">(Letter)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=HomeComputingWeekly/Issue133/Pages/HomeComputingWeekly13300038.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer Gamer</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">11.86</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 48</FONT><TD><FONT FACE="Arial,Helvetica">(Tips)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=ComputerGamer/Issue20/Pages/ComputerGamer2000048.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Crash</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">5.87</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 70</FONT><TD><FONT FACE="Arial,Helvetica">(Hack/Poke)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=Crash/Issue40/Pages/Crash4000070.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">YS Smash Tips</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">8.88</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 9</FONT><TD><FONT FACE="Arial,Helvetica">(Hack/Poke)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=YourSinclair/SmashTips/SmashTips09.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Crash</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">10.88</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 54</FONT><TD><FONT FACE="Arial,Helvetica">(Hack/Poke)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=Crash/Issue57/Pages/Crash5700054.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Crash</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">12.88</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 44</FONT><TD><FONT FACE="Arial,Helvetica">(Hack/Poke)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=Crash/Issue59/Pages/Crash5900044.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Crash</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">7.91</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 26</FONT><TD><FONT FACE="Arial,Helvetica">(Hack/Poke)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=Crash/Issue90/Pages/Crash9000026.jpg">full page</A></I>]</FONT>
      |</TABLE><BR>
      |<FONT SIZE="+1"><B><I><U>Magazine adverts</U></I></B></FONT><BR>
      |<TABLE BORDER=0>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica"><B>Magazine</B></FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica"><B>Issue</B></FONT><TD NOWRAP><TD><FONT FACE="Arial,Helvetica"><B>Type</B></FONT><TD NOWRAP>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">ZX Computing</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">10.83</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 74</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=ZXComputing/Issue8310/Pages/ZXComputing831000074.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer & Video Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">12.83</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 30</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Full-page ad)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=C+VG/Issue026/Pages/CVG02600030.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Your Computer</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">12.83</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 245</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=YourComputer/Issue8312/Pages/YourComputer831200245.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer & Video Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">1.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 91</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Full-page ad)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=C+VG/Issue027/Pages/CVG02700091.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Home Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">1.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 404</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=HomeComputingWeekly/Issue046/Pages/HomeComputingWeekly04600004.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Your Computer</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">1.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 103</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=YourComputer/Issue8401/Pages/YourComputer840100103.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer Choice</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">2.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 35</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer & Video Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">2.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 8</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=C+VG/Issue028/Pages/CVG02800008.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Games Computing</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">2.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 57</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Home Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">2.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 314</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=HomeComputingWeekly/Issue050/Pages/HomeComputingWeekly05000014.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Your Computer</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">2.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 220</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=YourComputer/Issue8402/Pages/YourComputer840200220.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer Choice</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">3.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 54</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Crash</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">3.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 35</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=Crash/Issue02/Pages/Crash0200035.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer & Video Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">3.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 30</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=C+VG/Issue029/Pages/CVG02900030.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Games Computing</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">3.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 41</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Home Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">3.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 214</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=HomeComputingWeekly/Issue053/Pages/HomeComputingWeekly05300014.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Your Computer</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">3.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 134</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=YourComputer/Issue8403/Pages/YourComputer840300134.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer Choice</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">4.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 64</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Crash</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">4.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 87</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=Crash/Issue03/Pages/Crash0300087.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer & Video Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">4.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 88</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=C+VG/Issue030/Pages/CVG03000088.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Games Computing</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">4.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 15</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Home Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">4.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 104</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=HomeComputingWeekly/Issue056/Pages/HomeComputingWeekly05600004.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Home Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">4.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 438</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=HomeComputingWeekly/Issue059/Pages/HomeComputingWeekly05900038.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Your Computer</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">4.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 30</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=YourComputer/Issue8404/Pages/YourComputer840400030.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer Choice</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">5.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 75</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Crash</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">5.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 79</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=Crash/Issue04/Pages/Crash0400079.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer & Video Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">5.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 100</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=C+VG/Issue031/Pages/CVG03100100.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Home Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">5.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 304</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=HomeComputingWeekly/Issue062/Pages/HomeComputingWeekly06200004.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Your Computer</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">5.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 88</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=YourComputer/Issue8405/Pages/YourComputer840500088.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer Choice</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">6.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 4</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer & Video Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">6.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 40</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=C+VG/Issue032/Pages/CVG03200040.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Home Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">6.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 114</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=HomeComputingWeekly/Issue065/Pages/HomeComputingWeekly06500014.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Popular Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">6.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 245</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Full-page ad)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PopularComputingWeekly/Issue840614/Pages/PopularComputingWeekly84061400045.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Your Computer</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">6.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 32</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=YourComputer/Issue8406/Pages/YourComputer840600032.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">ZX User</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">6.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 316</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">ZX Computing</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">6.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 140</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=ZXComputing/Issue8406/Pages/ZXComputing840600140.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer & Video Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">7.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 87</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=C+VG/Issue033/Pages/CVG03300087.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Games Computing</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">7.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 38</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Home Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">7.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 104</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=HomeComputingWeekly/Issue069/Pages/HomeComputingWeekly06900004.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Home Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">7.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 311</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=HomeComputingWeekly/Issue071/Pages/HomeComputingWeekly07100011.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Personal Computer Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">7.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 113</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Full-page ad)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PersonalComputerGames/Issue08/Pages/PersonalComputerGames0800113.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Popular Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">7.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 230</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Full-page ad)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PopularComputingWeekly/Issue840712/Pages/PopularComputingWeekly84071200030.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Popular Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">7.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 427</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Full-page ad)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PopularComputingWeekly/Issue840726/Pages/PopularComputingWeekly84072600027.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer & Video Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">8.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 35</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=C+VG/Issue034/Pages/CVG03400035.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Personal Computer Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">8.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 87</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Full-page ad)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PersonalComputerGames/Issue09/Pages/PersonalComputerGames0900087.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Popular Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">8.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 216</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PopularComputingWeekly/Issue840809/Pages/PopularComputingWeekly84080900016.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Popular Computing Weekly</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">8.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 430</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Full-page ad)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PopularComputingWeekly/Issue840823/Pages/PopularComputingWeekly84082300030.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer & Video Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">9.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 88</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=C+VG/Issue035/Pages/CVG03500088.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Games Computing</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">9.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 9</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Full-page ad)</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Personal Computer Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">9.84</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 87</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Full-page ad)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=PersonalComputerGames/Issue10/Pages/PersonalComputerGames1000087.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">Computer & Video Games</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">7.85</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 95</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert)</FONT>
      |<TD NOWRAP><FONT FACE="Arial,Helvetica" SIZE="-1">[<I><A HREF="/showmag.cgi?mag=C+VG/Issue045/Pages/CVG04500095.jpg">full page</A></I>]</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">New Computer Express</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">11.88</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 245</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert - not a Spectrum advert)</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">New Computer Express</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">12.88</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 110</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert - not a Spectrum advert)</FONT>
      |<TR><TD NOWRAP><FONT FACE="Arial,Helvetica">New Computer Express</FONT><TD NOWRAP ALIGN=RIGHT><FONT FACE="Arial,Helvetica">12.88</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">page 428</FONT><TD NOWRAP><FONT FACE="Arial,Helvetica">(Advert - not a Spectrum advert)</FONT>
      |</TABLE><BR>
      |<BR>
      |
      |<BR><CENTER><IMG SRC="pics/zx-hline.gif" WIDTH=540 HEIGHT=8 ALT=""></CENTER>
      |<CENTER>
      |  <FONT SIZE="-1"><I>
      |  Sinclair Infoseek (v13.1) is written by Martijn van der Heide and &copy; 1999-2014 ThunderWare Research Center
      |  </I></FONT><BR>
      |  <FONT SIZE="-1"><A HREF="/archivenote.html" TITLE="Acceptable Use Policy">Acceptable Use Policy</A></FONT><BR>
      |  <FONT SIZE="-2">Generated in 0.049 seconds</FONT>
      |  <BR><TABLE CELLPADDING=5>
      |  <TR>
      |    <TD><A HREF="http://www.worldofspectrum.org/email.html"><IMG SRC="pics/m-mail.gif" WIDTH=80 HEIGHT=40 BORDER=0 ALT="[Mail]"></A>
      |    <TD><A HREF="http://www.worldofspectrum.org/index.html"><IMG SRC="pics/m-index.gif" WIDTH=80 HEIGHT=40 BORDER=0 ALT="[Index]"></A>
      |  </TABLE>
      |</CENTER>
      |</FONT>
      |</BODY>
      |</HTML>
      |
    """.stripMargin
}