import com.typesafe.tools.mima.core._

ThisBuild / tlBaseVersion := "0.15"

ThisBuild / organization := "org.typelevel"
ThisBuild / organizationName := "Typelevel"
ThisBuild / licenses := Seq(License.Apache2)
ThisBuild / developers := List(
  // your GitHub handle and name
  tlGitHubDev("rossabaker", "Ross A. Baker")
)
ThisBuild / startYear := Some(2022)

// publish website from this branch
ThisBuild / tlSitePublishBranch := Some("main")

Global / tlCommandAliases ++= Map(
  "generateSemanticConventions" -> List(
    "root/semanticConventionsGenerate",
    "headerCreateAll",
    "scalafixAll",
    "scalafmtAll"
  )
)

lazy val scalaJSLinkerSettings = Def.settings(
  scalaJSLinkerConfig ~= (_.withESFeatures(
    _.withESVersion(org.scalajs.linker.interface.ESVersion.ES2018)
  )),
  Test / scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
)

val Scala212 = "2.12.21"
val Scala213 = "2.13.18"
ThisBuild / crossScalaVersions := Seq(Scala213, "3.3.7")
ThisBuild / scalaVersion := Scala213 // the default Scala

ThisBuild / mergifyStewardConfig := None
ThisBuild / mergifyLabelPaths := Map(
  "module:core" -> file("core"),
  "module:oteljava" -> file("oteljava"),
  "module:semconv" -> file("semconv"),
  "documentation" -> file("docs")
)

ThisBuild / mergifyPrRules ++= Seq(
  MergifyPrRule(
    "Label metrics PRs",
    List(MergifyCondition.Custom("files~=/(metrics)/")),
    List(MergifyAction.Label(add = List("metrics")))
  ),
  MergifyPrRule(
    "Label trace PRs",
    List(MergifyCondition.Custom("files~=/(trace)/")),
    List(MergifyAction.Label(add = List("tracing")))
  ),
  MergifyPrRule(
    "Label logs PRs",
    List(MergifyCondition.Custom("files~=/(logs)/")),
    List(MergifyAction.Label(add = List("logs")))
  ),
  MergifyPrRule(
    "Label Scala Steward PRs",
    List(MergifyCondition.Custom("author=typelevel-steward[bot]")),
    List(MergifyAction.Label(add = List("dependencies")))
  )
)

val CatsVersion = "2.11.0"
val CatsEffectVersion = "3.6.3"
val CatsMtlVersion = "1.4.0"
val FS2Version = "3.12.2"
val MUnitVersion = "1.0.0"
val MUnitScalaCheckVersion = "1.0.0-M11"
val MUnitCatsEffectVersion = "2.1.0"
val MUnitDisciplineVersion = "2.0.0-M3"
val MUnitScalaCheckEffectVersion = "2.0.0-M2"
val OpenTelemetryVersion = "1.58.0"
val OpenTelemetryAlphaVersion = s"$OpenTelemetryVersion-alpha"
val OpenTelemetryInstrumentationVersion = "2.24.0"
val OpenTelemetryInstrumentationAlphaVersion = s"$OpenTelemetryInstrumentationVersion-alpha"
val OpenTelemetrySemConvVersion = "1.37.0"
val OpenTelemetrySemConvAlphaVersion = s"$OpenTelemetrySemConvVersion-alpha"
val Otel4sAgentVersion = "2.22.0"
val PekkoStreamVersion = "1.4.0"
val PekkoHttpVersion = "1.3.0"
val PlatformVersion = "1.0.2"
val ScodecVersion = "1.1.38"
val VaultVersion = "3.6.0"
val Http4sVersion = "0.23.33"
val ScribeVersion = "3.17.0"

lazy val scalaReflectDependency = Def.settings(
  libraryDependencies ++= {
    if (tlIsScala3.value) Nil
    else Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided)
  }
)

lazy val munitDependencies = Def.settings(
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % MUnitVersion % Test,
    "org.scalameta" %%% "munit-scalacheck" % MUnitScalaCheckVersion % Test,
    "org.typelevel" %%% "munit-cats-effect" % MUnitCatsEffectVersion % Test
  )
)

lazy val semanticConventionsGenerate = taskKey[Unit]("Generate semantic conventions")
semanticConventionsGenerate := {
  SemanticConventionsGenerator.generate(
    OpenTelemetrySemConvVersion.stripSuffix("-alpha"),
    baseDirectory.value
  )
}

