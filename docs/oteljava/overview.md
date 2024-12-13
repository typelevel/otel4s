# Overview

`otel4s-oteljava` modules provide implementation of the API interfaces. 
The implementation uses [opentelemetry-java][opentelemetry-java] under the hood.

There are several advantages of using the `otel4s-oteljava`:
- Low memory overhead
- Extensive [instrumentation ecosystem][opentelemetry-java-instrumentation]
- Easy integration with Java libraries
- Well-tested implementation

It's a recommended backend to use on JVM.

## Getting Started

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

## Creating an autoconfigured instance 

You can use `OtelJava.autoConfigured` to autoconfigure the SDK:
```scala mdoc:silent:reset
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.trace.TracerProvider

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] =
    OtelJava.autoConfigured[IO]().use { otel4s =>
      program(otel4s.meterProvider, otel4s.tracerProvider)
    }

  def program(
      meterProvider: MeterProvider[IO], 
      tracerProvider: TracerProvider[IO]
  ): IO[Unit] =
    ???
}
```

The `OtelJava.autoConfigured(...)` relies on the environment variables and system properties to configure the SDK.
For example, use `export OTEL_SERVICE_NAME=auth-service` to configure the name of the service.

See the full set of the [supported options][opentelemetry-java-configuration].

@:callout(warning)

`OtelJava.autoConfigured` creates an **isolated** **non-global** instance. 
If you create multiple instances, those instances won't interoperate (i.e. be able to see each others spans).

@:@

## Accessing the global instance

There are several reasons to use the global instance:
- You are using the [Java Agent][opentelemetry-java-agent]
- You must reuse the global instance to interoperate with Java libraries
- You have no control over the entry point 
  
In such a case, you can use the `OtelJava.global` to use the global OpenTelemetry instance:
```scala mdoc:silent:reset
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.trace.TracerProvider

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] =
    OtelJava.global[IO].flatMap { otel4s =>
      program(otel4s.meterProvider, otel4s.tracerProvider)
    }

  def program(
      meterProvider: MeterProvider[IO], 
      tracerProvider: TracerProvider[IO]
  ): IO[Unit] =
    ???
}
```

[opentelemetry-java]: https://opentelemetry.io/docs/languages/java/intro/
[opentelemetry-java-instrumentation]: https://opentelemetry.io/docs/languages/java/instrumentation/
[opentelemetry-java-autoconfigure]: https://opentelemetry.io/docs/languages/java/configuration/#zero-code-sdk-autoconfigure
[opentelemetry-java-configuration]: https://opentelemetry.io/docs/languages/java/configuration/#environment-variables-and-system-properties
[opentelemetry-java-agent]: https://opentelemetry.io/docs/zero-code/java/agent/