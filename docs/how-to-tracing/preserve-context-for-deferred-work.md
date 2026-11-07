# Preserve context for deferred work

Use `ContextManager` when an effect, `Resource`, or `Stream` is prepared in one otel4s context and evaluated later.
The captured context includes the active span, baggage, and any other values stored by the telemetry backend.

## Prerequisites

This guide assumes that you already have an `Otel4s` instance. For a complete JVM setup, see
[Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md).

## Get the context manager

`Otel4s` exposes the context manager used by its telemetry components. Pass that value to code that needs to preserve
context:

```scala mdoc:reset:silent
import cats.effect.IO
import org.typelevel.otel4s.context.ContextManager
import org.typelevel.otel4s.oteljava.OtelJava

def deferredWork(contextManager: ContextManager[IO]): IO[Unit] =
  contextManager.current.map(_ => ())

val program: IO[Unit] =
  OtelJava.autoConfigured[IO]().use { otel4s =>
    deferredWork(otel4s.contextManager)
  }
```

Code that only needs to capture or restore context can accept `ContextManager[F]`.

OtelJava also provides `contextManager` on `Traces`, `TracesTestkit`, `MetricsTestkit`, and `LogsTestkit`.
Standalone runtime `Metrics` and `Logs` use a constant root context and have no manager.

## Share context storage between modules

The default `LocalProvider` allocates a new `IOLocal` each time its `.local` effect runs.
Modules built with separate locals do not share context, even if they use the same provider.

To share context, allocate one local and pass `LocalProvider.fromLocal(local)` to each module.
For example, these tracing and metrics testkits share a local:

```scala mdoc:silent
import cats.syntax.all._
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.context.LocalContextProvider
import org.typelevel.otel4s.oteljava.testkit.metrics.MetricsTestkit
import org.typelevel.otel4s.oteljava.testkit.trace.TracesTestkit

val sharedTestkits: IO[Unit] =
  LocalProvider[IO, Context].local.flatMap { local =>
    implicit val shared: LocalContextProvider[IO] = LocalProvider.fromLocal(local)

    (TracesTestkit.inMemory[IO](), MetricsTestkit.inMemory[IO]()).tupled.use { case (traces, metrics) =>
      for {
        tracer <- traces.tracerProvider.get("example")
        meter <- metrics.meterProvider.get("example")
        histogram <- meter.histogram[Double]("duration").create
        _ <- tracer.span("request").surround(histogram.record(0.1))
      } yield ()
    }
  }
```

You can also pass this provider to `Traces` and `LogsTestkit`.
`OtelJava` and `OtelJavaTestkit` already share one local across their components.

## Capture a scope before deferring an effect

Run `captureScope` in the context you want to preserve. It returns an `F ~> F` that restores that context around
effects run later:

```scala mdoc:silent
def prepare[A](contextManager: ContextManager[IO])(
    effect: IO[A]
): IO[IO[A]] =
  contextManager.captureScope.map { restore =>
    restore(effect)
  }
```

Run the outer `IO` before passing the returned effect to a queue, callback, or scheduler.

You can reuse the returned scope. It replaces the current context while an effect runs, then restores the caller's
context on success, failure, or cancellation.

## Preserve context for `Resource` and `Stream`

Apply the captured scope with `Resource#mapK` or `Stream#translate`:

```scala mdoc:silent
import cats.effect.Resource
import fs2.Stream

def prepareResource[A](contextManager: ContextManager[IO])(
    resource: Resource[IO, A]
): IO[Resource[IO, A]] =
  contextManager.captureScope.map { restore =>
    resource.mapK(restore)
  }

def prepareStream[A](contextManager: ContextManager[IO])(
    stream: Stream[IO, A]
): IO[Stream[IO, A]] =
  contextManager.captureScope.map { restore =>
    stream.translate(restore)
  }
```

Run the outer `IO` in the context you want to capture.

`Resource.mapK` applies the scope to acquisition and release, including release after failure or cancellation.
The `use` callback runs in the caller's context. Apply the captured scope to that callback if it needs the captured
context too.

`Stream.translate` applies the scope to the stream's effects and finalizers. The caller's context is restored afterward.
Multiple fibers can use the same captured scope concurrently.

## Choose the right kind of capture

Use `ContextManager.captureScope` to preserve the span, baggage, and custom context values together.

Capturing context does not keep a span open. The code that owns the span must end it.
If deferred work needs a new span, start it when the work runs. The captured span can be its parent.

Use `Tracer.childScope(spanContext)` to change the parent while keeping the baggage and custom values current when
the effect runs. See
[Choosing parent spans and tracing scopes](../explanations/choosing-parent-spans-and-tracing-scopes.md).

To pass context to another process or service, inject it into a carrier. See
[Propagate trace context across service boundaries](propagate-trace-context-across-service-boundaries.md).

## Related material

- [Context API reference](../instrumentation/context.md)
- [How otel4s context propagation works](../explanations/how-otel4s-context-propagation-works.md)
- [Trace Resource and fs2.Stream code](trace-resource-and-fs2-stream-code.md)
- [Work with baggage](work-with-baggage.md)
