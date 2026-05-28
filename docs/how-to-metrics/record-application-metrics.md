# Record application metrics

Use this page when you want to record counters, gauges, up-down counters, histograms, or observable measurements in
application code.

## Prerequisites

- [Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md)

## 1. Get a `Meter`

Get a `Meter` from the `MeterProvider` you created during setup.

```scala mdoc:silent
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.oteljava.OtelJava

object Main extends IOApp.Simple {
  def run: IO[Unit] =
    OtelJava.autoConfigured[IO]().use { otel4s =>
      otel4s.meterProvider.get("auth-service").flatMap { implicit meter =>
        program
      }
    }

  def program(implicit meter: Meter[IO]): IO[Unit] =
    IO(meter).void
}
```

`get("auth-service")` names the instrumentation scope for the meter. Use a stable name that identifies the code emitting
telemetry, such as your application or module name.


## 2. Create the instruments you need

Create instruments once and reuse them while the application runs.

- Use `Counter` for values that only go up.
- Use `Gauge` for non-additive values such as queue depth or cache size.
- Use `UpDownCounter` for values that can go up and down.
- Use `Histogram` for distributions such as durations or payload sizes.

```scala mdoc:reset:silent
import cats.effect.IO
import org.typelevel.otel4s.metrics.{Counter, Gauge, Histogram, UpDownCounter, Meter}

case class UserMetrics(
    missingUsers: Counter[IO, Long],
    cachedUsers: Gauge[IO, Long],
    activeRequests: UpDownCounter[IO, Long],
    lookupDuration: Histogram[IO, Double]
)

object UserMetrics {
  def create(implicit meter: Meter[IO]): IO[UserMetrics] =
    for {
      missingUsers <- meter.counter[Long]("user.lookup.missing").create
      cachedUsers <- meter.gauge[Long]("user.storage.size").create
      activeRequests <- meter.upDownCounter[Long]("http.server.active_requests").create
      lookupDuration <- meter.histogram[Double]("user.lookup.duration").withUnit("ms").create
    } yield UserMetrics(missingUsers, cachedUsers, activeRequests, lookupDuration)
}
```

## 3. Record measurements in application code

Use the instruments inline with the work they measure.

```scala mdoc:silent
import java.util.concurrent.TimeUnit

import cats.effect.{IO, Ref}
import cats.syntax.all._

case class User(id: Long, email: String)

class UserService(
    storage: Ref[IO, Map[Long, User]],
    metrics: UserMetrics
) {

  def handleRequest(userId: Long): IO[Option[User]] =
    metrics.activeRequests.inc() *>
      metrics.lookupDuration
        .recordDuration(TimeUnit.MILLISECONDS)
        .surround(
          storage.get.flatMap { current =>
            metrics.cachedUsers.record(current.size.toLong) *>
              IO.pure(current.get(userId)).flatTap {
                case Some(_) => IO.unit
                case None    => metrics.missingUsers.inc()
              }
          }
        )
        .guarantee(metrics.activeRequests.dec())
}
```

## 4. Register an observable instrument for on-demand values

Use an observable instrument when the value should be read at collection time instead of being recorded inline.

```scala mdoc:silent
import cats.effect.Resource

def registerStorageSize(
    storage: Ref[IO, Map[Long, User]]
)(implicit meter: Meter[IO]): Resource[IO, Unit] =
  meter
    .observableGauge[Long]("user.storage.size")
    .withDescription("Current number of cached users")
    .createWithCallback { cb =>
      storage.get.flatMap(users => cb.record(users.size.toLong))
    }
    .void
```

## What's next

- Export runtime metrics from Cats Effect:
  [Register Cats Effect runtime metrics](register-cats-effect-runtime-metrics.md)
- Customize histogram buckets for a specific metric:
  [Histogram custom buckets](../customization/histogram-custom-buckets/README.md)
- See the existing metrics page for the broader API surface:
  [Metrics](../instrumentation/metrics.md)
