# Traces testkit reference

The traces testkit provides in-memory span collection and structural expectation APIs for OpenTelemetry Java
`SpanData`.

Use this page as an API reference for `TracesTestkit`, `SpanExpectation`, `TraceExpectation`,
`TraceForestExpectation`, `SpanExpectations`, `TraceExpectations`, and the event, link, status, and span-context
expectation types.

For an end-to-end test setup, see
[Test traces emitted by your code](../how-to-testkit/test-traces-emitted-by-your-code.md).
For the overview of all signal testkits, see [Testkit](testkit.md).

The examples below assume these imports:

```scala mdoc:silent
import io.opentelemetry.sdk.trace.data.SpanData
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.oteljava.testkit.AttributesExpectation
import org.typelevel.otel4s.oteljava.testkit.{
  InstrumentationScopeExpectation,
  TelemetryResourceExpectation
}
import org.typelevel.otel4s.oteljava.testkit.trace._
```

## `TracesTestkit`

`TracesTestkit` is the signal-specific in-memory backend for traces.

| Member | Purpose |
| ------ | ------- |
| `TracesTestkit.inMemory[F]()` | Creates a `Resource[F, TracesTestkit[F]]` backed by an in-memory span exporter. |
| `TracesTestkit.builder[F]` | Creates a builder for customizing the underlying `SdkTracerProviderBuilder` and propagators. |
| `tracerProvider` | The otel4s `TracerProvider[F]` used by code under test. |
| `finishedSpans` | Returns `List[SpanData]` from the in-memory span exporter. |
| `resetSpans` | Clears the in-memory span exporter. |
| `propagators` | The context propagators used by the tracer provider. |
| `localContext` | The `LocalContext[F]` used by the tracer provider. |

`OtelJavaTestkit` also exposes the same trace members when a test needs traces together with metrics or logs.

## Flat vs structural matching

The traces expectation API has two layers:

- `SpanExpectations` for flat exported-span matching
- `TraceExpectations` for exact tree and forest matching

Use `SpanExpectations` when you only care that some exported spans exist:

```scala mdoc:silent
SpanExpectations.checkAllDistinct(
  Nil,
  SpanExpectation.server("GET /users"),
  SpanExpectation.client("SELECT users")
)
```

Use `TraceExpectations` when parent-child topology matters:

```scala mdoc:silent
TraceForestExpectation.unordered(
  TraceExpectation.unordered(
    SpanExpectation.server("GET /users").noParentSpanContext,
    TraceExpectation.leaf(SpanExpectation.client("SELECT users"))
  )
)
```

## Partial matching

`SpanExpectation` values are partial.

This means:

- unspecified span fields are ignored
- you can assert only the relevant properties for the current test
- you can still add more detail when needed

For example:

```scala mdoc:silent
SpanExpectation.name("app.span")
```

matches any span named `app.span`, regardless of timing, attributes, events, links, scope, or resource.

```scala mdoc:silent
import scala.concurrent.duration._

SpanExpectation
  .name("app.span")
  .startTimestamp(1.second)
  .endTimestamp(1500.millis)
```

adds exact timing checks on top of the name match.

The same principle applies recursively:

- `TraceExpectation` only checks the subtree shape you describe
- `SpanExpectation` only checks the span fields you set
- `EventExpectation` and `LinkExpectation` only check the fields you set
- `EventSetExpectation` and `LinkSetExpectation` only check the collection properties you set

## Trees and forests

The structural API uses two types:

- `TraceExpectation` for one subtree
- `TraceForestExpectation` for the full exported forest

Use:

- `TraceExpectation.leaf(...)` for a span with no expected children
- `TraceExpectation.ordered(...)` for a subtree whose direct children must appear in order
- `TraceExpectation.unordered(...)` for a subtree whose direct children may appear in any order

At the forest level:

- `TraceForestExpectation.ordered(...)` requires roots in order
- `TraceForestExpectation.unordered(...)` ignores root order
- `TraceForestExpectation.empty` requires no finished root spans

