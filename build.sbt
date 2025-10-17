import com.typesafe.tools.mima.core._

ThisBuild / tlBaseVersion := "0.14"

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

// VM runs out of memory when linking multiple targets concurrently, hence limit it
Global / concurrentRestrictions += Tags.limit(NativeTags.Link, 1)

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

lazy val scalaNativeSettings = Def.settings(
  Test / nativeBrewFormulas ++= Set("s2n", "utf8proc"),
  Test / envVars ++= Map("S2N_DONT_MLOCK" -> "1"),
)

// the JS and SN artifacts could be quite large and exceed the CI disk space limit
lazy val artifactUploadSettings = Def.settings(
  githubWorkflowArtifactUpload := {
    val platform = crossProjectPlatform.?.value
    if (platform.contains(NativePlatform) || platform.contains(JSPlatform)) false
    else githubWorkflowArtifactUpload.value
  }
)

val Scala212 = "2.12.20"
val Scala213 = "2.13.16"
ThisBuild / crossScalaVersions := Seq(Scala213, "3.3.6")
ThisBuild / scalaVersion := Scala213 // the default Scala

ThisBuild / githubWorkflowBuildPreamble ++= nativeBrewInstallWorkflowSteps.value

ThisBuild / mergifyStewardConfig := None
ThisBuild / mergifyLabelPaths := Map(
  "module:core" -> file("core"),
  "module:sdk" -> file("sdk"),
  "module:sdk:exporter" -> file("sdk-exporter"),
  "module:sdk:contrib:aws" -> file("sdk-contrib/aws"),
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
val OpenTelemetryVersion = "1.54.1"
val OpenTelemetryAlphaVersion = s"$OpenTelemetryVersion-alpha"
val OpenTelemetryInstrumentationVersion = "2.20.0"
val OpenTelemetryInstrumentationAlphaVersion = s"$OpenTelemetryInstrumentationVersion-alpha"
val OpenTelemetrySemConvVersion = "1.37.0"
val OpenTelemetrySemConvAlphaVersion = s"$OpenTelemetrySemConvVersion-alpha"
val OpenTelemetryProtoVersion = "1.8.0-alpha"
val Otel4sAgentVersion = "0.0.4"
val PekkoStreamVersion = "1.2.0"
val PekkoHttpVersion = "1.2.0"
val PlatformVersion = "1.0.2"
val ScodecVersion = "1.1.38"
val VaultVersion = "3.6.0"
val Http4sVersion = "0.23.30"
val CirceVersion = "0.14.8"
val ScalaPBCirceVersion = "0.15.1"
val CaseInsensitiveVersion = "1.4.2"
val ScalaJavaTimeVersion = "2.6.0"

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
    `sdk-common`,
    `sdk-logs`,
    `sdk-logs-testkit`,
    `sdk-metrics`,
    `sdk-metrics-testkit`,
    `sdk-trace`,
    `sdk-trace-testkit`,
    `sdk-testkit`,
    sdk,
    `sdk-exporter-common`,
    `sdk-exporter-proto`,
    `sdk-exporter-logs`,
    `sdk-exporter-metrics`,
    `sdk-exporter-prometheus`,
    `sdk-exporter-trace`,
    `sdk-exporter`,
    `sdk-contrib-aws-resource`,
    `sdk-contrib-aws-xray`,
    `sdk-contrib-aws-xray-propagator`,
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
  .settings(artifactUploadSettings)
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
  .settings(artifactUploadSettings)
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
  .settings(artifactUploadSettings)
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
  .settings(artifactUploadSettings)
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
  .settings(artifactUploadSettings)
  .settings(
    name := "otel4s-core"
  )

//
// Instrumentation
//

lazy val `instrumentation-metrics` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .in(file("instrumentation/metrics"))
  .dependsOn(`core-metrics`, `core-common` % "test->test", `sdk-metrics-testkit` % Test)
  .settings(artifactUploadSettings)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-instrumentation-metrics",
    startYear := Some(2024),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "scalacheck-effect-munit" % MUnitScalaCheckEffectVersion % Test
    )
  )

//
// SDK
//

