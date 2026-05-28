# Register Cats Effect runtime metrics

Use this page when you want to export runtime metrics such as CPU starvation and work-stealing thread pool activity from
a Cats Effect application.

## Prerequisites

- [Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md)

## 1. Add the dependency

@:select(build-tool)

@:choice(sbt)

Add settings to `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %%% "otel4s-instrumentation-metrics" % "@VERSION@" // <1>
)
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using dep "org.typelevel::otel4s-instrumentation-metrics::@VERSION@" // <1>
```

@:@

1. Add `otel4s-instrumentation-metrics`

## 2. Register the collectors at application startup

Register the collectors after you create `OtelJava` and keep them active while your application runs.

```scala mdoc:silent
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.instrumentation.ce.IORuntimeMetrics
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.OtelJava

object Main extends IOApp.Simple {
  def run: IO[Unit] =
    OtelJava.autoConfigured[IO]().use { otel4s =>
      implicit val meterProvider: MeterProvider[IO] = otel4s.meterProvider

      IORuntimeMetrics
        .register[IO](runtime.metrics, IORuntimeMetrics.Config.default)
        .surround(program)
    }

  def program: IO[Unit] =
    IO.never
}
```

## 3. Keep the registration scope open while metrics should be collected

`IORuntimeMetrics.register` returns a `Resource`.

That resource owns the runtime collectors. In practice, this means:

- use `.surround(...)` when the collectors should stay active for the whole program
- or keep the `Resource` open around the part of the application that should emit runtime metrics

## 4. Customize the config only when you need to

Start with `IORuntimeMetrics.Config.default`. Change it when you need to disable parts of the metric set or add
attributes.

For example, to disable CPU starvation metrics:

```scala mdoc:silent
import org.typelevel.otel4s.instrumentation.ce.IORuntimeMetrics.Config._
import org.typelevel.otel4s.metrics.MeterProvider

val runtime = cats.effect.unsafe.implicits.global

val config: IORuntimeMetrics.Config =
  IORuntimeMetrics.Config(
    CpuStarvationConfig.disabled,
    WorkStealingThreadPoolConfig.enabled
  )

def register(implicit meterProvider: MeterProvider[IO]) =
  IORuntimeMetrics.register[IO](runtime.metrics, config)
```

For the full metric list and the full configuration surface, see
[Metrics | Cats Effect IO runtime](../instrumentation/metrics-cats-effect-io-runtime.md).

## What's next

- Record application-specific metrics in your own code:
  [Record application metrics](record-application-metrics.md)
- Export JVM runtime metrics with OpenTelemetry Java:
  [Metrics | JVM Runtime](../oteljava/metrics-jvm-runtime.md)