```scala mdoc:silent
TraceExpectation.leaf(SpanExpectation.name("db.query"))

TraceExpectation.ordered(
  SpanExpectation.name("request").noParentSpanContext,
  TraceExpectation.leaf(SpanExpectation.name("decode")),
  TraceExpectation.leaf(SpanExpectation.name("persist"))
)

TraceExpectation.unordered(
  SpanExpectation.name("request").noParentSpanContext,
  TraceExpectation.leaf(SpanExpectation.name("cache")),
  TraceExpectation.leaf(SpanExpectation.name("db.query"))
)
```

Both ordered and unordered modes still require the exact number of direct children or roots.
What changes is whether relative order matters.

This is especially useful when sibling spans can finish in a nondeterministic order.

## Span expectations

`SpanExpectation` is the building block for each trace node.

Start with one of the entry points:

- `SpanExpectation.any`
- `SpanExpectation.name(...)`
- `SpanExpectation.internal(...)`
- `SpanExpectation.server(...)`
- `SpanExpectation.client(...)`
- `SpanExpectation.producer(...)`
- `SpanExpectation.consumer(...)`

```scala mdoc:silent
import org.typelevel.otel4s.trace.SpanKind

SpanExpectation.any

SpanExpectation.name("service.call")

SpanExpectation.internal("cache.lookup")

SpanExpectation.name("db.query").kind(SpanKind.Client)
```

### Timing and lifecycle

You can assert timing and end-state directly:

```scala mdoc:silent
import scala.concurrent.duration._

SpanExpectation
  .name("request")
  .startTimestamp(1.second)
  .endTimestamp(1500.millis)
  .hasEnded

SpanExpectation
  .name("still-open")
  .endTimestamp(None)
  .hasNotEnded
```

### Attributes

Span attributes follow the same conventions as the metrics testkit:

- `attributesExact(...)`
- `attributesSubset(...)`
- `attributes(AttributesExpectation...)`
- `attributesEmpty`

```scala mdoc:silent
SpanExpectation
  .name("request")
  .attributesExact(
    Attribute("http.method", "GET"),
    Attribute("http.route", "/users")
  )

SpanExpectation
  .name("request")
  .attributesSubset(Attribute("http.method", "GET"))

SpanExpectation
  .name("request")
  .attributes(
    AttributesExpectation.where("must contain at least one attribute")(_.nonEmpty)
  )
```

### Status

Status matching is available through `StatusExpectation`:

```scala mdoc:silent
import org.typelevel.otel4s.trace.StatusCode

SpanExpectation
  .name("request")
  .status(StatusExpectation.ok)

SpanExpectation
  .name("request")
  .status(StatusExpectation.error.description("boom"))

SpanExpectation
  .name("request")
  .status(StatusExpectation.code(StatusCode.Error).description(None))
```

### Span context and parent context

You can match the span context itself, its parent, or selected context fields.

```scala mdoc:silent
SpanExpectation
  .name("child")
  .parentSpanContext(
    SpanContextExpectation
      .any
      .traceIdHex("0af7651916cd43dd8448eb211c80319c")
      .sampled(true)
  )

SpanExpectation
  .name("root")
  .noParentSpanContext
```

If you already have a concrete otel4s `SpanContext`, use exact matching:

```scala mdoc:silent
import org.typelevel.otel4s.trace.{SpanContext, TraceFlags, TraceState}
import scodec.bits.ByteVector

val spanContext =
  SpanContext(
    traceId = ByteVector.fromValidHex("0af7651916cd43dd8448eb211c80319c"),
    spanId = ByteVector.fromValidHex("0102030405060708"),
    traceFlags = TraceFlags.Default,
    traceState = TraceState.empty,
    remote = false
  )

SpanExpectation.name("request").spanContextExact(spanContext)
```

### Scope and resource

Instrumentation scope and telemetry resource are matched the same way as in the metrics testkit reference:

```scala mdoc:silent
SpanExpectation
  .name("request")
  .scope(
    InstrumentationScopeExpectation
      .name("service")
      .version("1.0")
      .attributesEmpty
  )
  .resource(
    TelemetryResourceExpectation.any
      .attributesSubset(Attribute("service.name", "user-service"))
  )
```

### Events

Events are matched with:

