# Provide a no-op tracer

Use a no-op `Tracer` when code requires tracing capability but the application should not create or export spans.
The no-op implementation is part of the backend-agnostic tracing API, so it does not require an SDK or exporter.

## Use the no-op implicit

Import `Tracer.Implicits.noop` where the application assembles the instrumented program.

```scala mdoc:reset:silent
import cats.effect.IO
import org.typelevel.otel4s.trace.Tracer

def program(implicit tracer: Tracer[IO]): IO[Unit] =
  tracer.span("work").surround(IO.unit)

val result: IO[Unit] = {
  import Tracer.Implicits.noop
  program
}
```

The program runs normally, but the no-op tracer does not record or export the span.

## Pass the no-op tracer explicitly

Use `Tracer.noop` when the application wires dependencies explicitly:

```scala mdoc:reset:silent
import cats.effect.IO
import org.typelevel.otel4s.trace.Tracer

def program(implicit tracer: Tracer[IO]): IO[Unit] =
  tracer.span("work").surround(IO.unit)

val result: IO[Unit] =
  program(Tracer.noop[IO])
```

If the program also requires metrics capability, see
[Provide a no-op meter](../how-to-metrics/provide-a-no-op-meter.md).

## Related material

- [Tracing API reference](../instrumentation/tracing.md)
- [Modules and module families](../explanations/modules-and-module-families.md)
