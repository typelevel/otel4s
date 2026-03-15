# Testkit | Metrics

The metrics testkit provides a partial-matching expectation API for OpenTelemetry Java `MetricData`.

This is useful in tests because metric data contains much more than the values you usually care about:
instrumentation scope, telemetry resource, point attributes, collection timestamps, exemplars, histogram buckets,
and so on.

The expectation API lets you assert only the relevant parts of a metric while still preserving detail when you need
it. Metric point matching is expressed at the collection level, so a single metric expectation can accumulate
multiple point constraints.

## Getting started

@:select(build-tool)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-oteljava-testkit" % "@VERSION@" % Test,
)
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using test.dep "org.typelevel::otel4s-oteljava-testkit:@VERSION@" 
```

@:@

## Basic flow

The usual flow is:

1. Run your program against `MetricsTestkit` or `OtelJavaTestkit`
2. Collect metrics as `MetricData`
3. Build `MetricExpectation` values
4. Check them with `MetricExpectations.checkAll`

```scala mdoc:silent
import cats.effect.IO
import io.opentelemetry.sdk.metrics.data.MetricData
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.testkit.metrics._

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
      sys.error(MetricExpectations.format(mismatches))
  }

def test: IO[Unit] =
  MetricsTestkit.inMemory[IO]().use { testkit =>
    for {
      _ <- program(testkit.meterProvider)
      metrics <- testkit.collectAllMetrics
    } yield assertExpected(
      metrics,
      MetricExpectation.sum[Long]("service.counter").withValue(1L),
      MetricExpectation.gauge[Long]("service.gauge").withValue(42L)
    )
  }
```

`checkAll(...)` is non-consuming: each expectation is checked independently against the full collected metric list.
If you need to ensure that repeated expectations match different collected metrics, use
`MetricExpectations.checkAllDistinct(...)` instead.

```scala mdoc:silent
def checkAllDistinct(metrics: List[MetricData]) =
  MetricExpectations.checkAllDistinct(
    metrics,
    MetricExpectation.sum[Long]("service.counter").withValue(1L),
    MetricExpectation.sum[Long]("service.counter").withValue(1L)
  )
```

## Partial matching

All expectations are partial.

This means:
- unspecified properties are ignored
- you can assert only the parts that matter for a test
- you can still add detail when needed

At the metric level, partial matching is also non-consuming by default: repeating the same `MetricExpectation` in
`checkAll(...)` does not require two distinct collected metrics.

For example:

```scala mdoc:silent
MetricExpectation.name("service.counter")
```

matches any collected metric named `service.counter`, regardless of its type, points, scope, or resource.

```scala mdoc:silent
MetricExpectation.sum[Long]("service.counter").withValue(1L)
```

matches a long sum metric named `service.counter` that has at least one point with value `1L`.

## Numeric metrics

Use `MetricExpectation.gauge[A]` and `MetricExpectation.sum[A]` for numeric metrics.

```scala mdoc:silent
MetricExpectation.gauge[Double]("service.temperature")
MetricExpectation.sum[Long]("service.requests")
```

The measurement type remains generic. `Long` and `Double` use the same API.

For value-only checks, `withValue(...)` is the most compact form:

```scala mdoc:silent
MetricExpectation.sum[Long]("service.requests").withValue(1L)
```

If you also care about point attributes, there is a shorthand for exact point matching:

```scala mdoc:silent
import org.typelevel.otel4s.Attribute

MetricExpectation
  .sum[Long]("service.requests")
  .withValue(1L, Attribute("http.method", "GET"))
```

`withValue(value, attributes)` uses exact attribute matching. It is equivalent to:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .withPoints(
    PointSetExpectation.exists(
      PointExpectation
        .numeric(1L)
        .withAttributesExact(Attribute("http.method", "GET"))
    )
  )
```

Use `withPoints(PointSetExpectation.exists(...))` directly when you need subset matching or a more detailed point expectation.

## Point attributes

Point attributes can be matched in three ways:

- `withAttributesExact(...)` for exact equality
- `withAttributesSubset(...)` for subset matching
- `withAttributes(AttributesExpectation...)` for full control

Both `withAttributesExact(...)` and `withAttributesSubset(...)` support `Attributes` and varargs of `Attribute[_]`.

```scala mdoc:silent
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
    AttributesExpectation.where("expected only one attribute") { attributes =>
      attributes == Attributes(Attribute("http.method", "GET"))
    }
  )
```

Empty attributes can be matched with:

```scala mdoc:silent
PointExpectation.numeric(1L).withAttributesEmpty
```

## Scope and resource expectations

