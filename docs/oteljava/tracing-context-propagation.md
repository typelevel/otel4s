# Tracing | Context propagation

[OpenTelemetry Java SDK][opentelemetry-java] and otel4s rely on different context manipulation approaches,
which aren't interoperable out of the box.
Java SDK utilizes ThreadLocal variables to share tracing information,
otel4s, on the other hand, uses [Local][cats-mtl-local].

Cats Effect 3.6.0 introduced a new method of fiber context tracking,
which can be integrated almost seamlessly with the OpenTelemetry Java SDK.

## Getting started

@:select(build-tool)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-oteljava" % "@VERSION@", // <1>
  "org.typelevel" %% "otel4s-oteljava-context-storage" % "@VERSION@", // <2>
)
javaOptions += "-Dcats.effect.trackFiberContext=true" // <3>
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using dep "org.typelevel::otel4s-oteljava:@VERSION@" // <1>
//> using dep "org.typelevel::otel4s-oteljava-context-storage:@VERSION@" // <2>
//> using `java-opt` "-Dcats.effect.trackFiberContext=true" // <3>
```

@:@

1. Add the `otel4s-oteljava` library
2. Add the `otel4s-oteljava-context-storage` library
3. Enable Cats Effect fiber context tracking

## Configuration

You need to use `IOLocalContextStorage.localProvider[IO]` to provide the global context storage, backed by `IOLocal`:
```scala mdoc:silent
import cats.effect.IO
import cats.effect.IOApp
import io.opentelemetry.api.trace.{Span => JSpan}
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.oteljava.IOLocalContextStorage
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.trace.Tracer

object Main extends IOApp.Simple {
  def program(tracer: Tracer[IO]): IO[Unit] =
    tracer.span("test").use { span => // start 'test' span using otel4s
      println(s"jctx: ${JSpan.current().getSpanContext}") // get a span from a ThreadLocal
      IO.println(s"otel4s: ${span.context}")
    }

  def run: IO[Unit] = {
    implicit val provider: LocalProvider[IO, Context] =
      IOLocalContextStorage.localProvider[IO]

    OtelJava.autoConfigured[IO]().use { otelJava =>
      otelJava.tracerProvider.tracer("com.service").get.flatMap { tracer =>
        program(tracer)
      }
    }
  } 
}
```

According to the output, the context is the same:
```
jctx  : SpanContext{traceId=58b8ed50a558ca53fcc64a0d80b5e662, spanId=fc25fe2c9fb41905, ...} 
otel4s: SpanContext{traceId=58b8ed50a558ca53fcc64a0d80b5e662, spanId=fc25fe2c9fb41905, ...}
```

## Limitations

The `IOLocalContextStorageProvider` doesn't work with [OpenTelemetry Java Agent][opentelemetry-java-agent].

[opentelemetry-java]: https://github.com/open-telemetry/opentelemetry-java
[opentelemetry-java-agent]: https://opentelemetry.io/docs/zero-code/java/agent/
[cats-mtl-local]: https://typelevel.org/cats-mtl/mtl-classes/local.html
