# otel4s

![Typelevel Organization Project](https://img.shields.io/badge/typelevel-organization%20project-FF6169.svg)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.typelevel/otel4s-core_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.typelevel/otel4s-core_2.13)

## Telemetry meets higher-kinded types

_otel4s_ is an [OpenTelemetry][otel] implementation for Scala.  The
design goal is to fully and faithfully implement the [OpenTelemetry
Specification][otel spec] atop [Cats Effect][cats-effect].

## Features

* **Simple and idiomatic metrics and tracing API**

  Provides a user-friendly and idiomatic API for telemetry, designed with Typelevel ecosystem best practices.
  Intuitive interfaces for metrics, tracing, and context propagation.

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

  The backend utilizes [OpenTelemetry Java SDK][opentelemetry-java] under the hood, offering production-ready telemetry:
  1. Low memory overhead
  2. Extensive [instrumentation ecosystem][opentelemetry-java-instrumentation]
  3. Well-tested implementation

* **SDK backend**

  SDK modules are implemented in Scala from scratch. Available for JVM, Scala.js, and Scala Native.
  While the implementation is compliant with the OpenTelemetry specification,
  it remains **experimental** and some functionality may be lacking.

* **Testkit**

  A testkit simplifies the validation of telemetry behavior in the applications and libraries:
  1. Framework-agnostic, works with any test framework, such as weaver, munit, scalatest
  2. Ideal for testing of the instrumented applications in end-to-end or unit tests
  3. Available for OpenTelemetry Java and SDK backends

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

## Learn more

See the [website](https://typelevel.org/otel4s).

[cats-effect]: https://typelevel.org/cats-effect/
[opentelemetry-java]: https://opentelemetry.io/docs/languages/java/
[opentelemetry-java-instrumentation]: https://opentelemetry.io/docs/languages/java/instrumentation/
[otel]: https://opentelemetry.io/
[otel spec]: https://opentelemetry.io/docs/reference/specification/
