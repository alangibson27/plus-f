val testDependencies = Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.12",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.12" % "test"
)

libraryDependencies ++= testDependencies

mainClass in Compile := Some("com.socialthingy.plusf.p2p.discovery.DiscoveryService")
