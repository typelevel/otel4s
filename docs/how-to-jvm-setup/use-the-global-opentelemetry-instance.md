# Use the global OpenTelemetry instance

Use this page when OpenTelemetry is already configured outside `otel4s`, and you want to reuse that process-wide instance.

Common cases:

- you use a Java agent
- another library or framework configures OpenTelemetry before your code runs
- you need `otel4s` to interoperate with Java libraries that expect the global SDK

## Prerequisites

- Your application depends on `"org.typelevel" %% "otel4s-oteljava" % "@VERSION@"`.
- A global OpenTelemetry SDK is configured elsewhere.
- If you need Cats Effect and Java SDK context to stay aligned, add
  `otel4s-oteljava-context-storage` and follow
  [Keep otel4s context in sync with OpenTelemetry Java](keep-otel4s-context-in-sync-with-opentelemetry-java.md).

## 1. Make sure the global SDK is configured elsewhere

`OtelJava.global` does not create or configure an SDK.
It only reads the current global OpenTelemetry instance.

That instance might come from:

- the OpenTelemetry Java agent
- your own SDK bootstrap code
- framework-managed startup code

## 2. Read the global instance from otel4s

```scala mdoc:reset:silent
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.trace.TracerProvider

object Main extends IOApp.Simple {
  def run: IO[Unit] =
    OtelJava.global[IO].flatMap { otel4s =>
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

## 3. Do not create a second SDK in the same application

If your process already has a configured global SDK, do not also call `OtelJava.autoConfigured` for the same application path.

Use:

- `OtelJava.autoConfigured` when your application owns SDK creation
- `OtelJava.global` when SDK creation happens elsewhere

## What's next

- Align Cats Effect and Java SDK context:
  [Keep otel4s context in sync with OpenTelemetry Java](keep-otel4s-context-in-sync-with-opentelemetry-java.md)
- Use otel4s with the Java agent:
  [Zero-code | Java Agent](../oteljava/agent.md)
- Create spans in your application code:
  [Create spans around effectful code](../how-to-tracing/create-spans-around-effectful-code.md)
- Use otel4s with Java-instrumented libraries:
  [Use otel4s with Java-instrumented libraries](../how-to-tracing/use-otel4s-with-java-instrumented-libraries.md)
- Record application metrics:
  [Record application metrics](../how-to-metrics/record-application-metrics.md)
- Register Cats Effect runtime metrics:
  [Register Cats Effect runtime metrics](../how-to-metrics/register-cats-effect-runtime-metrics.md)