lazy val `sdk-common` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .enablePlugins(BuildInfoPlugin)
  .in(file("sdk/common"))
  .dependsOn(
    `core-common` % "compile->compile;test->test",
    `semconv-stable`,
    `semconv-experimental` % Test
  )
  .settings(artifactUploadSettings)
  .settings(
    name := "otel4s-sdk-common",
    startYear := Some(2023),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect-kernel" % CatsEffectVersion,
      "org.typelevel" %%% "cats-mtl" % CatsMtlVersion,
      "org.typelevel" %%% "cats-laws" % CatsVersion % Test,
      "org.typelevel" %%% "discipline-munit" % MUnitDisciplineVersion % Test,
    ),
    buildInfoPackage := "org.typelevel.otel4s.sdk",
    buildInfoOptions += sbtbuildinfo.BuildInfoOption.PackagePrivate,
    buildInfoKeys := Seq[BuildInfoKey](
      version
    )
  )
  .settings(munitDependencies)
  .jsSettings(scalaJSLinkerSettings)

lazy val `sdk-logs` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("sdk/logs"))
  .dependsOn(
    `sdk-common` % "compile->compile;test->test",
    `core-logs` % "compile->compile;test->test",
  )
  .settings(artifactUploadSettings)
  .settings(
    name := "otel4s-sdk-logs",
    startYear := Some(2025),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % CatsEffectVersion,
      "org.typelevel" %%% "cats-laws" % CatsVersion % Test,
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion % Test,
      "org.typelevel" %%% "discipline-munit" % MUnitDisciplineVersion % Test,
      "org.typelevel" %%% "scalacheck-effect-munit" % MUnitScalaCheckEffectVersion % Test
    ),
  )
  .settings(munitDependencies)
  .jsSettings(scalaJSLinkerSettings)
  .jsSettings(
    libraryDependencies += "io.github.cquiroz" %%% "scala-java-time" % ScalaJavaTimeVersion,
  )

lazy val `sdk-logs-testkit` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("sdk/logs-testkit"))
  .dependsOn(`sdk-logs`)
  .settings(artifactUploadSettings)
  .settings(
    name := "otel4s-sdk-logs-testkit",
    startYear := Some(2025)
  )

lazy val `sdk-metrics` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("sdk/metrics"))
  .dependsOn(
    `sdk-common` % "compile->compile;test->test",
    `core-metrics` % "compile->compile;test->test"
  )
  .settings(artifactUploadSettings)
  .settings(
    name := "otel4s-sdk-metrics",
    startYear := Some(2024),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % CatsEffectVersion,
      "org.scodec" %%% "scodec-bits" % ScodecVersion,
      "org.typelevel" %%% "case-insensitive" % CaseInsensitiveVersion,
      "org.typelevel" %%% "cats-laws" % CatsVersion % Test,
      "org.typelevel" %%% "discipline-munit" % MUnitDisciplineVersion % Test,
      "org.typelevel" %%% "scalacheck-effect-munit" % MUnitScalaCheckEffectVersion % Test
    )
  )
  .settings(munitDependencies)
  .jsSettings(scalaJSLinkerSettings)

lazy val `sdk-metrics-testkit` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("sdk/metrics-testkit"))
    .dependsOn(`sdk-metrics`)
    .settings(artifactUploadSettings)
    .settings(
      name := "otel4s-sdk-metrics-testkit",
      startYear := Some(2024)
    )

lazy val `sdk-trace` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("sdk/trace"))
  .dependsOn(
    `sdk-common` % "compile->compile;test->test",
    `core-trace` % "compile->compile;test->test"
  )
  .settings(artifactUploadSettings)
  .settings(
    name := "otel4s-sdk-trace",
    startYear := Some(2023),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % CatsEffectVersion,
      "org.typelevel" %%% "cats-laws" % CatsVersion % Test,
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion % Test,
      "org.typelevel" %%% "discipline-munit" % MUnitDisciplineVersion % Test,
      "org.typelevel" %%% "scalacheck-effect-munit" % MUnitScalaCheckEffectVersion % Test
    ),
  )
  .settings(munitDependencies)
  .jsSettings(scalaJSLinkerSettings)

lazy val `sdk-trace-testkit` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("sdk/trace-testkit"))
    .settings(artifactUploadSettings)
    .dependsOn(`sdk-trace`)
    .settings(
      name := "otel4s-sdk-trace-testkit",
      startYear := Some(2024)
    )

lazy val `sdk-testkit` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("sdk/testkit"))
  .dependsOn(core, `sdk-logs-testkit`, `sdk-metrics-testkit`, `sdk-trace-testkit`)
  .settings(artifactUploadSettings)
  .settings(
    name := "otel4s-sdk-testkit",
    startYear := Some(2024)
  )

lazy val sdk = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("sdk/all"))
  .dependsOn(
    core,
    `sdk-common`,
    `sdk-logs` % "compile->compile;test->test",
    `sdk-metrics` % "compile->compile;test->test",
    `sdk-metrics-testkit` % Test,
    `sdk-trace` % "compile->compile;test->test",
    `sdk-trace-testkit` % Test
  )
  .settings(artifactUploadSettings)
  .settings(
    name := "otel4s-sdk"
  )
  .settings(munitDependencies)
  .jsSettings(scalaJSLinkerSettings)

