# otel4s

![Typelevel Organization Project](https://img.shields.io/badge/typelevel-organization%20project-FF6169.svg)
[![otel4s-core Scala version support](https://index.scala-lang.org/typelevel/otel4s/otel4s-core/latest.svg)](https://index.scala-lang.org/typelevel/otel4s/otel4s-core)

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

* **Pure Scala SDK**

  A pure Scala SDK backend is available in the separate
  [`otel4s-sdk`](https://typelevel.org/otel4s-sdk/sdk/overview.html) project
  for JVM, Scala.js, and Scala Native.

* **Testkit**

  A testkit simplifies the validation of telemetry behavior in the applications and libraries:
  1. Framework-agnostic, works with any test framework, such as weaver, munit, scalatest
  2. Ideal for testing of the instrumented applications in end-to-end or unit tests
  3. Included for the OpenTelemetry Java backend in this repo

## Status

`otel4s` 1.0 establishes the compatibility baseline for the modules published
from this repository, primarily `otel4s-core*`, `otel4s-oteljava*`, and the
semantic-convention and instrumentation modules.

Artifacts that remain unstable are explicitly marked, such as
`*-experimental` semantic-convention modules. The pure Scala SDK backend lives
in the separate [`otel4s-sdk`](https://typelevel.org/otel4s-sdk/sdk/overview.html)
project.

## Published modules in this repo

| Module family                | JVM | Scala Native | Scala.js |
|:----------------------------:|:---:|:------------:|:--------:|
|        `otel4s-core*`        |  ✅  |      ✅       |    ✅     |
|      `otel4s-semconv*`       |  ✅  |      ✅       |    ✅     |
| `otel4s-instrumentation-*`   |  ✅  |      ✅       |    ✅     |
|      `otel4s-oteljava*`      |  ✅  |      ❌       |    ❌     |

## Learn more

See the [website](https://typelevel.org/otel4s).

[cats-effect]: https://typelevel.org/cats-effect/
[opentelemetry-java]: https://opentelemetry.io/docs/languages/java/
[opentelemetry-java-instrumentation]: https://opentelemetry.io/docs/languages/java/instrumentation/
[otel]: https://opentelemetry.io/
[otel spec]: https://opentelemetry.io/docs/reference/specification/
