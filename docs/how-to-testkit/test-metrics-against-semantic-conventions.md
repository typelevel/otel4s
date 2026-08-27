# Test metrics against semantic conventions

Use generated metric specs with `MetricsTestkit` to verify that an instrumentation emits the canonical metric name,
type, description, unit, and required attributes.

## Prerequisites

- [Test metrics emitted by your code](test-metrics-emitted-by-your-code.md)
- A metric implementation that can run with a supplied `Meter`

## 1. Add the test dependencies

@:select(build-tool)

@:choice(sbt)

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-oteljava-testkit" % "@VERSION@" % Test,
  "org.typelevel" %% "otel4s-semconv-metrics"  % "@VERSION@" % Test,
)
```

@:choice(scala-cli)

```scala
//> using test.dep "org.typelevel::otel4s-oteljava-testkit:@VERSION@"
//> using test.dep "org.typelevel::otel4s-semconv-metrics:@VERSION@"
```

@:@

## 2. Run the metric implementation with the testkit meter

This example records the stable HTTP client request duration metric. In a project test, call the instrumentation entry
point you want to verify instead.

```scala mdoc:reset:silent
import cats.effect.IO
import org.typelevel.otel4s.metrics.{BucketBoundaries, Meter}
import org.typelevel.otel4s.semconv.attributes.{HttpAttributes, ServerAttributes}
import org.typelevel.otel4s.semconv.metrics.HttpMetrics

def emitClientRequestDuration(meter: Meter[IO]): IO[Unit] = {
  implicit val currentMeter: Meter[IO] = meter

  HttpMetrics.ClientRequestDuration
    .create[IO, Double](BucketBoundaries(0.01, 0.05, 0.1, 0.5, 1.0))
    .flatMap {
      _.record(
        0.125,
        HttpAttributes.HttpRequestMethod("GET"),
        ServerAttributes.ServerAddress("example.com"),
        ServerAttributes.ServerPort(443L),
      )
    }
}
```

## 3. Define the expectation and assertion helpers

Select the `MetricExpectation` builder that matches the metric instrument. `histogramExpectation` checks the generated
metadata and verifies that every exported point contains the attributes marked as required by the spec. `assertMetrics`
converts structured mismatches into the failure type used by the test.

```scala mdoc:silent
import io.opentelemetry.sdk.metrics.data.MetricData
import org.typelevel.otel4s.semconv.{MetricSpec, Requirement}
import org.typelevel.otel4s.oteljava.testkit.AttributesExpectation
import org.typelevel.otel4s.oteljava.testkit.metrics.{MetricExpectation, MetricExpectations}
import org.typelevel.otel4s.oteljava.testkit.metrics.{PointExpectation, PointSetExpectation}

def histogramExpectation(spec: MetricSpec): MetricExpectation.Histogram = {
  val requiredKeys = spec.attributeSpecs.collect {
    case attribute if attribute.requirement.level == Requirement.Level.Required =>
      attribute.key
  }

  val requiredAttributes =
    AttributesExpectation.where(
      s"required attributes: ${requiredKeys.map(_.name).sorted.mkString(", ")}"
    ) { attributes =>
      requiredKeys.forall { key =>
        attributes.exists(_.key == key)
      }
    }

  MetricExpectation
    .histogram(spec.name)
    .description(spec.description)
    .unit(spec.unit)
    .points(
      PointSetExpectation.forall(
        PointExpectation.histogram.attributes(requiredAttributes)
      )
    )
}

def assertMetrics(metrics: List[MetricData], expected: MetricExpectation*): IO[Unit] =
  MetricExpectations.checkAll(metrics, expected: _*) match {
    case Right(_) =>
      IO.unit
    case Left(mismatches) =>
      IO.raiseError(new AssertionError(MetricExpectations.format(mismatches)))
  }
```

`MetricSpec` does not expose the instrument type, so the helper chooses `MetricExpectation.histogram` explicitly. Use
the corresponding gauge or sum expectation for other metric instruments.

## 4. Collect and check the metric

```scala mdoc:silent
import org.typelevel.otel4s.oteljava.testkit.metrics.MetricsTestkit

def testSemanticMetric: IO[Unit] =
  MetricsTestkit.inMemory[IO]().use { testkit =>
    for {
      meter <- testkit.meterProvider.get("http-client")
      _ <- emitClientRequestDuration(meter)
      metrics <- testkit.collectMetrics
      _ <- assertMetrics(
        metrics,
        histogramExpectation(HttpMetrics.ClientRequestDuration),
      )
    } yield ()
  }
```

```scala mdoc:invisible
import cats.effect.unsafe.implicits.global

testSemanticMetric.unsafeRunSync()
```

Add implementation-specific expectations for measurement values, optional attributes, and bucket boundaries when they
are part of the behavior under test.

## What's next

- Create metrics with generated constructors:
  [Create metrics from semantic metric specs](../how-to-semantic-conventions/create-metrics-from-semantic-metric-specs.md)
- Look up the complete metrics expectation API:
  [Metrics testkit reference](../oteljava/testkit-metrics.md)