//
// SDK exporter
//

lazy val `sdk-exporter-proto` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("sdk-exporter/proto"))
    .settings(artifactUploadSettings)
    .settings(
      name := "otel4s-sdk-exporter-proto",
      Compile / PB.protoSources += baseDirectory.value.getParentFile / "src" / "main" / "protobuf",
      Compile / PB.targets ++= Seq(
        scalapb.gen(grpc = false) -> (Compile / sourceManaged).value / "scalapb"
      ),
      Compile / PB.generate := {
        val files = (Compile / PB.generate).value

        files.filter(_.isFile).foreach { file =>
          val content = IO.read(file)

          // see: https://github.com/scalapb/ScalaPB/issues/1778
          val updated = content
            .replaceAll(
              """(?m)^object (\w+) extends _root_\.scalapb\.GeneratedEnumCompanion\[\w+\]""",
              "private[exporter] object $1 extends _root_.scalapb.GeneratedEnumCompanion[$1]"
            )
            .replaceAll(
              """(?m)^object (\w+) extends _root_\.scalapb\.GeneratedFileObject""",
              "private[exporter] object $1 extends _root_.scalapb.GeneratedFileObject"
            )

          IO.write(file, updated)
        }

        files
      },
      scalacOptions := {
        val opts = scalacOptions.value
        if (tlIsScala3.value) opts.filterNot(_ == "-Wvalue-discard") else opts
      },
      // We use open-telemetry protobuf spec to generate models
      // See https://scalapb.github.io/docs/third-party-protos/#there-is-a-library-on-maven-with-the-protos-and-possibly-generated-java-code
      libraryDependencies ++= Seq(
        "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf",
        "io.opentelemetry.proto" % "opentelemetry-proto" % OpenTelemetryProtoVersion % "protobuf-src" intransitive ()
      )
    )
    .jvmSettings(
      // scalafix settings to ensure there are no public classes in the module
      // run scalafix against generated sources
      Compile / ScalafixPlugin.autoImport.scalafix / unmanagedSources := (Compile / managedSources).value,
      // run scalafix only on scala 2.13
      scalafixOnCompile := !tlIsScala3.value,
      // read scalafix rules from a shared folder
      ScalafixConfig / sourceDirectory := {
        if (tlIsScala3.value) {
          (ScalafixConfig / sourceDirectory).value
        } else {
          baseDirectory.value.getParentFile / "src" / "scalafix"
        }
      },
      // required by scalafix rules
      libraryDependencies ++= {
        if (tlIsScala3.value) Nil
        else Seq("ch.epfl.scala" %% "scalafix-core" % _root_.scalafix.sbt.BuildInfo.scalafixVersion % ScalafixConfig)
      }
    )

lazy val `sdk-exporter-common` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("sdk-exporter/common"))
    .dependsOn(
      `sdk-common` % "compile->compile;test->test",
      `sdk-exporter-proto`
    )
    .settings(artifactUploadSettings)
    .settings(
      name := "otel4s-sdk-exporter-common",
      startYear := Some(2023),
      libraryDependencies ++= Seq(
        "co.fs2" %%% "fs2-scodec" % FS2Version,
        "co.fs2" %%% "fs2-io" % FS2Version,
        "org.http4s" %%% "http4s-ember-client" % Http4sVersion,
        "org.http4s" %%% "http4s-circe" % Http4sVersion,
        "io.github.scalapb-json" %%% "scalapb-circe" % ScalaPBCirceVersion,
        "org.typelevel" %%% "cats-laws" % CatsVersion % Test,
        "org.typelevel" %%% "discipline-munit" % MUnitDisciplineVersion % Test,
        "io.circe" %%% "circe-generic" % CirceVersion % Test
      )
    )
    .jsSettings(scalaJSLinkerSettings)
    .nativeEnablePlugins(ScalaNativeBrewedConfigPlugin)
    .nativeSettings(scalaNativeSettings)
    .settings(munitDependencies)

