# Metrics | Cats Effect IO runtime

## Available metrics

```scala mdoc:invisible
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.typelevel.otel4s.instrumentation.ce.IORuntimeMetrics
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.testkit.metrics.MetricsTestkit
import IORuntimeMetrics.Config._

def printMetrics(config: IORuntimeMetrics.Config): Unit = {
  val metrics = MetricsTestkit.inMemory[IO]().use { testkit =>
    implicit val mp: MeterProvider[IO] = testkit.meterProvider

    IORuntimeMetrics
      .register[IO](global.metrics, config)
      .surround(testkit.collectMetrics)
  }.unsafeRunSync()

  println("| Name | Description | Unit |")
  println("|-|-|-|")
  println(metrics.sortBy(_.name).map(m => s"${m.name} | ${m.description.getOrElse("")} | ${m.unit.getOrElse("")}").mkString("\n"))
}
```

### CPU Starvation

**Platforms**: JVM, Scala.js, Scala Native.

These metrics could help identify performance bottlenecks caused by an overloaded compute pool, 
excessive task scheduling, or lack of CPU resources.

```scala mdoc:passthrough
printMetrics(IORuntimeMetrics.Config(CpuStarvationConfig.enabled, WorkStealingThreadPoolConfig.disabled))
```

### Work-stealing thread pool - compute

**Platforms**: JVM.

**Built-in attributes**:
* `pool.id` - the id of the work-stealing thread pool

These metrics provide insights about fibers and threads within the compute pool.
They help diagnose load distribution, identify bottlenecks, and monitor the pool’s efficiency in handling tasks.

```scala mdoc:passthrough
printMetrics(
  IORuntimeMetrics.Config(
    CpuStarvationConfig.disabled, 
    WorkStealingThreadPoolConfig(
      WorkStealingThreadPoolConfig.ComputeConfig.enabled,
      WorkStealingThreadPoolConfig.WorkerThreadsConfig.disabled,
    )
  )
)
```

### Work-stealing thread pool - thread

**Platforms**: JVM.

**Built-in attributes**:
* `pool.id` - the id of the work-stealing thread pool the worker is used by
* `worker.index` - the index of the worker thread
* `thread.event` - the thread event 
    * `parked` - a thread is parked
    * `polled` - a thread is polled for I/O events 
    * `blocked` - a thread is switched to a blocking thread and been replaced
    * `respawn` - a thread is replaced by a newly spawned thread

These metrics provide detailed information about threads state within the compute pool. 

```scala mdoc:passthrough
printMetrics(
  IORuntimeMetrics.Config(
    CpuStarvationConfig.disabled, 
    WorkStealingThreadPoolConfig(
      WorkStealingThreadPoolConfig.ComputeConfig.disabled,
      WorkStealingThreadPoolConfig.WorkerThreadsConfig(
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.ThreadConfig.enabled,
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.LocalQueueConfig.disabled,
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.TimerHeapConfig.disabled,
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.PollerConfig.disabled
      ),
    )
  )
)
```

### Work-stealing thread pool - local queue

**Platforms**: JVM.

**Built-in attributes**:
* `pool.id` - the id of the work-stealing thread pool the queue is used by
* `worker.index` - the index of the worker thread the queue is used by

These metrics provide a detailed view of fiber distribution within the pool. They help diagnose 
load imbalances and system inefficiency.

```scala mdoc:passthrough
printMetrics(
  IORuntimeMetrics.Config(
    CpuStarvationConfig.disabled,
    WorkStealingThreadPoolConfig(
      WorkStealingThreadPoolConfig.ComputeConfig.disabled,
      WorkStealingThreadPoolConfig.WorkerThreadsConfig(
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.ThreadConfig.disabled,
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.LocalQueueConfig.enabled,
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.TimerHeapConfig.disabled,
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.PollerConfig.disabled
      ),
    )
  )
)
```

### Work-stealing thread pool - timer heap

**Platforms**: JVM.

**Built-in attributes**:
* `pool.id` - the id of the work-stealing thread pool the timer heap is used by
* `worker.index` - the index of the worker thread the timer heap is used by
* `timer.state` - the state of the timer
    * `executed` - the successfully executed timer
    * `scheduled` - the scheduled timer
    * `canceled` - the canceled timer

These metrics provide a detailed view of timer stats within the pool. 

