# Logs testkit reference

The logs testkit provides in-memory log collection and partial-matching expectation APIs for OpenTelemetry Java
`LogRecordData`.

Use this page as an API reference for `LogsTestkit`, `LogRecordExpectation`, and `LogRecordExpectations`.

For an end-to-end test setup, see
[Test logs emitted by your code](../how-to-testkit/test-logs-emitted-by-your-code.md).
For the overview of all signal testkits, see [Testkit](testkit.md).

The examples below assume these imports:

```scala mdoc:silent
import io.opentelemetry.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.{AnyValue, Attribute}
import org.typelevel.otel4s.logs.Severity
import org.typelevel.otel4s.oteljava.testkit.AttributesExpectation
import org.typelevel.otel4s.oteljava.testkit.{
  InstrumentationScopeExpectation,
  TelemetryResourceExpectation
}
import org.typelevel.otel4s.oteljava.testkit.logs._
```

## `LogsTestkit`

`LogsTestkit` is the signal-specific in-memory backend for logs.

| Member | Purpose |
| ------ | ------- |
| `LogsTestkit.inMemory[F]()` | Creates a `Resource[F, LogsTestkit[F]]` backed by an in-memory log exporter. |
| `LogsTestkit.builder[F]` | Creates a builder for customizing the underlying `SdkLoggerProviderBuilder`. |
| `loggerProvider` | The otel4s `LoggerProvider[F, Context]` used by code under test. |
| `finishedLogs` | Returns `List[LogRecordData]` from the in-memory log exporter. |
| `resetLogs` | Clears the in-memory log exporter. |

`OtelJavaTestkit` also exposes `loggerProvider`, `finishedLogs`, and `resetLogs` when a test needs logs together with
metrics or traces.

## Partial matching

`LogRecordExpectation` values are partial.

This means:

- unspecified fields are ignored
- you can assert only the parts that matter for a test
- you can add more detail when needed for correlation, scope, resource, and timestamps

For example:

```scala mdoc:silent
LogRecordExpectation.message("request failed")

LogRecordExpectation
  .message("request failed")
  .severity(Severity.error)
```

The first expectation ignores attributes, timestamps, trace correlation, and scope.
The second adds severity while still ignoring everything else.

## Message and body

OpenTelemetry log body is not always a string. The testkit therefore exposes two entry points:

- `message(String)` for the common string-log case
- `body(AnyValue)` for exact raw body matching

```scala mdoc:silent
LogRecordExpectation.message("request failed")

LogRecordExpectation.body(
  AnyValue.map(
    Map(
      "status" -> AnyValue.string("failed"),
      "count" -> AnyValue.long(2L)
    )
  )
)
```

Use `message(...)` when your instrumentation emits ordinary text logs.
Use `body(...)` when your log body is structured.

## Severity

Severity matching is intentionally shallow:

- `severity(...)`
- `severityText(...)`

```scala mdoc:silent
LogRecordExpectation
  .message("request failed")
  .severity(Severity.error)
  .severityText("ERROR")
```

This covers the main cases without introducing a nested severity matcher type.

## Trace and span correlation

One of the highest-value log assertions is correlation with the current span.

Use:

- `traceId(...)`
- `spanId(...)`
- `untraced`

```scala mdoc:silent
LogRecordExpectation
  .message("request failed")
  .traceId("0af7651916cd43dd8448eb211c80319c")
  .spanId("b7ad6b7169203331")

LogRecordExpectation
  .message("startup complete")
  .untraced
```

This makes it easy to assert that:

- a log emitted inside a traced operation carries the expected trace/span ids
- a log emitted outside a span remains uncorrelated

## Attributes

Log attributes use the same helpers as the metrics and traces expectation APIs:

- `attributesExact(...)`
- `attributesSubset(...)`
- `attributes(AttributesExpectation...)`
- `attributesEmpty`

```scala mdoc:silent
LogRecordExpectation
  .message("request failed")
  .attributesExact(
    Attribute("http.route", "/users"),
    Attribute("error.type", "timeout")
  )

LogRecordExpectation
  .message("request failed")
  .attributesSubset(Attribute("http.route", "/users"))

LogRecordExpectation
  .message("request failed")
  .attributes(
    AttributesExpectation.where("must contain at least one attribute")(_.nonEmpty)
  )
```

## Scope and resource

Instrumentation scope and telemetry resource can be asserted directly:

```scala mdoc:silent
LogRecordExpectation
  .message("request failed")
  .scope(
    InstrumentationScopeExpectation
      .name("service")
      .version("1.0.0")
      .attributesEmpty
  )
  .resource(
    TelemetryResourceExpectation.any
      .attributesSubset(Attribute("service.name", "auth-service"))
  )
```

This is useful when you want logs to be asserted with the same level of detail as metrics and traces.

## Timestamps

The log expectation API intentionally keeps timestamp matching simple.

Use:

- `timestampWhere(...)`
- `observedTimestampWhere(...)`

Both methods accept either:

- a raw predicate
- or a predicate with a clue

```scala mdoc:silent
LogRecordExpectation
  .message("request failed")
  .timestampWhere("source timestamp must be set")(_ > 0L)
  .observedTimestampWhere("observed timestamp must be set")(_ > 0L)
```

These hooks are enough for most tests without committing the public API to a large timestamp DSL.

## Top-level matching

Use `LogRecordExpectations` to match expectations against a collected list of exported log records.

Available helpers:

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

### `check`

Use `check(...)` when a single expected record should exist somewhere in the export:

```scala mdoc:silent
LogRecordExpectations.check(
  Nil,
  LogRecordExpectation.message("request failed")
)
```

### `checkAll`

`checkAll(...)` is non-consuming: each expectation is checked independently against the full exported list.

```scala mdoc:silent
LogRecordExpectations.checkAll(
  Nil,
  LogRecordExpectation.message("request failed"),
  LogRecordExpectation.message("request failed")
)
```

This does not require two distinct exported records.

### `checkAllDistinct`

`checkAllDistinct(...)` enforces distinct assignment.

This is the safer default when repeated expectations should match different exported records:

```scala mdoc:silent
LogRecordExpectations.checkAllDistinct(
  Nil,
  LogRecordExpectation.message("request failed"),
  LogRecordExpectation.message("request failed")
)
```

The implementation uses distinct matching rather than greedy first-match assignment, so repeated expectations behave
deterministically.

## Clues and custom predicates

You can always drop to custom predicates:

```scala mdoc:silent
LogRecordExpectation
  .message("request failed")
  .where("must have event name unset")(_.getEventName == null)
  .clue("request failure log")
```

Use clues whenever a test contains several similar expectations. The clue is preserved in mismatch messages and makes
failures much easier to interpret.

## Formatting mismatches

Use `LogRecordExpectations.format(...)` when integrating with a test framework:

```scala mdoc:silent
def assertLogs(
    records: List[LogRecordData],
    expected: LogRecordExpectation*
): Unit =
  LogRecordExpectations.checkAllDistinct(records, expected: _*) match {
    case Right(_) =>
      ()
    case Left(mismatches) =>
      sys.error(LogRecordExpectations.format(mismatches))
  }
```

Mismatch selection prioritizes trace/span correlation before generic mismatch count.
This makes failures more useful when several logs have similar bodies or severities but only one is correlated to the
expected span.