Metric expectations can also assert the instrumentation scope and telemetry resource.

This is useful when you want tests to preserve the same level of detail as raw `MetricData`.

```scala mdoc:silent
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.oteljava.testkit.{
  InstrumentationScopeExpectation,
  TelemetryResourceExpectation
}

MetricExpectation
  .sum[Long]("service.requests")
  .withPoints(
    PointSetExpectation.exists(
      PointExpectation
        .numeric(1L)
        .withAttributesExact(Attribute("http.method", "GET"))
    )
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
They also provide `withAttributesEmpty` when you want to assert that no attributes are present.

## Summaries and histograms

The testkit also supports non-numeric point kinds directly.

### Summary

```scala mdoc:silent
MetricExpectation
  .summary("rpc.duration")
  .withPoints(
    PointSetExpectation.exists(
      PointExpectation.summary
        .withCount(1L)
        .withSum(42.0)
    )
  )
```

### Histogram

```scala mdoc:silent
import org.typelevel.otel4s.metrics.BucketBoundaries

MetricExpectation
  .histogram("http.server.duration")
  .withPoints(
    PointSetExpectation.exists(
      PointExpectation.histogram
        .withCount(3L)
        .withSum(42.0)
        .withBoundaries(BucketBoundaries(0.1, 1.0, 10.0))
        .withCounts(1L, 1L, 1L, 0L)
    )
  )
```

### Exponential histogram

```scala mdoc:silent
MetricExpectation
  .exponentialHistogram("queue.depth")
  .withPoints(
    PointSetExpectation.exists(
      PointExpectation.exponentialHistogram
        .withScale(2)
        .withCount(10L)
        .withSum(100.0)
        .withZeroCount(0L)
    )
  )
```

## Point-set expectations

Point matching is collection-based. A `MetricExpectation` does not check points one by one in isolation. Instead,
it evaluates a `PointSetExpectation` against the full point collection, which lets you express presence, absence,
cardinality, and collection-wide invariants.

All typed metric expectations expose:

- `withPoints(...)` for raw `PointSetExpectation` composition
- `containsPoints(...)` as shorthand for `PointSetExpectation.contains(...)`
- `withExactlyPoints(...)` as shorthand for `PointSetExpectation.exactly(...)`
- `withPointCount(...)` as shorthand for `PointSetExpectation.count(...)`
- `withNoPointsMatching(...)` as shorthand for `PointSetExpectation.none(...)`
- `wherePoints(...)` as shorthand for `PointSetExpectation.predicate(...)`

### `exists`

Use `exists` when at least one collected point must match:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .withPoints(
    PointSetExpectation.exists(
      PointExpectation
        .numeric(1L)
        .withAttributesExact(Attribute("http.method", "GET"))
    )
  )
```

This is the default mental model for helpers like `withValue(...)`: they require at least one matching point, not
that every point looks the same.

### `forall`

Use `forall` when every collected point must satisfy the same rule:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .withPoints(
    PointSetExpectation.forall(
      PointExpectation
        .numeric(1L)
        .withAttributesSubset(Attribute("kind", "ok"))
    )
  )
```

`forall` is useful for invariants such as "every point has the same value shape" or "every point includes this
attribute subset". Unlike plain universal quantification over a Scala collection, it fails on an empty point set.

### `contains` and `exactly`

Use `contains` when the metric must contain several distinct matching points, but extra points are still allowed:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .containsPoints(
    PointExpectation
      .numeric(1L)
      .withAttributesSubset(Attribute("region", "eu")),
    PointExpectation
      .numeric(1L)
      .withAttributesSubset(Attribute("region", "us"))
  )
```

`contains` enforces distinct matching. If you ask for the same expected point twice, the collected data must contain
two matching points.

Use `exactly` when the metric must contain exactly the expected points and no extras:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .withExactlyPoints(
    PointExpectation
      .numeric(1L)
      .withAttributesSubset(Attribute("region", "eu")),
    PointExpectation
      .numeric(1L)
      .withAttributesSubset(Attribute("region", "us"))
  )
```

### Cardinality combinators

Use `withPointCount(...)` when only the total point count matters:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .withPointCount(2)
```

For lower-level count constraints, use `PointSetExpectation` directly:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .withPoints(PointSetExpectation.minCount[PointExpectation.NumericPointData[Long]](1))
  .withPoints(PointSetExpectation.maxCount[PointExpectation.NumericPointData[Long]](2))
```

Use `countWhere` when the total number of matching points matters more than the exact full set:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .withPoints(
    PointSetExpectation.countWhere(
      PointExpectation
        .numeric(1L)
        .withAttributesSubset(Attribute("region", "eu")),
      expected = 2
    )
  )
```

