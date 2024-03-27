ThisBuild / tlBaseVersion := "0.5"

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

lazy val scalaJSLinkerSettings = Def.settings(
  scalaJSLinkerConfig ~= (_.withESFeatures(
    _.withESVersion(org.scalajs.linker.interface.ESVersion.ES2018)
  )),
  Test / scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
)

lazy val scalaNativeSettings = Def.settings(
  libraryDependencies += "com.armanbilge" %%% "epollcat" % EpollcatVersion % Test,
  Test / nativeBrewFormulas ++= Set("s2n", "utf8proc"),
  Test / envVars ++= Map("S2N_DONT_MLOCK" -> "1")
)

val Scala212 = "2.12.18"
val Scala213 = "2.13.13"
ThisBuild / crossScalaVersions := Seq(Scala213, "3.3.3")
ThisBuild / scalaVersion := Scala213 // the default Scala

ThisBuild / githubWorkflowBuildPreamble ++= nativeBrewInstallWorkflowSteps.value

val CatsVersion = "2.10.0"
val CatsEffectVersion = "3.5.4"
val CatsMtlVersion = "1.4.0"
val FS2Version = "3.10.1"
val MUnitVersion = "1.0.0-M11"
val MUnitCatsEffectVersion = "2.0.0-M4"
val MUnitDisciplineVersion = "2.0.0-M3"
val MUnitScalaCheckEffectVersion = "2.0.0-M2"
val OpenTelemetryVersion = "1.36.0"
val OpenTelemetryInstrumentationVersion = "2.2.0"
val OpenTelemetrySemConvVersion = "1.23.1-alpha"
val OpenTelemetryProtoVersion = "1.1.0-alpha"
val PekkoStreamVersion = "1.0.2"
val PekkoHttpVersion = "1.0.1"
val PlatformVersion = "1.0.2"
val ScodecVersion = "1.1.38"
val VaultVersion = "3.5.0"
val Http4sVersion = "0.23.26"
val CirceVersion = "0.14.6"
val EpollcatVersion = "0.1.6"
val ScalaPBCirceVersion = "0.15.1"

lazy val scalaReflectDependency = Def.settings(
  libraryDependencies ++= {
    if (tlIsScala3.value) Nil
    else Seq("org.scala-lang" % "scala-reflect" % scalaVersion.value % Provided)
  }
)

lazy val munitDependencies = Def.settings(
  libraryDependencies ++= Seq(
    "org.scalameta" %%% "munit" % MUnitVersion % Test,
    "org.scalameta" %%% "munit-scalacheck" % MUnitVersion % Test,
    "org.typelevel" %%% "munit-cats-effect" % MUnitCatsEffectVersion % Test
  )
)

lazy val semanticConventionsGenerate =
  taskKey[Unit]("Generate semantic conventions")
semanticConventionsGenerate := {
  SemanticConventionsGenerator.generate(
    OpenTelemetrySemConvVersion.stripSuffix("-alpha"),
    baseDirectory.value
  )
}

lazy val root = tlCrossRootProject
  .aggregate(
    `core-common`,
    `core-metrics`,
    `core-trace`,
    core,
    `sdk-common`,
    `sdk-metrics`,
    `sdk-trace`,
    `sdk-trace-testkit`,
    sdk,
    `sdk-exporter-common`,
    `sdk-exporter-proto`,
    `sdk-exporter-trace`,
    `sdk-exporter`,
    `oteljava-common`,
    `oteljava-common-testkit`,
    `oteljava-metrics`,
    `oteljava-metrics-testkit`,
    `oteljava-trace`,
    `oteljava-trace-testkit`,
    `oteljava-testkit`,
    oteljava,
    semconv,
    benchmarks,
    examples,
    unidocs
  )
  .configureRoot(
    _.aggregate(scalafix.componentProjectReferences: _*)
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
      "org.typelevel" %%% "vault" % VaultVersion % Test,
      "org.typelevel" %%% "cats-laws" % CatsVersion % Test,
      "org.typelevel" %%% "cats-mtl-laws" % CatsMtlVersion % Test,
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion % Test,
      "org.typelevel" %%% "discipline-munit" % MUnitDisciplineVersion % Test,
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
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion % Test,
      "org.typelevel" %%% "cats-laws" % CatsVersion % Test,
      "org.typelevel" %%% "discipline-munit" % MUnitDisciplineVersion % Test
    )
  )
  .settings(scalafixSettings)

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
      "org.typelevel" %%% "scalacheck-effect-munit" % MUnitScalaCheckEffectVersion % Test
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

//
// SDK
//

lazy val `sdk-common` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .enablePlugins(BuildInfoPlugin)
  .in(file("sdk/common"))
  .dependsOn(`core-common` % "compile->compile;test->test", semconv)
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
  .settings(scalafixSettings)

