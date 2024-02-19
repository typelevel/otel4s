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
| `otel4s-oteljava` |  ✅  |      ❌       |    ❌     |

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
//> using lib "org.typelevel::otel4s-oteljava:@VERSION@" // <1>
//> using lib "io.opentelemetry:opentelemetry-exporter-otlp:@OPEN_TELEMETRY_VERSION@" // <2>
//> using lib "io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:@OPEN_TELEMETRY_VERSION@" // <3>
//> using `java-opt` "-Dotel.java.global-autoconfigure.enabled=true" // <4>
```

@:@

1. Add the `otel4s-oteljava` library  
2. Add an OpenTelemetry exporter. Without the exporter, the application will crash  
3. Add an OpenTelemetry autoconfigure extension  
4. Enable OpenTelemetry SDK [autoconfigure mode][opentelemetry-java-autoconfigure]  

## Examples

* Start tracing your application with [Jaeger and Docker](examples/jaeger-docker/README.md)
* Implement tracing and metrics with [Honeycomb](examples/honeycomb/README.md)

[cats-effect]: https://typelevel.org/cats-effect/
[opentelemetry-java]: https://github.com/open-telemetry/opentelemetry-java/tree/main/api/all
[opentelemetry-java-autoconfigure]: https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md
[otel]: https://opentelemetry.io/
[otel spec]: https://opentelemetry.io/docs/reference/specification/
