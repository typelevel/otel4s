# Semantic conventions

Semantic conventions standardize attribute and metric names across instrumentation so telemetry is comparable across
libraries and services. The `semconv` modules provide generated, typesafe attribute keys and metric specs that match the
[OpenTelemetry semantic conventions][opentelemetry-semconv] specification.

## Modules overview

| Module                                | Stability  | Package prefix                                         |
|:--------------------------------------|:-----------|:-------------------------------------------------------|
| `otel4s-semconv`                      | Stable     | `org.typelevel.otel4s.semconv.attributes`              |
| `otel4s-semconv-metrics`              | Stable     | `org.typelevel.otel4s.semconv.metrics`                 |
| `otel4s-semconv-experimental`         | Incubating | `org.typelevel.otel4s.semconv.experimental.attributes` |
| `otel4s-semconv-metrics-experimental` | Incubating | `org.typelevel.otel4s.semconv.experimental.metrics`    |

Stable modules track the stable spec, while experimental modules track incubating conventions. 
Use stable modules by default. The experimental modules track incubating semantic conventions and can change between
versions. 

@:callout(warning)

The `*-experimental` modules have no binary-compatibility guarantees between releases and may introduce binary breaking
changes at any time.

@:@

## Getting started

@:select(build-tool)

@:choice(sbt)

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-semconv"         % "@VERSION@", // stable attributes
  "org.typelevel" %% "otel4s-semconv-metrics" % "@VERSION@", // stable metric specs
)
```

@:choice(scala-cli)

```scala
//> using dep "org.typelevel::otel4s-semconv:@VERSION@"         // stable attributes
//> using dep "org.typelevel::otel4s-semconv-metrics:@VERSION@" // stable metric specs
```

@:@

If you need incubating conventions, add the `-experimental` variants instead of the stable ones.

## Use semantic attribute keys

The generated attribute keys are typesafe and can be used to build an attribute.

```scala mdoc:compile-only
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.semconv.attributes.{ExceptionAttributes, HttpAttributes}

val attrs = Attributes(
  HttpAttributes.HttpRequestMethod("GET"),
  HttpAttributes.HttpResponseStatusCode(200),
  ExceptionAttributes.ExceptionType("java.lang.RuntimeException")
)
```

The same keys work for spans, log records, or metric attributes because they are plain `AttributeKey[A]` values.

## Use semantic metric specs

Semantic metric specs provide the canonical metric name, unit, description, and attribute expectations.

```scala mdoc:compile-only
import org.typelevel.otel4s.metrics.{BucketBoundaries, Histogram, Meter}
import org.typelevel.otel4s.semconv.metrics.HttpMetrics

def createHttpClientDuration[F[_]: Meter](
  boundaries: BucketBoundaries
): F[Histogram[F, Double]] =
  Meter[F]
    .histogram[Double](HttpMetrics.ClientRequestDuration.name)
    .withUnit(HttpMetrics.ClientRequestDuration.unit)
    .withDescription(HttpMetrics.ClientRequestDuration.description)
    .withExplicitBucketBoundaries(boundaries)
    .create
```

For convenience, every metric also has the generated `create` method, that creates an instrument with the
spec's name, unit, and description. The example below is equivalent to the previous one:

```scala mdoc:compile-only
import org.typelevel.otel4s.metrics.{BucketBoundaries, Histogram, Meter}
import org.typelevel.otel4s.semconv.metrics.HttpMetrics

def createHttpClientDuration[F[_]: Meter](
  boundaries: BucketBoundaries
): F[Histogram[F, Double]] =
  HttpMetrics.ClientRequestDuration.create[F, Double](boundaries)
```

## Use metric specs in tests

Metric specs are also useful for validating exported metrics. The example below checks that expected server metrics
exist and that each exported metric matches the semantic name, unit, description, and required attributes.

```scala mdoc:compile-only
import cats.effect.IO
import io.opentelemetry.sdk.metrics.data.{MetricData, PointData}
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.semconv.MetricSpec
import org.typelevel.otel4s.semconv.Requirement
import org.typelevel.otel4s.semconv.metrics.HttpMetrics
import org.typelevel.otel4s.oteljava.testkit.metrics._

def semanticTest(scenario: Meter[IO] => IO[Unit]): IO[Unit] = {
  // the set of metrics to check
  val specs = List(
    HttpMetrics.ServerRequestDuration
  )

  MetricsTestkit.inMemory[IO]().use { testkit =>
    testkit.meterProvider.get("meter").flatMap { meter =>
      for {
        // run a scenario to generate metrics 
        _       <- scenario(meter)
        // collect metrics
        metrics <- testkit.collectAllMetrics
        // ensure the expected metrics exist and match the spec
      } yield assertExpected(metrics, specs.map(specExpectation))
    }
  }
}

def assertExpected(metrics: List[MetricData], expected: List[MetricExpectation]): Unit =
  MetricExpectations.checkAll(metrics, expected) match {
    case Right(_) =>
      ()
    case Left(mismatches) =>
      // or use an assert function from the testing framework here
      sys.error(MetricExpectations.format(mismatches))
  }

def specExpectation(spec: MetricSpec): MetricExpectation = {
  val required = spec.attributeSpecs
    .filter(_.requirement.level == Requirement.Level.Required)
    .map(_.key)
    .toSet

  MetricExpectation
    .name(spec.name)
    .withDescription(spec.description)
    .withUnit(spec.unit)
    .where(s"[${spec.name}] is missing required semantic attributes") { metric =>
      import scala.jdk.CollectionConverters._
      import org.typelevel.otel4s.oteljava.AttributeConverters._

      val current = metric.getData.getPoints.asScala
        .flatMap(_.asInstanceOf[PointData].getAttributes.asMap().keySet().asScala)
        .map(_.toScala)
        .toSet

      current == required
    }
}
```

[opentelemetry-semconv]: https://opentelemetry.io/docs/specs/semconv/
