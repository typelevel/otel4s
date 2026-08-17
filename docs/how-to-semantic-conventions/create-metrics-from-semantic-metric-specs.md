# Create metrics from semantic metric specs

Use a generated metric constructor when you are implementing a metric defined by the OpenTelemetry semantic
conventions. The constructor applies the canonical metric name, unit, and description.

## Prerequisites

- [Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md)
- [Record application metrics](../how-to-metrics/record-application-metrics.md)

## 1. Add the stable metrics module

@:select(build-tool)

@:choice(sbt)

```scala
libraryDependencies +=
  "org.typelevel" %% "otel4s-semconv-metrics" % "@VERSION@"
```

@:choice(scala-cli)

```scala
//> using dep "org.typelevel::otel4s-semconv-metrics:@VERSION@"
```

@:@

The stable metric specs are available under `org.typelevel.otel4s.semconv.metrics`. This artifact also brings in the
stable semantic attributes used by those specs.

## 2. Call the generated constructor

Get a `Meter`, then call `create` on the generated metric object. Histogram constructors take explicit bucket
boundaries because the appropriate distribution depends on the values produced by your application.

```scala mdoc:reset:silent
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.metrics.{BucketBoundaries, Meter}
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.semconv.attributes.{HttpAttributes, ServerAttributes}
import org.typelevel.otel4s.semconv.metrics.HttpMetrics

object Main extends IOApp.Simple {
  def run: IO[Unit] =
    OtelJava.autoConfigured[IO]().use { otel4s =>
      otel4s.meterProvider.get("http-client").flatMap { implicit meter =>
        recordRequestDuration(0.125)
      }
    }

  def recordRequestDuration(seconds: Double)(implicit meter: Meter[IO]): IO[Unit] =
    HttpMetrics.ClientRequestDuration
      .create[IO, Double](
        BucketBoundaries(0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0)
      )
      .flatMap { duration =>
        duration.record(
          seconds,
          HttpAttributes.HttpRequestMethod("GET"),
          ServerAttributes.ServerAddress("example.com"),
          ServerAttributes.ServerPort(443L),
        )
      }
}
```

`HttpMetrics.ClientRequestDuration` defines the instrument as `http.client.request.duration`, with unit `s` and the
description from the semantic convention. The example records seconds and uses bucket boundaries in the same unit.

## 3. Supply the attributes required by the metric spec

The generated constructor configures the instrument metadata. It does not add attributes to measurements.

Use the metric object's `attributeSpecs` to see which attributes are required, conditionally required, recommended, or
opt-in. Add the applicable generated attributes each time you record a measurement.

## What's next

- Verify a metric implementation against its generated spec:
  [Test metrics against semantic conventions](../how-to-testkit/test-metrics-against-semantic-conventions.md)
- Customize histogram boundaries:
  [Customize histogram buckets](../how-to-metrics/customize-histogram-buckets.md)
- Look up the fields and constructors generated for metric specs:
  [Semantic conventions reference](../instrumentation/semantic-conventions.md)