```scala mdoc:passthrough
printMetrics(
  IORuntimeMetrics.Config(
    CpuStarvationConfig.disabled,
    WorkStealingThreadPoolConfig(
      WorkStealingThreadPoolConfig.ComputeConfig.disabled,
      WorkStealingThreadPoolConfig.WorkerThreadsConfig(
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.ThreadConfig.disabled,
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.LocalQueueConfig.disabled,
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.TimerHeapConfig.enabled,
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.PollerConfig.disabled
      ),
    )
  )
)
```

### Work-stealing thread pool - poller

**Platforms**: JVM.

**Built-in attributes**:
* `pool.id` - the id of the work-stealing thread pool the poller is used by
* `worker.index` - the index of the worker thread the poller is used by
* `poller.operation` - the operation performed by the poller
    * `accept`
    * `connect` 
    * `read`
    * `write` 
* `poller.operation.status` - the status of the operation
    * `submitted` - the operation has been submitted
    * `succeeded` - the operation has errored
    * `errored` - the operation has errored
    * `canceled` - the operation has been canceled

These metrics provide a detailed view of poller stats within the pool. 

```scala mdoc:passthrough
printMetrics(
  IORuntimeMetrics.Config(
    CpuStarvationConfig.disabled,
    WorkStealingThreadPoolConfig(
      WorkStealingThreadPoolConfig.ComputeConfig.disabled,
      WorkStealingThreadPoolConfig.WorkerThreadsConfig(
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.ThreadConfig.disabled,  
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.LocalQueueConfig.disabled,  
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.TimerHeapConfig.disabled,  
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.PollerConfig.enabled  
      ),
    )
  )
)
```

## Getting started

Add the following configuration to the favorite build tool:

@:select(build-tool)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %%% "otel4s-instrumentation-metrics" % "@VERSION@" // <1>
)
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using dep "org.typelevel::otel4s-instrumentation-metrics::@VERSION@" // <1>
```

@:@

1. Add the `otel4s-instrumentation-metrics` library

## Registering metrics collectors

`IORuntimeMetrics.register` takes care of the metrics lifecycle management.  

@:select(otel-backend)

@:choice(oteljava)

```scala mdoc:reset:silent
import cats.effect._
import org.typelevel.otel4s.instrumentation.ce.IORuntimeMetrics
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.trace.TracerProvider
import org.typelevel.otel4s.oteljava.OtelJava

object Main extends IOApp.Simple {

  def run: IO[Unit] =
    OtelJava.autoConfigured[IO]().use { otel4s =>
      implicit val mp: MeterProvider[IO] = otel4s.meterProvider
      IORuntimeMetrics
        .register[IO](runtime.metrics, IORuntimeMetrics.Config.default)
        .surround {
          program(otel4s.meterProvider, otel4s.tracerProvider)
        }
    }

  def program(
      meterProvider: MeterProvider[IO],
      tracerProvider: TracerProvider[IO]
  ): IO[Unit] = {
    val _ = (meterProvider, tracerProvider)
    IO.unit
  }

}
```

@:choice(sdk)

```scala mdoc:reset:silent
import cats.effect._
import org.typelevel.otel4s.instrumentation.ce.IORuntimeMetrics
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.trace.TracerProvider
import org.typelevel.otel4s.sdk.OpenTelemetrySdk

object Main extends IOApp.Simple {

  def run: IO[Unit] =
    OpenTelemetrySdk.autoConfigured[IO]().use { autoConfigured =>
      val sdk = autoConfigured.sdk
      implicit val mp: MeterProvider[IO] = sdk.meterProvider
      IORuntimeMetrics
        .register[IO](runtime.metrics, IORuntimeMetrics.Config.default)
        .surround {
          program(sdk.meterProvider, sdk.tracerProvider)
        }
    }

  def program(
      meterProvider: MeterProvider[IO],
      tracerProvider: TracerProvider[IO]
  ): IO[Unit] = {
    val _ = (meterProvider, tracerProvider)
    IO.unit
  }

}
```

@:@


## Customization

The behavior of the `IORuntimeMetrics.register` can be customized via `IORuntimeMetrics.Config`.

### CPU Starvation

```scala mdoc:reset:invisible
import cats.effect.IO
import org.typelevel.otel4s.{Attribute, Attributes}
import org.typelevel.otel4s.instrumentation.ce.IORuntimeMetrics
import org.typelevel.otel4s.metrics.MeterProvider

