# Testkit | Metrics

The metrics testkit provides a partial-matching expectation API for OpenTelemetry Java `MetricData`.

This is useful in tests because metric data contains much more than the values you usually care about:
instrumentation scope, telemetry resource, point attributes, collection timestamps, exemplars, histogram buckets,
and so on.

The expectation API lets you assert only the relevant parts of a metric while still preserving detail when you need
it.

## Getting started

The metrics expectation API lives in `otel4s-oteljava-testkit`.

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-oteljava-testkit" % "@VERSION@" % Test
)
```

## Basic flow

The usual flow is:

1. Run your program against `MetricsTestkit` or `OtelJavaTestkit`
2. Collect metrics as `MetricData`
3. Build `MetricExpectation` values
4. Check them with `MetricExpectations.checkAll`

```scala
import cats.effect.IO
import io.opentelemetry.sdk.metrics.data.MetricData
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.testkit.metrics.{
  MetricExpectation,
  MetricExpectations,
  MetricsTestkit,
  PointExpectation
}

def program(meterProvider: MeterProvider[IO]): IO[Unit] =
  for {
    meter <- meterProvider.get("service")
    counter <- meter.counter[Long]("service.counter").create
    _ <- counter.inc()
    gauge <- meter.gauge[Long]("service.gauge").create
    _ <- gauge.record(42L)
  } yield ()

def assertExpected(metrics: List[MetricData], expected: MetricExpectation*): Unit =
  MetricExpectations.checkAll(metrics, expected: _*) match {
    case Right(_) =>
      ()
    case Left(mismatches) =>
      fail(MetricExpectations.format(mismatches))
  }

def test: IO[Unit] =
  MetricsTestkit.inMemory[IO]().use { testkit =>
    for {
      _ <- program(testkit.meterProvider)
      metrics <- testkit.collectMetrics[MetricData]
    } yield assertExpected(
      metrics,
      MetricExpectation.sum[Long]("service.counter").withValue(1L),
      MetricExpectation.gauge[Long]("service.gauge").withValue(42L)
    )
  }
```

## Partial matching

All expectations are partial.

This means:
- unspecified properties are ignored
- you can assert only the parts that matter for a test
- you can still add detail when needed

For example:

```scala
MetricExpectation.name("service.counter")
```

matches any collected metric named `service.counter`, regardless of its type, points, scope, or resource.

```scala
MetricExpectation.sum[Long]("service.counter").withValue(1L)
```

matches a long sum metric named `service.counter` that has at least one point with value `1L`.

## Numeric metrics

Use `MetricExpectation.gauge[A]` and `MetricExpectation.sum[A]` for numeric metrics.

```scala
MetricExpectation.gauge[Double]("service.temperature")
MetricExpectation.sum[Long]("service.requests")
```

The measurement type remains generic. `Long` and `Double` use the same API.

For value-only checks, `withValue(...)` is the most compact form:

```scala
MetricExpectation.sum[Long]("service.requests").withValue(1L)
```

If you also care about point attributes, prefer `withAnyPoint(...)`:

```scala
import org.typelevel.otel4s.Attribute

MetricExpectation
  .sum[Long]("service.requests")
  .withValue(1L, Attributes(Attribute("http.method", "GET")))
```

`withValue(value, attributes...)` uses exact attribute matching. It is equivalent to:

```scala
MetricExpectation
  .sum[Long]("service.requests")
  .withAnyPoint(
    PointExpectation
      .numeric(1L)
      .withAttributesExact(Attribute("http.method", "GET"))
  )
```

Use `withAnyPoint(...)` directly when you need subset matching or a more detailed point expectation.

## Point attributes

Point attributes can be matched in three ways:

- `withAttributesExact(...)` for exact equality
- `withAttributesSubset(...)` for subset matching
- `withAttributes(AttributesExpectation...)` for full control

Both `withAttributesExact(...)` and `withAttributesSubset(...)` support `Attributes` and varargs of `Attribute[_]`.

```scala
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.testkit.AttributesExpectation

PointExpectation
  .numeric(1L)
  .withAttributesExact(Attribute("http.method", "GET"))

PointExpectation
  .numeric(1L)
  .withAttributesSubset(
    Attribute("http.method", "GET"),
    Attribute("http.route", "/users")
  )

PointExpectation
  .numeric(1L)
  .withAttributes(
    AttributesExpectation.predicate("expected only one attribute") { attributes =>
      attributes == Attributes(Attribute("http.method", "GET"))
    }
  )
