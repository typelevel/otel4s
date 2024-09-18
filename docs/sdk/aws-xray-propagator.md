# AWS | X-Ray propagator

The X-Ray propagator implements AWS X-Ray Trace Header [propagation protocol][xray-concepts].
The propagator utilizes `X-Amzn-Trace-Id` header to extract and inject tracing details.
An example of the AWS X-Ray Tracing Header:
```
X-Amzn-Trace-Id: Root=1-5759e988-bd862e3fe1be46a994272793;Parent=53995c3f42cd8ad8;Sampled=1
```

## Getting Started

@:select(build-tool)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %%% "otel4s-sdk" % "@VERSION@", // <1>
  "org.typelevel" %%% "otel4s-sdk-exporter" % "@VERSION@", // <2>
  "org.typelevel" %%% "otel4s-sdk-contrib-aws-xray-propagator" % "@VERSION@" // <3>
)
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using lib "org.typelevel::otel4s-sdk::@VERSION@" // <1>
//> using lib "org.typelevel::otel4s-sdk-exporter::@VERSION@" // <2>
//> using lib "org.typelevel::otel4s-sdk-contrib-aws-xray-propagator::@VERSION@" // <3>
```

@:@

1. Add the `otel4s-sdk` library
2. Add the `otel4s-sdk-exporter` library. Without the exporter, the application will crash
3. Add the `otel4s-sdk-contrib-aws-xray-propagator` library 

_______

Then autoconfigure the SDK:

@:select(sdk-entry-point)

@:choice(sdk)

`OpenTelemetrySdk.autoConfigured` configures both `MeterProvider` and `TracerProvider`:

```scala mdoc:silent:reset
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.OpenTelemetrySdk
import org.typelevel.otel4s.sdk.contrib.aws.context.propagation._
import org.typelevel.otel4s.sdk.exporter.otlp.autoconfigure.OtlpExportersAutoConfigure
import org.typelevel.otel4s.trace.TracerProvider

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] =
    OpenTelemetrySdk
      .autoConfigured[IO]( 
        // register OTLP exporters configurer
        _.addExportersConfigurer(OtlpExportersAutoConfigure[IO])
        // add AWS X-Ray Propagator
         .addTracerProviderCustomizer((b, _) => 
           b.addTextMapPropagators(AwsXRayPropagator())
         )
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
import org.typelevel.otel4s.sdk.contrib.aws.context.propagation._
import org.typelevel.otel4s.sdk.exporter.otlp.trace.autoconfigure.OtlpSpanExporterAutoConfigure
import org.typelevel.otel4s.sdk.trace.SdkTraces
import org.typelevel.otel4s.trace.TracerProvider

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] =
    SdkTraces
      .autoConfigured[IO]( 
        // register OTLP exporters configurer
        _.addExporterConfigurer(OtlpSpanExporterAutoConfigure[IO])
        // add AWS X-Ray Propagator
         .addTracerProviderCustomizer((b, _) => 
           b.addTextMapPropagators(AwsXRayPropagator())
         )
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

## AWS Lambda

AWS Lambda can [utilize][lambda-xray-envvars] `_X_AMZN_TRACE_ID` environment variable or 
`com.amazonaws.xray.traceHeader` system property to set the X-Ray tracing header.

Use `AwsXRayLambdaPropagator` in such a case.

@:select(sdk-entry-point)

@:choice(sdk)

`OpenTelemetrySdk.autoConfigured` configures both `MeterProvider` and `TracerProvider`:

```scala mdoc:silent:reset
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.OpenTelemetrySdk
import org.typelevel.otel4s.sdk.contrib.aws.context.propagation._
import org.typelevel.otel4s.sdk.exporter.otlp.autoconfigure.OtlpExportersAutoConfigure
import org.typelevel.otel4s.trace.TracerProvider

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] =
    OpenTelemetrySdk
      .autoConfigured[IO]( 
        // register OTLP exporters configurer
        _.addExportersConfigurer(OtlpExportersAutoConfigure[IO])
        // add AWS X-Ray Lambda Propagator
         .addTracerProviderCustomizer((b, _) => 
           b.addTextMapPropagators(AwsXRayLambdaPropagator())
         )
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
import org.typelevel.otel4s.sdk.contrib.aws.context.propagation._
import org.typelevel.otel4s.sdk.exporter.otlp.trace.autoconfigure.OtlpSpanExporterAutoConfigure
import org.typelevel.otel4s.sdk.trace.SdkTraces
import org.typelevel.otel4s.trace.TracerProvider

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] =
    SdkTraces
      .autoConfigured[IO]( 
        // register OTLP exporters configurer
        _.addExporterConfigurer(OtlpSpanExporterAutoConfigure[IO])
        // add AWS X-Ray Lambda Propagator
         .addTracerProviderCustomizer((b, _) => 
           b.addTextMapPropagators(AwsXRayLambdaPropagator())
         )
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

[xray-concepts]: https://docs.aws.amazon.com/xray/latest/devguide/xray-concepts.html#xray-concepts-tracingheader
[lambda-xray-envvars]: https://docs.aws.amazon.com/lambda/latest/dg/configuration-envvars.html