val runtime = cats.effect.unsafe.implicits.global
implicit val mp: MeterProvider[IO] = MeterProvider.noop[IO]
```

To disable CPU starvation metrics:
```scala mdoc:silent
val config: IORuntimeMetrics.Config = {
  import IORuntimeMetrics.Config._
  IORuntimeMetrics.Config(
    CpuStarvationConfig.disabled, // disable CPU starvation metrics 
    WorkStealingThreadPoolConfig.enabled
  )
}

IORuntimeMetrics.register[IO](runtime.metrics, config)
```

To attach attributes to CPU starvation metrics:
```scala mdoc:nest:silent
val config: IORuntimeMetrics.Config = {
  import IORuntimeMetrics.Config._
  IORuntimeMetrics.Config(
    CpuStarvationConfig.enabled(
      Attributes(Attribute("key", "value")) // the attributes
    ), 
    WorkStealingThreadPoolConfig.enabled
  )
}

IORuntimeMetrics.register[IO](runtime.metrics, config)
```

### Work-stealing thread pool - compute

To disable worker metrics:
```scala mdoc:nest:silent
val config: IORuntimeMetrics.Config = {
  import IORuntimeMetrics.Config._
  import WorkStealingThreadPoolConfig._

  IORuntimeMetrics.Config(
    CpuStarvationConfig.enabled,
    WorkStealingThreadPoolConfig(
      ComputeConfig.disabled, // disable compute metrics
      WorkStealingThreadPoolConfig.WorkerThreadsConfig.enabled
    )
  )
}

IORuntimeMetrics.register[IO](runtime.metrics, config)
```

To attach attributes to compute metrics:
```scala mdoc:nest:silent
val config: IORuntimeMetrics.Config = {
  import IORuntimeMetrics.Config._
  import WorkStealingThreadPoolConfig._

  IORuntimeMetrics.Config(
    CpuStarvationConfig.enabled,
    WorkStealingThreadPoolConfig(
      ComputeConfig.enabled(
        Attributes(Attribute("key", "value")) // attributes
      ),
      WorkStealingThreadPoolConfig.WorkerThreadsConfig.enabled
    )
  )
}

IORuntimeMetrics.register[IO](runtime.metrics, config)
```

### Work-stealing thread pool - thread

To disable thread metrics:
```scala mdoc:nest:silent
val config: IORuntimeMetrics.Config = {
  import IORuntimeMetrics.Config._
  import WorkStealingThreadPoolConfig._

  IORuntimeMetrics.Config(
    CpuStarvationConfig.enabled,
    WorkStealingThreadPoolConfig(
      ComputeConfig.enabled,
      WorkerThreadsConfig(
        WorkerThreadsConfig.ThreadConfig.disabled, // disable worker thread metrics
        WorkerThreadsConfig.LocalQueueConfig.enabled,
        WorkerThreadsConfig.TimerHeapConfig.enabled,
        WorkerThreadsConfig.PollerConfig.enabled
      )
    )
  )
}

IORuntimeMetrics.register[IO](runtime.metrics, config)
```

To attach attributes to thread metrics:
```scala mdoc:nest:silent
val config: IORuntimeMetrics.Config = {
  import IORuntimeMetrics.Config._
  import WorkStealingThreadPoolConfig._

  IORuntimeMetrics.Config(
    CpuStarvationConfig.enabled,
    WorkStealingThreadPoolConfig(
      ComputeConfig.enabled,
      WorkerThreadsConfig(
        WorkerThreadsConfig.ThreadConfig.enabled(
          Attributes(Attribute("key", "value")) // the attributes
        ),
        WorkerThreadsConfig.LocalQueueConfig.enabled,
        WorkerThreadsConfig.TimerHeapConfig.enabled,
        WorkerThreadsConfig.PollerConfig.enabled
      )
    )
  )
}

IORuntimeMetrics.register[IO](runtime.metrics, config)
```

### Work-stealing thread pool - local queue

To disable local queue metrics:
```scala mdoc:nest:silent
val config: IORuntimeMetrics.Config = {
  import IORuntimeMetrics.Config._
  import WorkStealingThreadPoolConfig._

  IORuntimeMetrics.Config(
    CpuStarvationConfig.enabled,
    WorkStealingThreadPoolConfig(
      ComputeConfig.enabled,
      WorkerThreadsConfig(
        WorkerThreadsConfig.ThreadConfig.enabled,
        WorkerThreadsConfig.LocalQueueConfig.disabled, // disable local queue metrics
        WorkerThreadsConfig.TimerHeapConfig.enabled,
        WorkerThreadsConfig.PollerConfig.enabled
      )
    )
  )
}

