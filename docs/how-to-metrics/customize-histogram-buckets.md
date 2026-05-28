# Customize histogram buckets

Use this page when the default histogram buckets do not match the values you record.

For example, if a latency histogram is recorded in seconds and most values are between `0.1` and `1.0`, narrower
buckets may be more useful.

## Prerequisites

- [Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md)
- [Record application metrics](record-application-metrics.md)

## 1. Choose the path that matches who owns the histogram

- If your code creates the histogram, set the bucket boundaries on the histogram builder.
- If the histogram is created elsewhere, such as in middleware or a library, register a view that matches it by name and
  type.

## 2. If you create the histogram yourself, use `withExplicitBucketBoundaries`

This is the simpler path when the histogram is under your control.

```scala mdoc:silent:reset
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.metrics.{BucketBoundaries, Histogram, Meter}
import org.typelevel.otel4s.oteljava.OtelJava

object Main extends IOApp.Simple {
  def run: IO[Unit] =
    OtelJava.autoConfigured[IO]().use { otel4s =>
      otel4s.meterProvider.get("auth-service").flatMap { implicit meter =>
        program
      }
    }

  def program(implicit meter: Meter[IO]): IO[Unit] =
    createLatencyHistogram.flatMap(_.record(0.12))

  def createLatencyHistogram(implicit meter: Meter[IO]): IO[Histogram[IO, Double]] =
    meter
      .histogram[Double]("http.server.duration")
      .withUnit("s")
      .withExplicitBucketBoundaries(
        BucketBoundaries(0.05, 0.1, 0.25, 0.5, 1.0)
      )
      .create
}
```

## 3. If the histogram is created elsewhere, register a view

Use a view when you cannot change the histogram builder directly.

This is the right path when the histogram is created by:

- middleware
- framework instrumentation
- a library you depend on

In this example, the histogram is still named `http.server.duration` and records values in seconds.

```scala mdoc:silent
import java.{util => ju}

import cats.effect.{IO, IOApp}
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder
import io.opentelemetry.sdk.metrics.Aggregation
import io.opentelemetry.sdk.metrics.ExplicitBucketHistogramOptions
import io.opentelemetry.sdk.metrics.InstrumentSelector
import io.opentelemetry.sdk.metrics.InstrumentType
import io.opentelemetry.sdk.metrics.View
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.oteljava.OtelJava

object MainWithView extends IOApp.Simple {
  def run: IO[Unit] =
    OtelJava.autoConfigured[IO](configureBuilder).use { otel4s =>
      otel4s.meterProvider.get("auth-service").flatMap { implicit meter =>
        program
      }
    }

  def program(implicit meter: Meter[IO]): IO[Unit] =
    meter
      .histogram[Double]("http.server.duration")
      .withUnit("s")
      .create
      .flatMap(_.record(0.12))

  def configureBuilder(
      builder: AutoConfiguredOpenTelemetrySdkBuilder
  ): AutoConfiguredOpenTelemetrySdkBuilder =
    builder.addMeterProviderCustomizer { (meterProviderBuilder, _) =>
      meterProviderBuilder.registerView(
        InstrumentSelector
          .builder()
          .setName("http.server.duration")
          .setType(InstrumentType.HISTOGRAM)
          .build(),
        View
          .builder()
          .setAggregation(
            Aggregation.explicitBucketHistogram(
              ExplicitBucketHistogramOptions
                .builder()
                .setBucketBoundaries(ju.Arrays.asList(0.05, 0.1, 0.25, 0.5, 1.0))
                .build()
            )
          )
          .build()
      )
    }
}
```

## 4. Choose bucket boundaries in the same unit you record

The custom bucket boundaries must use the same unit as the histogram values.

In the examples above:

- the histogram unit is seconds
- `0.12` means `120ms`
- bucket boundaries such as `0.05`, `0.1`, and `0.25` are also in seconds

## 5. Keep the histogram name stable when you use a view

The view only applies to histograms that match the selector.

If you rename the histogram in application code, update the selector to match the new name.

## What's next

- Record the histogram from application code:
  [Record application metrics](record-application-metrics.md)
- Export runtime metrics from the same application:
  [Register Cats Effect runtime metrics](register-cats-effect-runtime-metrics.md)
- See how view-based bucket customization works:
  [Histogram bucket customization with views](../explanations/histogram-bucket-customization-with-views.md)
