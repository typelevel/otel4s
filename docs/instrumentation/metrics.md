# Metrics API reference

The core metrics API is in `org.typelevel.otel4s.metrics`.

The main types form this construction and lifecycle chain:

| Type | Role | Obtained through |
|------|------|------------------|
| `MeterProvider[F]` | Creates named meters | An otel4s backend, such as `OtelJava` |
| `MeterBuilder[F]` | Configures meter instrumentation-scope metadata | `MeterProvider[F].meter` |
| `Meter[F]` | Creates synchronous and observable instruments | `MeterProvider[F].get` or `MeterBuilder[F].get` |
| Instrument builder | Configures an instrument before creating it | A method such as `Meter[F].counter` |
| Synchronous instrument | Records measurements directly from application code | The builder's `create` method |
| Observable instrument | Registers a collection callback | `create` or `createWithCallback` on an observable builder |
| `ObservableMeasurement[F, A]` | Records values in a batch callback | An observable builder's `createObserver` method |
| `BatchCallback[F]` | Registers one callback for multiple observable instruments | `Meter[F].batchCallback` |

For setup and task-oriented examples, see:

- [Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md)
- [Record application metrics](../how-to-metrics/record-application-metrics.md)
- [Customize histogram buckets](../how-to-metrics/customize-histogram-buckets.md)
- [Register Cats Effect runtime metrics](../how-to-metrics/register-cats-effect-runtime-metrics.md)
- [Register JVM runtime metrics](../how-to-metrics/register-jvm-runtime-metrics.md)

This page focuses on methods for creating, configuring, and recording metrics, including the low-level instrument
backends. The Scala 2 and Scala 3 `*Macro` objects implement instrument recording methods at compile time; they are
implementation details rather than instrumentation interfaces.

## `MeterProvider[F]`

`MeterProvider[F]` is the entry point for creating `Meter[F]` instances.

| Member | Result | Description |
|--------|--------|-------------|
| `get(name)` | `F[Meter[F]]` | Creates a named meter. Equivalent to `meter(name).get`. |
| `meter(name)` | `MeterBuilder[F]` | Creates a builder for a named meter. |

The `name` identifies the instrumentation scope that emits metrics. Use a stable library, module, or application name.

The companion object provides:

- `MeterProvider[F]` to summon an implicit provider
- `MeterProvider.noop[F]` to create a provider whose meters are no-op

## `MeterBuilder[F]`

`MeterBuilder[F]` adds instrumentation-scope metadata before creating a meter.

| Member | Description |
|--------|-------------|
| `withVersion(version)` | Sets the instrumentation-scope version. |
| `withSchemaUrl(schemaUrl)` | Sets the OpenTelemetry schema URL associated with the instrumentation scope. |
| `get` | Creates `F[Meter[F]]`. |

Use `MeterProvider[F].get` when the meter does not need a version or schema URL.

`MeterBuilder.noop[F]` creates a builder that returns a no-op meter. It is also the builder returned by
`MeterProvider.noop[F]`.

```scala mdoc:silent
import cats.effect.IO
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.oteljava.OtelJava

val program: IO[Unit] =
  OtelJava.autoConfigured[IO]().use { otel4s =>
    otel4s.meterProvider
      .meter("com.example.checkout")
      .withVersion("1.0.0")
      .get
      .flatMap { implicit meter: Meter[IO] =>
        Meter[IO].counter[Long]("checkout.requests").create.flatMap(_.inc())
      }
  }
```

## `Meter[F]`

`Meter[F]` creates synchronous and observable instruments. Instrument methods require an explicit measurement type,
such as `Long` or `Double`.

### Synchronous instrument builders

OpenTelemetry calls instruments synchronous when application code records measurements directly. The term does not
describe how the effect `F` is evaluated.

| Member | Builder | Instrument behavior |
|--------|---------|---------------------|
| `counter[A](name)` | `Counter.Builder[F, A]` | Records non-negative additions to a monotonic sum. |
| `upDownCounter[A](name)` | `UpDownCounter.Builder[F, A]` | Records changes to a non-monotonic sum. |
| `gauge[A](name)` | `Gauge.Builder[F, A]` | Records current, non-additive values. |
| `histogram[A](name)` | `Histogram.Builder[F, A]` | Records values into a distribution. |

