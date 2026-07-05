# Test metrics emitted by your code

Use `MetricsTestkit` to run code against an in-memory metrics backend and assert the exported metrics.

## 1. Add the testkit dependency

@:select(build-tool)

@:choice(sbt)

Add the testkit to the test scope in `build.sbt`:

```scala
libraryDependencies +=
  "org.typelevel" %% "otel4s-oteljava-testkit" % "@VERSION@" % Test
```

@:choice(scala-cli)

Add the test dependency to the test source file:

```scala
//> using test.dep "org.typelevel::otel4s-oteljava-testkit:@VERSION@"
```

@:@

## 2. Allocate the instruments once

Group instruments in a value that can be created at application startup and passed to the code that records metrics.
Accepting a `MeterProvider` lets the test allocate the same instruments with the in-memory provider.

```scala mdoc:silent
import cats.effect.IO
import org.typelevel.otel4s.metrics.{Counter, MeterProvider}

case class ServiceMetrics(
    requests: Counter[IO, Long]
)

object ServiceMetrics {
  def create(meterProvider: MeterProvider[IO]): IO[ServiceMetrics] =
    for {
      meter <- meterProvider.get("service")
      requests <- meter.counter[Long]("service.requests").create
    } yield ServiceMetrics(requests)
}

final class Service(metrics: ServiceMetrics) {
  def handleRequest: IO[Unit] =
    metrics.requests.inc()
}
```

## 3. Define an assertion helper

The expectation API returns structured mismatches. Convert them into the failure type used by your test framework.

```scala mdoc:silent
import io.opentelemetry.sdk.metrics.data.MetricData
import org.typelevel.otel4s.oteljava.testkit.metrics.{MetricExpectation, MetricExpectations}

def assertMetrics(metrics: List[MetricData], expected: MetricExpectation*): IO[Unit] =
  MetricExpectations.checkAll(metrics, expected: _*) match {
    case Right(_) =>
      IO.unit
    case Left(mismatches) =>
      IO.raiseError(new AssertionError(MetricExpectations.format(mismatches)))
  }
```

This helper uses `IO.raiseError` so it remains independent of a specific test framework. Replace it with the framework's
failure mechanism when appropriate.

## 4. Run the code with `MetricsTestkit`

Create the testkit as a `Resource`, allocate `ServiceMetrics` once with its `meterProvider`, and use those instruments
throughout the test. Collect and assert the exported metrics before the resource closes.

```scala mdoc:silent
import org.typelevel.otel4s.oteljava.testkit.metrics.MetricsTestkit

def test: IO[Unit] =
  MetricsTestkit.inMemory[IO]().use { testkit =>
    for {
      serviceMetrics <- ServiceMetrics.create(testkit.meterProvider)
      service = new Service(serviceMetrics)
      _ <- service.handleRequest
      _ <- service.handleRequest
      metrics <- testkit.collectMetrics
      _ <- assertMetrics(
        metrics,
        MetricExpectation.sum[Long]("service.requests").value(2L)
      )
    } yield ()
  }
```

`MetricExpectation` ignores unspecified fields, such as timestamps and resource attributes.

## What's next

For point attributes, histograms, collection-wide constraints, and custom numeric comparison, see
[Testkit | Metrics](../oteljava/testkit-metrics.md).
