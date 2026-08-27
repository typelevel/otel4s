# Tracing API reference

The core tracing API is in `org.typelevel.otel4s.trace`.

The main types form this construction and lifecycle chain:

| Type | Role | Obtained through |
|------|------|------------------|
| `TracerProvider[F]` | Creates named tracers | An otel4s backend, such as `OtelJava` |
| `TracerBuilder[F]` | Configures tracer instrumentation-scope metadata | `TracerProvider[F].tracer` |
| `Tracer[F]` | Creates spans and controls tracing scopes and propagation | `TracerProvider[F].get` or `TracerBuilder[F].get` |
| `SpanBuilder[F]` | Configures a span before it starts | `Tracer[F].spanBuilder` |
| `SpanOps[F]` | Selects how a configured span is started and ended | `SpanBuilder[F].build`, `Tracer[F].span`, or `Tracer[F].rootSpan` |
| `Span[F]` | Represents a running span | `SpanOps[F].use` callback, `SpanOps[F].resource` as `SpanOps.Res[F].span`, or `SpanOps[F].startUnmanaged` |

For setup and task-oriented examples, see:

- [Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md)
- [Create spans around effectful code](../how-to-tracing/create-spans-around-effectful-code.md)
- [Propagate trace context across service boundaries](../how-to-tracing/propagate-trace-context-across-service-boundaries.md)
- [Use unmanaged spans when a span must end outside its scope](../how-to-tracing/use-unmanaged-spans-when-a-span-must-end-outside-its-scope.md)
- [Trace Resource and fs2.Stream code](../how-to-tracing/trace-resource-and-fs2-stream-code.md)

## `TracerProvider[F]`

`TracerProvider[F]` is the entry point for creating `Tracer[F]` instances.

| Member | Result | Description |
|--------|--------|-------------|
| `get(name)` | `F[Tracer[F]]` | Creates a named tracer. Equivalent to `tracer(name).get`. |
| `tracer(name)` | `TracerBuilder[F]` | Creates a builder for a named tracer. |
| `liftTo[G]` | `TracerProvider[G]` | Lifts the provider from effect `F` to effect `G`. |

The `name` identifies the instrumentation scope that emits spans. Use a stable library, module, or application name.

The companion object provides:

- `TracerProvider[F]` to summon an implicit provider
- `TracerProvider.noop[F]` to create a provider whose tracers are no-op

## `TracerBuilder[F]`

`TracerBuilder[F]` adds instrumentation-scope metadata before creating a tracer.

| Member | Description |
|--------|-------------|
| `withVersion(version)` | Sets the instrumentation-scope version. |
| `withSchemaUrl(schemaUrl)` | Sets the OpenTelemetry schema URL associated with the instrumentation scope. |
| `get` | Creates `F[Tracer[F]]`. |
| `liftTo[G]` | Lifts the builder from effect `F` to effect `G`. |

Use `TracerProvider[F].get` when the tracer does not need a version or schema URL.

`TracerBuilder.noop[F]` creates a builder that returns a no-op tracer. It is also the builder returned by
`TracerProvider.noop[F]`.

```scala mdoc:reset:silent
import cats.effect.IO
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.trace.Tracer

val program: IO[Unit] =
  OtelJava.autoConfigured[IO]().use { otel4s =>
    otel4s.tracerProvider
      .tracer("com.example.checkout")
      .withVersion("1.0.0")
      .get
      .flatMap { implicit tracer: Tracer[IO] =>
        Tracer[IO].span("checkout").use_
      }
  }
```

## `Tracer[F]`

`Tracer[F]` creates spans, reads the current span, changes the active tracing scope, and propagates context through
carriers.

### Span access and creation

| Member | Result | Description |
|--------|--------|-------------|
| `meta` | `InstrumentMeta[F]` | Reports whether the tracer is enabled. |
| `currentSpanContext` | `F[Option[SpanContext]]` | Returns the current non-no-op span context, if one exists. |
| `currentSpanOrNoop` | `F[Span[F]]` | Returns the current span or a no-op span. |
| `currentSpanOrThrow` | `F[Span[F]]` | Returns the current span or raises `IllegalStateException` when no span is in scope. |
| `withCurrentSpanOrNoop(f)` | `F[A]` | Applies `f` to the current span or a no-op span. |
| `span(name, attributes*)` | `SpanOps[F]` | Configures a span that follows the current tracing scope. |
| `rootSpan(name, attributes*)` | `SpanOps[F]` | Configures a span that ignores the current parent. |
| `spanBuilder(name)` | `SpanBuilder[F]` | Creates a builder for additional span configuration. |

`span` and `rootSpan` accept individual attributes or an immutable collection of attributes. They are shortcuts for
configuring and building a `SpanBuilder`.

`Tracer.noop[F].currentSpanOrThrow` returns its no-op span rather than raising an error.

### Scope control

