import S3._

s3Settings

val testDependencies = Seq(
  "org.easytesting" % "fest-swing" % "1.2.1" % "test"
)

lazy val pkgName = "Plus-F"

name := pkgName

packageName in Universal := pkgName

jdkPackagerBasename := pkgName 

jdkAppIcon := Some(file("src/main/assembly/plus-f.ico"))

jdkPackagerType := "exe"

jdkPackagerToolkit := SwingToolkit

parallelExecution in Test := false

mappings in upload := Seq(
  (target.value / "universal" / s"$pkgName.zip", s"$pkgName.zip"),
  (target.value / "universal" / "jdkpackager" / "bundles" / s"$pkgName-${version.value}.exe", s"$pkgName.exe")
).map{i => println(i); i}.filter(_._1.exists())

host in upload := "download.socialthingy.com.s3.amazonaws.com"

progress in upload := true

libraryDependencies ++= testDependencies