lazy val `sdk-metrics` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .enablePlugins(NoPublishPlugin)
  .in(file("sdk/metrics"))
  .dependsOn(`sdk-common` % "compile->compile;test->test", `core-metrics`)
  .settings(
    name := "otel4s-sdk-metrics",
    startYear := Some(2024),
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % CatsEffectVersion,
      "org.scodec" %%% "scodec-bits" % ScodecVersion,
      "org.typelevel" %%% "cats-laws" % CatsVersion % Test,
      "org.typelevel" %%% "discipline-munit" % MUnitDisciplineVersion % Test,
    )
  )
  .settings(munitDependencies)
  .settings(scalafixSettings)

lazy val `sdk-trace` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("sdk/trace"))
  .dependsOn(
    `sdk-common` % "compile->compile;test->test",
    `core-trace` % "compile->compile;test->test"
  )
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
  .settings(scalafixSettings)

lazy val `sdk-trace-testkit` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("sdk/trace-testkit"))
    .dependsOn(`sdk-trace`)
    .settings(
      name := "otel4s-sdk-trace-testkit",
      startYear := Some(2024)
    )
    .settings(scalafixSettings)

lazy val sdk = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("sdk/all"))
  .dependsOn(
    core,
    `sdk-common`,
    `sdk-trace` % "compile->compile;test->test",
    `sdk-trace-testkit` % Test
  )
  .settings(
    name := "otel4s-sdk"
  )
  .settings(munitDependencies)
  .settings(scalafixSettings)

//
// SDK exporter
//

lazy val `sdk-exporter-proto` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("sdk-exporter/proto"))
    .settings(
      name := "otel4s-sdk-exporter-proto",
      Compile / PB.protoSources += baseDirectory.value.getParentFile / "src" / "main" / "protobuf",
      Compile / PB.targets ++= Seq(
        scalapb.gen(grpc = false) -> (Compile / sourceManaged).value / "scalapb"
      ),
      scalacOptions := {
        val opts = scalacOptions.value
        if (tlIsScala3.value) opts.filterNot(_ == "-Wvalue-discard") else opts
      },
      // We use open-telemetry protobuf spec to generate models
      // See https://scalapb.github.io/docs/third-party-protos/#there-is-a-library-on-maven-with-the-protos-and-possibly-generated-java-code
      libraryDependencies ++= Seq(
        "io.opentelemetry.proto" % "opentelemetry-proto" % OpenTelemetryProtoVersion % "protobuf-src" intransitive ()
      )
    )
    .settings(scalafixSettings)

lazy val `sdk-exporter-common` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("sdk-exporter/common"))
    .dependsOn(
      `sdk-common` % "compile->compile;test->test",
      `sdk-exporter-proto`
    )
    .settings(
      name := "otel4s-sdk-exporter-common",
      startYear := Some(2023),
      libraryDependencies ++= Seq(
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
    .settings(scalafixSettings)

lazy val `sdk-exporter-trace` =
  crossProject(JVMPlatform, JSPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("sdk-exporter/trace"))
    .enablePlugins(DockerComposeEnvPlugin)
    .dependsOn(
      `sdk-exporter-common` % "compile->compile;test->test",
      `sdk-trace` % "compile->compile;test->test"
    )
    .settings(
      name := "otel4s-sdk-exporter-trace",
      startYear := Some(2023),
      dockerComposeEnvFile := crossProjectBaseDirectory.value / "docker" / "docker-compose.yml"
    )
    .jsSettings(scalaJSLinkerSettings)
    .nativeEnablePlugins(ScalaNativeBrewedConfigPlugin)
    .nativeSettings(scalaNativeSettings)
    .settings(munitDependencies)
    .settings(scalafixSettings)

lazy val `sdk-exporter` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("sdk-exporter/all"))
  .dependsOn(`sdk-exporter-common`, `sdk-exporter-trace`)
  .settings(
    name := "otel4s-sdk-exporter"
  )
  .settings(scalafixSettings)

//
// OpenTelemetry Java
//

lazy val `oteljava-common` = project
  .in(file("oteljava/common"))
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(`core-common`.jvm)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-oteljava-common",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-effect" % CatsEffectVersion,
      "org.typelevel" %%% "cats-mtl" % CatsMtlVersion,
      "io.opentelemetry" % "opentelemetry-api" % OpenTelemetryVersion,
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion % Test,
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion % Test
    ),
    buildInfoPackage := "org.typelevel.otel4s.oteljava",
    buildInfoOptions += sbtbuildinfo.BuildInfoOption.PackagePrivate,
    buildInfoKeys := Seq[BuildInfoKey](
      "openTelemetrySdkVersion" -> OpenTelemetryVersion
    )
  )
  .settings(scalafixSettings)

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
  .settings(scalafixSettings)

lazy val `oteljava-metrics` = project
  .in(file("oteljava/metrics"))
  .dependsOn(`oteljava-common`, `core-metrics`.jvm)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-oteljava-metrics",
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion % Test
    )
  )
  .settings(scalafixSettings)

