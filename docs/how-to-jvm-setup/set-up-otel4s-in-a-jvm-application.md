# Set up otel4s in a JVM application

Use this page when you want to start a JVM application with `otel4s-oteljava` and export telemetry with OTLP.

## 1. Add the dependencies

@:select(build-tool)

@:choice(sbt)

Add settings to `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-oteljava" % "@VERSION@", // <1>
  "io.opentelemetry" % "opentelemetry-exporter-otlp" % "@OPEN_TELEMETRY_VERSION@" % Runtime, // <2>
  "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "@OPEN_TELEMETRY_VERSION@" % Runtime // <3>
)
javaOptions += "-Dotel.java.global-autoconfigure.enabled=true" // <4>
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using dep "org.typelevel::otel4s-oteljava:@VERSION@" // <1>
//> using dep "io.opentelemetry:opentelemetry-exporter-otlp:@OPEN_TELEMETRY_VERSION@" // <2>
//> using dep "io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:@OPEN_TELEMETRY_VERSION@" // <3>
//> using javaOpt "-Dotel.java.global-autoconfigure.enabled=true" // <4>
```

@:@

1. Add `otel4s-oteljava`
2. Add an OTLP exporter
3. Add the OpenTelemetry Java autoconfigure extension
4. Enable SDK autoconfiguration

## 2. Configure the SDK

Set the service name and the OTLP endpoint with environment variables or JVM properties.

Example environment variables:

```shell
export OTEL_SERVICE_NAME=auth-service
export OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
```

Example JVM properties:

```shell
-Dotel.service.name=auth-service
-Dotel.exporter.otlp.endpoint=http://localhost:4317
```

See the full list of supported options in the
[OpenTelemetry Java configuration guide][opentelemetry-java-configuration].

## 3. Create `OtelJava` and get the providers

Use `OtelJava.autoConfigured` when your application is responsible for creating its own OpenTelemetry SDK instance.

```scala mdoc:reset:silent
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.trace.TracerProvider

object Main extends IOApp.Simple {
  def run: IO[Unit] =
    OtelJava.autoConfigured[IO]().use { otel4s =>
      program(otel4s.meterProvider, otel4s.tracerProvider)
    }

  def program(
      meterProvider: MeterProvider[IO],
      tracerProvider: TracerProvider[IO]
  ): IO[Unit] =
    for {
      meter <- meterProvider.get("auth-service")
      tracer <- tracerProvider.get("auth-service")

      counter <- meter.counter[Long]("service.requests").create
      _ <- counter.inc()

      _ <- tracer.span("startup").surround(IO.unit)
    } yield ()
}
```

@:callout(warning)

`OtelJava.autoConfigured` creates an isolated, non-global SDK instance.
If you need to reuse the process-wide OpenTelemetry instance, use
[Use the global OpenTelemetry instance](use-the-global-opentelemetry-instance.md).

@:@

## 4. Run the application

Start your application with the same environment variables or JVM properties you used for configuration.

At that point, the application can create meters and tracers and export telemetry through OTLP.

## 5. Verify the setup

If you want a local target for traces, use
[Jaeger and Docker](../examples/jaeger-docker/README.md).
If you want a local stack for traces and metrics, use
[Grafana](../examples/grafana/README.md).

## What's next

- Create spans in your application code:
  [Tracing](../instrumentation/tracing.md)
- Record application metrics:
  [Record application metrics](../how-to-metrics/record-application-metrics.md)
- Register Cats Effect runtime metrics:
  [Register Cats Effect runtime metrics](../how-to-metrics/register-cats-effect-runtime-metrics.md)
- Reuse an OpenTelemetry instance that is configured elsewhere:
  [Use the global OpenTelemetry instance](use-the-global-opentelemetry-instance.md)

[opentelemetry-java-configuration]: https://opentelemetry.io/docs/languages/java/configuration/