| Member | Description |
|--------|-------------|
| `childScope(parent)(fa)` | Runs `fa` with `parent` as the parent of newly created non-root spans. |
| `childOrContinue(parent)(fa)` | Uses `childScope` for `Some(parent)` and runs `fa` unchanged for `None`. |
| `joinOrRoot(carrier)(fa)` | Extracts a parent with `TextMapGetter`; runs `fa` in a root scope when extraction fails. |
| `rootScope(fa)` | Runs `fa` without the current parent span. |
| `noopScope(fa)` | Runs `fa` with tracing operations disabled in that scope. |

For the parent-selection rules, see
[Choosing parent spans and tracing scopes](../explanations/choosing-parent-spans-and-tracing-scopes.md).

### Carrier propagation

| Member | Description |
|--------|-------------|
| `propagate(carrier)` | Returns `F[C]` with the current context injected through `TextMapUpdater[C]`. |

`joinOrRoot` reads an incoming carrier. `propagate` writes to an outgoing immutable carrier and returns the updated
value. See [Cross-service trace propagation](tracing-cross-service-propagation.md) for carrier and propagator details.

### Effect lifting

`liftTo[G]` creates a `Tracer[G]` using an implicit `cats.mtl.LiftKind[F, G]`. The source and target effects require
`MonadCancelThrow`.

The companion object provides:

- `Tracer[F]` to summon an implicit tracer
- `Tracer.noop[F]` to create a no-op tracer
- `Tracer.Implicits.noop` to supply a no-op implicit tracer

## `SpanBuilder[F]`

`SpanBuilder[F]` stores span configuration. Calling `build` returns `SpanOps[F]`; it does not start the span.

| Member | Description |
|--------|-------------|
| `meta` | Returns `InstrumentMeta[F]` for the builder. |
| `addAttribute(attribute)` | Adds or replaces one attribute. |
| `addAttributes(attributes*)` | Adds or replaces multiple attributes. |
| `addLink(context, attributes*)` | Adds a link to another span context. |
| `withFinalizationStrategy(strategy)` | Selects the finalizer applied to managed spans. |
| `withSpanKind(kind)` | Sets the span kind. |
| `withStartTimestamp(timestamp)` | Sets an epoch-based start timestamp. |
| `withParent(parent)` | Sets an explicit parent span context. |
| `root` | Configures the span to ignore the current parent. |
| `modifyState(f)` | Applies a lower-level update to `SpanBuilder.State`. |
| `build` | Returns `SpanOps[F]` using the current builder state. |
| `liftTo[G]` | Lifts the builder from effect `F` to effect `G`. |

The initial `SpanBuilder.State` is:

| Setting | Initial value |
|---------|---------------|
| Attributes | `Attributes.empty` |
| Links | `Vector.empty` |
| Parent | `SpanBuilder.Parent.propagate` |
| Finalization strategy | `SpanFinalizer.Strategy.reportAbnormal` |
| Span kind | `None` |
| Start timestamp | `None` |

`SpanBuilder.Parent.propagate` follows the current tracing scope: a valid current span becomes the parent, no current
span starts a root span, and `noopScope` keeps tracing disabled. When the span kind is `None`, the backend uses
`SpanKind.Internal`. When the start timestamp is `None`, the backend uses `Clock[F].realTime` when the span starts.
`root` and `withParent` both update the parent-selection setting. When both are called, the last call takes effect.
Explicit timestamps must use the same epoch-based time source as `Clock[F].realTime`.

For lower-level state updates, `SpanBuilder.Parent` provides `propagate`, `root`, and `explicit(context)`.

```scala mdoc:reset:silent
import cats.effect.IO
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.{SpanKind, Tracer}

def serverSpan(implicit tracer: Tracer[IO]): IO[Unit] =
  Tracer[IO]
    .spanBuilder("http.request")
    .withSpanKind(SpanKind.Server)
    .addAttribute(Attribute("http.route", "/users"))
    .build
    .surround(IO.unit)
```

## `SpanOps[F]`

`SpanOps[F]` selects the lifecycle used to start and end a configured span.

| Member | Result | Lifecycle |
|--------|--------|-----------|
| `use(f)` | `F[A]` | Starts the span, passes it to `f`, and ends it when `f` completes. |
| `surround(fa)` | `F[A]` | Starts the span around `fa` and ends it when `fa` completes. |
| `use_` | `F[Unit]` | Starts and immediately ends the span. |
| `resource` | `Resource[F, SpanOps.Res[F]]` | Manages the span and exposes its handle and scope transformation. |
| `startUnmanaged` | `F[Span[F]]` | Starts a span that the caller must end. |
| `liftTo[G]` | `SpanOps[G]` | Lifts the operations from effect `F` to effect `G`. |

`use`, `surround`, and `resource` use the builder's finalization strategy. The default strategy records errors and
cancellation on abnormal termination.

`startUnmanaged` does not make the returned span current. Use `Tracer[F].childScope(span.context)` when later work
should create children of it.

### `SpanOps.Res[F]`