lazy val root = tlCrossRootProject
  .aggregate(
    `core-common`,
    `core-logs`,
    `core-metrics`,
    `core-trace`,
    core,
    `instrumentation-metrics`,
    `oteljava-common`,
    `oteljava-common-testkit`,
    `oteljava-logs`,
    `oteljava-logs-testkit`,
    `oteljava-metrics`,
    `oteljava-metrics-testkit`,
    `oteljava-trace`,
    `oteljava-trace-testkit`,
    `oteljava-testkit`,
    `oteljava-context-storage`,
    `oteljava-context-storage-testkit`,
    oteljava,
    `semconv-stable`,
    `semconv-experimental`,
    `semconv-metrics-stable`,
    `semconv-metrics-experimental`,
    benchmarks,
    examples,
    unidocs
  )
  .configureRoot(
    _.aggregate(scalafix.componentProjectReferences *)
  )
  .settings(name := "otel4s")

//
// Core
//

lazy val `core-common` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core/common"))
  .settings(munitDependencies)
  .settings(
    name := "otel4s-core-common",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % CatsVersion,
      "org.typelevel" %%% "cats-effect" % CatsEffectVersion,
      "org.typelevel" %%% "cats-mtl" % CatsMtlVersion,
      "org.scodec" %%% "scodec-bits" % ScodecVersion,
      "org.typelevel" %%% "vault" % VaultVersion % Test,
      "org.typelevel" %%% "cats-laws" % CatsVersion % Test,
      "org.typelevel" %%% "cats-mtl-laws" % CatsMtlVersion % Test,
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion % Test,
      "org.typelevel" %%% "discipline-munit" % MUnitDisciplineVersion % Test,
      "lgbt.princess" %%% "platform" % PlatformVersion % Test
    )
  )

lazy val `core-logs` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core/logs"))
  .dependsOn(`core-common`)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-core-logs",
    startYear := Some(2025),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-laws" % CatsVersion % Test,
      "org.typelevel" %%% "discipline-munit" % MUnitDisciplineVersion % Test
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
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion % Test,
      "org.typelevel" %%% "cats-laws" % CatsVersion % Test,
      "org.typelevel" %%% "discipline-munit" % MUnitDisciplineVersion % Test
    )
  )

lazy val `core-trace` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core/trace"))
  .dependsOn(`core-common` % "compile->compile;test->test")
  .settings(scalaReflectDependency)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-core-trace",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect-kernel" % CatsEffectVersion,
      "org.scodec" %%% "scodec-bits" % ScodecVersion,
      "org.typelevel" %%% "cats-laws" % CatsVersion % Test,
      "org.typelevel" %%% "discipline-munit" % MUnitDisciplineVersion % Test,
      "org.typelevel" %%% "scalacheck-effect-munit" % MUnitScalaCheckEffectVersion % Test,
    ),
    mimaBinaryIssueFilters ++= Seq(
      // # 1002
      // all of these classes are private (and not serializable), so this is safe
      ProblemFilters.exclude[MissingClassProblem]("org.typelevel.otel4s.trace.SpanBuilder$MappedK"),
      ProblemFilters.exclude[MissingClassProblem]("org.typelevel.otel4s.trace.SpanOps$MappedK"),
      ProblemFilters.exclude[MissingClassProblem]("org.typelevel.otel4s.trace.Tracer$MappedK"),
      ProblemFilters.exclude[MissingClassProblem]("org.typelevel.otel4s.trace.TracerBuilder$MappedK"),
      ProblemFilters.exclude[MissingClassProblem]("org.typelevel.otel4s.trace.TracerProvider$MappedK"),
    ),
  )

lazy val core = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core/all"))
  .dependsOn(`core-common`, `core-logs`, `core-metrics`, `core-trace`)
  .settings(
    name := "otel4s-core"
  )

//
// Instrumentation
//

lazy val `instrumentation-metrics` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("instrumentation/metrics"))
  .dependsOn(`core-metrics`, `core-common` % "test->test")
  .settings(munitDependencies)
  .settings(
    name := "otel4s-instrumentation-metrics",
    startYear := Some(2024),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "scalacheck-effect-munit" % MUnitScalaCheckEffectVersion % Test
    )
  )
  .jvmConfigure(_.dependsOn(`oteljava-metrics-testkit` % Test))

