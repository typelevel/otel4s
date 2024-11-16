# otel4s

## Telemetry meets higher-kinded types

_otel4s_ is an [OpenTelemetry][otel] implementation for Scala.  The
design goal is to fully and faithfully implement the [OpenTelemetry
Specification][otel spec] atop [Cats Effect][cats-effect].

## Features

* otel4s provides APIs and implementations for metrics and tracing.

* Core modules for JVM, Scala.js, and Scala Native offer an API with
  no-op implementations.  These are appropriate for library
  instrumentation.

* SDK modules provide working telemetry for applications.
  SDK modules are implemented in Scala from scratch. Available for JVM, Scala.js, and Scala Native.
  The implementation remains **experimental** and some functionality may be lacking. 

* [opentelemetry-java][opentelemetry-java] backend provided for the
  JVM.  This provides working telemetry for applications.

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

## Examples

* Start tracing your application with [Jaeger and Docker](examples/jaeger-docker/README.md)
* Implement tracing and metrics with [Honeycomb](examples/honeycomb/README.md)

## The Noop Tracer  

If you use a library that supports otel4s (eg [Skunk](https://github.com/typelevel/skunk)) but do not want to use Open Telemetry, then you can place the [No-op Tracer](https://www.javadoc.io/doc/org.typelevel/otel4s-docs_2.13/latest/org/typelevel/otel4s/trace/Tracer$.html) into implicit scope.

The no-op `Tracer` can be provided in the following ways:

@:select(scala-version)

@:choice(scala-2)

By using the `import Tracer.Implicits.noop`:
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
import org.typelevel.otel4s.trace.Tracer

def program[F[_]: Tracer]: F[Unit] = ???

implicit val tracer: Tracer[IO] = Tracer.noop
val io: IO[Unit] = program[IO]
```

@:choice(scala-3)

By using the `import Tracer.Implicits.noop`:
```dotty
import cats.effect.IO
import org.typelevel.otel4s.trace.Tracer

def program[F[_]](using Tracer[F]): F[Unit] = ???

import Tracer.Implicits.noop
val io: IO[Unit] = program[IO]
```

By defining a `given`:

```dotty
import cats.effect.IO
import org.typelevel.otel4s.trace.Tracer

def program[F[_]](using Tracer[F]): F[Unit] = ???

given Tracer[IO] = Tracer.noop
val io: IO[Unit] = program[IO]
```

@:@

[cats-effect]: https://typelevel.org/cats-effect/
[opentelemetry-java]: https://github.com/open-telemetry/opentelemetry-java/tree/main/api/all
[opentelemetry-java-autoconfigure]: https://opentelemetry.io/docs/languages/java/configuration/
[otel]: https://opentelemetry.io/
[otel spec]: https://opentelemetry.io/docs/reference/specification/
