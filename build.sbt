lazy val commonDependencies = Seq(
	"io.dropwizard.metrics" % "metrics-core" % "3.1.2",
	"com.typesafe" % "config" % "1.3.0",
	"com.typesafe.akka" %% "akka-actor" % "2.4.12",
	"net.jpountz.lz4" % "lz4" % "1.3.0",
	"org.scalatest" %% "scalatest" % "3.0.0" % "test",
	"org.mockito" % "mockito-all" % "1.10.19" % "test"
)

lazy val commonSettings = Seq(
	scalaVersion := "2.12.0",
	organization := "com.socialthingy",
	version := "1.1",
	libraryDependencies ++= commonDependencies
)

lazy val `plus-f` = project.in(file("plus-f"))
	.enablePlugins(JavaAppPackaging, UniversalPlugin, JDKPackagerPlugin)
	.settings(commonSettings)
	.dependsOn(discovery)

lazy val discovery = project.in(file("discovery"))
	.enablePlugins(UniversalPlugin, JavaAppPackaging)
	.settings(commonSettings)

addCommandAlias("dist-universal", ";clean;test;plus-f/universal:packageBin;plus-f/s3-upload")

addCommandAlias("dist-windows", ";clean;test;plus-f/jdkPackager:packageBin;plus-f/s3-upload")

