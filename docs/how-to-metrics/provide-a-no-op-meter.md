# Provide a no-op meter

Use a no-op `Meter` when code requires metrics capability but the application should not record or export
measurements. The no-op implementation is part of the backend-agnostic metrics API, so it does not require an SDK or
exporter.

## Use the no-op implicit

Import `Meter.Implicits.noop` where the application assembles the instrumented program.

```scala mdoc:reset:silent
import cats.effect.IO
import org.typelevel.otel4s.metrics.Meter

def program(implicit meter: Meter[IO]): IO[Unit] =
  meter.counter[Long]("requests").create.flatMap(_.inc())

val result: IO[Unit] = {
  import Meter.Implicits.noop
  program
}
```

The program runs normally, but the no-op meter discards the measurement.

## Pass the no-op meter explicitly

Use `Meter.noop` when the application wires dependencies explicitly:

```scala mdoc:reset:silent
import cats.effect.IO
import org.typelevel.otel4s.metrics.Meter

def program(implicit meter: Meter[IO]): IO[Unit] =
  meter.counter[Long]("requests").create.flatMap(_.inc())

val result: IO[Unit] =
  program(Meter.noop[IO])
```

If the program also requires tracing capability, see
[Provide a no-op tracer](../how-to-tracing/provide-a-no-op-tracer.md).

## Related material

- [Metrics API reference](../instrumentation/metrics.md)
- [Modules and module families](../explanations/modules-and-module-families.md)
