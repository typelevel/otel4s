# otel4s

## Telemetry meets higher-kinded types

_otel4s_ is an [OpenTelemetry][otel] implementation for Scala.  The
design goal is to fully and faithfully implement the [OpenTelemetry
Specification][otel spec] atop [Cats Effect][cats-effect].

## Features

* **Simple and idiomatic metrics and tracing API**

  Provides a user-friendly and idiomatic API for telemetry, designed with Typelevel ecosystem best practices.
  Intuitive interfaces for [metrics](instrumentation/metrics.md), [tracing](instrumentation/tracing.md), 
  and context propagation.

* **Minimal overhead**

  The library utilizes metaprogramming techniques to reduce runtime costs and allocations.
  **Near-zero** overhead when telemetry is disabled,
  ensuring production performance is unaffected when tracing or metrics collection is not required.

* **Modularity**

  A modular architecture allows to include only the required components:
  1. Core modules: designed for library instrumentation, offering a lightweight dependency footprint
  2. Selective integration: use only the metrics or tracing functionality without requiring the other

* **Cross-platform**

  All modules are available for Scala 2.13 and Scala 3.
  Core modules are available on all platforms: JVM, Scala.js, and Scala Native.

* **OpenTelemetry Java SDK backend**

  The [backend](oteljava/overview.md) utilizes [OpenTelemetry Java SDK][opentelemetry-java] under the hood, 
  offering production-ready telemetry:
  1. Low memory overhead
  2. Extensive [instrumentation ecosystem][opentelemetry-java-instrumentation]
  3. Well-tested implementation

* **SDK backend**

  [SDK backend](sdk/overview.md) is implemented in Scala from scratch. Available for JVM, Scala.js, and Scala Native.
  While the implementation is compliant with the OpenTelemetry specification,
  it remains **experimental** and some functionality may be lacking.

* **Testkit**

  A testkit simplifies the validation of telemetry behavior in the applications and libraries:
  1. Framework-agnostic, works with any test framework, for example weaver, munit, scalatest
  2. Ideal for testing of the instrumented applications in end-to-end or unit tests
  3. Available for [OpenTelemetry Java](oteljava/testkit.md) and [SDK](sdk/testkit.md) backends

## Status

The API is still highly experimental, but we are actively
instrumenting various libraries and applications to check for fit.
Don't put it in a binary-stable library yet, but we invite you to try
it out and let us know what you think.

## Modules availability

| Module / Platform | JVM | Scala Native | Scala.js |  
|:-----------------:|:---:|:------------:|:--------:|
|   `otel4s-core`   |  ✅  |      ✅       |    ✅     |
|   `otel4s-sdk`    |  ✅  |      ✅       |    ✅     |
| `otel4s-oteljava` |  ✅  |      ❌       |    ❌     |

## How to choose a backend

For most cases, `otel4s-oteljava` is the recommended backend, 
that utilizes [OpenTelemetry Java][opentelemetry-java] library under the hood.
You can benefit from various integrations and low memory overhead.

`otel4s-sdk` is an **experimental** implementation of the Open Telemetry specification in pure Scala
and available for all platforms: JVM, Scala.js, and Scala Native.
However, some features are missing, and the memory overhead may be noticeable.

## Getting started

If you develop an application and want to export the telemetry, use `otel4s-oteljava` module. 
If you develop a library, check out this [recommendation](modules-structure.md#which-module-do-i-need).

@:select(build-tool)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-oteljava" % "@VERSION@", // <1>
  "io.opentelemetry" % "opentelemetry-exporter-otlp" % "@OPEN_TELEMETRY_VERSION@" % Runtime, // <2>
  "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "@OPEN_TELEMETRY_VERSION@" % Runtime // <3>
)
javaOptions += "-Dotel.java.global-autoconfigure.enabled=true" // <4>
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using dep "org.typelevel::otel4s-oteljava:@VERSION@" // <1>
//> using dep "io.opentelemetry:opentelemetry-exporter-otlp:@OPEN_TELEMETRY_VERSION@" // <2>
//> using dep "io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:@OPEN_TELEMETRY_VERSION@" // <3>
//> using javaOpt "-Dotel.java.global-autoconfigure.enabled=true" // <4>
```

@:@

1. Add the `otel4s-oteljava` library  
2. Add an OpenTelemetry exporter. Without the exporter, the application will crash  
3. Add an OpenTelemetry autoconfigure extension  
4. Enable OpenTelemetry SDK [autoconfigure mode][opentelemetry-java-autoconfigure]  

Then, the instance can be materialized:
```scala mdoc:reset:silent
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.trace.TracerProvider

