val mainDependencies = Seq(
  "com.socialthingy" %% "plusf-discovery" % "1.0.1",
  "io.dropwizard.metrics" % "metrics-core" % "3.1.2",
  "com.typesafe" % "config" % "1.3.0",
  "com.typesafe.akka" %% "akka-actor" % "2.4.12",
  "net.jpountz.lz4" % "lz4" % "1.3.0"
)

val testDependencies = Seq(
  "org.scalatest" %% "scalatest" % "3.0.0" % "test",
  "org.mockito" % "mockito-all" % "1.10.19" % "test",
  "org.easytesting" % "fest-swing" % "1.2.1" % "test"
)

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, UniversalPlugin)
  .settings(
    scalaVersion := "2.12.0",
    organization := "com.socialthingy",
    version := "1.0",
    name := "plus-f",
    libraryDependencies ++= mainDependencies,
    libraryDependencies ++= testDependencies
  )
