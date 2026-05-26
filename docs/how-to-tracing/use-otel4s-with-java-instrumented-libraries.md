# Use otel4s with Java-instrumented libraries

Use this page when your application crosses a boundary between otel4s code and Java code that reads or
writes OpenTelemetry context.

Without explicit bridging at those boundaries, Java code and otel4s can observe different current spans.
That usually shows up when a Java framework starts the trace, or when a Java library expects the current
OpenTelemetry Java context.

## Prerequisites

- Finish [Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md).
- Keep otel4s context in sync with OpenTelemetry Java context by following
  [Tracing | Context propagation](../oteljava/tracing-context-propagation.md).
- Have `Local[F, Context]` available. When you create `OtelJava`, you usually get it from
  `otel4s.localContext`.

## 1. Bring `Local[F, Context]` into scope

`Local[F, Context]` lets you move between Cats Effect code and the OpenTelemetry Java context.

```scala mdoc:silent
import cats.effect.IO
import cats.mtl.Local
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.context.Context

def program(otel4s: OtelJava[IO]): IO[Unit] = {
  import otel4s.localContext
  Local[IO, Context].ask.void
}
```

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

## 3. Run Java library calls under the current otel4s context

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
  Tracer[IO].span("client.call").surround {
    useJContext[IO, Unit] { _ =>
      val _ = JSpan.current().getSpanContext
      ()
    }
  }
```

## What's next

- Reuse a global SDK when something else owns OpenTelemetry setup:
  [Use the global OpenTelemetry instance](../how-to-jvm-setup/use-the-global-opentelemetry-instance.md)
- Continue incoming traces and propagate them downstream:
  [Propagate trace context across service boundaries](propagate-trace-context-across-service-boundaries.md)
- For more background and framework-specific examples, use the existing
  [Tracing | Interop with Java](../oteljava/tracing-java-interop.md) page.