object Main extends IOApp.Simple {
  def run: IO[Unit] =
    OtelJava.autoConfigured[IO]().use { otel4s =>
      program(otel4s.meterProvider, otel4s.tracerProvider)
    }

  def program(
      meterProvider: MeterProvider[IO],
      tracerProvider: TracerProvider[IO]
  ): IO[Unit] =
    for {
      meter <- meterProvider.get("service")
      tracer <- tracerProvider.get("service")

      // create a counter and increment its value
      counter <- meter.counter[Long]("counter").create
      _ <- counter.inc()

      // create and materialize a span
      _ <- tracer.span("span").surround(IO.unit)
    } yield ()
}
```

@:callout(warning)

`OtelJava.autoConfigured` creates an **isolated** **non-global** instance.
If you create multiple instances, those instances won't interoperate (i.e. be able to see each others spans).

@:@

## Examples

* Start tracing your application with [Jaeger and Docker](examples/jaeger-docker/README.md)
* Implement tracing and metrics with [Honeycomb](examples/honeycomb/README.md)

## The noop Tracer and Meter  

If you use a library that supports otel4s (eg [Skunk](https://github.com/typelevel/skunk)) but do not want to use Open Telemetry, 
then you can place the [No-op Tracer](https://www.javadoc.io/doc/org.typelevel/otel4s-docs_2.13/latest/org/typelevel/otel4s/trace/Tracer$.html) into implicit scope.

The no-op `Tracer` or `Meter` can be provided in the following ways:

@:select(scala-version)

@:choice(scala-2)

By using the `import Tracer.Implicits.noop` and `import Meter.Implicits.noop`:
```scala mdoc:compile-only
import cats.effect.IO
import org.typelevel.otel4s.trace.Tracer

def program[F[_]: Tracer]: F[Unit] = ???

import Tracer.Implicits.noop
val io: IO[Unit] = program[IO]
```

By defining an `implicit val`:

```scala mdoc:compile-only
import cats.effect.IO
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.trace.Tracer

def program[F[_]: Meter: Tracer]: F[Unit] = ???

implicit val meter: Meter[IO] = Meter.noop
implicit val tracer: Tracer[IO] = Tracer.noop
val io: IO[Unit] = program[IO]
```

@:choice(scala-3)

By using the `import Tracer.Implicits.noop` and `import Meter.Implicits.noop`:
```dotty
import cats.effect.IO
import org.typelevel.otel4s.trace.Tracer

def program[F[_]](using Meter[F], Tracer[F]): F[Unit] = ???

import Tracer.Implicits.noop
val io: IO[Unit] = program[IO]
```

By defining a `given`:

```dotty
import cats.effect.IO
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.trace.Tracer

def program[F[_]](using Meter[F], Tracer[F]): F[Unit] = ???

given Meter[IO] = Meter.noop
given Tracer[IO] = Tracer.noop
val io: IO[Unit] = program[IO]
```

@:@

[cats-effect]: https://typelevel.org/cats-effect/
[opentelemetry-java]: https://opentelemetry.io/docs/languages/java/
[opentelemetry-java-instrumentation]: https://opentelemetry.io/docs/languages/java/instrumentation/
[opentelemetry-java-autoconfigure]: https://opentelemetry.io/docs/languages/java/configuration/
[otel]: https://opentelemetry.io/
[otel spec]: https://opentelemetry.io/docs/reference/specification/
