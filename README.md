# otel4s

![Typelevel Organization Project](https://img.shields.io/badge/typelevel-organization%20project-FF6169.svg)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.typelevel/otel4s-core_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.typelevel/otel4s-core_2.13)

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

## Learn more

See the [website](https://typelevel.org/otel4s).

[cats-effect]: https://typelevel.org/cats-effect/
[opentelemetry-java]: https://github.com/open-telemetry/opentelemetry-java/tree/main/api/all
[otel]: https://opentelemetry.io/
[otel spec]: https://opentelemetry.io/docs/reference/specification/