`resource` returns a managed `SpanOps.Res[F]` with these members:

| Member | Description |
|--------|-------------|
| `span` | The managed `Span[F]`. |
| `trace` | An `F ~> F` natural transformation that runs an effect in the span's scope. |
| `liftTo[G]` | Lifts both the span and scope transformation to effect `G`. |

The body of `Resource.use` does not automatically run in the managed span's scope. Apply `trace` to effects that should
run in that scope. See [Trace Resource and fs2.Stream code](../how-to-tracing/trace-resource-and-fs2-stream-code.md)
for the complete workflow.

## `Span[F]`

`Span[F]` is the handle to a running span.

| Member | Description |
|--------|-------------|
| `context` | Returns the span's `SpanContext`. |
| `isRecording` | Returns `F[Boolean]` indicating whether the span records updates. |
| `updateName(name)` | Replaces the span name. |
| `addAttribute(attribute)` | Adds or replaces one attribute. |
| `addAttributes(attributes*)` | Adds or replaces multiple attributes. |
| `addEvent(name, attributes*)` | Adds an event using the current timestamp. |
| `addEvent(name, timestamp, attributes*)` | Adds an event using an explicit epoch-based timestamp. |
| `addLink(context, attributes*)` | Adds a link to another span context. |
| `recordException(exception, attributes*)` | Records an exception and optional attributes. |
| `setStatus(status)` | Sets the span status. |
| `setStatus(status, description)` | Sets the span status and description. |
| `end` | Ends the span using the current real time. |
| `end(timestamp)` | Ends the span using an explicit epoch-based timestamp. |
| `liftTo[G]` | Lifts the span using an implicit `cats.mtl.LiftValue[F, G]`. |
| `backend` | Returns the low-level `Span.Backend[F]` used by the public operations. |

Attribute, event, link, and exception methods also accept immutable collections of attributes.

Only the first call to `end` determines the end timestamp. Spans passed to `use` or `resource` are ended by their
managed lifecycle. Call `end` explicitly for spans created by `startUnmanaged`.

```scala mdoc:reset:silent
import cats.effect.IO
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.{StatusCode, Tracer}

def annotate(implicit tracer: Tracer[IO]): IO[Unit] =
  Tracer[IO].span("job.run").use { span =>
    span
      .addEvent("job.started")
      .flatMap(_ => span.addAttribute(Attribute("job.id", "42")))
      .flatMap(_ => span.setStatus(StatusCode.Ok))
  }
```

## Supporting types

### `SpanContext`

`SpanContext` contains the identifiers and propagation state associated with a span.

| Member | Description |
|--------|-------------|
| `traceId` / `traceIdHex` | The 16-byte trace identifier as a `ByteVector` or 32-character lowercase hexadecimal string. |
| `spanId` / `spanIdHex` | The 8-byte span identifier as a `ByteVector` or 16-character lowercase hexadecimal string. |
| `traceFlags` | The `TraceFlags` associated with the trace. |
| `traceState` | The vendor-specific `TraceState` entries. |
| `isSampled` | Indicates whether the sampled trace flag is set. |
| `isValid` | Indicates whether the trace and span identifiers are valid. |
| `isRemote` | Indicates whether the context came from a remote parent. |

`SpanContext.invalid` is the standard invalid context. `SpanContext.apply` creates a context from trace and span
identifiers, flags, state, and the remote flag.

### `SpanKind`

| Value | Meaning |
|-------|---------|
| `Internal` | Internal application or library work. This is the default. |
| `Server` | Server-side handling of a remote request. |
| `Client` | Client-side work for a remote request. |
| `Producer` | Sending a message to a broker or other destination. |
| `Consumer` | Receiving a message from a broker or other source. |

### `StatusCode`

| Value | Meaning |
|-------|---------|
| `Unset` | No explicit status has been assigned. |
| `Ok` | The operation completed successfully. |
| `Error` | The operation completed with an error. |

### `SpanFinalizer.Strategy`

`SpanFinalizer.Strategy` is a `PartialFunction[Resource.ExitCase, SpanFinalizer]`.

The built-in strategies are:

- `reportAbnormal`, the default, which records an exception and error status for failure and sets an error status for
  cancellation
- `empty`, which does not apply a finalizer

Custom strategies can return finalizers created with `SpanFinalizer.recordException`, `setStatus`, `addAttribute`,
`addAttributes`, `updateName`, `addEvent`, `addLink`, or `multiple`.

## Related material

- [Provide a no-op tracer](../how-to-tracing/provide-a-no-op-tracer.md)
- [Choosing parent spans and tracing scopes](../explanations/choosing-parent-spans-and-tracing-scopes.md)
- [Tracing Resource and fs2.Stream scopes](../explanations/tracing-resource-and-fs2-stream-scopes.md)
- [How otel4s context propagation works](../explanations/how-otel4s-context-propagation-works.md)
- [Cross-service trace propagation](tracing-cross-service-propagation.md)