This is useful when each point still carries extra dimensions that you do not want to enumerate explicitly.

### `none`

Use `none` or `withNoPointsMatching(...)` when a point shape must not appear:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .withNoPointsMatching(
    PointExpectation
      .numeric(1L)
      .withAttributesSubset(Attribute("region", "test"))
  )
```

### `predicate`

Use `predicate` or `wherePoints(...)` for collection-wide assertions that are awkward to express with the built-in
combinators:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .wherePoints("expected exactly EU and US points") { points =>
    points.map(_.attributes).toSet == Set(
      Attributes(Attribute("region", "eu")),
      Attributes(Attribute("region", "us"))
    )
  }
```

For numeric metrics, the `points` passed to `wherePoints` are typed `NumericPointData[A]`, so you can inspect
`.value`, `.attributes`, and `.underlying`.

### `and` and `or`

Point-set expectations can be combined explicitly:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .withPoints(
    PointSetExpectation
      .contains(
        PointExpectation
          .numeric(1L)
          .withAttributesSubset(Attribute("region", "eu")),
        PointExpectation
          .numeric(1L)
          .withAttributesSubset(Attribute("region", "us"))
      )
      .and(PointSetExpectation.count[PointExpectation.NumericPointData[Long]](2))
  )
```

Use `or` when a metric may legitimately satisfy one of several point layouts:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .withPoints(
    PointSetExpectation
      .count[PointExpectation.NumericPointData[Long]](1)
      .or(PointSetExpectation.count[PointExpectation.NumericPointData[Long]](2))
  )
```

Because point expectations accumulate, you can also layer multiple `withPoints(...)` calls on the same metric:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .withPoints(
    PointSetExpectation.exists(
      PointExpectation
        .numeric(1L)
        .withAttributesSubset(Attribute("region", "eu"))
    )
  )
  .withPoints(
    PointSetExpectation.exists(
      PointExpectation
        .numeric(1L)
        .withAttributesSubset(Attribute("region", "us"))
      )
  )
```

Each `withPoints(...)` clause is checked independently against the full collected point set. Chaining
`withPoints(PointSetExpectation.exists(...))` does not reserve matched points for later clauses, so it should not be
used to require multiple distinct points.

If you need distinct matching, use `containsPoints(...)` or `withExactlyPoints(...)` instead:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .containsPoints(
    PointExpectation
      .numeric(1L)
      .withAttributesSubset(Attribute("region", "eu")),
    PointExpectation
      .numeric(1L)
      .withAttributesSubset(Attribute("region", "us"))
  )
```

## Failure reporting

The API is framework-agnostic, so it does not provide assertions directly.
Instead, it returns structured mismatches and a formatter.

```scala mdoc:silent:reset
import io.opentelemetry.sdk.metrics.data.MetricData
import org.typelevel.otel4s.oteljava.testkit.metrics.{MetricExpectation, MetricExpectations}

def assertExpected(metrics: List[MetricData], expected: MetricExpectation*): Unit =
  MetricExpectations.checkAll(metrics, expected: _*) match {
    case Right(_) =>
      ()
    case Left(mismatches) =>
      sys.error(MetricExpectations.format(mismatches))
  }
```

There are two main failure cases:

- `NotFound`: no collected metric looked like a match
- `ClosestMismatch`: a likely candidate existed, but some part of it did not match
- `DistinctMatchUnavailable`: the expectation matched collected metrics, but none remained available as a distinct
  match in `checkAllDistinct(...)`

This is especially helpful when a metric name is correct but, for example, one scope attribute or one point
attribute is wrong.

## Clues

Metric, point, and point-set expectations support optional clues.

```scala mdoc:silent
import org.typelevel.otel4s.oteljava.testkit.metrics.{PointExpectation, PointSetExpectation}
import org.typelevel.otel4s.Attribute

MetricExpectation
  .sum[Long]("service.requests")
  .withClue("the request counter must be emitted")
  .withPoints(
    PointSetExpectation
      .exists(
        PointExpectation
          .numeric(1L)
          .withClue("GET requests should increment the counter")
          .withAttributesExact(Attribute("http.method", "GET"))
      )
      .withClue("the GET point must be present")
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

```scala mdoc:silent
import org.typelevel.otel4s.oteljava.testkit.metrics.NumberComparison

implicit val cmp: NumberComparison[Double] =
  NumberComparison.within(1e-4)
```

After that, double-based point expectations built in that scope will use the custom comparison.
