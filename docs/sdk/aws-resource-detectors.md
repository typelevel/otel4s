# AWS | Resource detectors

Resource detectors can add environment-specific attributes to the telemetry resource. 
AWS detectors are implemented as a third-party library, and you need to enable them manually.

## The list of detectors

### 1. aws-lambda

The detector relies on the `AWS_REGION`, `AWS_LAMBDA_FUNCTION_NAME`, and `AWS_LAMBDA_FUNCTION_VERSION` environment variables
to configure the telemetry resource.
Either `AWS_LAMBDA_FUNCTION_NAME` or `AWS_LAMBDA_FUNCTION_VERSION` must be present.

```scala mdoc:passthrough
import cats.effect.IO
import cats.effect.std.Env
import cats.effect.unsafe.implicits.global
import org.typelevel.otel4s.sdk.contrib.aws.resource._
import scala.collection.immutable

val envEntries = Map(
  "AWS_REGION" -> "eu-west-1",
  "AWS_LAMBDA_FUNCTION_NAME" -> "function",
  "AWS_LAMBDA_FUNCTION_VERSION" -> "0.0.1"
)

implicit val env: Env[IO] =
  new Env[IO] {
    def get(name: String): IO[Option[String]] = IO.pure(envEntries.get(name))
    def entries: IO[immutable.Iterable[(String, String)]] = IO.pure(envEntries)
  }

println("Environment: ")
println("```")
envEntries.foreach { case (k, v) => println(s"${k.replace("_", "_")}=$v") }
println("```")

println("Detected resource: ")
println("```")
AWSLambdaDetector[IO].detect.unsafeRunSync().foreach { resource =>
  resource.attributes.toList.sortBy(_.key.name).foreach { attribute =>
    println(attribute.key.name + ": " + attribute.value)
  }
}
println("```")
```

## Getting Started

@:select(build-tool)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %%% "otel4s-sdk" % "@VERSION@", // <1>
  "org.typelevel" %%% "otel4s-sdk-exporter" % "@VERSION@", // <2>
  "org.typelevel" %%% "otel4s-sdk-contrib-aws-resource" % "@VERSION@" // <3>
)
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using lib "org.typelevel::otel4s-sdk::@VERSION@" // <1>
//> using lib "org.typelevel::otel4s-sdk-exporter::@VERSION@" // <2>
//> using lib "org.typelevel::otel4s-sdk-contrib-aws-resource::@VERSION@" // <3>
```

@:@

1. Add the `otel4s-sdk` library
2. Add the `otel4s-sdk-exporter` library. Without the exporter, the application will crash
3. Add the `otel4s-sdk-contrib-aws-resource` library 

_______

Then autoconfigure the SDK:

@:select(sdk-entry-point)

@:choice(sdk)

`OpenTelemetrySdk.autoConfigured` configures both `MeterProvider` and `TracerProvider`:

```scala mdoc:silent:reset
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.OpenTelemetrySdk
import org.typelevel.otel4s.sdk.contrib.aws.resource._
import org.typelevel.otel4s.sdk.exporter.otlp.autoconfigure.OtlpExportersAutoConfigure
import org.typelevel.otel4s.trace.TracerProvider

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] =
    OpenTelemetrySdk
      .autoConfigured[IO](
        // register OTLP exporters configurer
        _.addExportersConfigurer(OtlpExportersAutoConfigure[IO])
        // register AWS Lambda detector
         .addResourceDetector(AWSLambdaDetector[IO])
      )
      .use { autoConfigured =>
        val sdk = autoConfigured.sdk
        program(sdk.meterProvider, sdk.tracerProvider)
      }

  def program(
      meterProvider: MeterProvider[IO],
      tracerProvider: TracerProvider[IO]
  ): IO[Unit] =
    ???
}
```

@:choice(traces)

`SdkTraces` configures only `TracerProvider`:

```scala mdoc:silent:reset
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.sdk.contrib.aws.resource._
import org.typelevel.otel4s.sdk.exporter.otlp.trace.autoconfigure.OtlpSpanExporterAutoConfigure
import org.typelevel.otel4s.sdk.trace.SdkTraces
import org.typelevel.otel4s.trace.TracerProvider

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] =
    SdkTraces
      .autoConfigured[IO]( 
        // register OTLP exporters configurer
        _.addExporterConfigurer(OtlpSpanExporterAutoConfigure[IO])
        // register AWS Lambda detector
         .addResourceDetector(AWSLambdaDetector[IO])
      )
      .use { autoConfigured =>
        program(autoConfigured.tracerProvider)
      }

  def program(
      tracerProvider: TracerProvider[IO]
  ): IO[Unit] =
    ???
}
```

@:@

## Configuration

The `OpenTelemetrySdk.autoConfigured(...)` and `SdkTraces.autoConfigured(...)` rely on the environment variables and system properties to configure the SDK.
Check out the [configuration details](configuration.md#telemetry-resource-detectors).

There are several ways to configure the options:

@:select(sdk-options-source)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
javaOptions += "-Dotel.otel4s.resource.detectors.enabled=aws-lambda"
envVars ++= Map("OTEL_OTEL4S_RESOURCE_DETECTORS_ENABLE" -> "aws-lambda")
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using javaOpt -Dotel.otel4s.resource.detectors.enabled=aws-lambda
```

@:choice(shell)

```shell
$ export OTEL_OTEL4S_RESOURCE_DETECTORS_ENABLED=aws-lambda
```
@:@
