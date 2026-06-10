# Keep otel4s context in sync with OpenTelemetry Java

Use this page when otel4s code and Java code in the same process need to observe the same current span.

This usually matters when:

- a Java framework starts or reads OpenTelemetry context
- a Java library expects `io.opentelemetry.context.Context.current()`
- you want otel4s spans to be visible to Java instrumentation in the same application

OpenTelemetry Java SDK and otel4s use different context propagation mechanisms by default.
OpenTelemetry Java uses `ThreadLocal` state, while otel4s uses `Local`.
Without extra setup, those contexts do not stay aligned automatically.

## 1. Add the context-storage dependency

This page assumes you already depend on `otel4s-oteljava` from the JVM setup guide.

@:select(build-tool)

@:choice(sbt)

Add settings to `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-oteljava-context-storage" % "@VERSION@" // <1>
)
javaOptions += "-Dcats.effect.trackFiberContext=true" // <2>
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using dep "org.typelevel::otel4s-oteljava-context-storage:@VERSION@" // <1>
//> using javaOpt "-Dcats.effect.trackFiberContext=true" // <2>
```

@:@

1. Add `otel4s-oteljava-context-storage`
2. Enable Cats Effect fiber context tracking

## 2. Provide `IOLocalContextStorage.localProvider[IO]`

Create `OtelJava` with a `LocalProvider[IO, Context]` backed by `IOLocalContextStorage`.

```scala mdoc:silent
import cats.effect.{IO, IOApp}
import io.opentelemetry.api.trace.{Span => JSpan}
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.context.IOLocalContextStorage
import org.typelevel.otel4s.trace.Tracer

object Main extends IOApp.Simple {
  implicit val provider: LocalProvider[IO, Context] =
    IOLocalContextStorage.localProvider[IO]

  def run: IO[Unit] =
    OtelJava.autoConfigured[IO]().use { otel4s =>
      otel4s.tracerProvider.get("auth-service").flatMap { implicit tracer =>
        program
      }
    }

  def program(implicit tracer: Tracer[IO]): IO[Unit] =
    Tracer[IO].span("test").use { span =>
      IO.println(s"jctx: ${JSpan.current().getSpanContext}") *>
        IO.println(s"otel4s: ${span.context}")
    }
}
```

With that provider in place, the current Java context and the current otel4s span stay aligned while your `IO`
program runs.

Example output:

```text
jctx: SpanContext{traceId=58b8ed50a558ca53fcc64a0d80b5e662, spanId=fc25fe2c9fb41905, ...}
otel4s: SpanContext{traceId=58b8ed50a558ca53fcc64a0d80b5e662, spanId=fc25fe2c9fb41905, ...}
```

## 3. Use this setup before tracing across Java boundaries

Once the context storage is configured, you can:

- run otel4s code under an incoming Java context
- run Java library calls under the current otel4s context
- let Java code and otel4s code see the same current span

This page covers the setup only.
For handler, client, and library boundary patterns, see the tracing how-to pages.

## 4. Know the limitations

- `IOLocalContextStorage` does not work with the standard OpenTelemetry Java agent
- if you use `otel4s-opentelemetry-javaagent`, follow
  [Use the otel4s Java agent](use-the-otel4s-java-agent.md)
- for background on why that agent needs its own setup path, see
  [How otel4s context works with the otel4s Java agent](../explanations/how-otel4s-context-works-with-the-otel4s-java-agent.md)
- if you use the testkit and rely on `IOLocalContextStorage`, see the existing
  [Testkit](../oteljava/testkit.md#iolocalcontextstorage) page

## What's next

- Use otel4s with Java code that reads or writes OpenTelemetry context:
  [Use otel4s with Java-instrumented libraries](../how-to-tracing/use-otel4s-with-java-instrumented-libraries.md)
- Continue incoming traces and propagate them downstream:
  [Propagate trace context across service boundaries](../how-to-tracing/propagate-trace-context-across-service-boundaries.md)
