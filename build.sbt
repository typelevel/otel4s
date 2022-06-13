// https://typelevel.org/sbt-typelevel/faq.html#what-is-a-base-version-anyway
ThisBuild / tlBaseVersion := "0.0" // your current series x.y

ThisBuild / organization := "org.typelevel"
ThisBuild / organizationName := "Typelevel"
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("rossabaker", "Ross A. Baker")
)

// publish to s01.oss.sonatype.org (set to true to publish to oss.sonatype.org instead)
ThisBuild / tlSonatypeUseLegacyHost := false

// publish website from this branch
ThisBuild / tlSitePublishBranch := Some("main")

val Scala213 = "2.13.8"
ThisBuild / crossScalaVersions := Seq(Scala213, "3.1.2")
ThisBuild / scalaVersion := Scala213 // the default Scala

lazy val root = tlCrossRootProject
  .aggregate(core, java)
  .settings(name := "otel4s")

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "otel4s-core",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.7.0",
      "org.typelevel" %%% "cats-effect" % "3.3.12",
      "org.scalameta" %%% "munit" % "0.7.29" % Test,
      "org.typelevel" %%% "munit-cats-effect-3" % "1.0.7" % Test
    )
  )

lazy val java = crossProject(JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("java"))
  .settings(
    name := "otel4s-java",
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-api" % "1.15.0",
      "io.opentelemetry" % "opentelemetry-sdk" % "1.15.0" % Test,
      "io.opentelemetry" % "opentelemetry-sdk-testing" % "1.15.0" % Test,
      "org.scalameta" %% "munit" % "0.7.29" % Test,
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.7" % Test
    )
  )
  .dependsOn(core % "compile->compile,test->test")

lazy val docs = project.in(file("site")).enablePlugins(TypelevelSitePlugin)
