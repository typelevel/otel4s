import com.typesafe.tools.mima.core._

ThisBuild / tlBaseVersion := "0.2"

ThisBuild / organization := "org.typelevel"
ThisBuild / organizationName := "Typelevel"
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("rossabaker", "Ross A. Baker")
)
ThisBuild / startYear := Some(2022)

// publish to s01.oss.sonatype.org (set to true to publish to oss.sonatype.org instead)
ThisBuild / tlSonatypeUseLegacyHost := false

// publish website from this branch
ThisBuild / tlSitePublishBranch := Some("main")

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"

val Scala213 = "2.13.10"
ThisBuild / crossScalaVersions := Seq(Scala213, "3.2.2")
ThisBuild / scalaVersion := Scala213 // the default Scala

val CatsVersion = "2.9.0"
val CatsEffectVersion = "3.4.5"
val CatsMtlVersion = "1.3.0"
val FS2Version = "3.6.1"
val MUnitVersion = "1.0.0-M7"
val MUnitCatsEffectVersion = "2.0.0-M3"
val OpenTelemetryVersion = "1.23.0"
val ScodecVersion = "1.1.35"
val VaultVersion = "3.5.0"

lazy val scalaReflectDependency = Def.settings(
  libraryDependencies ++= {
    if (tlIsScala3.value) Nil
    else Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided)
  }
)

lazy val munitDependencies = Def.settings(
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % MUnitVersion % Test,
    "org.typelevel" %%% "munit-cats-effect" % MUnitCatsEffectVersion % Test
  )
)

lazy val root = tlCrossRootProject
  .aggregate(
    `core-common`,
    `core-metrics`,
    `core-trace`,
    core,
    `testkit-common`,
    `testkit-metrics`,
    testkit,
    `java-common`,
    `java-metrics`,
    `java-trace`,
    java,
    benchmarks,
    examples
  )
  .settings(name := "otel4s")

lazy val `core-common` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core/common"))
  .settings(
    name := "otel4s-core-common",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % CatsVersion,
      "org.typelevel" %%% "vault" % VaultVersion,
      "org.scalameta" %%% "munit" % MUnitVersion % Test
    )
  )

lazy val `core-metrics` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core/metrics"))
  .dependsOn(`core-common`)
  .settings(scalaReflectDependency)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-core-metrics",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect-kernel" % CatsEffectVersion,
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion % Test
    )
  )

lazy val `core-trace` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core/trace"))
  .dependsOn(`core-common`)
  .settings(scalaReflectDependency)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-core-trace",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect-kernel" % CatsEffectVersion,
      "org.scodec" %%% "scodec-bits" % ScodecVersion
    )
  )

lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core/all"))
  .dependsOn(`core-common`, `core-metrics`, `core-trace`)
  .settings(
    name := "otel4s-core"
  )

lazy val `testkit-common` = crossProject(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("testkit/common"))
  .dependsOn(`core-common`)
  .settings(
    name := "otel4s-testkit-common"
  )

lazy val `testkit-metrics` = crossProject(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("testkit/metrics"))
  .dependsOn(`testkit-common`, `core-metrics`)
  .settings(
    name := "otel4s-testkit-metrics"
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-api" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion
    )
  )

lazy val testkit = crossProject(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("testkit/all"))
  .dependsOn(`testkit-common`, `testkit-metrics`)
  .settings(
    name := "otel4s-testkit"
  )

lazy val `java-common` = project
  .in(file("java/common"))
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(`core-common`.jvm, `testkit-common`.jvm)
  .settings(
    name := "otel4s-java-common",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect-kernel" % CatsEffectVersion,
      "org.typelevel" %%% "cats-mtl" % CatsMtlVersion,
      "io.opentelemetry" % "opentelemetry-sdk" % OpenTelemetryVersion,
      "org.scalameta" %%% "munit" % MUnitVersion % Test
    ),
    buildInfoPackage := "org.typelevel.otel4s.java",
    buildInfoOptions += sbtbuildinfo.BuildInfoOption.PackagePrivate,
    buildInfoKeys := Seq[BuildInfoKey](
      "openTelemetrySdkVersion" -> OpenTelemetryVersion
    )
  )

lazy val `java-metrics` = project
  .in(file("java/metrics"))
  .dependsOn(`java-common`, `core-metrics`.jvm, `testkit-metrics`.jvm)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-java-metrics",
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion % Test
    )
  )

lazy val `java-trace` = project
  .in(file("java/trace"))
  .dependsOn(`java-common`, `core-trace`.jvm)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-java-trace",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % CatsEffectVersion,
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion % Test,
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion % Test,
      "co.fs2" %% "fs2-core" % FS2Version % Test
    ),
    mimaBinaryIssueFilters ++= Seq(
      ProblemFilters.exclude[MissingClassProblem](
        "org.typelevel.otel4s.java.trace.SpanBuilderImpl$Runner"
      ),
      ProblemFilters.exclude[MissingClassProblem](
        "org.typelevel.otel4s.java.trace.SpanBuilderImpl$Runner$"
      ),
      ProblemFilters.exclude[MissingClassProblem](
        "org.typelevel.otel4s.java.trace.SpanBuilderImpl$TimestampSelect"
      ),
      ProblemFilters.exclude[MissingClassProblem](
        "org.typelevel.otel4s.java.trace.SpanBuilderImpl$TimestampSelect$"
      ),
      ProblemFilters.exclude[MissingClassProblem](
        "org.typelevel.otel4s.java.trace.SpanBuilderImpl$TimestampSelect$Delegate$"
      ),
      ProblemFilters.exclude[MissingClassProblem](
        "org.typelevel.otel4s.java.trace.SpanBuilderImpl$TimestampSelect$Explicit$"
      )
    )
  )

lazy val java = project
  .in(file("java/all"))
  .dependsOn(core.jvm, `java-metrics`, `java-trace`)
  .settings(
    name := "otel4s-java"
  )

lazy val benchmarks = project
  .enablePlugins(NoPublishPlugin)
  .enablePlugins(JmhPlugin)
  .in(file("benchmarks"))
  .dependsOn(core.jvm, java)
  .settings(
    name := "otel4s-benchmarks"
  )

lazy val examples = project
  .enablePlugins(NoPublishPlugin)
  .in(file("examples"))
  .dependsOn(core.jvm, java)
  .settings(
    name := "otel4s-examples",
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-exporter-otlp" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % s"${OpenTelemetryVersion}-alpha"
    ),
    run / fork := true,
    javaOptions += "-Dotel.java.global-autoconfigure.enabled=true"
  )

lazy val docs = project
  .in(file("site"))
  .enablePlugins(TypelevelSitePlugin)
  .dependsOn(java)
  .settings(
    mdocVariables := Map(
      "VERSION" -> version.value,
      "OPEN_TELEMETRY_VERSION" -> OpenTelemetryVersion
    ),
    laikaConfig := {
      import laika.rewrite.nav.{ChoiceConfig, Selections, SelectionConfig}

      laikaConfig.value.withConfigValue(
        Selections(
          SelectionConfig(
            "build-tool",
            ChoiceConfig("sbt", "sbt"),
            ChoiceConfig("scala-cli", "Scala CLI")
          ).withSeparateEbooks
        )
      )
    }
  )
