# otel4s

otel4s provides [OpenTelemetry][otel] APIs and integrations for Scala, built on [Cats Effect][cats-effect]. Use it to
instrument applications and libraries with traces, metrics, and logs while keeping the telemetry backend explicit.

The core APIs support Scala 2.13 and Scala 3 on the JVM, Scala.js, and Scala Native. This repository also provides the
JVM-only `otel4s-oteljava` backend, which implements those APIs with OpenTelemetry Java.

## Why otel4s?

- **Designed for low overhead.** Compile-time techniques reduce runtime allocations, while no-op providers let
  instrumented code run without an SDK or exporter.
- **Modular.** Applications and libraries can depend only on the signals they use and choose a telemetry backend
  separately. See [Modules and module families](explanations/modules-and-module-families.md).
- **Cross-platform.** The core APIs support Scala 2.13 and Scala 3 on the JVM, Scala.js, and Scala Native.
- **Built for testing.** In-memory testkits collect traces, metrics, and logs and provide structured expectation APIs
  without requiring an external collector. See [Testkit](oteljava/testkit.md).

## Quick start

For a JVM application, add the OpenTelemetry Java backend, OTLP exporter, and autoconfiguration extension.

@:select(build-tool)

@:choice(sbt)

Add these settings to `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-oteljava" % "@VERSION@",
  "io.opentelemetry" % "opentelemetry-exporter-otlp" % "@OPEN_TELEMETRY_VERSION@" % Runtime,
  "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "@OPEN_TELEMETRY_VERSION@" % Runtime
)
javaOptions += "-Dotel.java.global-autoconfigure.enabled=true"
```

@:choice(scala-cli)

Add these directives to the `*.scala` file:

```scala
//> using dep "org.typelevel::otel4s-oteljava:@VERSION@"
//> using dep "io.opentelemetry:opentelemetry-exporter-otlp:@OPEN_TELEMETRY_VERSION@"
//> using dep "io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:@OPEN_TELEMETRY_VERSION@"
//> using javaOpt "-Dotel.java.global-autoconfigure.enabled=true"
```

@:@

Create a tracer and meter, then record a span and counter measurement:

```scala mdoc:reset:silent
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.oteljava.OtelJava

object Main extends IOApp.Simple {
  def run: IO[Unit] =
    OtelJava.autoConfigured[IO]().use { otel4s =>
      for {
        tracer <- otel4s.tracerProvider.get("com.example.app")
        meter <- otel4s.meterProvider.get("com.example.app")
        counter <- meter.counter[Long]("hello.count").create
        _ <- tracer.span("hello").surround {
          counter.inc().flatMap(_ => IO.println("hello"))
        }
      } yield ()
    }
}
```

For instrument types and recording patterns, see
[Record application metrics](how-to-metrics/record-application-metrics.md).

@:callout(info)

`OtelJava.autoConfigured` creates an isolated, non-global SDK instance and uses OpenTelemetry Java's OTLP defaults
unless you configure them. Follow [Set up otel4s in a JVM application][jvm-setup] to set the service name and exporter
endpoint and verify the exported span.

@:@

## Start here

- To configure a JVM application and export telemetry, follow
  [Set up otel4s in a JVM application][jvm-setup].
- To instrument a library without choosing a backend for its users, see
  [Modules and module families](explanations/modules-and-module-families.md#which-module-do-i-need).
- To use a backend on Scala.js or Scala Native, see the separate [otel4s SDK][otel4s-sdk] project.

## Guides

| Task | Guide |
|---|---|
| Configure the OpenTelemetry Java backend | [JVM setup](how-to-jvm-setup/index.md) |
| Create spans and propagate trace context | [Tracing](how-to-tracing/index.md) |
| Record application and runtime metrics | [Metrics](how-to-metrics/index.md) |
| Bridge application logs into OpenTelemetry | [Logs](how-to-logs/index.md) |
| Use generated OpenTelemetry attributes and metric specs | [Semantic conventions](how-to-semantic-conventions/index.md) |
| Assert exported telemetry in tests | [Testkit](how-to-testkit/index.md) |

## Understand otel4s

- [Modules and module families](explanations/modules-and-module-families.md) explains how the API, backend,
  instrumentation, semantic convention, and testkit artifacts fit together.
- [The JVM backend](explanations/oteljava-jvm-backend.md) explains when to use `OtelJava.autoConfigured` or
  `OtelJava.global`.
- [How otel4s context propagation works](explanations/how-otel4s-context-propagation-works.md) describes the tracing
  context model used by the core APIs.
- [Semantic conventions and stability](explanations/semantic-conventions-and-stability.md) describes the stable and
  experimental semantic convention modules.

## API reference

- [Tracing API](instrumentation/tracing.md)
- [Metrics API](instrumentation/metrics.md)
- [Logs API](instrumentation/logs.md)
- [Cross-service trace propagation](instrumentation/tracing-cross-service-propagation.md)
- [Semantic conventions](instrumentation/semantic-conventions.md)
- [OpenTelemetry Java testkit](oteljava/testkit.md)

To run instrumented code without an SDK or exporter, provide a
[no-op tracer](how-to-tracing/provide-a-no-op-tracer.md) or
[no-op meter](how-to-metrics/provide-a-no-op-meter.md).

## Published modules

`otel4s` 1.0 establishes the compatibility baseline for the modules published from this repository. Artifacts that
remain unstable are explicitly marked, including the `*-experimental` semantic convention modules.

| Module family | JVM | Scala Native | Scala.js |
|---|:---:|:---:|:---:|
| `otel4s-core*` | ✅ | ✅ | ✅ |
| `otel4s-semconv*` | ✅ | ✅ | ✅ |
| `otel4s-instrumentation-*` | ✅ | ✅ | ✅ |
| `otel4s-oteljava*` | ✅ | ❌ | ❌ |

## Examples and integrations

- Run otel4s locally with [Jaeger and Docker](examples/jaeger-docker/README.md) or
  [Grafana](examples/grafana/README.md).
- Export traces and metrics to [Honeycomb](examples/honeycomb/README.md) or [Dash0](examples/dash0/README.md).
- Find integrations for other Typelevel libraries on the [Ecosystem](ecosystem.md) page.

[cats-effect]: https://typelevel.org/cats-effect/
[jvm-setup]: how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md
[otel]: https://opentelemetry.io/
[otel4s-sdk]: https://typelevel.org/otel4s-sdk/sdk/overview.html
