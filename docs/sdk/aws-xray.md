# AWS | X-Ray ID Generator

The AWS X-Ray ID generator generates trace IDs that are compatible with AWS X-Ray tracing [spec][xray-traceid].
An example of the AWS X-Ray trace and span IDs:
```scala mdoc:passthrough
import cats.effect.IO
import cats.effect.std.Random
import cats.effect.unsafe.implicits.global
import org.typelevel.otel4s.sdk.trace.contrib.aws.xray._

val (traceId, spanId) = Random.scalaUtilRandom[IO].flatMap { implicit random =>
  val generator = AwsXRayIdGenerator[IO]
  for {
    traceId <- generator.generateTraceId
    spanId <- generator.generateSpanId
  } yield (traceId.toHex, spanId.toHex)
}.unsafeRunSync()

println("```yaml")
println("trace_id: " + traceId)
println("span_id: " + spanId)
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
  "org.typelevel" %%% "otel4s-sdk-contrib-aws-xray" % "@VERSION@" // <3>
)
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using dep "org.typelevel::otel4s-sdk::@VERSION@" // <1>
//> using dep "org.typelevel::otel4s-sdk-exporter::@VERSION@" // <2>
//> using dep "org.typelevel::otel4s-sdk-contrib-aws-xray::@VERSION@" // <3>
```

@:@

1. Add the `otel4s-sdk` library
2. Add the `otel4s-sdk-exporter` library. Without the exporter, the application will crash
3. Add the `otel4s-sdk-contrib-aws-xray` library

_______

Then autoconfigure the SDK:

@:select(sdk-entry-point)

@:choice(sdk)

`OpenTelemetrySdk.autoConfigured` configures both `MeterProvider` and `TracerProvider`:

```scala mdoc:reset:silent
import cats.effect.{IO, IOApp}
import cats.effect.std.Random
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.OpenTelemetrySdk
import org.typelevel.otel4s.sdk.exporter.otlp.autoconfigure.OtlpExportersAutoConfigure
import org.typelevel.otel4s.sdk.trace.contrib.aws.xray._
import org.typelevel.otel4s.trace.TracerProvider

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] = 
    Random.scalaUtilRandom[IO].flatMap { implicit random =>
      OpenTelemetrySdk
        .autoConfigured[IO](
          // register OTLP exporters configurer
          _.addExportersConfigurer(OtlpExportersAutoConfigure[IO])
            // add AWS X-Ray ID generator
            .addTracerProviderCustomizer((b, _) =>
              b.withIdGenerator(AwsXRayIdGenerator[IO])
            )
        )
        .use { autoConfigured =>
          val sdk = autoConfigured.sdk
          program(sdk.meterProvider, sdk.tracerProvider)
        } 
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

```scala mdoc:reset:silent
import cats.effect.{IO, IOApp}
import cats.effect.std.Random
import org.typelevel.otel4s.sdk.exporter.otlp.trace.autoconfigure.OtlpSpanExporterAutoConfigure
import org.typelevel.otel4s.sdk.trace.SdkTraces
import org.typelevel.otel4s.sdk.trace.contrib.aws.xray._
import org.typelevel.otel4s.trace.TracerProvider

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] =
    Random.scalaUtilRandom[IO].flatMap { implicit random =>
      SdkTraces
        .autoConfigured[IO](
          // register OTLP exporters configurer
          _.addExporterConfigurer(OtlpSpanExporterAutoConfigure[IO])
            // add AWS X-Ray ID generator
            .addTracerProviderCustomizer((b, _) =>
              b.withIdGenerator(AwsXRayIdGenerator[IO])
            )
        )
        .use { autoConfigured =>
          program(autoConfigured.tracerProvider)
        }
    }

  def program(
      tracerProvider: TracerProvider[IO]
  ): IO[Unit] =
    ???
}
```

@:@

[xray-traceid]: https://docs.aws.amazon.com/xray/latest/devguide/xray-api-sendingdata.html#xray-api-traceids
