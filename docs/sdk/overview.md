# Overview

SDK modules are implemented in Scala and available for all platforms: JVM, Scala Native, and Scala.js.
The implementation remains **experimental** and some functionality may be lacking.

## Getting Started

@:select(build-tool)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %%% "otel4s-sdk" % "@VERSION@", // <1>
  "org.typelevel" %%% "otel4s-sdk-exporter" % "@VERSION@" // <2>
)
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using lib "org.typelevel::otel4s-sdk::@VERSION@" // <1>
//> using lib "org.typelevel::otel4s-sdk-exporter::@VERSION@" // <2>
```

@:@

1. Add the `otel4s-sdk` library
2. Add the `otel4s-sdk-exporter` library. Without the exporter, the application will crash

_______

Then use `OpenTelemetrySdk.autoConfigured` to autoconfigure the SDK:
```scala mdoc:silent:reset
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.sdk.OpenTelemetrySdk
import org.typelevel.otel4s.sdk.exporter.otlp.autoconfigure.OtlpExportersAutoConfigure
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.trace.TracerProvider

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] =
    OpenTelemetrySdk
      .autoConfigured[IO]( // register OTLP exporters configurer
        _.addExportersConfigurer(OtlpExportersAutoConfigure[IO]) 
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

## Configuration

The `.autoConfigured(...)` relies on the environment variables and system properties to configure the SDK.
For example, use `export OTEL_SERVICE_NAME=auth-service` to configure the name of the service.

See the full set of the [supported options](configuration.md).

## Limitations

### No autoload of third-party components

Since Scala Native and Scala.js lack [SPI](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html) support, 
third-party components cannot be loaded dynamically as OpenTelemetry Java does.

Hence, the configurers must be registered manually:
```scala mdoc:silent
OpenTelemetrySdk.autoConfigured[IO](
  _.addExportersConfigurer(OtlpExportersAutoConfigure[IO])
)
```

### No environment-aware Telemetry Resource

OpenTelemetry Java can detect a large variety of environments (e.g. GCP, AWS ECS, etc) and add additional 
environment-specific attributes to the Telemetry Resource.

Otel4s SDK module does not support this yet. But we have a plan to provide this functionality in the future.

### Metrics missing features

- `Exponential Histogram` aggregation is not supported yet
- `Prometheus` exporter is not implemented yet
