# Histogram custom buckets

By default, OpenTelemetry use the following boundary values for histogram
bucketing:［0, 5, 10, 25, 50, 75, 100, 250, 500, 750, 1000, 2500, 5000, 7500, 10000］.

In some cases, these boundaries don't represent the distribution of the values. For example, we expect that HTTP server
latency should be somewhere between 100ms and 1s. Therefore, 2.5, 5, 7.5, and 10 seconds buckets are redundant.

In this example, we will
customize the [OpenTelemetry Autoconfigure](https://opentelemetry.io/docs/instrumentation/java/manual/#auto-configuration)
extension with a [View](https://opentelemetry.io/docs/instrumentation/java/manual/#views) to configure custom buckets
for a histogram.

### Project setup

Configure the project using your favorite tool:

@:select(build-tool)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-oteljava" % "@VERSION@", // <1>
  "io.opentelemetry" % "opentelemetry-exporter-otlp" % "@OPEN_TELEMETRY_VERSION@" % Runtime, // <2>
  "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "@OPEN_TELEMETRY_VERSION@" % Runtime // <3>
)
run / fork := true
javaOptions += "-Dotel.service.name=histogram-buckets-example" // <4>
```

@:choice(scala-cli)

Add directives to the `histogram-buckets.scala`:

```scala
//> using lib "org.typelevel::otel4s-oteljava:@VERSION@" // <1>
//> using lib "io.opentelemetry:opentelemetry-exporter-otlp:@OPEN_TELEMETRY_VERSION@" // <2>
//> using lib "io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:@OPEN_TELEMETRY_VERSION@" // <3>
//> using `java-opt` "-Dotel.service.name=histogram-buckets-example" // <4>
```

@:@

1) Add the `otel4s` library  
2) Add an OpenTelemetry exporter. Without the exporter, the application will crash  
3) Add an OpenTelemetry autoconfigure extension  
4) Add the name of the application to use in the traces and metrics

### OpenTelemetry SDK configuration

As mentioned above, we use `otel.service.name` system properties to configure the OpenTelemetry SDK.
The SDK can be configured via environment variables too. Check the full list
of [environment variable configurations](https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md)
for more options.

### OpenTelemetry Autoconfigure extension customization

We can utilize `AutoConfiguredOpenTelemetrySdk.builder` to customize the `MeterProvider` and, as the result, a histogram
bucket. This approach leverages the automatic configuration functionality (e.g. configuring context propagators through
environment variables) and provides a handy way to re-configure some components.

In order to register a view, there are two essential components that must be provided: an instrument selector and a view
definition.

#### Instrument selector

Here we establish the configuration for replacing **existing** instruments.

In the following example, we specifically target an instrument of type `HISTOGRAM` with the
name `service.work.duration`:

```scala mdoc:silent
import io.opentelemetry.sdk.metrics.{InstrumentSelector, InstrumentType}

InstrumentSelector
  .builder()
  .setName("service.work.duration")
  .setType(InstrumentType.HISTOGRAM)
  .build()
```

To select multiple instruments, a wildcard pattern can be used: `service.*.duration`.

#### View definition

The view determines how the selected instruments should be changed or aggregated.

In our particular case, we create a histogram view with custom buckets:［.005, .01, .025, .05, .075, .1, .25, .5］.

```scala mdoc:silent
import io.opentelemetry.sdk.metrics.{Aggregation, View}

View
  .builder()
  .setName("service.work.duration")
  .setAggregation(
    Aggregation.explicitBucketHistogram(
      java.util.Arrays.asList(.005, .01, .025, .05, .075, .1, .25, .5)
    )
  )
  .build()
```

### Application example

By putting all the snippets together, we get the following:

```scala mdoc:silent:reset
import cats.Parallel
import cats.effect._
import cats.effect.kernel.Temporal
import cats.effect.std.Console
import cats.effect.std.Random
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.parallel._
import io.opentelemetry.sdk.metrics.Aggregation
import io.opentelemetry.sdk.metrics.InstrumentSelector
import io.opentelemetry.sdk.metrics.InstrumentType
import io.opentelemetry.sdk.metrics.View
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.metrics.Histogram

import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

object HistogramBucketsExample extends IOApp.Simple {

  def work[F[_] : Temporal : Console](
    histogram: Histogram[F, Double], 
    random: Random[F]
  ): F[Unit] =
    for {
      sleepDuration <- random.nextIntBounded(5000)
      _ <- histogram
        .recordDuration(TimeUnit.SECONDS)
        .surround(
          Temporal[F].sleep(sleepDuration.millis) >>
            Console[F].println(s"I'm working after [$sleepDuration ms]")
        )
    } yield ()

  def program[F[_] : Async : LiftIO : Parallel : Console]: F[Unit] =
    configureSdk[F]
      .evalMap(_.meterProvider.get("histogram-example"))
      .use { meter =>
        for {
          random <- Random.scalaUtilRandom[F]
          histogram <- meter.histogram("service.work.duration").create
          _ <- work[F](histogram, random).parReplicateA_(50)
        } yield ()
      }

  def run: IO[Unit] =
    program[IO]

  private def configureSdk[F[_] : Async : LiftIO]: Resource[F, OtelJava[F]] =
    OtelJava.autoConfigured { sdkBuilder =>
      sdkBuilder
        .addMeterProviderCustomizer { (meterProviderBuilder, _) =>
          meterProviderBuilder
            .registerView(
              InstrumentSelector
                .builder()
                .setName("service.work.duration")
                .setType(InstrumentType.HISTOGRAM)
                .build(),
              View
                .builder()
                .setName("service.work.duration")
                .setAggregation(
                  Aggregation.explicitBucketHistogram(
                    java.util.Arrays.asList(.005, .01, .025, .05, .075, .1, .25, .5)
                  )
                )
                .build()
            )
        }
        .setResultAsGlobal
    }
}
```

### Run the application

@:select(build-tool)

@:choice(sbt)

```shell
$ sbt run
```

@:choice(scala-cli)

```shell
$ scala-cli run histogram-buckets.scala
```

@:@