### Observable instrument builders

Observable instruments run registered callbacks when the backend collects metrics.

| Member | Builder | Instrument behavior |
|--------|---------|---------------------|
| `observableCounter[A](name)` | `ObservableCounter.Builder[F, A]` | Observes a monotonic sum. |
| `observableUpDownCounter[A](name)` | `ObservableUpDownCounter.Builder[F, A]` | Observes a non-monotonic sum. |
| `observableGauge[A](name)` | `ObservableGauge.Builder[F, A]` | Observes current, non-additive values. |

### Other members

| Member | Result | Description |
|--------|--------|-------------|
| `meta` | `InstrumentMeta[F]` | Reports whether the meter is enabled. |
| `batchCallback` | `BatchCallback[F]` | Observes several instruments in one callback. |

The companion object provides:

- `Meter[F]` to summon an implicit meter
- `Meter.noop[F]` to create a meter whose instruments are no-op
- `Meter.Implicits.noop` to supply a no-op implicit meter

## `Meter.Synchronous[F]`

`Meter.Synchronous[F]` exposes `meta` and the four synchronous instrument builders without the observable APIs. It is
useful when code only records measurements directly and needs to lift its metrics capability to another effect.

| Member | Description |
|--------|-------------|
| `meta` | Returns `InstrumentMeta[F]`. |
| `counter[A](name)` | Creates a `Counter.Builder[F, A]`. |
| `upDownCounter[A](name)` | Creates an `UpDownCounter.Builder[F, A]`. |
| `gauge[A](name)` | Creates a `Gauge.Builder[F, A]`. |
| `histogram[A](name)` | Creates a `Histogram.Builder[F, A]`. |
| `liftTo[G]` | Lifts the synchronous meter from effect `F` to effect `G`. |

Every `Meter[F]` is also a `Meter.Synchronous[F]`.

The companion object provides:

- `Meter.Synchronous[F]` to summon an implicit synchronous meter
- `Meter.Synchronous.noop[F]` to create a no-op synchronous meter
- `Meter.Synchronous.Implicits.noop` to supply a no-op implicit synchronous meter

`liftTo[G]` requires `MonadCancel` for `F` and `G` and a `LiftValue[F, G]`.

## Synchronous instrument builders

`Counter.Builder`, `UpDownCounter.Builder`, `Gauge.Builder`, and `Histogram.Builder` share these members:

| Member | Result | Description |
|--------|--------|-------------|
| `withUnit(unit)` | The same builder type | Sets the unit, which may contain at most 63 ASCII characters. |
| `withDescription(description)` | The same builder type | Sets the instrument description. |
| `create` | `F[instrument]` | Creates the configured instrument. |
| `liftTo[G]` | The corresponding builder in `G` | Lifts the builder from effect `F` to effect `G`. |

The `create` result depends on the builder:

| Builder | `create` result |
|---------|-----------------|
| `Counter.Builder[F, A]` | `F[Counter[F, A]]` |
| `UpDownCounter.Builder[F, A]` | `F[UpDownCounter[F, A]]` |
| `Gauge.Builder[F, A]` | `F[Gauge[F, A]]` |
| `Histogram.Builder[F, A]` | `F[Histogram[F, A]]` |

`Histogram.Builder` also provides `withExplicitBucketBoundaries(boundaries)`. The boundaries are advisory; a backend
may use them when selecting the histogram aggregation.

## Synchronous instruments

Recording methods have two attribute forms:

- a varargs form for individual attributes
- an `immutable.Iterable[Attribute[_]]` form

On Scala 3, the varargs form accepts `AttributeOrIterableOnce*`, so a call can also include iterable attribute values.

### `Counter[F, A]`

`Counter[F, A]` records non-negative additions to a monotonic sum.