```

Empty attributes can be matched with:

```scala
PointExpectation.numeric(1L).withAttributesEmpty
```

## Scope and resource expectations

Metric expectations can also assert the instrumentation scope and telemetry resource.

This is useful when you want tests to preserve the same level of detail as raw `MetricData`.

```scala
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.oteljava.testkit.{
  InstrumentationScopeExpectation,
  TelemetryResourceExpectation
}

MetricExpectation
  .sum[Long]("service.requests")
  .withAnyPoint(
    PointExpectation
      .numeric(1L)
      .withAttributesExact(Attribute("http.method", "GET"))
  )
  .withScope(
    InstrumentationScopeExpectation
      .name("service")
      .withVersion("1.0")
      .withSchemaUrl("https://opentelemetry.io/schemas/1.24.0")
      .withAttributesEmpty
  )
  .withResource(
    TelemetryResourceExpectation.any
      .withAttributesSubset(Attribute("service.name", "auth-service"))
      .withSchemaUrl(None)
  )
```

As with point attributes, scope and resource attribute helpers support both `Attributes` and varargs.

## Summaries and histograms

The testkit also supports non-numeric point kinds directly.

### Summary

```scala
MetricExpectation
  .summary("rpc.duration")
  .withAnyPoint(
    PointExpectation.summary
      .withCount(1L)
      .withSum(42.0)
  )
```

### Histogram

```scala
import org.typelevel.otel4s.metrics.BucketBoundaries

MetricExpectation
  .histogram("http.server.duration")
  .withAnyPoint(
    PointExpectation.histogram
      .withCount(3L)
      .withSum(42.0)
      .withBoundaries(BucketBoundaries(Vector(0.1, 1.0, 10.0)))
      .withCounts(List(1L, 1L, 1L, 0L))
  )
```

### Exponential histogram

```scala
MetricExpectation
  .exponentialHistogram("queue.depth")
  .withAnyPoint(
    PointExpectation.exponentialHistogram
      .withScale(2)
      .withCount(10L)
      .withSum(100.0)
      .withZeroCount(0L)
  )
```

## Matching multiple points

Use `withAnyPoint(...)` when at least one collected point must match.

```scala
MetricExpectation
  .sum[Long]("service.requests")
  .withAnyPoint(
    PointExpectation
      .numeric(1L)
      .withAttributesExact(Attribute("http.method", "GET"))
  )
```

Use `withAllPoints(...)` when every collected point must satisfy the same expectation.

```scala
MetricExpectation
  .sum[Long]("service.requests")
  .withAllPoints(
    PointExpectation
      .numeric(1L)
      .withAttributesSubset(Attribute("region", "eu"))
  )
```

## Failure reporting

The API is framework-agnostic, so it does not provide assertions directly.
Instead, it returns structured mismatches and a formatter.

```scala
MetricExpectations.checkAll(metrics, expected: _*) match {
  case Right(_) =>
    ()
  case Left(mismatches) =>
    fail(MetricExpectations.format(mismatches))
}
```

There are two main failure cases:

- `NotFound`: no collected metric looked like a match
- `ClosestMismatch`: a likely candidate existed, but some part of it did not match

This is especially helpful when a metric name is correct but, for example, one scope attribute or one point
attribute is wrong.

## Clues

Both metric and point expectations support optional clues.

```scala
MetricExpectation
  .sum[Long]("service.requests")
  .withClue("the request counter must be emitted")
  .withAnyPoint(
    PointExpectation
      .numeric(1L)
      .withClue("GET requests should increment the counter")
      .withAttributesExact(Attribute("http.method", "GET"))
  )
```

Clues are included in mismatch messages to make failures easier to interpret.

## Numeric comparison

Numeric expectations use `NumberComparison[A]`.

- `Long` uses exact comparison
- `Double` uses the default `NumberComparison[Double]`

This applies to:
- numeric point values
- summary sums
- histogram sums
- histogram boundaries
- exponential histogram sums

If needed, you can override the default `Double` comparison implicitly in a test suite:

```scala
import org.typelevel.otel4s.oteljava.testkit.metrics.NumberComparison

implicit val cmp: NumberComparison[Double] =
  NumberComparison.within(1e-4)
```

After that, double-based point expectations built in that scope will use the custom comparison.

## Suggested helper

Most suites benefit from a small local helper:

```scala
def assertExpected(metrics: List[MetricData], expected: MetricExpectation*): Unit =
  MetricExpectations.checkAll(metrics, expected: _*) match {
    case Right(_) =>
      ()
    case Left(mismatches) =>
      fail(MetricExpectations.format(mismatches))
  }
```

This keeps tests concise while preserving detailed mismatch output.
