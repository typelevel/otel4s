# Jaeger - collecting traces

In this example, we are going to use [Jaeger](https://jaegertracing.io/) to collect and visualize traces produced by an
application.
We will cover the installation and configuration of Jaeger, as well as the instrumentation of the application using the
otel4s library.

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
javaOptions += "-Dotel.java.global-autoconfigure.enabled=true" // <4>
javaOptions += "-Dotel.service.name=jaeger-example"            // <5>
javaOptions += "-Dotel.metrics.exporter=none"                  // <6>
```

@:choice(scala-cli)

Add directives to the `tracing.scala`:
```scala
//> using lib "org.typelevel::otel4s-oteljava:@VERSION@" // <1>
//> using lib "io.opentelemetry:opentelemetry-exporter-otlp:@OPEN_TELEMETRY_VERSION@" // <2>
//> using lib "io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:@OPEN_TELEMETRY_VERSION@" // <3>
//> using `java-opt` "-Dotel.java.global-autoconfigure.enabled=true" // <4>
//> using `java-opt` "-Dotel.service.name=jaeger-example"            // <5>
//> using `java-opt` "-Dotel.metrics.exporter=none"                  // <6>
```

@:@

1) Add the `otel4s` library  
2) Add an OpenTelemetry exporter. Without the exporter, the application will crash  
3) Add an OpenTelemetry autoconfigure extension  
4) Enable OpenTelemetry SDK autoconfigure mode  
5) Add the name of the application to use in the traces  
6) Disable metrics exporter since Jaeger is compatible only with traces  

### OpenTelemetry SDK configuration

As mentioned above, we use `otel.service.name` and `otel.metrics.exporter` system properties to configure the
OpenTelemetry SDK.
The SDK can be configured via environment variables too. Check the full list
of [environment variable configurations](https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md)
for more options.

### Jaeger configuration

In order to collect and visualize traces, you can run [Jaeger](https://jaegertracing.io/)
using [Docker](https://www.docker.com/).

```shell
$ docker run --name jaeger \
  -e COLLECTOR_OTLP_ENABLED=true \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  jaegertracing/all-in-one:1.35
```

1) `-e COLLECTOR_OTLP_ENABLED=true` - enable OpenTelemetry receiver  
2) `-p 16686:16686` - forward Jaeger UI  
3) `-p 4317:4317` and `-p 4318:4318` - the OpenTelemetry receiver ports for HTTP and gRPC protocols  

### Application example

```scala mdoc:silent
import cats.effect.{Async, IO, IOApp, Resource}
import cats.effect.std.Console
import cats.effect.std.Random
import cats.syntax.all._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.trace.Tracer

import scala.concurrent.duration._

trait Work[F[_]] {
  def doWork: F[Unit]
}

object Work {
  def apply[F[_]: Async: Tracer: Console]: Work[F] =
    new Work[F] {
      def doWork: F[Unit] =
        Tracer[F].span("Work.DoWork").use { span =>
          span.addEvent("Starting the work.") *>
            doWorkInternal(steps = 10) *>
            span.addEvent("Finished working.")
        }

      def doWorkInternal(steps: Int): F[Unit] = {
        val step = Tracer[F]
          .span("internal", Attribute("steps", steps.toLong))
          .surround {
            for {
              random <- Random.scalaUtilRandom
              delay <- random.nextIntBounded(1000)
              _ <- Async[F].sleep(delay.millis)
              _ <- Console[F].println("Doin' work")
            } yield ()
          }

        if (steps > 0) step *> doWorkInternal(steps - 1) else step
      }
    }
}

object TracingExample extends IOApp.Simple {
  def tracer: Resource[IO, Tracer[IO]] =
    OtelJava.autoConfigured[IO]().evalMap(_.tracerProvider.get("Example"))

  def run: IO[Unit] =
    tracer
      .evalMap { implicit tracer: Tracer[IO] =>
        Work[IO].doWork
      }
      .use_
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
$ scala-cli run tracing.scala
```

@:@

### Review collected traces

Jaeger UI is available at http://localhost:16686. You can find the collected traces there.

@:image(traces_example.png) {
  alt = Jaeger Traces Example
}
