import S3._

s3Settings

val plusfDependencies = Seq(
  "org.easytesting" % "fest-swing" % "1.2.1" % "test",
  "com.github.tomakehurst" % "wiremock" % "2.5.1" % "test"
)

lazy val pkgName = "Plus-F"

name := pkgName

mainClass in Compile := Some("com.socialthingy.plusf.spectrum.ui.PlusF")
packageName in Universal := pkgName
maintainer in Linux := "Alan Gibson <alangibson27@gmail.com>"
packageSummary in Linux := "ZX Spectrum Emulator with Network Play Capability"
packageDescription in Linux := "ZX Spectrum Emulator with Network Play Capability"

rpmVendor := "socialthingy.com"
rpmUrl := Some("http://plus-f.socialthingy.com")
rpmLicense := Some("MIT")

jdkPackagerBasename := pkgName
jdkPackagerType := "exe"
jdkPackagerToolkit := SwingToolkit

linuxPackageMappings ++= {
  val desktopFile = baseDirectory.value / "src" / "main" / "usr" / "share" / "applications" / "plus-f" / "plus-f.desktop"
  val iconFile = baseDirectory.value / "src" / "main" / "resources" / "icons" / "plus-f.png"

  Seq(
    packageMapping(desktopFile -> "/usr/share/applications/plus-f.desktop"),
    packageMapping(iconFile -> "/usr/share/plus-f/bin/plus-f.png")
  )
}

parallelExecution in Test := false

mappings in upload := {
  (target.value ** ("*.zip" || "*.exe" || "*.deb")).get.map{x => println(x); x}.map {
    case exe if exe.getName.endsWith("exe") => (exe, "Plus-F.exe")
    case deb if deb.getName.endsWith("deb") => (deb, "Plus-F.deb")
    case zip if zip.getName.endsWith("zip") => (zip, "Plus-F.zip")
  }
}

host in upload := "download.socialthingy.com.s3.amazonaws.com"

progress in upload := true

libraryDependencies ++= plusfDependencies

lazy val buildZip = taskKey[File]("creates universal zip file")
lazy val buildDebian = taskKey[File]("creates Debian file")
lazy val buildWindows = taskKey[File]("creates Windows installer")

buildZip := (packageBin in Universal).value
buildWindows := (packageBin in JDKPackager).value
buildDebian := (packageBin in Debian).value