lazy val `oteljava-metrics-testkit` = project
  .in(file("oteljava/metrics-testkit"))
  .dependsOn(`oteljava-metrics`, `oteljava-common-testkit`)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-oteljava-metrics-testkit",
    startYear := Some(2024)
  )
  .settings(scalafixSettings)

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
  .settings(scalafixSettings)

lazy val `oteljava-trace-testkit` = project
  .in(file("oteljava/trace-testkit"))
  .dependsOn(`oteljava-trace`, `oteljava-common-testkit`)
  .settings(munitDependencies)
  .settings(
    name := "otel4s-oteljava-trace-testkit",
    startYear := Some(2024)
  )
  .settings(scalafixSettings)

lazy val `oteljava-testkit` = project
  .in(file("oteljava/testkit"))
  .dependsOn(core.jvm, `oteljava-metrics-testkit`, `oteljava-trace-testkit`)
  .settings(
    name := "otel4s-oteljava-testkit",
    startYear := Some(2024)
  )
  .settings(scalafixSettings)

lazy val oteljava = project
  .in(file("oteljava/all"))
  .dependsOn(
    core.jvm,
    `oteljava-metrics`,
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
  .settings(scalafixSettings)

lazy val semconv = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .enablePlugins(BuildInfoPlugin)
  .in(file("semconv"))
  .dependsOn(`core-common`)
  .settings(
    name := "otel4s-semconv",
    startYear := Some(2023),
    // We use opentelemetry-semconv dependency to track releases of the OpenTelemetry semantic convention spec
    libraryDependencies += "io.opentelemetry.semconv" % "opentelemetry-semconv" % OpenTelemetrySemConvVersion % "compile-internal" intransitive (),
    buildInfoPackage := "org.typelevel.otel4s.semconv",
    buildInfoOptions += sbtbuildinfo.BuildInfoOption.PackagePrivate,
    buildInfoKeys := Seq[BuildInfoKey](
      "openTelemetrySemanticConventionsVersion" -> OpenTelemetrySemConvVersion
    )
  )
  .settings(munitDependencies)
  .settings(scalafixSettings)

lazy val scalafix = tlScalafixProject
  .rulesSettings(
    name := "otel4s-scalafix",
    crossScalaVersions := Seq(Scala212),
    startYear := Some(2024)
  )
  .inputSettings(
    crossScalaVersions := Seq(Scala213),
    libraryDependencies += "org.typelevel" %% "otel4s-java" % "0.4.0",
    headerSources / excludeFilter := AllPassFilter
  )
  .inputConfigure(_.disablePlugins(ScalafixPlugin))
  .outputSettings(
    crossScalaVersions := Seq(Scala213),
    headerSources / excludeFilter := AllPassFilter
  )
  .outputConfigure(_.dependsOn(oteljava).disablePlugins(ScalafixPlugin))
  .testsSettings(
    crossScalaVersions := Seq(Scala212),
    startYear := Some(2024)
  )

lazy val benchmarks = project
  .enablePlugins(NoPublishPlugin)
  .enablePlugins(JmhPlugin)
  .in(file("benchmarks"))
  .dependsOn(core.jvm, sdk.jvm, oteljava)
  .settings(
    name := "otel4s-benchmarks",
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion
    )
  )
  .settings(scalafixSettings)

lazy val examples = project
  .enablePlugins(NoPublishPlugin, JavaAgent)
  .in(file("examples"))
  .dependsOn(core.jvm, oteljava, sdk.jvm, `sdk-exporter`.jvm)
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
    envVars ++= Map(
      "OTEL_PROPAGATORS" -> "b3multi",
      "OTEL_SERVICE_NAME" -> "Trace Example"
    )
  )
  .settings(scalafixSettings)

lazy val docs = project
  .in(file("site"))
  .enablePlugins(TypelevelSitePlugin)
  .dependsOn(oteljava, sdk.jvm, `sdk-exporter`.jvm)
  .settings(
    libraryDependencies ++= Seq(
      "org.apache.pekko" %% "pekko-http" % PekkoHttpVersion,
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % OpenTelemetryVersion,
      "io.opentelemetry.instrumentation" % "opentelemetry-instrumentation-annotations" % OpenTelemetryInstrumentationVersion
    ),
    mdocVariables ++= Map(
      "OPEN_TELEMETRY_VERSION" -> OpenTelemetryVersion
    ),
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
      `sdk-metrics`.jvm,
      `sdk-trace`.jvm,
      `sdk-trace-testkit`.jvm,
      sdk.jvm,
      `sdk-exporter-common`.jvm,
      `sdk-exporter-trace`.jvm,
      `sdk-exporter`.jvm,
      `oteljava-common`,
      `oteljava-common-testkit`,
      `oteljava-metrics`,
      `oteljava-metrics-testkit`,
      `oteljava-trace`,
      `oteljava-trace-testkit`,
      `oteljava-testkit`,
      oteljava,
      semconv.jvm
    )
  )
