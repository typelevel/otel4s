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

### 3. aws-ec2

The detector fetches instance metadata from the `http://169.254.169.254` endpoint.
See [AWS documentation](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instancedata-data-retrieval.html) for more
details.

```scala mdoc:reset:passthrough
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import io.circe.Json
import io.circe.syntax._
import org.http4s._
import org.http4s.circe.jsonEncoder
import org.http4s.client.Client
import org.http4s.dsl.io._
import org.http4s.syntax.literals._
import org.typelevel.otel4s.sdk.contrib.aws.resource._

val hostname = "ip-10-0-0-1.eu-west-1.compute.internal"
val metadata = Json.obj(
  "accountId" := "1234567890",
  "architecture" := "x86_64",
  "availabilityZone" := "eu-west-1a",
  "imageId" := "ami-abc123de",
  "instanceId" := "i-abc321de",
  "instanceType" := "t3.small",
  "privateIp" := "10.0.0.1",
  "region" := "eu-west-1",
  "version" := "2017-09-30"
)

val client = Client.fromHttpApp[IO](
  HttpRoutes
    .of[IO] {
      case GET -> Root / "latest" / "meta-data" / "hostname" => Ok(hostname)
      case GET -> Root / "latest" / "dynamic" / "instance-identity" / "document" => Ok(metadata)
      case PUT -> Root / "latest" / "api" / "token" => Ok("token")
    }
    .orNotFound
)

println("The `http://169.254.169.254/latest/dynamic/instance-identity/document` response: ")
println("```json")
println(metadata)
println("```")

println("The `http://169.254.169.254/latest/meta-data/hostname` response:")
println("```")
println(hostname)
println("```")

println("Detected resource: ")
println("```")
AWSEC2Detector[IO](uri"", client).detect.unsafeRunSync().foreach { resource =>
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
        // register AWS EC2 detector
         .addResourceDetector(AWSEC2Detector[IO])
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
        // register AWS EC2 detector
         .addResourceDetector(AWSEC2Detector[IO])
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
javaOptions += "-Dotel.otel4s.resource.detectors.enabled=aws-lambda,aws-ec2"
envVars ++= Map("OTEL_OTEL4S_RESOURCE_DETECTORS_ENABLE" -> "aws-lambda,aws-ec2")
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using javaOpt -Dotel.otel4s.resource.detectors.enabled=aws-lambda,aws-ec2
```

@:choice(shell)

```shell
$ export OTEL_OTEL4S_RESOURCE_DETECTORS_ENABLED=aws-lambda,aws-ec2
```
@:@
