# Use otel4s with Java-instrumented libraries

Use this page when your application crosses a boundary between otel4s code and Java code that reads or
writes OpenTelemetry context.

Without explicit bridging at those boundaries, Java code and otel4s can observe different current spans.
That usually shows up when a Java framework starts the trace, or when a Java library expects the current
OpenTelemetry Java context.

## Prerequisites

Choose the setup that owns your OpenTelemetry SDK:

- [Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md) when your
  application creates the SDK
- [Use the global OpenTelemetry instance](../how-to-jvm-setup/use-the-global-opentelemetry-instance.md) when a Java
  agent or framework creates it

The examples below use `OtelJava.autoConfigured`. Both setup paths provide `otel4s.localContext`, which gives you the
`Local[F, Context]` instance used by the bridging helpers.

## 1. Create `OtelJava` and bring `Local[F, Context]` into scope

`Local[F, Context]` lets you move between Cats Effect code and the OpenTelemetry Java context.

```scala mdoc:silent
import cats.effect.{IO, IOApp}
import cats.mtl.Local
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.trace.Tracer

object Main extends IOApp.Simple {
  def run: IO[Unit] =
    OtelJava.autoConfigured[IO]().use { otel4s =>
      import otel4s.localContext
      otel4s.tracerProvider.get("auth-service").flatMap { implicit tracer =>
        program
      }
    }

  def program(implicit tracer: Tracer[IO], local: Local[IO, Context]): IO[Unit] =
    Tracer[IO].currentSpanContext.flatMap(_ => Local[IO, Context].ask.void)
}
```

When you create `OtelJava`, you usually get `Local[F, Context]` from `otel4s.localContext`.

## 2. Run otel4s code under an existing Java context

Use this pattern at a handler boundary, when a Java framework or library gives you an
`io.opentelemetry.context.Context`.

```scala mdoc:silent
import cats.effect.IO
import cats.mtl.Local
import io.opentelemetry.context.{Context => JContext}
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.trace.Tracer

def withJContext[F[_], A](ctx: JContext)(fa: F[A])(implicit
    L: Local[F, Context]
): F[A] =
  Local[F, Context].scope(fa)(Context.wrap(ctx))

def handleRequest(implicit tracer: Tracer[IO], local: Local[IO, Context]): IO[Unit] =
  withJContext(JContext.current()) {
    Tracer[IO].span("request.handle").surround(IO.unit)
  }
```

`Context.wrap(ctx)` converts the Java context to the otel4s context type.
`Local[F, Context].scope` then makes it current while `fa` runs.

## 3. Use the current otel4s context with OpenTelemetry Java

Use this pattern at a client or library boundary, when otel4s created the current span and Java code expects
the current Java context.

```scala mdoc:silent
import cats.effect.{IO, Sync}
import cats.mtl.Local
import cats.syntax.flatMap._
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.context.{Context => JContext}
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.trace.Tracer

def useJContext[F[_]: Sync, A](use: JContext => A)(implicit
    L: Local[F, Context]
): F[A] =
  Local[F, Context].ask.flatMap { ctx =>
    Sync[F].delay {
      val jContext = ctx.underlying
      val scope = jContext.makeCurrent()
      try use(jContext)
      finally scope.close()
    }
  }

def callJavaLibrary(implicit tracer: Tracer[IO], local: Local[IO, Context]): IO[Unit] =
  Tracer[IO].span("client.call").use { span =>
    useJContext[IO, String] { _ =>
      JSpan.current().getSpanContext.toString
    }.flatMap { javaContext =>
      IO.println(s"Java ctx: $javaContext").flatMap { _ =>
        IO.println(s"otel4s ctx: ${span.context}")
      }
    }
  }
```

The helper performs four operations:

1. `Local[F, Context].ask` reads the current otel4s context.
2. `ctx.underlying` extracts its OpenTelemetry Java context.
3. `makeCurrent()` installs that context in the Java thread-local storage.
4. `scope.close()` restores the previous Java context.

`Sync[F].delay` keeps those side effects inside `F`.
For a blocking Java call, define a variant that uses `Sync[F].blocking` or `Sync[F].interruptible` instead.

The Java and otel4s output should contain the same trace and span IDs:

```text
Java ctx: {traceId=06f5d9112efbe711947ebbded1287a30, spanId=26ed80c398cc039f, ...}
otel4s ctx: {traceId=06f5d9112efbe711947ebbded1287a30, spanId=26ed80c398cc039f, ...}
```

## What's next

- Apply both bridging patterns with the standard OpenTelemetry Java agent:
  [Use otel4s with Pekko HTTP instrumentation](use-otel4s-with-pekko-http-instrumentation.md)
- Continue incoming traces and propagate them downstream:
  [Propagate trace context across service boundaries](propagate-trace-context-across-service-boundaries.md)
- Understand why otel4s and OpenTelemetry Java have separate current-context views:
  [How otel4s context propagation works](../explanations/how-otel4s-context-propagation-works.md)
