# Testkit

The `otel4s-oteljava-testkit` module provides in-memory metric, trace, and log exporters for the OpenTelemetry Java
backend.

Use it when you want to:

- run instrumentation against a real `otel4s-oteljava` implementation
- collect exported telemetry as OpenTelemetry Java SDK models
- assert telemetry with the expectation APIs instead of building local test ADTs

The testkit is framework-agnostic, so it can be used with munit, weaver, ScalaTest, or any other test framework.

## Getting started

@:select(build-tool)

@:choice(sbt)

Add settings to `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-oteljava-testkit" % "@VERSION@" % Test, // <1>
)
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using test.dep "org.typelevel::otel4s-oteljava-testkit:@VERSION@" // <1>
```

@:@

1. Add the `otel4s-oteljava-testkit` module to the test scope

## Choosing a testkit

You can use either the domain-specific testkits or the combined testkit:

- `MetricsTestkit` for metrics only
- `TracesTestkit` for traces only
- `LogsTestkit` for logs only
- `OtelJavaTestkit` when one test needs multiple signal types

The combined testkit exposes:

- `meterProvider`
- `tracerProvider`
- `loggerProvider`
- `collectMetrics`
- `finishedSpans`
- `finishedLogs`

```scala mdoc:silent
import cats.effect.IO
import org.typelevel.otel4s.oteljava.testkit.OtelJavaTestkit

def test: IO[Unit] =
  OtelJavaTestkit.inMemory[IO]().use { testkit =>
    for {
      metrics <- testkit.collectMetrics
      spans <- testkit.finishedSpans
      logs <- testkit.finishedLogs
    } yield {
      val _ = (metrics, spans, logs)
    }
  }
```

If a test exercises only one signal, the signal-specific testkit usually reads a bit more clearly.

## Expectation APIs

The recommended testing style is:

1. run the program against the in-memory testkit
2. collect raw OpenTelemetry Java SDK models
3. assert them with the expectation API

The expectation APIs are partial by default. This means:

- unspecified fields are ignored
- you can match only the properties that matter for the current test
- you can still add detail for timestamps, attributes, scope, resource, and structure when needed

Dedicated guides:

- [Testkit | Metrics](testkit-metrics.md)
- [Testkit | Traces](testkit-traces.md)
- [Testkit | Logs](testkit-logs.md)

### Metrics

Metrics are matched against OpenTelemetry Java `MetricData` using:

- `MetricExpectation`
- `PointExpectation`
- `PointSetExpectation`
- `MetricExpectations`

Use this when you want to assert metric name, metric type, values, point attributes, scope, resource, summaries,
histograms, exemplars, and collection-wide point constraints.

### Traces

Traces are matched against OpenTelemetry Java `SpanData` using:

- `SpanExpectation`
- `EventExpectation`
- `LinkExpectation`
- `TraceExpectation`
- `TraceForestExpectation`
- `SpanExpectations`
- `TraceExpectations`

Use `SpanExpectations` for flat exported-span checks and `TraceExpectations` when exact parent/child topology matters.

### Logs

Logs are matched against OpenTelemetry Java `LogRecordData` using:

- `LogRecordExpectation`
- `LogRecordExpectations`

Use this when you want to assert body/message, severity, trace/span correlation, attributes, scope, resource, and
timestamps directly on exported log records.

## Mismatch rendering

Each top-level expectation API has a formatting helper that turns structured mismatches into a readable failure message:

- `MetricExpectations.format(...)`
- `SpanExpectations.format(...)`
- `TraceExpectations.format(...)`
- `LogRecordExpectations.format(...)`

This is the easiest way to integrate expectations with a testing framework:

```scala mdoc:silent
import cats.data.NonEmptyList

def failWith[A](mismatches: NonEmptyList[A], render: NonEmptyList[A] => String): Nothing =
  sys.error(render(mismatches))
```

In practice:

```scala mdoc:silent
import cats.effect.IO
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.testkit.metrics._

def program(meterProvider: MeterProvider[IO]): IO[Unit] = {
  val _ = meterProvider
  IO.unit
}

def assertMetrics(testkit: MetricsTestkit[IO]): IO[Unit] =
  for {
    _ <- program(testkit.meterProvider)
    metrics <- testkit.collectMetrics
  } yield {
    MetricExpectations.checkAllDistinct(
      metrics,
      MetricExpectation.sum[Long]("service.requests")
    ) match {
      case Right(_) =>
        ()
      case Left(mismatches) =>
        sys.error(MetricExpectations.format(mismatches))
    }
  }
```

## When to use raw SDK models

The expectation APIs should be the default path, but raw model assertions still make sense when:

- you are investigating a failing test and want to inspect the full exported payload
- you need to compare a field that is not exposed by the current expectation DSL
- you want to prototype a new matcher before turning it into a reusable expectation

The in-memory testkits always expose the underlying OpenTelemetry Java SDK models:

- `MetricData`
- `SpanData`
- `LogRecordData`

That means you can mix both styles in the same test:

- use expectation APIs for the stable high-value assertions
- drop down to raw SDK models for one-off or low-level checks

## IOLocalContextStorage

The `otel4s-oteljava-testkit` module depends on `io.opentelemetry:opentelemetry-sdk-testing`, which sets the
`ContextStorageProvider` to `io.opentelemetry.sdk.testing.context.SettableContextStorageProvider`.

If you rely on `IOLocalContextStorage` in tests, you will see an error like:

```scala
java.lang.IllegalStateException: IOLocalContextStorage is not configured for use as the ContextStorageProvider.
The current ContextStorage is: io.opentelemetry.sdk.testing.context.SettableContextStorageProvider$SettableContextStorage
```

To solve this, use `IOLocalTestContextStorage` from the `otel4s-oteljava-context-storage-testkit` module.

@:select(build-tool)

@:choice(sbt)

Add settings to `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-oteljava-context-storage-testkit" % "@VERSION@" % Test,
)
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using test.dep "org.typelevel::otel4s-oteljava-context-storage-testkit:@VERSION@"
```

@:@

Parametrize your code to make `LocalContextProvider` overrideable:

```scala
import cats.effect.IO
import org.typelevel.otel4s.oteljava.context.LocalContextProvider

def program(implicit provider: LocalContextProvider[IO]): IO[Unit] = ???
```

And override it in tests:

```scala
import cats.effect.IO
import org.typelevel.otel4s.oteljava.context.LocalContextProvider
import org.typelevel.otel4s.oteljava.testkit.context.IOLocalTestContextStorage

def test: IO[Unit] = {
  implicit val provider: LocalContextProvider[IO] =
    IOLocalTestContextStorage.localProvider[IO]

  program
}
```
