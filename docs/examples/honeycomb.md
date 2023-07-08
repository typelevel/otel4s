# Honeycomb - metrics and traces

In this example, we are going to use [Honeycomb](https://honeycomb.io) to collect and visualize metrics and traces produced by an
application.
We will cover the configuration of OpenTelemetry exporter, as well as the instrumentation of the application using the
otel4s library.

Unlike [Jaeger example](jaeger-docker.md), you do not need to set up a collector service locally. The metrics and traces
will be sent to a remote Honeycomb API.

At the time of writing, Honeycomb allows having up to 20 million spans per month for a free account.
It offers robust analysis and visualization tools that are handy for exploring the world of telemetry.

### Project setup

Configure the project using your favorite tool:

@:select(build-tool)

@:choice(sbt)

Add settings to the `build.sbt`:
```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-java" % "@VERSION@", // <1>
  "io.opentelemetry" % "opentelemetry-exporter-otlp" % "@OPEN_TELEMETRY_VERSION@" % Runtime, // <2>
  "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "@OPEN_TELEMETRY_VERSION@" % Runtime // <3>
)
run / fork := true
javaOptions += "-Dotel.java.global-autoconfigure.enabled=true"            // <4>
javaOptions += "-Dotel.service.name=honeycomb-example"                    // <5>
javaOptions += "-Dotel.exporter.otlp.endpoint=https://api.honeycomb.io/"  // <6>
```

@:choice(scala-cli)

Add directives to the `tracing.scala`:
```scala
//> using lib "org.typelevel::otel4s-java:@VERSION@" // <1>
//> using lib "io.opentelemetry:opentelemetry-exporter-otlp:@OPEN_TELEMETRY_VERSION@" // <2>
//> using lib "io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:@OPEN_TELEMETRY_VERSION@" // <3>
//> using `java-opt` "-Dotel.java.global-autoconfigure.enabled=true"            // <4>
//> using `java-opt` "-Dotel.service.name=honeycomb-example"                    // <5>
//> using `java-opt` "-Dotel.exporter.otlp.endpoint=https://api.honeycomb.io/"  // <6>
```

@:@

1) Add the `otel4s` library  
2) Add an OpenTelemetry exporter. Without the exporter, the application will crash  
3) Add an OpenTelemetry autoconfigure extension  
4) Enable OpenTelemetry SDK autoconfigure mode  
5) Add the name of the application to use in the traces  
6) Add the Honeycomb API endpoint  

### OpenTelemetry SDK configuration

As mentioned above, we use `otel.java.global-autoconfigure.enabled` and `otel.service.name` system properties to configure the
OpenTelemetry SDK.
The SDK can be configured via environment variables too. Check the full list
of [environment variable configurations](https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md)
for more options.

### Acquiring a Honeycomb API key

The Honeycomb [official guide](https://docs.honeycomb.io/getting-data-in/api-keys/#find-api-keys).

First, you must create an account on the [Honeycomb website](https://ui.honeycomb.io/login).
Once you have done this, log into your account and navigate to the environment settings page. There you can find a generated API key.

### Honeycomb configuration

The Honeycomb [official configuration guide](https://docs.honeycomb.io/getting-data-in/opentelemetry-overview/).

In order to send metrics and traces to Honeycomb, the API key and dataset name need to be configured.
Since the API key is sensitive data, we advise providing them via environment variables:

```shell
$ export OTEL_EXPORTER_OTLP_HEADERS="x-honeycomb-team=your-api-key,x-honeycomb-dataset=honeycomb-example"
```

1) `x-honeycomb-team` - the API key  
2) `x-honeycomb-dataset` - the name of the dataset to send metrics to. We use `honeycomb-example` so both metrics and traces appear in the same dataset.

**Note:** if the `x-honeycomb-dataset` header is not configured, the **metrics** will be sent to a dataset called `unknown_metrics`.

### Application example

```scala mdoc:silent
import java.util.concurrent.TimeUnit

import cats.effect.{Async, IO, IOApp}
import cats.effect.std.Console
import cats.effect.std.Random
import cats.syntax.all._
import org.typelevel.otel4s.{Attribute, AttributeKey}
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.metrics.Histogram
import org.typelevel.otel4s.trace.Tracer

import scala.concurrent.duration._

trait Work[F[_]] {
  def doWork: F[Unit]
}

object Work {
  def apply[F[_] : Async : Tracer : Console](histogram: Histogram[F, Double]): Work[F] =
    new Work[F] {
      def doWork: F[Unit] =
        Tracer[F].span("Work.DoWork").use { span =>
          span.addEvent("Starting the work.") *>
            doWorkInternal(steps = 10) *>
            span.addEvent("Finished working.")
        }

      def doWorkInternal(steps: Int): F[Unit] = {
        val step = Tracer[F]
          .span("internal", Attribute(AttributeKey.long("steps"), steps.toLong))
          .surround {
            for {
              random <- Random.scalaUtilRandom
              delay <- random.nextIntBounded(1000)
              _ <- Async[F].sleep(delay.millis)
              _ <- Console[F].println("Doin' work")
            } yield ()
          }

        val metered = histogram.recordDuration(TimeUnit.MILLISECONDS).surround(step)

        if (steps > 0) metered *> doWorkInternal(steps - 1) else metered
      }
    }
}

object TracingExample extends IOApp.Simple {
  def run: IO[Unit] = {
    OtelJava.global.flatMap { otel4s =>
      otel4s.tracerProvider.get("com.service.runtime")
        .flatMap { implicit tracer: Tracer[IO] =>
          for {
            meter <- otel4s.meterProvider.get("com.service.runtime")
            histogram <- meter.histogram("work.execution.duration").create
            _ <- Work[IO](histogram).doWork
          } yield ()
        }
    }
  }
}
```

### Run the application

@:select(build-tool)

@:choice(sbt)

```shell
$ export OTEL_EXPORTER_OTLP_HEADERS="x-honeycomb-team=your-api-key,x-honeycomb-dataset=honeycomb-example"
$ sbt run
```

@:choice(scala-cli)

```shell
$ export OTEL_EXPORTER_OTLP_HEADERS="x-honeycomb-team=your-api-key,x-honeycomb-dataset=honeycomb-example"
$ scala-cli run tracing.scala
```

@:@

### Query collected traces and metrics

You can query collected traces and metrics at https://ui.honeycomb.io/.

#### Traces

@:image(honeycomb_traces_example.png) {
  alt = Honeycomb Traces Example
}

#### Metrics

@:image(honeycomb_metrics_example.png) {
  alt = Honeycomb Metrics Example
}
