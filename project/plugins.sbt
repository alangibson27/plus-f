addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.3.2")

resolvers += Resolver.url("sbts3 ivy resolver", url("http://dl.bintray.com/emersonloureiro/sbt-plugins"))(Resolver.ivyStylePatterns)

addSbtPlugin("cf.janga" % "sbts3" % "0.10")