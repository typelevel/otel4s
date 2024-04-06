# Metrics

`Meter` is an entry point to the metrics capabilities and instrumentation.

## How to get the `Meter`

Currently, `otel4s` has a backend built on top of [OpenTelemetry Java][opentelemetry-java].
Add the following configuration to the favorite build tool:

@:select(build-tool)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-oteljava" % "@VERSION@", // <1>
  "io.opentelemetry" % "opentelemetry-exporter-otlp" % "@OPEN_TELEMETRY_VERSION@" % Runtime, // <2>
  "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "@OPEN_TELEMETRY_VERSION@" % Runtime // <3>
)
javaOptions += "-Dotel.java.global-autoconfigure.enabled=true" // <4>
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using lib "org.typelevel::otel4s-oteljava:@VERSION@" // <1>
//> using lib "io.opentelemetry:opentelemetry-exporter-otlp:@OPEN_TELEMETRY_VERSION@" // <2>
//> using lib "io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:@OPEN_TELEMETRY_VERSION@" // <3>
//> using `java-opt` "-Dotel.java.global-autoconfigure.enabled=true" // <4>
```

@:@

1. Add the `otel4s-oteljava` library
2. Add an OpenTelemetry exporter. Without the exporter, the application will crash
3. Add an OpenTelemetry autoconfigure extension
4. Enable OpenTelemetry SDK [autoconfigure mode][opentelemetry-java-autoconfigure]

Once the build configuration is up-to-date, the `Meter` can be created:

```scala mdoc:silent
import cats.effect.IO
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.oteljava.OtelJava

OtelJava.autoConfigured[IO]().evalMap { otel4s =>
  otel4s.meterProvider.get("com.service").flatMap { implicit meter: Meter[IO] =>
    val _ = meter // use meter here
    ???
  }
}
```

## Available instruments

The instruments are split into two categories: synchronous and asynchronous (observable).

@:callout(info)

The terms synchronous and asynchronous have nothing to do with
asynchronous programming. The naming follows the OpenTelemetry specification.

@:@

To create an instrument, you must specify the measurement type. The `Long` and `Double` are available out of the box.

```scala mdoc:compile-only
import cats.effect.IO
import org.typelevel.otel4s.metrics.{Counter, Meter}

@annotation.nowarn
val meter: Meter[IO] = ???

val doubleCounter: IO[Counter[IO, Double]] =
  meter.counter[Double]("double-counter").create

val longCounter: IO[Counter[IO, Long]] =
  meter.counter[Long]("long-counter").create
```

The recommended measurement types per instrument:

| Instrument              | Type         | Measurement type |
|-------------------------|--------------|------------------|
| Counter                 | Synchronous  | Long             |
| UpDownCounter           | Synchronous  | Long             |
| Histogram               | Synchronous  | Double           |
| ObservableCounter       | Asynchronous | Long             |
| ObservableGauge         | Asynchronous | Double           |
| ObservableUpDownCounter | Asynchronous | Long             |

## Synchronous instruments

Synchronous instruments  are meant to be invoked inline with application/business processing logic.
For instance, an HTTP client might utilize a counter to record the number of received bytes.

The synchronous instruments are:
- `Counter` - the monotonic instrument, the aggregated value is nominally increasing.
- `UpDownCounter` - the non-monotonic instrument, the aggregated value can increase and decrease.
- `Histogram` - the instrument bundles a set of events into divided populations with an overall 
  event count and aggregate sum for all events.

The following example tracks the number of users missing in the storage and the duration of the retrieval:

```scala mdoc:silent:reset
import java.util.concurrent.TimeUnit

import cats.Monad
import cats.effect.{Concurrent, MonadCancelThrow, Ref}
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.metrics.{Counter, Histogram, Meter}

case class User(email: String)

class UserRepository[F[_]: MonadCancelThrow](
    storage: Ref[F, Map[Long, User]],
    missingCounter: Counter[F, Long],
    searchDuration: Histogram[F, Double]
) {

  def findUser(userId: Long): F[Option[User]] =
    searchDuration.recordDuration(TimeUnit.SECONDS).surround(
      for {
        current <- storage.get
        user    <- Monad[F].pure(current.get(userId))
        _       <- missingCounter.inc().whenA(user.isEmpty)
      } yield user
    )

}

object UserRepository {

  def create[F[_]: Concurrent: Meter]: F[UserRepository[F]] = {
    for {
      storage  <- Concurrent[F].ref(Map.empty[Long, User])
      missing  <- Meter[F].counter[Long]("user.search.missing").create
      duration <- Meter[F].histogram[Double]("user.search.duration").withUnit("s").create
    } yield new UserRepository(storage, missing, duration)
  }

}
```

## Asynchronous (observable) instruments

Asynchronous instruments offer users the ability to register callback functions, 
which are only triggered on demand. 
For example, an asynchronous gauge can be used to collect the temperature
from a sensor every 15 seconds, which means the callback function will
only be invoked every 15 seconds.

The asynchronous instruments are:
- `ObservableCounter` - the monotonic instrument, the aggregated value is nominally increasing.
- `ObservableUpDownCounter` - the non-monotonic instrument, the aggregated value can increase and decrease.
- `ObservableGauge` - the instrument can be used to record non-additive values.

The following example shows how to collect MBean metrics:

```scala mdoc:silent:reset
import java.lang.management.ManagementFactory
import javax.management.ObjectName

import cats.effect.{Resource, Sync}
import org.typelevel.otel4s.metrics.Meter

object CatsEffectMetrics {

  private val mbeanName = new ObjectName(
    "cats.effect.metrics:type=CpuStarvation"
  )

  def register[F[_]: Sync: Meter]: Resource[F, Unit] =
    for {
      mBeanServer <- Resource.eval(
        Sync[F].delay(ManagementFactory.getPlatformMBeanServer)
      )
      _ <- Meter[F]
        .observableCounter[Long]("cats_effect.runtime.cpu_starvation.count")
        .createWithCallback { cb =>
          cb.record(
            mBeanServer
              .getAttribute(mbeanName, "CpuStarvationCount")
              .asInstanceOf[Long]
          )
        }
    } yield ()

}
```

[opentelemetry-java]: https://github.com/open-telemetry/opentelemetry-java
[opentelemetry-java-autoconfigure]: https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md