//
// OpenTelemetry Java
//

lazy val `oteljava-common` = project
  .in(file("oteljava/common"))
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(`core-common`.jvm % "compile->compile;test->test")
  .settings(munitDependencies)
  .settings(
    name := "otel4s-oteljava-common",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % CatsEffectVersion,
      "org.typelevel" %%% "cats-mtl" % CatsMtlVersion,
      "io.opentelemetry" % "opentelemetry-api" % OpenTelemetryVersion,
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion % Test,
      "io.opentelemetry" % "opentelemetry-api-incubator" % OpenTelemetryAlphaVersion % Test,
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion % Test,
    ),
    buildInfoPackage := "org.typelevel.otel4s.oteljava",
    buildInfoOptions += sbtbuildinfo.BuildInfoOption.PackagePrivate,
    buildInfoKeys := Seq[BuildInfoKey](
      "openTelemetrySdkVersion" -> OpenTelemetryVersion
    )
  )

lazy val `oteljava-common-testkit` = project
  .in(file("oteljava/common-testkit"))
  .dependsOn(`oteljava-common`)
  .settings(
    name := "otel4s-oteljava-common-testkit",
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion
    ),
    startYear := Some(2024)
  )

lazy val `oteljava-logs` = project
  .in(file("oteljava/logs"))
  .dependsOn(
    `oteljava-common` % "compile->compile;test->test",
    `core-logs`.jvm % "compile->compile;test->test"
  )
  .settings(munitDependencies)
  .settings(
    name := "otel4s-oteljava-logs",
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion % Test,
      "org.typelevel" %%% "scalacheck-effect-munit" % MUnitScalaCheckEffectVersion % Test,
    ),
    startYear := Some(2025),
  )

lazy val `oteljava-logs-testkit` = project
  .in(file("oteljava/logs-testkit"))
  .dependsOn(`oteljava-logs`, `oteljava-common-testkit`)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-oteljava-logs-testkit",
    startYear := Some(2025)
  )

lazy val `oteljava-metrics` = project
  .in(file("oteljava/metrics"))
  .dependsOn(
    `oteljava-common`,
    `core-metrics`.jvm % "compile->compile;test->test"
  )
  .settings(munitDependencies)
  .settings(
    name := "otel4s-oteljava-metrics",
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion % Test
    )
  )

lazy val `oteljava-metrics-testkit` = project
  .in(file("oteljava/metrics-testkit"))
  .dependsOn(`oteljava-metrics`, `oteljava-common-testkit`)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-oteljava-metrics-testkit",
    startYear := Some(2024)
  )

lazy val `oteljava-trace` = project
  .in(file("oteljava/trace"))
  .dependsOn(
    `oteljava-common`,
    `core-trace`.jvm % "compile->compile;test->test"
  )
  .settings(munitDependencies)
  .settings(
    name := "otel4s-oteljava-trace",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % CatsEffectVersion,
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion % Test,
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion % Test,
      "co.fs2" %% "fs2-core" % FS2Version % Test,
    ),
  )

lazy val `oteljava-trace-testkit` = project
  .in(file("oteljava/trace-testkit"))
  .dependsOn(`oteljava-trace`, `oteljava-common-testkit`)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-oteljava-trace-testkit",
    startYear := Some(2024)
  )

lazy val `oteljava-testkit` = project
  .in(file("oteljava/testkit"))
  .dependsOn(core.jvm, `oteljava-logs-testkit`, `oteljava-metrics-testkit`, `oteljava-trace-testkit`)
  .settings(
    name := "otel4s-oteljava-testkit",
    startYear := Some(2024)
  )

lazy val `oteljava-context-storage` = project
  .in(file("oteljava/context-storage"))
  .dependsOn(`oteljava-common`)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-oteljava-context-storage",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion % Test,
    ),
    Test / javaOptions ++= Seq(
      "-Dcats.effect.trackFiberContext=true",
    ),
    Test / fork := true,
  )

lazy val `oteljava-context-storage-testkit` = project
  .in(file("oteljava/context-storage-testkit"))
  .dependsOn(`oteljava-context-storage`, `oteljava-common-testkit`)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-oteljava-context-storage-testkit",
    Test / javaOptions ++= Seq(
      "-Dcats.effect.trackFiberContext=true",
    ),
    Test / fork := true,
  )

