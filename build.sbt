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

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

val Scala213 = "2.13.8"
ThisBuild / crossScalaVersions := Seq(Scala213, "3.1.3")
ThisBuild / scalaVersion := Scala213 // the default Scala

lazy val root = tlCrossRootProject
  .aggregate(core, testkit, java)
  .settings(name := "otel4s")

lazy val core = crossProject(JVMPlatform, JSPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "otel4s-core",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % "2.8.0",
      "org.typelevel" %%% "cats-effect" % "3.3.13",
      "org.scalameta" %%% "munit" % "0.7.29" % Test,
      "org.typelevel" %%% "munit-cats-effect-3" % "1.0.7" % Test,
      "org.typelevel" %%% "cats-effect-testkit" % "3.3.13" % Test
    ),
    libraryDependencies ++= {
      if (tlIsScala3.value) Nil
      else
        Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided)
    }
  )

lazy val testkit = crossProject(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("testkit"))
  .settings(
    name := "otel4s-testkit"
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-api" % "1.15.0",
      "io.opentelemetry" % "opentelemetry-sdk" % "1.15.0",
      "io.opentelemetry" % "opentelemetry-sdk-testing" % "1.15.0"
    )
  )
  .dependsOn(core)

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
  .dependsOn(core, testkit)

lazy val docs = project.in(file("site")).enablePlugins(TypelevelSitePlugin)