IORuntimeMetrics.register[IO](runtime.metrics, config)
```

To attach attributes to local queue metrics:
```scala mdoc:nest:silent
val config: IORuntimeMetrics.Config = {
  import IORuntimeMetrics.Config._
  import WorkStealingThreadPoolConfig._

  IORuntimeMetrics.Config(
    CpuStarvationConfig.enabled,
    WorkStealingThreadPoolConfig(
      ComputeConfig.enabled,
      WorkerThreadsConfig(
        WorkerThreadsConfig.ThreadConfig.enabled,
        WorkerThreadsConfig.LocalQueueConfig.enabled(
          Attributes(Attribute("key", "value")) // the attributes
        ),
        WorkerThreadsConfig.TimerHeapConfig.enabled,
        WorkerThreadsConfig.PollerConfig.enabled
      )
    )
  )
}

IORuntimeMetrics.register[IO](runtime.metrics, config)
```

### Work-stealing thread pool - timer heap

To disable timer heap metrics:
```scala mdoc:nest:silent
val config: IORuntimeMetrics.Config = {
  import IORuntimeMetrics.Config._
  import WorkStealingThreadPoolConfig._

  IORuntimeMetrics.Config(
    CpuStarvationConfig.enabled,
    WorkStealingThreadPoolConfig(
      ComputeConfig.enabled,
      WorkerThreadsConfig(
        WorkerThreadsConfig.ThreadConfig.enabled,
        WorkerThreadsConfig.LocalQueueConfig.enabled,
        WorkerThreadsConfig.TimerHeapConfig.enabled, // disable timer heap metrics
        WorkerThreadsConfig.PollerConfig.enabled
      )
    )
  )
}

IORuntimeMetrics.register[IO](runtime.metrics, config)
```

To attach attributes to timer heap metrics:
```scala mdoc:nest:silent
val config: IORuntimeMetrics.Config = {
  import IORuntimeMetrics.Config._
  import WorkStealingThreadPoolConfig._

  IORuntimeMetrics.Config(
    CpuStarvationConfig.enabled,
    WorkStealingThreadPoolConfig(
      ComputeConfig.enabled,
      WorkerThreadsConfig(
        WorkerThreadsConfig.ThreadConfig.enabled,
        WorkerThreadsConfig.LocalQueueConfig.enabled,
        WorkerThreadsConfig.TimerHeapConfig.enabled(
          Attributes(Attribute("key", "value")) // the attributes
        ),
        WorkerThreadsConfig.PollerConfig.enabled
      )
    )
  )
}

IORuntimeMetrics.register[IO](runtime.metrics, config)
```

### Work-stealing thread pool - poller

To disable poller metrics:
```scala mdoc:nest:silent
val config: IORuntimeMetrics.Config = {
  import IORuntimeMetrics.Config._
  import WorkStealingThreadPoolConfig._
  
  IORuntimeMetrics.Config(
    CpuStarvationConfig.enabled,
    WorkStealingThreadPoolConfig(
      ComputeConfig.enabled,
      WorkerThreadsConfig(
        WorkerThreadsConfig.ThreadConfig.enabled,
        WorkerThreadsConfig.LocalQueueConfig.enabled,
        WorkerThreadsConfig.TimerHeapConfig.enabled,
        WorkerThreadsConfig.PollerConfig.disabled // disable poller metrics
      )
    )
  )
}

IORuntimeMetrics.register[IO](runtime.metrics, config)
```

To attach attributes to poller metrics:
```scala mdoc:nest:silent
val config: IORuntimeMetrics.Config = {
  import IORuntimeMetrics.Config._
  import WorkStealingThreadPoolConfig._

  IORuntimeMetrics.Config(
    CpuStarvationConfig.enabled,
    WorkStealingThreadPoolConfig(
      ComputeConfig.enabled,
      WorkerThreadsConfig(
        WorkerThreadsConfig.ThreadConfig.enabled,
        WorkerThreadsConfig.LocalQueueConfig.enabled,
        WorkerThreadsConfig.TimerHeapConfig.enabled,
        WorkerThreadsConfig.PollerConfig.enabled(
          Attributes(Attribute("key", "value")) // the attributes
        )
      )
    )
  )
}

IORuntimeMetrics.register[IO](runtime.metrics, config)
```