lazy val oteljava = project
  .in(file("oteljava/all"))
  .dependsOn(
    core.jvm,
    `oteljava-logs` % "compile->compile;test->test",
    `oteljava-metrics` % "compile->compile;test->test",
    `oteljava-metrics-testkit` % Test,
    `oteljava-trace` % "compile->compile;test->test",
    `oteljava-trace-testkit` % Test
  )
  .settings(
    name := "otel4s-oteljava",
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-sdk" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion % Test
    )
  )
  .settings(munitDependencies)

//
// Semantic conventions
//

lazy val `semconv-stable` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .enablePlugins(BuildInfoPlugin)
    .in(file("semconv/stable"))
    .dependsOn(`core-common`)
    .settings(
      name := "otel4s-semconv",
      startYear := Some(2023),
      description := "Stable semantic conventions.",
      // We use opentelemetry-semconv dependency to track releases of the OpenTelemetry semantic convention spec
      libraryDependencies += "io.opentelemetry.semconv" % "opentelemetry-semconv" % OpenTelemetrySemConvVersion % "compile-internal" intransitive (),
      buildInfoPackage := "org.typelevel.otel4s.semconv",
      buildInfoOptions += sbtbuildinfo.BuildInfoOption.PackagePrivate,
      buildInfoKeys := Seq[BuildInfoKey](
        "openTelemetrySemanticConventionsVersion" -> OpenTelemetrySemConvVersion
      )
    )
    .settings(munitDependencies)

lazy val `semconv-experimental` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("semconv/experimental"))
    .dependsOn(`core-common`)
    .settings(
      name := "otel4s-semconv-experimental",
      description := "Experimental (incubating) semantic conventions. Breaking changes expected. Library instrumentation SHOULD NOT depend on this.",
      startYear := Some(2023),
      // We use opentelemetry-semconv dependency to track releases of the OpenTelemetry semantic convention spec
      libraryDependencies += "io.opentelemetry.semconv" % "opentelemetry-semconv-incubating" % OpenTelemetrySemConvAlphaVersion % "compile-internal" intransitive (),
      mimaPreviousArtifacts := Set.empty
    )
    .settings(munitDependencies)

lazy val `semconv-metrics-stable` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("semconv/metrics/stable"))
    .dependsOn(`core-metrics`, `semconv-stable`)
    .settings(
      name := "otel4s-semconv-metrics",
      startYear := Some(2024),
      description := "Stable semantic metrics.",
    )
    .settings(munitDependencies)

lazy val `semconv-metrics-experimental` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("semconv/metrics/experimental"))
    .dependsOn(`core-metrics`, `semconv-metrics-stable`, `semconv-experimental`)
    .settings(
      name := "otel4s-semconv-metrics-experimental",
      startYear := Some(2024),
      description := "Experimental (incubating) semantic metrics. Breaking changes expected. Library instrumentation SHOULD NOT depend on this.",
      mimaPreviousArtifacts := Set.empty
    )
    .settings(munitDependencies)

//
//
//

lazy val scalafix = tlScalafixProject
  .rulesSettings(
    name := "otel4s-scalafix",
    crossScalaVersions := Seq(Scala212),
    startYear := Some(2024)
  )
  .inputSettings(
    crossScalaVersions := Seq(Scala213),
    // scala-steward:off
    libraryDependencies += "org.typelevel" %% "otel4s-java" % "0.4.0",
    // scala-steward:on
    headerSources / excludeFilter := AllPassFilter
  )
  .inputConfigure(_.disablePlugins(ScalafixPlugin))
  .outputSettings(
    crossScalaVersions := Seq(Scala213),
    headerSources / excludeFilter := AllPassFilter
  )
  .outputConfigure(_.dependsOn(oteljava).disablePlugins(ScalafixPlugin))
  .testsSettings(
    scalaVersion := _root_.scalafix.sbt.BuildInfo.scala212,
    startYear := Some(2024)
  )

lazy val benchmarks = project
  .enablePlugins(NoPublishPlugin)
  .enablePlugins(JmhPlugin)
  .in(file("benchmarks"))
  .dependsOn(core.jvm, oteljava)
  .settings(
    name := "otel4s-benchmarks",
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion
    )
  )

