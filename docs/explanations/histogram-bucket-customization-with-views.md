# Histogram bucket customization with views

Use [Customize histogram buckets](../how-to-metrics/customize-histogram-buckets.md) for the task itself.

This page explains the view-based path: why you need it, what `InstrumentSelector` and `View` do, and when it is the
right tool.

## When views are needed

If your code creates the histogram, you can usually set bucket boundaries directly with
`withExplicitBucketBoundaries(...)`.

Views are needed when the histogram is created outside your control, for example by:

- middleware
- framework instrumentation
- a library you depend on

In those cases, you cannot change the histogram builder directly, so the SDK must override the aggregation by matching
the instrument after it is created.

## What the selector matches

An `InstrumentSelector` chooses which instruments a view applies to.

For histogram bucket customization, the selector usually matches:

- the instrument name
- the instrument type

```scala mdoc:silent
import io.opentelemetry.sdk.metrics.{InstrumentSelector, InstrumentType}

InstrumentSelector
  .builder()
  .setName("http.server.duration")
  .setType(InstrumentType.HISTOGRAM)
  .build()
```

If you need to match multiple histograms, you can use a wildcard pattern such as `http.server.*`.

## What the view changes

A `View` changes how the selected instrument is aggregated.

For histogram bucket customization, the relevant part is `Aggregation.explicitBucketHistogram(...)`.

```scala mdoc:silent
import java.{util => ju}

import io.opentelemetry.sdk.metrics.{Aggregation, ExplicitBucketHistogramOptions, View}

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
```

The bucket boundaries use the same unit as the histogram values. If the histogram records seconds, boundaries such as
`0.05` and `0.1` also mean seconds.

## How the selector and view fit together

The selector identifies the histogram. The view replaces the default aggregation for that histogram.

That is why view-based customization is useful for third-party instruments:

- you do not change the code that creates the histogram
- you change how the SDK aggregates the values it receives

## Example: registering a view with `OtelJava.autoConfigured`

```scala mdoc:silent:reset
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

object Main extends IOApp.Simple {
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