| Member | Result | Description |
|--------|--------|-------------|
| `backend` | `Counter.Backend[F, A]` | Returns the instrument's low-level backend. |
| `add(value, attributes*)` | `F[Unit]` | Adds a non-negative value with individual attributes. |
| `add(value, attributes)` | `F[Unit]` | Adds a non-negative value with an immutable attribute collection. |
| `inc(attributes*)` | `F[Unit]` | Adds one with individual attributes. |
| `inc(attributes)` | `F[Unit]` | Adds one with an immutable attribute collection. |
| `liftTo[G]` | `Counter[G, A]` | Lifts the instrument from effect `F` to effect `G`. |

`Counter.noop[F, A]` creates a counter that discards measurements.

### `UpDownCounter[F, A]`

`UpDownCounter[F, A]` records changes to a non-monotonic sum.

| Member | Result | Description |
|--------|--------|-------------|
| `backend` | `UpDownCounter.Backend[F, A]` | Returns the instrument's low-level backend. |
| `add(value, attributes*)` | `F[Unit]` | Adds a value with individual attributes. |
| `add(value, attributes)` | `F[Unit]` | Adds a value with an immutable attribute collection. |
| `inc(attributes*)` | `F[Unit]` | Adds one with individual attributes. |
| `inc(attributes)` | `F[Unit]` | Adds one with an immutable attribute collection. |
| `dec(attributes*)` | `F[Unit]` | Subtracts one with individual attributes. |
| `dec(attributes)` | `F[Unit]` | Subtracts one with an immutable attribute collection. |
| `liftTo[G]` | `UpDownCounter[G, A]` | Lifts the instrument from effect `F` to effect `G`. |

`UpDownCounter.noop[F, A]` creates an up-down counter that discards measurements.

### `Gauge[F, A]`

`Gauge[F, A]` records current, non-additive values.

| Member | Result | Description |
|--------|--------|-------------|
| `backend` | `Gauge.Backend[F, A]` | Returns the instrument's low-level backend. |
| `record(value, attributes*)` | `F[Unit]` | Records a value with individual attributes. |
| `record(value, attributes)` | `F[Unit]` | Records a value with an immutable attribute collection. |
| `liftTo[G]` | `Gauge[G, A]` | Lifts the instrument from effect `F` to effect `G`. |

`Gauge.noop[F, A]` creates a gauge that discards measurements.

### `Histogram[F, A]`

`Histogram[F, A]` records values into a distribution and can measure the duration of a `Resource` scope.

| Member | Result | Description |
|--------|--------|-------------|
| `backend` | `Histogram.Backend[F, A]` | Returns the instrument's low-level backend. |
| `record(value, attributes*)` | `F[Unit]` | Records a value with individual attributes. |
| `record(value, attributes)` | `F[Unit]` | Records a value with an immutable attribute collection. |
| `recordDuration(timeUnit, attributes*)` | `Resource[F, Unit]` | Records elapsed time with individual attributes. |
| `recordDuration(timeUnit, attributes)` | `Resource[F, Unit]` | Uses an immutable attribute collection. |
| `recordDuration(timeUnit, attributesForExit)` | `Resource[F, Unit]` | Computes attributes from `Resource.ExitCase`. |
| `liftTo[G]` | `Histogram[G, A]` | Lifts the instrument from effect `F` to effect `G`. |

`recordDuration` records the elapsed time when its resource is released.

The companion object provides:

- `Histogram.noop[F, A]` to create a histogram that discards measurements
- `Histogram.causeAttributes(exitCase)` to map failure or cancellation to a `cause` attribute

`causeAttributes` returns no attributes for a successful exit case.

`Counter`, `UpDownCounter`, and `Gauge` lifting requires `Monad[G]` and `LiftValue[F, G]`. `Histogram` lifting also
requires `MonadCancel` for both effects because `recordDuration` manages a `Resource` lifecycle. The same requirements
apply to the corresponding builders.

## Low-level instrument backends

Each synchronous instrument exposes a sealed `Backend` interface. The top-level recording methods use `meta` to skip
disabled instrumentation before invoking these lower-level methods.