- `EventExpectation` for one event
- `EventSetExpectation` for the event collection

`EventExpectation` supports:

- `any`
- `name(...)`
- `timestamp(...)`
- `attributesExact(...)`
- `attributesSubset(...)`
- `attributesEmpty`
- `where(...)`

```scala mdoc:silent
import scala.concurrent.duration._

EventExpectation.name("started")

EventExpectation
  .name("exception")
  .timestamp(2.seconds)
  .attributesSubset(Attribute("exception.message", "boom"))
```

`EventSetExpectation` is collection-based. Use:

- `any`
- `exists`
- `forall`
- `contains`
- `exactly`
- `count`
- `minCount`
- `maxCount`
- `none`
- `predicate`
- `.and(...)` and `.or(...)`

```scala mdoc:silent
SpanExpectation
  .name("work")
  .events(
    EventSetExpectation
      .contains(
        EventExpectation.name("started"),
        EventExpectation.name("finished")
      )
      .and(EventSetExpectation.count(2))
  )

SpanExpectation
  .name("work")
  .events(
    EventSetExpectation.none(EventExpectation.name("exception"))
  )
```

The convenience span-level helpers are:

- `containsEvents(...)`
- `exactlyEvents(...)`
- `eventCount(...)`

### Links

Links are matched with:

- `LinkExpectation` for one link
- `LinkSetExpectation` for the link collection

`LinkExpectation` supports:

- `any`
- `spanContext(...)`
- `spanContextExact(...)`
- `traceId(...)`
- `traceIdHex(...)`
- `spanId(...)`
- `spanIdHex(...)`
- `sampled`
- `notSampled`
- `attributesExact(...)`
- `attributesSubset(...)`
- `attributesEmpty`
- `where(...)`

```scala mdoc:silent
LinkExpectation.any

LinkExpectation
  .any
  .traceIdHex("0af7651916cd43dd8448eb211c80319c")
  .sampled
```

`LinkSetExpectation` follows the same collection-level conventions as events:

- `any`
- `exists`
- `forall`
- `contains`
- `exactly`
- `count`
- `minCount`
- `maxCount`
- `none`
- `predicate`
- `.and(...)` and `.or(...)`

Span-level convenience helpers:

- `containsLinks(...)`
- `exactlyLinks(...)`
- `linkCount(...)`

## Flat span matching

When exact topology is not important, use `SpanExpectations` directly:

```scala mdoc:silent
SpanExpectations.checkAllDistinct(
  Nil,
  SpanExpectation.server("GET /users"),
  SpanExpectation.client("SELECT users")
)
```

The top-level helpers are:

- `exists`
- `find`
- `check`
- `checkAll`
- `checkAllDistinct`
- `missing`
- `missingDistinct`
- `allMatch`
- `allMatchDistinct`
- `format`

`checkAll(...)` is non-consuming: the same exported span may satisfy multiple expectations.

`checkAllDistinct(...)` enforces distinct assignment and is the safer default when repeated expectations should match
different collected spans.

## Clues and custom predicates

Every level of the trace API supports custom predicates and optional clues.

```scala mdoc:silent
SpanExpectation
  .name("request")
  .where("must be ended")(_.hasEnded())
  .clue("request span")

TraceExpectation
  .leaf(SpanExpectation.name("request"))
  .clue("root request subtree")
```

Clues are preserved in mismatch messages and make failures much easier to read in large tests.

## Formatting mismatches

Use the formatting helpers when connecting expectations to your test framework:

```scala mdoc:silent
def assertTrace(
    spans: List[SpanData],
    expected: TraceForestExpectation
): Unit =
  TraceExpectations.check(spans, expected) match {
    case Right(_) =>
      ()
    case Left(mismatches) =>
      sys.error(TraceExpectations.format(mismatches))
  }
```

For flat span checks:

```scala mdoc:silent
def assertSpans(
    spans: List[SpanData],
    expected: SpanExpectation*
): Unit =
  SpanExpectations.checkAllDistinct(spans, expected: _*) match {
    case Right(_) =>
      ()
    case Left(mismatches) =>
      sys.error(SpanExpectations.format(mismatches))
  }
```
