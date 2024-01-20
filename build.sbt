import com.typesafe.tools.mima.core._

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

val Scala213 = "2.13.12"
ThisBuild / crossScalaVersions := Seq(Scala213, "3.3.1")
ThisBuild / scalaVersion := Scala213 // the default Scala

val CatsVersion = "2.10.0"
val CatsEffectVersion = "3.5.3"
val CatsMtlVersion = "1.4.0"
val FS2Version = "3.9.4"
val MUnitVersion = "1.0.0-M10"
val MUnitCatsEffectVersion = "2.0.0-M4"
val MUnitDisciplineVersion = "2.0.0-M3"
val MUnitScalaCheckEffectVersion = "2.0.0-M2"
val OpenTelemetryVersion = "1.34.1"
val OpenTelemetryInstrumentationVersion = "2.0.0"
val OpenTelemetrySemConvVersion = "1.23.1-alpha"
val PekkoStreamVersion = "1.0.2"
val PekkoHttpVersion = "1.0.0"
val PlatformVersion = "1.0.2"
val ScodecVersion = "1.1.38"
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
    `sdk-trace`,
    sdk,
    `testkit-common`,
    `testkit-metrics`,
    testkit,
    `oteljava-common`,
    `oteljava-metrics`,
    `oteljava-trace`,
    oteljava,
    semconv,
    benchmarks,
    examples,
    unidocs
  )
  .settings(name := "otel4s")

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
      "org.typelevel" %%% "cats-effect-testkit" % CatsEffectVersion % Test
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
      "org.typelevel" %%% "discipline-munit" % MUnitDisciplineVersion % Test
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

lazy val `sdk-trace` = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .enablePlugins(NoPublishPlugin)
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

lazy val sdk = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .enablePlugins(NoPublishPlugin)
  .in(file("sdk/all"))
  .dependsOn(`sdk-common`, `sdk-trace`)
  .settings(
    name := "otel4s-sdk"
  )
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

lazy val `oteljava-common` = project
  .in(file("oteljava/common"))
  .enablePlugins(BuildInfoPlugin)
  .dependsOn(`core-common`.jvm, `testkit-common`.jvm % Test)
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

lazy val `oteljava-metrics` = project
  .in(file("oteljava/metrics"))
  .dependsOn(
    `oteljava-common`,
    `core-metrics`.jvm,
    `testkit-metrics`.jvm % Test
  )
  .settings(munitDependencies)
  .settings(
    name := "otel4s-oteljava-metrics",
    libraryDependencies ++= Seq(
      "io.opentelemetry" % "opentelemetry-sdk-testing" % OpenTelemetryVersion % Test
    )
  )
  .settings(scalafixSettings)

lazy val `oteljava-trace` = project
  .in(file("oteljava/trace"))
  .dependsOn(`oteljava-common`, `core-trace`.jvm)
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

lazy val oteljava = project
  .in(file("oteljava/all"))
  .dependsOn(core.jvm, `oteljava-metrics`, `oteljava-trace`)
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

lazy val benchmarks = project
  .enablePlugins(NoPublishPlugin)
  .enablePlugins(JmhPlugin)
  .in(file("benchmarks"))
  .dependsOn(core.jvm, sdk.jvm, oteljava, testkit.jvm)
  .settings(
    name := "otel4s-benchmarks"
  )
  .settings(scalafixSettings)

lazy val examples = project
  .enablePlugins(NoPublishPlugin, JavaAgent)
  .in(file("examples"))
  .dependsOn(core.jvm, oteljava)
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
  .dependsOn(oteljava)
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
      `sdk-trace`.jvm,
      sdk.jvm,
      `testkit-common`.jvm,
      `testkit-metrics`.jvm,
      testkit.jvm,
      `oteljava-common`,
      `oteljava-metrics`,
      `oteljava-trace`,
      oteljava,
      semconv.jvm
    )
  )
