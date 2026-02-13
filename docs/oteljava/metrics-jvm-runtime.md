# Metrics | JVM Runtime

See the [semantic conventions][semantic-conventions] for JVM metrics. 

## Java 8 and newer

The OpenTelemetry [runtime-telemetry-java8][otel-jvm-metrics-8] module provides the JVM runtime metrics for Java 8 and newer.
The module uses JMX to produce metrics.

Add the following configuration to the favorite build tool:

@:select(build-tool)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-oteljava" % "@VERSION@", // <1>
  "io.opentelemetry.instrumentation" % "opentelemetry-runtime-telemetry-java8" % "@OPEN_TELEMETRY_INSTRUMENTATION_ALPHA_VERSION@" // <2>
)
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using dep "org.typelevel::otel4s-oteljava:@VERSION@" // <1>
//> using dep "io.opentelemetry.instrumentation:opentelemetry-runtime-telemetry-java8:@OPEN_TELEMETRY_INSTRUMENTATION_ALPHA_VERSION@" // <2>
```

@:@

1. Add the `otel4s-oteljava` library
2. Add the OpenTelemetry [runtime metrics][otel-jvm-metrics-8] library

The producers can be registered manually:

```scala mdoc:silent
import cats.effect.{IO, IOApp, Resource, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.opentelemetry.api.{OpenTelemetry => JOpenTelemetry}
import io.opentelemetry.instrumentation.runtimemetrics.java8._
import org.typelevel.otel4s.oteljava.OtelJava

object Service extends IOApp.Simple {
  
  def run: IO[Unit] =
    OtelJava
      .autoConfigured[IO]()
      .flatTap(otel4s => registerRuntimeMetrics(otel4s.underlying))
      .use { otel4s =>
        val _ = otel4s
        ???
      }
  
  private def registerRuntimeMetrics[F[_]: Sync](
      openTelemetry: JOpenTelemetry
  ): Resource[F, Unit] = {
    val acquire = Sync[F].delay(RuntimeMetrics.create(openTelemetry))
  
    Resource.fromAutoCloseable(acquire).void
  }

}
```

## Java 17 and newer

The OpenTelemetry [runtime-telemetry-java17][otel-jvm-metrics-17] module provides JVM runtime metrics for Java 17 and newer.
The module uses JMX and JFR to produce metrics. 

Add the following configuration to the favorite build tool:

@:select(build-tool)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-oteljava" % "@VERSION@", // <1>
  "io.opentelemetry.instrumentation" % "opentelemetry-runtime-telemetry-java17" % "@OPEN_TELEMETRY_INSTRUMENTATION_ALPHA_VERSION@" // <2>
)
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using dep "org.typelevel::otel4s-oteljava:@VERSION@" // <1>
//> using dep "io.opentelemetry.instrumentation:opentelemetry-runtime-telemetry-java17:@OPEN_TELEMETRY_INSTRUMENTATION_ALPHA_VERSION@" // <2>
```

@:@

1. Add the `otel4s-oteljava` library
2. Add the OpenTelemetry [runtime metrics][otel-jvm-metrics-17] library

The producers can be registered manually:

```scala mdoc:silent:reset
import cats.effect.{IO, IOApp, Resource, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.opentelemetry.api.{OpenTelemetry => JOpenTelemetry}
import io.opentelemetry.instrumentation.runtimemetrics.java17._
import org.typelevel.otel4s.oteljava.OtelJava

object Service extends IOApp.Simple {
  
  def run: IO[Unit] = 
    OtelJava
      .autoConfigured[IO]()
      .flatTap(otel4s => registerRuntimeMetrics(otel4s.underlying))
      .use { otel4s =>
        val _ = otel4s
        ???
      }

  private def registerRuntimeMetrics[F[_]: Sync](
      openTelemetry: JOpenTelemetry
  ): Resource[F, Unit] = {
    val acquire = Sync[F].delay(RuntimeMetrics.create(openTelemetry))

    Resource.fromAutoCloseable(acquire).void
  }

}
```

[semantic-conventions]: https://opentelemetry.io/docs/specs/semconv/runtime/jvm-metrics
[otel-jvm-metrics-8]: https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/runtime-telemetry/runtime-telemetry-java8/library
[otel-jvm-metrics-17]: https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/runtime-telemetry/runtime-telemetry-java17/library