lazy val examples = project
  .enablePlugins(NoPublishPlugin, JavaAgent)
  .in(file("examples"))
  .dependsOn(core.jvm, oteljava, `oteljava-context-storage`)
  .settings(
    name := "otel4s-examples",
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-stream" % PekkoStreamVersion,
      "org.apache.pekko" %% "pekko-http" % PekkoHttpVersion,
      "io.opentelemetry" % "opentelemetry-exporter-otlp" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % OpenTelemetryVersion,
      "io.opentelemetry" % "opentelemetry-extension-trace-propagators" % OpenTelemetryVersion % Runtime,
      "io.opentelemetry.instrumentation" % "opentelemetry-instrumentation-annotations" % OpenTelemetryInstrumentationVersion
    ),
    javaAgents += "io.opentelemetry.javaagent" % "opentelemetry-javaagent" % OpenTelemetryInstrumentationVersion % Runtime,
    run / fork := true,
    javaOptions += "-Dotel.java.global-autoconfigure.enabled=true",
    javaOptions += "-Dcats.effect.trackFiberContext=true",
    envVars ++= Map(
      "OTEL_PROPAGATORS" -> "b3multi",
      "OTEL_SERVICE_NAME" -> "Trace Example"
    )
  )

lazy val docs = project
  .in(file("site"))
  .enablePlugins(TypelevelSitePlugin)
  .dependsOn(
    oteljava,
    `oteljava-context-storage`,
    `oteljava-context-storage-testkit`,
    `oteljava-testkit`,
    `instrumentation-metrics`.jvm,
    `semconv-stable`.jvm,
  )
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http" % PekkoHttpVersion,
      "org.http4s" %% "http4s-client" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % OpenTelemetryVersion,
      "io.opentelemetry.instrumentation" % "opentelemetry-instrumentation-annotations" % OpenTelemetryInstrumentationVersion,
      "io.opentelemetry.instrumentation" % "opentelemetry-runtime-telemetry-java8" % OpenTelemetryInstrumentationAlphaVersion,
      "io.opentelemetry.instrumentation" % "opentelemetry-runtime-telemetry-java17" % OpenTelemetryInstrumentationAlphaVersion,
      "com.outr" %% "scribe" % ScribeVersion,
      // a trick to make Scala-Steward provide updates for this dependency
      "io.github.irevive" % "otel4s-opentelemetry-javaagent" % Otel4sAgentVersion % Test
    ),
    mdocVariables ++= Map(
      "OPEN_TELEMETRY_VERSION" -> OpenTelemetryVersion,
      "OPEN_TELEMETRY_INSTRUMENTATION_ALPHA_VERSION" -> OpenTelemetryInstrumentationAlphaVersion,
      "OTEL4S_AGENT_VERSION" -> Otel4sAgentVersion,
    ),
    run / fork := true,
    javaOptions += "-Dcats.effect.trackFiberContext=true",
    laikaConfig := {
      import laika.config.{ChoiceConfig, Selections, SelectionConfig}

      laikaConfig.value.withConfigValue(
        Selections(
          SelectionConfig(
            "build-tool",
            ChoiceConfig("sbt", "sbt"),
            ChoiceConfig("scala-cli", "Scala CLI")
          ).withSeparateEbooks,
          SelectionConfig(
            "sdk-options-source",
            ChoiceConfig("sbt", "sbt"),
            ChoiceConfig("scala-cli", "Scala CLI"),
            ChoiceConfig("shell", "Shell")
          ).withSeparateEbooks,
          SelectionConfig(
            "scala-version",
            ChoiceConfig("scala-2", "Scala 2"),
            ChoiceConfig("scala-3", "Scala 3")
          ).withSeparateEbooks,
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
      `core-logs`.jvm,
      `core-metrics`.jvm,
      `core-trace`.jvm,
      core.jvm,
      `instrumentation-metrics`.jvm,
      `oteljava-common`,
      `oteljava-common-testkit`,
      `oteljava-logs`,
      `oteljava-logs-testkit`,
      `oteljava-metrics`,
      `oteljava-metrics-testkit`,
      `oteljava-trace`,
      `oteljava-trace-testkit`,
      `oteljava-testkit`,
      `oteljava-context-storage`,
      `oteljava-context-storage-testkit`,
      oteljava,
      `semconv-stable`.jvm,
      `semconv-experimental`.jvm,
      `semconv-metrics-stable`.jvm,
      `semconv-metrics-experimental`.jvm
    )
  )
