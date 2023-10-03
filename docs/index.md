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

## Getting started

* Start tracing your application with [Jaeger and Docker](examples/jaeger-docker/README.md)
* Implement tracing and metrics with [Honeycomb](examples/honeycomb/README.md)

[cats-effect]: https://typelevel.org/cats-effect/
[opentelemetry-java]: https://github.com/open-telemetry/opentelemetry-java/tree/main/api/all
[otel]: https://opentelemetry.io/
[otel spec]: https://opentelemetry.io/docs/reference/specification/