lazy val `sdk-exporter-logs` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("sdk-exporter/logs"))
    .enablePlugins(DockerComposeEnvPlugin)
    .dependsOn(
      `sdk-exporter-common` % "compile->compile;test->test",
      `sdk-logs` % "compile->compile;test->test"
    )
    .settings(artifactUploadSettings)
    .settings(
      name := "otel4s-sdk-exporter-logs",
      startYear := Some(2025),
      dockerComposeEnvFile := crossProjectBaseDirectory.value / "docker" / "docker-compose.yml",
      Test / scalacOptions ++= {
        // see https://github.com/circe/circe/issues/2162
        if (tlIsScala3.value) Seq("-Xmax-inlines", "64") else Nil
      }
    )
    .jsSettings(scalaJSLinkerSettings)
    .nativeEnablePlugins(ScalaNativeBrewedConfigPlugin)
    .nativeSettings(scalaNativeSettings)
    .settings(munitDependencies)

lazy val `sdk-exporter-metrics` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("sdk-exporter/metrics"))
    .enablePlugins(DockerComposeEnvPlugin)
    .dependsOn(
      `sdk-exporter-common` % "compile->compile;test->test",
      `sdk-metrics` % "compile->compile;test->test"
    )
    .settings(artifactUploadSettings)
    .settings(
      name := "otel4s-sdk-exporter-metrics",
      startYear := Some(2024),
      dockerComposeEnvFile := crossProjectBaseDirectory.value / "docker" / "docker-compose.yml"
    )
    .jsSettings(scalaJSLinkerSettings)
    .nativeEnablePlugins(ScalaNativeBrewedConfigPlugin)
    .nativeSettings(scalaNativeSettings)
    .settings(munitDependencies)

lazy val `sdk-exporter-prometheus` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("sdk-exporter/prometheus"))
    .dependsOn(
      `sdk-exporter-common` % "compile->compile;test->test",
      `sdk-metrics` % "compile->compile;test->test"
    )
    .settings(artifactUploadSettings)
    .settings(
      name := "otel4s-sdk-exporter-prometheus",
      startYear := Some(2024),
      libraryDependencies ++= Seq(
        "org.http4s" %%% "http4s-ember-server" % Http4sVersion
      ),
      mimaBinaryIssueFilters ++= Seq(
        // see #838
        ProblemFilters.exclude[DirectMissingMethodProblem](
          "org.typelevel.otel4s.sdk.exporter.prometheus.PrometheusMetricExporter#HttpServerBuilderImpl.*"
        ),
        ProblemFilters.exclude[IncompatibleResultTypeProblem](
          "org.typelevel.otel4s.sdk.exporter.prometheus.PrometheusMetricExporter#HttpServerBuilderImpl.*"
        ),
        ProblemFilters.exclude[ReversedMissingMethodProblem](
          "org.typelevel.otel4s.sdk.exporter.prometheus.PrometheusMetricExporter#HttpServerBuilder.withShutdownTimeout"
        )
      )
    )
    .jsSettings(scalaJSLinkerSettings)
    .nativeEnablePlugins(ScalaNativeBrewedConfigPlugin)
    .nativeSettings(scalaNativeSettings)
    .settings(munitDependencies)

lazy val `sdk-exporter-trace` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("sdk-exporter/trace"))
    .enablePlugins(DockerComposeEnvPlugin)
    .dependsOn(
      `sdk-exporter-common` % "compile->compile;test->test",
      `sdk-trace` % "compile->compile;test->test"
    )
    .settings(artifactUploadSettings)
    .settings(
      name := "otel4s-sdk-exporter-trace",
      startYear := Some(2023),
      dockerComposeEnvFile := crossProjectBaseDirectory.value / "docker" / "docker-compose.yml",
      Test / scalacOptions ++= {
        // see https://github.com/circe/circe/issues/2162
        if (tlIsScala3.value) Seq("-Xmax-inlines", "64") else Nil
      }
    )
    .jsSettings(scalaJSLinkerSettings)
    .nativeEnablePlugins(ScalaNativeBrewedConfigPlugin)
    .nativeSettings(scalaNativeSettings)
    .settings(munitDependencies)

lazy val `sdk-exporter` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("sdk-exporter/all"))
  .dependsOn(
    sdk,
    `sdk-exporter-common`,
    `sdk-exporter-logs`,
    `sdk-exporter-metrics`,
    `sdk-exporter-trace`
  )
  .settings(artifactUploadSettings)
  .settings(
    name := "otel4s-sdk-exporter"
  )

//
// SDK contrib modules
//

lazy val `sdk-contrib-aws-resource` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("sdk-contrib/aws/resource"))
    .dependsOn(`sdk-common`, `semconv-experimental` % Test)
    .settings(
      name := "otel4s-sdk-contrib-aws-resource",
      startYear := Some(2024),
      libraryDependencies ++= Seq(
        "co.fs2" %%% "fs2-io" % FS2Version,
        "io.circe" %%% "circe-parser" % CirceVersion,
        "org.http4s" %%% "http4s-ember-client" % Http4sVersion,
        "org.http4s" %%% "http4s-circe" % Http4sVersion,
        "org.http4s" %%% "http4s-dsl" % Http4sVersion % Test
      )
    )
    .settings(artifactUploadSettings)
    .settings(munitDependencies)
    .jsSettings(scalaJSLinkerSettings)
    .nativeEnablePlugins(ScalaNativeBrewedConfigPlugin)
    .nativeSettings(scalaNativeSettings)

