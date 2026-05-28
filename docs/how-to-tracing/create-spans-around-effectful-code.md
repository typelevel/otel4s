# Create spans around effectful code

Use this page when you want to trace application code with `Tracer`.

## Prerequisites

- [Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md)

## 1. Get a `Tracer`

Create `OtelJava`, then get a `Tracer` from its `TracerProvider`.

```scala mdoc:reset:silent
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.trace.Tracer

object Main extends IOApp.Simple {
  def run: IO[Unit] =
    OtelJava.autoConfigured[IO]().use { otel4s =>
      otel4s.tracerProvider.get("auth-service").flatMap { implicit tracer =>
        handleRequest(userId = 42L)
      }
    }

  def handleRequest(userId: Long)(implicit tracer: Tracer[IO]): IO[Unit] =
    Tracer[IO].currentSpanContext.flatMap(_ => IO.println(s"handling user $userId"))
}
```

`get("auth-service")` names the instrumentation scope for the tracer. Use a stable name that identifies the code
emitting telemetry, such as your application or module name.

## 2. Wrap work in a span

Use `span(...).surround(...)` for the common case where the span should cover one effect.

Inside that scope, otel4s keeps the span current for nested effects.
You only need explicit propagation when crossing process or context boundaries.

```scala mdoc:reset:silent
import cats.effect.IO
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.Tracer

def handleRequest(userId: Long)(implicit tracer: Tracer[IO]): IO[Unit] =
  Tracer[IO].span("http.request", Attribute("user.id", userId)).surround {
    Tracer[IO].currentSpanContext.flatMap(_ => IO.println(s"handling user $userId"))
  }
```

If you need to add events or attributes while the span is open, use `use`.

```scala mdoc:reset:silent
import cats.effect.IO
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.Tracer

def handleRequest(userId: Long)(implicit tracer: Tracer[IO]): IO[Unit] =
  Tracer[IO].span("http.request", Attribute("user.id", userId)).use { span =>
    span.addEvent("loading-user").flatMap { _ =>
      span.addAttribute(Attribute("user.found", true))
    }
  }
```

## 3. Add child spans for nested work

When you create spans inside another span, otel4s attaches them as children of the current span.

```scala mdoc:reset:silent
import cats.effect.IO
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.Tracer

def handleRequest(userId: Long)(implicit tracer: Tracer[IO]): IO[Unit] =
  Tracer[IO].span("http.request", Attribute("user.id", userId)).surround {
    validateUser(userId).flatMap(_ => fetchUser(userId))
  }

def validateUser(userId: Long)(implicit tracer: Tracer[IO]): IO[Unit] =
  Tracer[IO].span("user.validate").surround(IO.println(s"validating user $userId"))

def fetchUser(userId: Long)(implicit tracer: Tracer[IO]): IO[Unit] =
  Tracer[IO].span("user.fetch").use { span =>
    span.addEvent("loading-user").flatMap { _ =>
      span.addAttribute(Attribute("user.found", true)).flatMap { _ =>
        IO.println(s"fetching user $userId")
      }
    }
  }
```

## 4. Start a root span when the work should begin a new trace

Use `rootSpan` when the current work should not inherit the active trace context.

```scala mdoc:reset:silent
import cats.effect.IO
import org.typelevel.otel4s.trace.Tracer

def runIndependentTask(implicit tracer: Tracer[IO]): IO[Unit] =
  Tracer[IO].rootSpan("maintenance.run").surround {
    Tracer[IO].span("cleanup").surround(IO.println("cleanup"))
  }
```

## What's next

- Continue incoming traces and propagate them downstream:
  [Propagate trace context across service boundaries](propagate-trace-context-across-service-boundaries.md)
- Work with Java libraries that depend on OpenTelemetry context:
  [Use otel4s with Java-instrumented libraries](use-otel4s-with-java-instrumented-libraries.md)
- For `Resource`, `fs2.Stream`, and other advanced tracing patterns, see the existing
  [Tracing](../instrumentation/tracing.md) page.
