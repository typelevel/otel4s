# Baggage

In [OpenTelemetry][opentelemetry-baggage], `Baggage` is a mechanism for propagating key-value pairs across service boundaries. 
It allows applications to attach contextual metadata (e.g., request IDs, user IDs, or debug flags) to distributed traces, 
ensuring that relevant data flows alongside requests without modifying their payloads.

Baggage is primarily used for:
- Enriching logs and traces with contextual information
- Debugging and monitoring distributed applications
- Passing request-scoped metadata without modifying the business logic

## What is `BaggageManager`?

`BaggageManager` is a functional abstraction that provides an interface to manage `Baggage`. 
It extends `Local[F, Baggage]` and can:
- Read the current `Baggage`: `current`
- Modify the `Baggage` in a scoped manner: `local`, `scope`
- Read specific entries: `get`, `getValue`

## How to get the `BaggageManager`

@:select(otel-backend)

@:choice(oteljava)

```scala mdoc:silent
import cats.effect.IO
import org.typelevel.otel4s.baggage.BaggageManager
import org.typelevel.otel4s.oteljava.OtelJava

OtelJava.autoConfigured[IO]().use { otel4s =>
  val baggageManager: BaggageManager[IO] = otel4s.baggageManager
  IO.println("BaggageManager: " + baggageManager)
}
```

@:choice(sdk)

```scala mdoc:silent
import cats.effect.IO
import org.typelevel.otel4s.baggage.BaggageManager
import org.typelevel.otel4s.sdk.OpenTelemetrySdk
import org.typelevel.otel4s.sdk.exporter.otlp.autoconfigure.OtlpExportersAutoConfigure

OpenTelemetrySdk
  .autoConfigured[IO](_.addExportersConfigurer(OtlpExportersAutoConfigure[IO]))
  .use { auto =>
    val baggageManager: BaggageManager[IO] = auto.sdk.baggageManager
    IO.println("BaggageManager: " + baggageManager)
  }
```

@:@

## How to use `BaggageManager`

@:callout(info)

`Baggage` can be modified exclusively in a scoped-manner.

@:@

### Reading the current `Baggage`

```scala mdoc:silent
def printBaggage(implicit bm: BaggageManager[IO]): IO[Unit] =
  BaggageManager[IO].current.flatMap(b => IO.println(s"Baggage: $b"))
```

### Setting and modifying `Baggage`

```scala mdoc:silent
import org.typelevel.otel4s.baggage.Baggage

def withUserId[A](fa: IO[A])(implicit bm: BaggageManager[IO]): IO[A] =
  bm.local(b => b.updated("user-id", "12345"))(fa)

def withScopedBaggage[A](fa: IO[A])(implicit bm: BaggageManager[IO]): IO[A] =
  bm.scope(Baggage.empty.updated("request-id", "req-abc"))(fa)
```

### Retrieving a specific baggage entry

```scala mdoc:silent
def fetchBaggageEntry(implicit bm: BaggageManager[IO]): IO[Unit] =
  bm.getValue("user-id").flatMap {
    case Some(userId) => IO.println(s"User ID: $userId")
    case None         => IO.println("User ID not found in baggage")
  }
```

[opentelemetry-baggage]: https://opentelemetry.io/docs/concepts/signals/baggage
