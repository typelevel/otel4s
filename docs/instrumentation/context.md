# Context API reference

`org.typelevel.otel4s.context.ContextManager[F]` reads and restores the active span, baggage, and custom context values.
Get it from `Otel4s[F].contextManager`. OtelJava also provides it on `Traces`, `TracesTestkit`, `MetricsTestkit`, and
`LogsTestkit`.

| Member | Result | Description |
|--------|--------|-------------|
| `current` | `F[Ctx]` | Reads the context when the effect runs. |
| `scope(context)` | `F ~> F` | Runs effects in the given context. Restores the caller's context on success, failure, or cancellation. |
| `captureScope` | `F[F ~> F]` | Captures the context when this effect runs. Returns a reusable scope. |
| `liftTo[G]` | `ContextManager.Aux[G, Ctx]` | Lifts the manager through `LiftKind[F, G]` and keeps the same context type. |

`Ctx` is the backend's context type. Use `ContextManager.Aux[F, C]` when code needs to name that type.
Code that only captures and restores context can use `ContextManager[F]`.

Use `ContextManager[F]` to summon an implicit manager. Each manager uses its module's context storage.
To share context across modules, give them the same local. See
[Preserve context for deferred work](../how-to-tracing/preserve-context-for-deferred-work.md).

```scala mdoc:reset:silent
import cats.effect.IO
import org.typelevel.otel4s.context.ContextManager
import org.typelevel.otel4s.oteljava.OtelJava

def prepare[A](manager: ContextManager[IO])(effect: IO[A]): IO[IO[A]] =
  manager.captureScope.map(restore => restore(effect))

val program: IO[Unit] =
  OtelJava.autoConfigured[IO]().use { otel4s =>
    prepare(otel4s.contextManager)(IO.unit).flatten
  }
```

The manager works when tracing is disabled or a span is unsampled. Capturing context does not change a span's lifetime
or record telemetry.

Use `Otel4s.propagators` for propagation.

For deferred effects, resources, and streams, see
[Preserve context for deferred work](../how-to-tracing/preserve-context-for-deferred-work.md).
For context storage and fiber inheritance, see
[How otel4s context propagation works](../explanations/how-otel4s-context-propagation-works.md).
