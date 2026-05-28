# Register JVM runtime metrics

Use this page when you want to export JVM runtime metrics such as memory, threads, garbage collection, and CPU activity.

## Prerequisites

- [Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md)

## 1. Add the runtime telemetry dependency

This page assumes you already depend on `otel4s-oteljava` from the JVM setup guide.

@:select(build-tool)

@:choice(sbt)

Add settings to `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "io.opentelemetry.instrumentation" % "opentelemetry-runtime-telemetry" % "@OPEN_TELEMETRY_INSTRUMENTATION_ALPHA_VERSION@" // <1>
)
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using dep "io.opentelemetry.instrumentation:opentelemetry-runtime-telemetry:@OPEN_TELEMETRY_INSTRUMENTATION_ALPHA_VERSION@" // <1>
```

@:@

1. Add the OpenTelemetry Java runtime telemetry module

## 2. Register the runtime telemetry producers at startup

Register the producers after you create `OtelJava` and keep them active while your application runs.

```scala mdoc:silent
import cats.effect.{IO, IOApp, Resource, Sync}
import cats.syntax.all._
import io.opentelemetry.api.{OpenTelemetry => JOpenTelemetry}
import io.opentelemetry.instrumentation.runtimetelemetry.RuntimeTelemetry
import org.typelevel.otel4s.oteljava.OtelJava

object Main extends IOApp.Simple {
  def run: IO[Unit] =
    OtelJava
      .autoConfigured[IO]()
      .flatTap(otel4s => registerRuntimeTelemetry(otel4s.underlying))
      .use { otel4s =>
        program(otel4s)
      }

  def program(otel4s: OtelJava[IO]): IO[Unit] = {
    val _ = otel4s
    IO.never
  }

  def registerRuntimeTelemetry[F[_]: Sync](
      openTelemetry: JOpenTelemetry
  ): Resource[F, Unit] =
    Resource
      .fromAutoCloseable(Sync[F].delay(RuntimeTelemetry.create(openTelemetry)))
      .void
}
```

## 3. Keep the registration scope open while metrics should be collected

The runtime telemetry module returns an `AutoCloseable`.

Wrap it in a `Resource` and keep that resource open for as long as you want the JVM runtime metrics to be exported.

## 4. Use this together with application or Cats Effect runtime metrics when needed

JVM runtime metrics are separate from:

- application metrics you record with `Meter`
- Cats Effect runtime metrics you register with `IORuntimeMetrics.register`

It is fine to export both when you need both views of the process.

See the JVM metric names and conventions in the
[OpenTelemetry JVM runtime semantic conventions][semantic-conventions].

## What's next

- Record application-specific metrics in your own code:
  [Record application metrics](record-application-metrics.md)
- Export Cats Effect runtime metrics from the same application:
  [Register Cats Effect runtime metrics](register-cats-effect-runtime-metrics.md)

[semantic-conventions]: https://opentelemetry.io/docs/specs/semconv/runtime/jvm-metrics