| Interface | Member | Result |
|-----------|--------|--------|
| `Counter.Backend[F, A]` | `meta` | `InstrumentMeta[F]` |
| | `add(value, attributes)` | `F[Unit]` |
| | `inc(attributes)` | `F[Unit]` |
| | `liftTo[G]` | `Counter.Backend[G, A]` |
| `UpDownCounter.Backend[F, A]` | `meta` | `InstrumentMeta[F]` |
| | `add(value, attributes)` | `F[Unit]` |
| | `inc(attributes)` | `F[Unit]` |
| | `dec(attributes)` | `F[Unit]` |
| | `liftTo[G]` | `UpDownCounter.Backend[G, A]` |
| `Gauge.Backend[F, A]` | `meta` | `InstrumentMeta[F]` |
| | `record(value, attributes)` | `F[Unit]` |
| | `liftTo[G]` | `Gauge.Backend[G, A]` |
| `Histogram.Backend[F, A]` | `meta` | `InstrumentMeta[F]` |
| | `record(value, attributes)` | `F[Unit]` |
| | `recordDuration(timeUnit, attributesForExit)` | `Resource[F, Unit]` |
| | `liftTo[G]` | `Histogram.Backend[G, A]` |

For every backend, `meta` returns `InstrumentMeta[F]` and recording attributes use
`immutable.Iterable[Attribute[_]]`. Backend lifting has the same effect constraints as its enclosing instrument.

## Observable instrument builders

`ObservableCounter.Builder`, `ObservableUpDownCounter.Builder`, and `ObservableGauge.Builder` share these members:

| Member | Result | Description |
|--------|--------|-------------|
| `withUnit(unit)` | The same builder type | Sets the instrument unit. |
| `withDescription(description)` | The same builder type | Sets the instrument description. |
| `create(measurements)` | `Resource` | Registers an effect that returns `Iterable[Measurement[A]]`. |
| `createWithCallback(callback)` | `Resource` | Registers an `ObservableMeasurement[F, A]` callback. |
| `createObserver` | `F[ObservableMeasurement[F, A]]` | Creates an observer for registration with `BatchCallback[F]`. |

The resource result depends on the builder:

| Builder | `create` and `createWithCallback` result |
|---------|------------------------------------------|
| `ObservableCounter.Builder[F, A]` | `Resource[F, ObservableCounter]` |
| `ObservableUpDownCounter.Builder[F, A]` | `Resource[F, ObservableUpDownCounter]` |
| `ObservableGauge.Builder[F, A]` | `Resource[F, ObservableGauge]` |

Keep the resource returned by `create` or `createWithCallback` open for as long as the instrument should be registered.
Releasing it unregisters the callback.

Observable callbacks may run repeatedly or concurrently. They should complete in finite time and avoid blocking work.

`createObserver` does not register a callback. Measurements recorded through the observer are used only inside a
`BatchCallback[F]` that includes that observer.

`ObservableCounter`, `ObservableUpDownCounter`, and `ObservableGauge` are lifecycle handles with no public members.

## `Measurement[A]`

`Measurement[A]` pairs a value with the attributes attached to that observation.

| Member | Result | Description |
|--------|--------|-------------|
| `value` | `A` | Returns the value to record. |
| `attributes` | `Attributes` | Returns the attributes associated with the value. |
| `Measurement(value)` | `Measurement[A]` | Creates a measurement without attributes. |
| `Measurement(value, attributes*)` | `Measurement[A]` | Creates a measurement with individual attributes. |
| `Measurement(value, attributes)` | `Measurement[A]` | Creates a measurement with an `Attributes` collection. |

## `ObservableMeasurement[F, A]`

`ObservableMeasurement[F, A]` is passed to observable callbacks and created explicitly for batch callbacks.

| Member | Result | Description |
|--------|--------|-------------|
| `record(value, attributes*)` | `F[Unit]` | Records a value with individual attributes. |
| `record(value, attributes)` | `F[Unit]` | Records a value with an `Attributes` collection. |

`ObservableMeasurement.noop[F, A]` creates an observer that discards measurements.

