# Work with baggage

Use this page when you want to attach request-scoped key-value pairs to the current OpenTelemetry context and read them later in the same flow.

In OpenTelemetry, baggage is a set of key-value pairs that travels with the current context. Use it for request-scoped metadata that should be available across service boundaries, such as request IDs, tenant IDs, or debug flags.

## Prerequisites

- [Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md)

## 1. Get `BaggageManager` from `OtelJava`

`BaggageManager[F]` gives you access to the current baggage and scoped baggage updates. The main operations are:

- `current` to read the current baggage
- `scope` and `local` to change baggage for a scoped effect
- `get` and `getValue` to read a specific entry

```scala mdoc:silent
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.baggage.BaggageManager
import org.typelevel.otel4s.oteljava.OtelJava

object Main extends IOApp.Simple {
  def run: IO[Unit] =
    OtelJava.autoConfigured[IO]().use { otel4s =>
      implicit val baggageManager: BaggageManager[IO] = otel4s.baggageManager
      program
    }

  def program(implicit baggageManager: BaggageManager[IO]): IO[Unit] = {
    val _ = baggageManager
    IO.unit
  }
}
```

## 2. Add baggage around the effect that needs it

`Baggage` is immutable. To change it, wrap the effect in `scope(...)` or `local(...)`.

```scala mdoc:silent
import org.typelevel.otel4s.baggage.Baggage

def withRequestMetadata[A](fa: IO[A])(implicit baggageManager: BaggageManager[IO]): IO[A] =
  baggageManager.scope(
    Baggage.empty
      .updated("request.id", "req-123")
      .updated("user.id", "user-42")
  )(fa)
```

If you want to update the current baggage instead of replacing it, use `local(...)`:

```scala mdoc:silent
def withDebugFlag[A](fa: IO[A])(implicit baggageManager: BaggageManager[IO]): IO[A] =
  baggageManager.local(_.updated("debug", "true"))(fa)
```

## 3. Read baggage values downstream

```scala mdoc:silent
def handleRequest(implicit baggageManager: BaggageManager[IO]): IO[Unit] =
  withRequestMetadata {
    for {
      current <- baggageManager.current
      userId <- baggageManager.getValue("user.id")
      _ <- IO.println(s"baggage: $current")
      _ <- IO.println(s"user.id: ${userId.getOrElse("missing")}")
    } yield ()
  }
```

## What's next

- Continue incoming traces and propagate trace context across services:
  [Propagate trace context across service boundaries](propagate-trace-context-across-service-boundaries.md)
- Work across otel4s and Java context boundaries:
  [Use otel4s with Java-instrumented libraries](use-otel4s-with-java-instrumented-libraries.md)

[opentelemetry-baggage]: https://opentelemetry.io/docs/concepts/signals/baggage
