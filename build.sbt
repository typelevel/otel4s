import com.typesafe.tools.mima.core._

ThisBuild / tlBaseVersion := "0.3"

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

lazy val scalafixSettings = Seq(
  semanticdbOptions ++= Seq("-P:semanticdb:synthetics:on").filter(_ =>
    !tlIsScala3.value
  )
)

val Scala213 = "2.13.11"
ThisBuild / crossScalaVersions := Seq(Scala213, "3.3.0")
ThisBuild / scalaVersion := Scala213 // the default Scala

val CatsVersion = "2.10.0"
val CatsEffectVersion = "3.5.1"
val CatsMtlVersion = "1.3.1"
val DisciplineMUnitVersion = "2.0.0-M3"
val FS2Version = "3.9.0"
val MUnitVersion = "1.0.0-M8"
val MUnitCatsEffectVersion = "2.0.0-M3"
val MUnitDisciplineVersion = "2.0.0-M3"
val OpenTelemetryVersion = "1.30.1"
val PlatformVersion = "1.0.2"
val ScodecVersion = "1.1.37"
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
    `sdk-common`,
    `testkit-common`,
    `testkit-metrics`,
    testkit,
    `java-common`,
    `java-metrics`,
    `java-trace`,
    java,
    semconv,
    benchmarks,
    examples,
    unidocs
  )
  .settings(name := "otel4s")

lazy val `core-common` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core/common"))
  .settings(
    name := "otel4s-core-common",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % CatsVersion,
      "org.typelevel" %%% "cats-mtl" % CatsMtlVersion,
      "org.typelevel" %%% "vault" % VaultVersion,
      "org.typelevel" %%% "cats-laws" % CatsVersion % Test,
      "org.typelevel" %%% "discipline-munit" % DisciplineMUnitVersion % Test,
      "org.scalameta" %%% "munit" % MUnitVersion % Test,
      "org.scalameta" %%% "munit-scalacheck" % MUnitVersion % Test,
      "lgbt.princess" %%% "platform" % PlatformVersion % Test
    )
  )
  .settings(scalafixSettings)

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
  .settings(scalafixSettings)

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
  .settings(scalafixSettings)

lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core/all"))
  .dependsOn(`core-common`, `core-metrics`, `core-trace`)
  .settings(
    name := "otel4s-core"
  )
  .settings(scalafixSettings)

lazy val `sdk-common` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(NoPublishPlugin)
  .in(file("sdk/common"))
  .dependsOn(`core-common`, semconv)
  .settings(
    name := "otel4s-sdk-common",
    startYear := Some(2023),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect-kernel" % CatsEffectVersion,
      "org.typelevel" %%% "cats-mtl" % CatsMtlVersion,
      "org.typelevel" %%% "cats-laws" % CatsVersion % Test,
      "org.typelevel" %%% "discipline-munit" % DisciplineMUnitVersion % Test,
      "org.scalameta" %%% "munit" % MUnitVersion % Test,
      "org.scalameta" %%% "munit-scalacheck" % MUnitVersion % Test,
    ),
    buildInfoPackage := "org.typelevel.otel4s.sdk",
    buildInfoOptions += sbtbuildinfo.BuildInfoOption.PackagePrivate,
    buildInfoKeys := Seq[BuildInfoKey](
      version
    )
  )
  .settings(munitDependencies)
  .settings(scalafixSettings)

lazy val `testkit-common` = crossProject(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("testkit/common"))
  .dependsOn(`core-common`)
  .settings(
    name := "otel4s-testkit-common"
  )
  .settings(scalafixSettings)

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
  .settings(scalafixSettings)

lazy val testkit = crossProject(JVMPlatform)
  .crossType(CrossType.Full)
  .in(file("testkit/all"))
  .dependsOn(`testkit-common`, `testkit-metrics`)
  .settings(
    name := "otel4s-testkit"
  )
  .settings(scalafixSettings)

lazy val `java-common` = project
  .in(file("java/common"))
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(`core-common`.jvm, `testkit-common`.jvm % Test)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-java-common",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect-kernel" % CatsEffectVersion,
      "org.typelevel" %%% "cats-mtl" % CatsMtlVersion,
      "io.opentelemetry" % "opentelemetry-sdk" % OpenTelemetryVersion,
      "org.typelevel" %%% "discipline-munit" % MUnitDisciplineVersion % Test,
      "org.typelevel" %%% "cats-mtl-laws" % CatsMtlVersion % Test,
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion % Test
    ),
    buildInfoPackage := "org.typelevel.otel4s.java",
    buildInfoOptions += sbtbuildinfo.BuildInfoOption.PackagePrivate,
    buildInfoKeys := Seq[BuildInfoKey](
      "openTelemetrySdkVersion" -> OpenTelemetryVersion
    )
  )
  .settings(scalafixSettings)

lazy val `java-metrics` = project
  .in(file("java/metrics"))
  .dependsOn(`java-common`, `core-metrics`.jvm, `testkit-metrics`.jvm % Test)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-java-metrics",
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion % Test
    )
  )
  .settings(scalafixSettings)

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
  .settings(scalafixSettings)

lazy val java = project
  .in(file("java/all"))
  .dependsOn(core.jvm, `java-metrics`, `java-trace`)
  .settings(
    name := "otel4s-java",
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion % Test
    )
  )
  .settings(munitDependencies)
  .settings(scalafixSettings)

lazy val semconv = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("semconv"))
  .dependsOn(`core-common`)
  .settings(
    name := "otel4s-semconv",
    startYear := Some(2023),
  )
  .settings(scalafixSettings)

lazy val benchmarks = project
  .enablePlugins(NoPublishPlugin)
  .enablePlugins(JmhPlugin)
  .in(file("benchmarks"))
  .dependsOn(core.jvm, java, testkit.jvm)
  .settings(
    name := "otel4s-benchmarks"
  )
  .settings(scalafixSettings)

lazy val examples = project
  .enablePlugins(NoPublishPlugin)
  .in(file("examples"))
  .dependsOn(core.jvm, java)
  .settings(
    name := "otel4s-examples",
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-exporter-otlp" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-extension-trace-propagators" % OpenTelemetryVersion % Runtime
    ),
    run / fork := true,
    javaOptions += "-Dotel.java.global-autoconfigure.enabled=true",
    envVars ++= Map(
      "OTEL_PROPAGATORS" -> "b3multi",
      "OTEL_SERVICE_NAME" -> "Trace Example"
    )
  )
  .settings(scalafixSettings)

lazy val docs = project
  .in(file("site"))
  .enablePlugins(TypelevelSitePlugin)
  .dependsOn(java)
  .settings(
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % OpenTelemetryVersion
    ),
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

lazy val unidocs = project
  .in(file("unidocs"))
  .enablePlugins(TypelevelUnidocPlugin)
  .settings(
    name := "otel4s-docs",
    ScalaUnidoc / unidoc / unidocProjectFilter := inProjects(
      `core-common`.jvm,
      `core-metrics`.jvm,
      `core-trace`.jvm,
      core.jvm,
      `sdk-common`.jvm,
      `testkit-common`.jvm,
      `testkit-metrics`.jvm,
      testkit.jvm,
      `java-common`,
      `java-metrics`,
      `java-trace`,
      java,
      semconv.jvm
    )
  )
