# Cats Effect IO runtime metrics reference

`IORuntimeMetrics` registers observable metrics from Cats Effect's runtime metrics interface. It uses the
`cats.effect.runtime` meter scope and returns a `Resource` that owns the collector registrations.

For dependency setup, registration, and a focused configuration example, see
[Register Cats Effect runtime metrics](../how-to-metrics/register-cats-effect-runtime-metrics.md).

## `IORuntimeMetrics.register`

`register[F]` requires `Sync[F]` and an implicit `MeterProvider[F]`.

| Parameter | Type | Description |
|-----------|------|-------------|
| `metrics` | `cats.effect.unsafe.metrics.IORuntimeMetrics` | The runtime metrics interface to observe. |
| `config` | `IORuntimeMetrics.Config` | Selects the collectors to register and their additional attributes. |

The result is a `Resource[F, Unit]`. Acquiring the resource registers the selected observable instruments; releasing it
removes their callbacks.

```scala mdoc:silent
import cats.effect.{IO, Resource}
import cats.effect.unsafe.metrics.{IORuntimeMetrics => CatsIORuntimeMetrics}
import org.typelevel.otel4s.instrumentation.ce.IORuntimeMetrics
import org.typelevel.otel4s.metrics.MeterProvider

def registerRuntimeMetrics(
    metrics: CatsIORuntimeMetrics
)(implicit meterProvider: MeterProvider[IO]): Resource[IO, Unit] =
  IORuntimeMetrics.register[IO](
    metrics = metrics,
    config = IORuntimeMetrics.Config.default,
  )
```

## Platform support

| Metric group | JVM | Scala.js | Scala Native |
|--------------|:---:|:--------:|:------------:|
| CPU starvation | ✓ | ✓ | ✓ |
| Work-stealing thread pool | ✓ | — | ✓ |

## Metric catalog

```scala mdoc:invisible
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import org.typelevel.otel4s.instrumentation.ce.IORuntimeMetrics
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.testkit.metrics.MetricsTestkit
import IORuntimeMetrics.Config._

def printMetrics(config: IORuntimeMetrics.Config): Unit = {
  val metrics = MetricsTestkit.inMemory[IO]().use { testkit =>
    implicit val mp: MeterProvider[IO] = testkit.meterProvider

    IORuntimeMetrics
      .register[IO](global.metrics, config)
      .surround(testkit.collectMetrics)
  }.unsafeRunSync()

  println("| Name | Description | Unit |")
  println("|------|-------------|------|")
  println(
    metrics
      .sortBy(_.getName)
      .map { metric =>
        val description = Option(metric.getDescription).getOrElse("")
        val unit = Option(metric.getUnit).filter(_.nonEmpty).fold("")(value => s"`$value`")
        s"| `${metric.getName}` | $description | $unit |"
      }
      .mkString("\n")
  )
}
```

### CPU starvation

**Platforms:** JVM, Scala.js, Scala Native.

These metrics report CPU starvation events and the current and maximum clock drift observed by the Cats Effect runtime.

```scala mdoc:passthrough
printMetrics(IORuntimeMetrics.Config(CpuStarvationConfig.enabled, WorkStealingThreadPoolConfig.disabled))
```

### Work-stealing thread pool: compute

**Platforms:** JVM, Scala Native.

| Built-in attribute | Description |
|--------------------|-------------|
| `pool.id` | Identifier of the work-stealing thread pool. |

These metrics report fiber and worker-thread activity for the compute pool.

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

### Work-stealing thread pool: threads

**Platforms:** JVM, Scala Native.

| Built-in attribute | Description |
|--------------------|-------------|
| `pool.id` | Identifier of the work-stealing thread pool. |
| `worker.index` | Index of the worker thread within the pool. |
| `thread.event` | Worker-thread lifecycle event. |

Every thread metric includes `pool.id` and `worker.index`. The
`cats.effect.runtime.wstp.worker.thread.event.count` metric also includes `thread.event`, with these values:

| Value | Description |
|-------|-------------|
| `parked` | The thread was parked. |
| `polled` | The thread polled for I/O events. |
| `blocked` | The thread switched to blocking work and was replaced. |
| `respawn` | The thread was replaced by a newly spawned thread. |

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
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.PollerConfig.disabled,
      ),
    )
  )
)
```

### Work-stealing thread pool: local queue

**Platforms:** JVM, Scala Native.

| Built-in attribute | Description |
|--------------------|-------------|
| `pool.id` | Identifier of the work-stealing thread pool. |
| `worker.index` | Index of the worker thread that owns the queue. |

These metrics report the distribution of fibers across worker-local queues.

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
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.PollerConfig.disabled,
      ),
    )
  )
)
```

### Work-stealing thread pool: timer heap

**Platforms:** JVM, Scala Native.

| Built-in attribute | Description |
|--------------------|-------------|
| `pool.id` | Identifier of the work-stealing thread pool. |
| `worker.index` | Index of the worker thread that owns the timer heap. |
| `timer.state` | State reported for the timer. |

Every timer-heap metric includes `pool.id` and `worker.index`. The
`cats.effect.runtime.wstp.worker.timerheap.timer.count` metric also includes `timer.state`, with these values:

| Value | Description |
|-------|-------------|
| `executed` | The timer was executed. |
| `scheduled` | The timer was scheduled. |
| `canceled` | The timer was canceled. |

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
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.PollerConfig.disabled,
      ),
    )
  )
)
```

### Work-stealing thread pool: poller

**Platforms:** JVM, Scala Native.

| Built-in attribute | Description |
|--------------------|-------------|
| `pool.id` | Identifier of the work-stealing thread pool. |
| `worker.index` | Index of the worker thread that owns the poller. |
| `poller.operation` | I/O operation performed by the poller. |
| `poller.operation.status` | State of the I/O operation. |

Every poller metric includes `pool.id`, `worker.index`, and `poller.operation`. `poller.operation` has the values
`accept`, `connect`, `read`, and `write`.

The `cats.effect.runtime.wstp.worker.poller.operation.count` metric also includes `poller.operation.status`, with these
values:

| Value | Description |
|-------|-------------|
| `submitted` | The operation was submitted. |
| `succeeded` | The operation completed successfully. |
| `errored` | The operation completed with an error. |
| `canceled` | The operation was canceled. |

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
        WorkStealingThreadPoolConfig.WorkerThreadsConfig.PollerConfig.enabled,
      ),
    )
  )
)
```

## Configuration

The configuration surface differs by platform because Scala.js does not expose work-stealing thread-pool metrics.

### Configuration hierarchy

| Field | Type | Platforms |
|-------|------|-----------|
| `config.cpuStarvation` | `CpuStarvationConfig` | All |
| `config.workStealingThreadPool` | `WorkStealingThreadPoolConfig` | JVM, Scala Native |
| `workStealingThreadPool.compute` | `ComputeConfig` | JVM, Scala Native |
| `workStealingThreadPool.workerThreads` | `WorkerThreadsConfig` | JVM, Scala Native |
| `workerThreads.thread` | `ThreadConfig` | JVM, Scala Native |
| `workerThreads.localQueue` | `LocalQueueConfig` | JVM, Scala Native |
| `workerThreads.timerHeap` | `TimerHeapConfig` | JVM, Scala Native |
| `workerThreads.poller` | `PollerConfig` | JVM, Scala Native |

### Create an `IORuntimeMetrics.Config`

The `IORuntimeMetrics.Config` companion object provides these methods:

| Companion method | Platforms | Description |
|------------------|-----------|-------------|
| `default` | All | Returns a config with every collector available on the current platform enabled. |
| `apply(cpuStarvation)` | Scala.js | Creates a config from a CPU starvation config. |
| `apply(cpuStarvation, workStealingThreadPool)` | JVM, Scala Native | Creates a config from both top-level configs. |

Scala allows an `apply` method to be called with the companion object's name. For example,
`IORuntimeMetrics.Config(cpuStarvation, workStealingThreadPool)` calls the two-argument `apply` method.

```scala mdoc:silent
import org.typelevel.otel4s.instrumentation.ce.IORuntimeMetrics
import IORuntimeMetrics.Config._

val config: IORuntimeMetrics.Config =
  IORuntimeMetrics.Config(
    cpuStarvation = CpuStarvationConfig.disabled,
    workStealingThreadPool = WorkStealingThreadPoolConfig.enabled,
  )
```

### Configure an individual collector

| Type | Metric group | Platforms |
|------|--------------|-----------|
| `CpuStarvationConfig` | CPU starvation | All |
| `ComputeConfig` | Work-stealing compute pool | JVM, Scala Native |
| `ThreadConfig` | Worker-thread events | JVM, Scala Native |
| `LocalQueueConfig` | Worker-local queues | JVM, Scala Native |
| `TimerHeapConfig` | Worker timer heaps | JVM, Scala Native |
| `PollerConfig` | Worker pollers | JVM, Scala Native |

Each config value exposes these fields:

| Field | Description |
|-------|-------------|
| `enabled: Boolean` | Whether the collector is enabled. |
| `attributes: Attributes` | Additional attributes attached to every metric from the collector. |

The companion object for each type provides the same creation methods. For example,
`CpuStarvationConfig` provides:

| Companion method | Description |
|------------------|-------------|
| `enabled` | Returns an enabled config with no additional attributes. |
| `enabled(attributes)` | Returns an enabled config with the given attributes. |
| `disabled` | Returns a disabled config. |

```scala mdoc:silent
import org.typelevel.otel4s.{Attribute, Attributes}

val cpuStarvation: CpuStarvationConfig =
  CpuStarvationConfig.enabled(
    attributes = Attributes(Attribute("example.attribute", "value"))
  )
```

### Create composite work-stealing configs

These types are available on the JVM and Scala Native.

| Companion method | Description |
|------------------|-------------|
| `WorkStealingThreadPoolConfig.apply(compute, workerThreads)` | Creates a config from its two child configs. |
| `WorkStealingThreadPoolConfig.enabled` | Returns a config with every work-stealing collector enabled. |
| `WorkStealingThreadPoolConfig.disabled` | Returns a config with every work-stealing collector disabled. |
| `WorkerThreadsConfig.apply(thread, localQueue, timerHeap, poller)` | Creates a config from its four child configs. |
| `WorkerThreadsConfig.enabled` | Returns a config with every worker-thread collector enabled. |
| `WorkerThreadsConfig.disabled` | Returns a config with every worker-thread collector disabled. |

```scala mdoc:silent
import WorkStealingThreadPoolConfig.{ComputeConfig, WorkerThreadsConfig}
import WorkerThreadsConfig._

val workStealingThreadPool: WorkStealingThreadPoolConfig =
  WorkStealingThreadPoolConfig(
    compute = ComputeConfig.enabled,
    workerThreads = WorkerThreadsConfig(
      thread = ThreadConfig.enabled,
      localQueue = LocalQueueConfig.disabled,
      timerHeap = TimerHeapConfig.enabled,
      poller = PollerConfig.disabled,
    ),
  )
```

All configuration types provide a `Show` instance and use it for `toString`.

## Related material

- [Register Cats Effect runtime metrics](../how-to-metrics/register-cats-effect-runtime-metrics.md)
- [Metrics API reference](metrics.md)
