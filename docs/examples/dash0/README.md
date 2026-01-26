# Dash0 - metrics and traces

In this example, we are going to use [Dash0](https://www.dash0.com) to collect and visualize metrics and traces produced by an application.
We will cover the configuration of OpenTelemetry exporter, as well as the instrumentation of the application using the otel4s library.

Unlike [Jaeger example](../jaeger-docker/README.md), you do not need to set up a collector service locally. The metrics and traces will be sent to a remote Dash0 API.

At the time of writing, Dash0 offers 14 day free trial, afterwards 1 million spans cost 20 cents.
It offers robust analysis and visualization tools that are handy for exploring the world of telemetry.

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
javaOptions += "-Dotel.java.global-autoconfigure.enabled=true"            // <4>
javaOptions += "-Dotel.service.name=dash0-example"                    // <5>
javaOptions += "-Dotel.exporter.otlp.endpoint=https://ingress.eu-west-1.aws.dash0.com  // <6>
```

@:choice(scala-cli)

Add directives to the `tracing.scala`:
```scala
//> using dep "org.typelevel::otel4s-oteljava:@VERSION@" // <1>
//> using dep "io.opentelemetry:opentelemetry-exporter-otlp:@OPEN_TELEMETRY_VERSION@" // <2>
//> using dep "io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:@OPEN_TELEMETRY_VERSION@" // <3>
//> using javaOpt "-Dotel.java.global-autoconfigure.enabled=true"            // <4>
//> using javaOpt "-Dotel.service.name=dash0-example"                    // <5>
//> using javaOpt "-Dotel.exporter.otlp.endpoint=https://ingress.eu-west-1.aws.dash0.com"  // <6>
```

@:@

1) Add the `otel4s` library  
2) Add an OpenTelemetry exporter. Without the exporter, the application will crash  
3) Add an OpenTelemetry autoconfigure extension  
4) Enable OpenTelemetry SDK autoconfigure mode  
5) Add the name of the application to use in the traces  
6) Add the Dash0 API endpoint  

### OpenTelemetry SDK configuration

As mentioned above, we use `otel.java.global-autoconfigure.enabled` and `otel.service.name` system properties to configure the
OpenTelemetry SDK.
The SDK can be configured via environment variables too. Check the full list
of [environment variable configurations](https://opentelemetry.io/docs/languages/java/configuration)
for more options.

### Acquiring a Dash0 Auth token

First, you must create an account on the [Dash0 website](https://www.dash0.com/sign-up).
Once you have done this, log into your account and navigate to the organization settings page. Under Auth Tokens, you can generate a new auth token.
Under Endpoints you can also discover the value for `-Dotel.exporter.otlp.endpoint` that we used above. It can differ depending on your cloud region choice.

Use a different auth tokens and datasets for test, production, and local development. This organizes your data in Dash0.

### Dash0 configuration

In order to send metrics and traces to Dash0, the auth token and metrics dataset name need to be configured.
Since the auth token is sensitive data, we advise providing them via environment variables:

```shell
$ export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Bearer auth_token,Dash0-Dataset=otel-metrics"
```

1) `Authorization` - the Bearer auth token  
2) `Dash0-Dataset` - the name of the dataset to send metrics to.

Each service's traces will land in a dataset defined in 'otel.service.name'.

**Note:** if the `Dash0-Dataset` header is not configured, the **metrics** will be sent to a dataset called `default`.

### Application example

```scala mdoc:silent
import java.util.concurrent.TimeUnit

import cats.effect.{Async, IO, IOApp}
import cats.effect.std.Console
import cats.effect.std.Random
import cats.syntax.all._
import org.typelevel.otel4s.{Attribute, AttributeKey}
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.metrics.Histogram
import org.typelevel.otel4s.trace.Tracer

import scala.concurrent.duration._

trait Work[F[_]] {
  def doWork: F[Unit]
}

object Work {
  def apply[F[_]: Async: Tracer: Console](histogram: Histogram[F, Double]): Work[F] =
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
    OtelJava
      .autoConfigured[IO]()
      .evalMap { otel4s =>
        otel4s.tracerProvider.get("com.service.runtime")
          .flatMap { implicit tracer: Tracer[IO] =>
            for {
              meter <- otel4s.meterProvider.get("com.service.runtime")
              histogram <- meter.histogram[Double]("work.execution.duration").create
              _ <- Work[IO](histogram).doWork
            } yield ()
          }
      }
      .use_
  }
}
```

### Run the application

@:select(build-tool)

@:choice(sbt)

```shell
$ export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Bearer auth_token,Dash0-Dataset=otel-metrics"
$ sbt run
```

@:choice(scala-cli)

```shell
$ export OTEL_EXPORTER_OTLP_HEADERS="Authorization=Bearer auth_token,Dash0-Dataset=otel-metrics"
$ scala-cli run tracing.scala
```

@:@

### Query collected traces and metrics

You can query collected traces and metrics at https://app.dash0.com/.