lazy val `sdk-contrib-aws-xray-propagator` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("sdk-contrib/aws/xray-propagator"))
    .dependsOn(
      `sdk-trace` % "compile->compile;test->test",
      `semconv-experimental` % Test
    )
    .settings(
      name := "otel4s-sdk-contrib-aws-xray-propagator",
      startYear := Some(2024)
    )
    .settings(artifactUploadSettings)
    .settings(munitDependencies)
    .jsSettings(scalaJSLinkerSettings)

lazy val `sdk-contrib-aws-xray` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("sdk-contrib/aws/xray"))
    .dependsOn(`sdk-trace` % "compile->compile;test->test")
    .settings(
      name := "otel4s-sdk-contrib-aws-xray",
      startYear := Some(2024),
    )
    .settings(artifactUploadSettings)
    .settings(munitDependencies)
    .jsSettings(scalaJSLinkerSettings)

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
    .settings(artifactUploadSettings)
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
    .settings(artifactUploadSettings)
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
    .settings(artifactUploadSettings)
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
    .settings(artifactUploadSettings)
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
  .dependsOn(core.jvm, sdk.jvm, `sdk-testkit`.jvm, oteljava)
  .settings(
    name := "otel4s-benchmarks",
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion
    )
  )

lazy val examples = project
  .enablePlugins(NoPublishPlugin, JavaAgent)
  .in(file("examples"))
  .dependsOn(core.jvm, oteljava, `oteljava-context-storage`, sdk.jvm, `sdk-exporter`.jvm, `sdk-exporter-prometheus`.jvm)
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
    sdk.jvm,
    `sdk-exporter`.jvm,
    `sdk-exporter-prometheus`.jvm,
    `sdk-contrib-aws-resource`.jvm,
    `sdk-contrib-aws-xray`.jvm,
    `sdk-contrib-aws-xray-propagator`.jvm,
    `sdk-testkit`.jvm
  )
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http" % PekkoHttpVersion,
      "org.http4s" %% "http4s-client" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % OpenTelemetryVersion,
      "io.opentelemetry.instrumentation" % "opentelemetry-instrumentation-annotations" % OpenTelemetryInstrumentationVersion,
      "io.opentelemetry.instrumentation" % "opentelemetry-runtime-telemetry-java8" % OpenTelemetryInstrumentationAlphaVersion,
      "io.opentelemetry.instrumentation" % "opentelemetry-runtime-telemetry-java17" % OpenTelemetryInstrumentationAlphaVersion
    ),
    libraryDependencies ++= Seq(
      "io.github.irevive" % "otel4s-opentelemetry-javaagent" % Otel4sAgentVersion
    ).filter(_ => false), // a trick to make Scala-Steward provide updates for this dependency
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
          SelectionConfig(
            "otel-backend",
            ChoiceConfig("oteljava", "OpenTelemetry Java"),
            ChoiceConfig("sdk", "SDK")
          ).withSeparateEbooks,
          SelectionConfig(
            "sdk-entry-point",
            ChoiceConfig("traces", "SdkTraces"),
            ChoiceConfig("metrics", "SdkMetrics"),
            ChoiceConfig("sdk", "OpenTelemetrySDK")
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
      `core-logs`.jvm,
      `core-metrics`.jvm,
      `core-trace`.jvm,
      core.jvm,
      `instrumentation-metrics`.jvm,
      `sdk-common`.jvm,
      `sdk-logs`.jvm,
      `sdk-logs-testkit`.jvm,
      `sdk-metrics`.jvm,
      `sdk-metrics-testkit`.jvm,
      `sdk-trace`.jvm,
      `sdk-trace-testkit`.jvm,
      `sdk-testkit`.jvm,
      sdk.jvm,
      `sdk-exporter-common`.jvm,
      `sdk-exporter-logs`.jvm,
      `sdk-exporter-metrics`.jvm,
      `sdk-exporter-prometheus`.jvm,
      `sdk-exporter-trace`.jvm,
      `sdk-exporter`.jvm,
      `sdk-contrib-aws-resource`.jvm,
      `sdk-contrib-aws-xray`.jvm,
      `sdk-contrib-aws-xray-propagator`.jvm,
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