## `BatchCallback[F]`

`BatchCallback[F]` registers one callback for multiple observable instruments. Registration returns a
`Resource[F, Unit]`; keep it open while the callback should be active.

| Member | Result | Description |
|--------|--------|-------------|
| `apply(callback, observable, rest*)` | `Resource[F, Unit]` | Registers a callback and one or more observers. |
| `of(a1, a2)(callback)` | `Resource[F, Unit]` | Evaluates two observer effects and registers the callback. |
| `of(a1, a2, a3)(callback)` | `Resource[F, Unit]` | Evaluates three observer effects and registers the callback. |
| `of(a1, ..., a4)(callback)` | `Resource[F, Unit]` | Evaluates four observer effects and registers the callback. |
| `of(a1, ..., a5)(callback)` | `Resource[F, Unit]` | Evaluates five observer effects and registers the callback. |
| `of(a1, ..., a6)(callback)` | `Resource[F, Unit]` | Evaluates six observer effects and registers the callback. |
| `of(a1, ..., a7)(callback)` | `Resource[F, Unit]` | Evaluates seven observer effects and registers the callback. |
| `of(a1, ..., a8)(callback)` | `Resource[F, Unit]` | Evaluates eight observer effects and registers the callback. |
| `of(a1, ..., a9)(callback)` | `Resource[F, Unit]` | Evaluates nine observer effects and registers the callback. |

Every `of` overload requires `Apply[F]`. Values recorded for observers that are not part of the registered callback are
ignored.

`BatchCallback.noop[F]` creates a callback registrar that does nothing.

## `MeasurementValue[A]`

`MeasurementValue[A]` identifies how an instrument value is represented by a metrics backend.

| Member | Result | Description |
|--------|--------|-------------|
| `contramap[B](f)` | `MeasurementValue[B]` | Derives a value for `B` by converting it to `A`. |
| `apply[A]` or `MeasurementValue[A]` | `MeasurementValue[A]` | Summons the implicit instance. |
| `longMeasurementValue` | `MeasurementValue[Long]` | The default `Long` instance. |
| `doubleMeasurementValue` | `MeasurementValue[Double]` | The default `Double` instance. |

## `BucketBoundaries`

`BucketBoundaries` stores the explicit boundaries supplied to `Histogram.Builder`.

| Member | Result | Description |
|--------|--------|-------------|
| `boundaries` | `Vector[Double]` | Returns the underlying boundaries. |
| `length` | `Int` | Returns the number of boundaries. |
| `apply(vector)` or `BucketBoundaries(vector)` | `BucketBoundaries` | Creates boundaries from a `Vector[Double]`. |
| `apply(values*)` or `BucketBoundaries(values*)` | `BucketBoundaries` | Creates boundaries from `Double` values. |

The two constructors validate that:

- no boundary is `NaN`
- boundaries are strictly increasing
- the first boundary is not negative infinity
- the last boundary is not positive infinity

For task-oriented bucket configuration, see
[Customize histogram buckets](../how-to-metrics/customize-histogram-buckets.md).

## `InstrumentMeta[F]`

`InstrumentMeta[F]` is in `org.typelevel.otel4s.metrics.meta`. It reports whether instrumentation is enabled.

| Member | Result | Description |
|--------|--------|-------------|
| `isEnabled` | `F[Boolean]` | Reports whether instrumentation is enabled. |
| `unit` | `F[Unit]` | Returns a no-op effect. |
| `whenEnabled(f)` | `F[Unit]` | Runs `f` only when instrumentation is enabled. |
| `liftTo[G]` | `InstrumentMeta[G]` | Lifts the metadata from effect `F` to effect `G`. |

## Related material

- [Provide a no-op meter](../how-to-metrics/provide-a-no-op-meter.md)
- [Record application metrics](../how-to-metrics/record-application-metrics.md)
- [Customize histogram buckets](../how-to-metrics/customize-histogram-buckets.md)
- [Histogram bucket customization with views](../explanations/histogram-bucket-customization-with-views.md)
