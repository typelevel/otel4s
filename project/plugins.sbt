addSbtPlugin("org.typelevel" % "sbt-typelevel" % "0.8.0")
addSbtPlugin("org.typelevel" % "sbt-typelevel-mergify" % "0.8.0")
addSbtPlugin("org.typelevel" % "sbt-typelevel-scalafix" % "0.8.0")
addSbtPlugin("org.typelevel" % "sbt-typelevel-site" % "0.8.0")
addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.20.1")
addSbtPlugin("org.scala-native" % "sbt-scala-native" % "0.5.8")
addSbtPlugin("org.portable-scala" % "sbt-scala-native-crossproject" % "1.3.2")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.13.1")
addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.4.7")
addSbtPlugin("com.github.sbt" % "sbt-javaagent" % "0.1.8")
addSbtPlugin("com.thesamet" % "sbt-protoc" % "1.0.8")
addSbtPlugin("com.armanbilge" % "sbt-scala-native-config-brew-github-actions" % "0.4.0")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.11.20"
