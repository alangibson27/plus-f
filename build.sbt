lazy val commonDependencies = Seq(
	"io.dropwizard.metrics" % "metrics-core" % "3.1.2",
	"com.typesafe" % "config" % "1.3.0",
	"ch.qos.logback" % "logback-classic" % "1.1.3",
	"net.jpountz.lz4" % "lz4" % "1.3.0",
	"org.codehaus.janino" % "janino" % "2.7.8",
	"org.scalatest" %% "scalatest" % "3.0.0" % "test",
	"org.mockito" % "mockito-all" % "1.10.19" % "test"
)

lazy val commonSettings = Seq(
	scalaVersion := "2.12.0",
	organization := "com.socialthingy",
	version := "1.5.0",
	libraryDependencies ++= commonDependencies
)

lazy val `plus-f` = project.in(file("plus-f"))
	.enablePlugins(JavaAppPackaging, UniversalPlugin, JDKPackagerPlugin)
	.settings(commonSettings)

lazy val discovery = project.in(file("discovery"))
	.enablePlugins(UniversalPlugin, JavaAppPackaging)
	.settings(commonSettings)
	.dependsOn(`plus-f`)

addCommandAlias("dist-non-windows", ";clean;test;plus-f/buildZip;plus-f/buildDebian;plus-f/s3-upload")

addCommandAlias("dist-windows", ";clean;test;plus-f/buildWindows;plus-f/s3-upload")

