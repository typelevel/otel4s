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
      // or use an assert function from the testing framework here
      sys.error(MetricExpectations.format(mismatches))
  }

def test: IO[Unit] =
  MetricsTestkit.inMemory[IO]().use { testkit =>
    for {
      _ <- program(testkit.meterProvider)
      metrics <- testkit.collectMetrics
    } yield assertExpected(
      metrics,
      MetricExpectation.sum[Long]("service.counter").value(1L),
      MetricExpectation.gauge[Long]("service.gauge").value(42L)
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
    MetricExpectation.sum[Long]("service.counter").value(1L),
    MetricExpectation.sum[Long]("service.counter").value(1L)
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
MetricExpectation.sum[Long]("service.counter").value(1L)
```

matches a long sum metric named `service.counter` that has at least one point with value `1L`.

## Numeric metrics

Use `MetricExpectation.gauge[A]` and `MetricExpectation.sum[A]` for numeric metrics.

```scala mdoc:silent
MetricExpectation.gauge[Double]("service.temperature")
MetricExpectation.sum[Long]("service.requests")
```

The measurement type remains generic. `Long` and `Double` use the same API.

For value-only checks, `value(...)` is the most compact form:

```scala mdoc:silent
MetricExpectation.sum[Long]("service.requests").value(1L)
```

If you also care about point attributes, there is a shorthand for exact point matching:

```scala mdoc:silent
import org.typelevel.otel4s.Attribute

MetricExpectation
  .sum[Long]("service.requests")
  .value(1L, Attribute("http.method", "GET"))
```

`value(value, attributes)` uses exact attribute matching. It is equivalent to:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .points(
    PointSetExpectation.exists(
      PointExpectation
        .numeric(1L)
        .attributesExact(Attribute("http.method", "GET"))
    )
  )
```

Use `points(PointSetExpectation.exists(...))` directly when you need subset matching or a more detailed point expectation.

## Point attributes

Point attributes can be matched in three ways:

- `attributesExact(...)` for exact equality
- `attributesSubset(...)` for subset matching
- `attributes(AttributesExpectation...)` for full control

Both `attributesExact(...)` and `attributesSubset(...)` support `Attributes` and varargs of `Attribute[_]`.

```scala mdoc:silent
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.testkit.AttributesExpectation

PointExpectation
  .numeric(1L)
  .attributesExact(Attribute("http.method", "GET"))

PointExpectation
  .numeric(1L)
  .attributesSubset(
    Attribute("http.method", "GET"),
    Attribute("http.route", "/users")
  )

PointExpectation
  .numeric(1L)
  .attributes(
    AttributesExpectation.where("expected only one attribute") { attributes =>
      attributes == Attributes(Attribute("http.method", "GET"))
    }
  )
```

Empty attributes can be matched with:

```scala mdoc:silent
PointExpectation.numeric(1L).attributesEmpty
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
  .points(
    PointSetExpectation.exists(
      PointExpectation
        .numeric(1L)
        .attributesExact(Attribute("http.method", "GET"))
    )
  )
  .scope(
    InstrumentationScopeExpectation
      .name("service")
      .version("1.0")
      .schemaUrl("https://opentelemetry.io/schemas/1.24.0")
      .attributesEmpty
  )
  .resource(
    TelemetryResourceExpectation.any
      .attributesSubset(Attribute("service.name", "auth-service"))
      .schemaUrl(None)
  )
```

As with point attributes, scope and resource attribute helpers support both `Attributes` and varargs.
They also provide `attributesEmpty` when you want to assert that no attributes are present.

## Summaries and histograms

The testkit also supports non-numeric point kinds directly.

### Summary

```scala mdoc:silent
MetricExpectation
  .summary("rpc.duration")
  .points(
    PointSetExpectation.exists(
      PointExpectation.summary
        .count(1L)
        .sum(42.0)
    )
  )
```

### Histogram

```scala mdoc:silent
import org.typelevel.otel4s.metrics.BucketBoundaries

MetricExpectation
  .histogram("http.server.duration")
  .points(
    PointSetExpectation.exists(
      PointExpectation.histogram
        .count(3L)
        .sum(42.0)
        .boundaries(BucketBoundaries(0.1, 1.0, 10.0))
        .counts(1L, 1L, 1L, 0L)
    )
  )
```

### Exponential histogram

```scala mdoc:silent
MetricExpectation
  .exponentialHistogram("queue.depth")
  .points(
    PointSetExpectation.exists(
      PointExpectation.exponentialHistogram
        .scale(2)
        .count(10L)
        .sum(100.0)
        .zeroCount(0L)
    )
  )
```

## Point-set expectations

Point matching is collection-based. A `MetricExpectation` does not check points one by one in isolation. Instead,
it evaluates a `PointSetExpectation` against the full point collection, which lets you express presence, absence,
cardinality, and collection-wide invariants.

All typed metric expectations expose:

- `points(...)` for raw `PointSetExpectation` composition
- `containsPoints(...)` as shorthand for `PointSetExpectation.contains(...)`
- `exactlyPoints(...)` as shorthand for `PointSetExpectation.exactly(...)`
- `pointCount(...)` as shorthand for `PointSetExpectation.count(...)`
- `withoutPointsMatching(...)` as shorthand for `PointSetExpectation.none(...)`
- `pointsWhere(...)` as shorthand for `PointSetExpectation.predicate(...)`

### `exists`

Use `exists` when at least one collected point must match:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .points(
    PointSetExpectation.exists(
      PointExpectation
        .numeric(1L)
        .attributesExact(Attribute("http.method", "GET"))
    )
  )
```

This is the default mental model for helpers like `value(...)`: they require at least one matching point, not
that every point looks the same.

### `forall`

Use `forall` when every collected point must satisfy the same rule:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .points(
    PointSetExpectation.forall(
      PointExpectation
        .numeric(1L)
        .attributesSubset(Attribute("kind", "ok"))
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
      .attributesSubset(Attribute("region", "eu")),
    PointExpectation
      .numeric(1L)
      .attributesSubset(Attribute("region", "us"))
  )
```

`contains` enforces distinct matching. If you ask for the same expected point twice, the collected data must contain
two matching points.

Use `exactly` when the metric must contain exactly the expected points and no extras:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .exactlyPoints(
    PointExpectation
      .numeric(1L)
      .attributesSubset(Attribute("region", "eu")),
    PointExpectation
      .numeric(1L)
      .attributesSubset(Attribute("region", "us"))
  )
```

### Cardinality combinators

Use `pointCount(...)` when only the total point count matters:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .pointCount(2)
```

For lower-level count constraints, use `PointSetExpectation` directly:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .points(PointSetExpectation.minCount[PointExpectation.NumericPointData[Long]](1))
  .points(PointSetExpectation.maxCount[PointExpectation.NumericPointData[Long]](2))
```

Use `countWhere` when the total number of matching points matters more than the exact full set:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .points(
    PointSetExpectation.countWhere(
      PointExpectation
        .numeric(1L)
        .attributesSubset(Attribute("region", "eu")),
      expected = 2
    )
  )
```

This is useful when each point still carries extra dimensions that you do not want to enumerate explicitly.

### `none`

Use `none` or `withoutPointsMatching(...)` when a point shape must not appear:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .withoutPointsMatching(
    PointExpectation
      .numeric(1L)
      .attributesSubset(Attribute("region", "test"))
  )
```

### `predicate`

Use `predicate` or `pointsWhere(...)` for collection-wide assertions that are awkward to express with the built-in
combinators:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .pointsWhere("expected exactly EU and US points") { points =>
    points.map(_.attributes).toSet == Set(
      Attributes(Attribute("region", "eu")),
      Attributes(Attribute("region", "us"))
    )
  }
```

For numeric metrics, the `points` passed to `pointsWhere` are typed `NumericPointData[A]`, so you can inspect
`.value`, `.attributes`, and `.underlying`.

### `and` and `or`

Point-set expectations can be combined explicitly:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .points(
    PointSetExpectation
      .contains(
        PointExpectation
          .numeric(1L)
          .attributesSubset(Attribute("region", "eu")),
        PointExpectation
          .numeric(1L)
          .attributesSubset(Attribute("region", "us"))
      )
      .and(PointSetExpectation.count[PointExpectation.NumericPointData[Long]](2))
  )
```

Use `or` when a metric may legitimately satisfy one of several point layouts:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .points(
    PointSetExpectation
      .count[PointExpectation.NumericPointData[Long]](1)
      .or(PointSetExpectation.count[PointExpectation.NumericPointData[Long]](2))
  )
```

Because point expectations accumulate, you can also layer multiple `points(...)` calls on the same metric:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .points(
    PointSetExpectation.exists(
      PointExpectation
        .numeric(1L)
        .attributesSubset(Attribute("region", "eu"))
    )
  )
  .points(
    PointSetExpectation.exists(
      PointExpectation
        .numeric(1L)
        .attributesSubset(Attribute("region", "us"))
      )
  )
```

Each `points(...)` clause is checked independently against the full collected point set. Chaining
`points(PointSetExpectation.exists(...))` does not reserve matched points for later clauses, so it should not be
used to require multiple distinct points.

If you need distinct matching, use `containsPoints(...)` or `exactlyPoints(...)` instead:

```scala mdoc:silent
MetricExpectation
  .sum[Long]("service.requests")
  .containsPoints(
    PointExpectation
      .numeric(1L)
      .attributesSubset(Attribute("region", "eu")),
    PointExpectation
      .numeric(1L)
      .attributesSubset(Attribute("region", "us"))
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
  .clue("the request counter must be emitted")
  .points(
    PointSetExpectation
      .exists(
        PointExpectation
          .numeric(1L)
          .clue("GET requests should increment the counter")
          .attributesExact(Attribute("http.method", "GET"))
      )
      .clue("the GET point must be present")
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
