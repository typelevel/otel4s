# Logs API reference

The core logs API is in `org.typelevel.otel4s.logs`.

It is intended for adapters that forward records from an existing logging framework into OpenTelemetry. Application
code continues to use its logging facade or implementation for log levels, formatting, appenders, and output.

The main types form this construction and emission chain:

| Type | Role | Obtained through |
|------|------|------------------|
| `LoggerProvider[F, Ctx]` | Creates named loggers | An otel4s backend, such as `OtelJava` |
| `LoggerBuilder[F, Ctx]` | Configures logger instrumentation-scope metadata | `LoggerProvider.logger` |
| `Logger[F, Ctx]` | Creates log records and exposes the current context | `LoggerProvider.get` or `LoggerBuilder.get` |
| `InstrumentMeta[F, Ctx]` | Reports whether the logs pipeline accepts a record | `Logger.meta` |
| `LogRecordBuilder[F, Ctx]` | Configures and emits one log record | `Logger.logRecordBuilder` |
| `Severity` | Represents an OpenTelemetry severity number and name | The `Severity` companion object |

`Ctx` is the context type supplied by the backend. The OpenTelemetry Java backend uses
`org.typelevel.otel4s.oteljava.context.Context`.

For setup and task-oriented examples, see:

- [Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md)
- [Bridge Scribe logs into OpenTelemetry](../how-to-logs/bridge-scribe-logs-into-opentelemetry.md)
- [Test logs emitted by your code](../how-to-testkit/test-logs-emitted-by-your-code.md)

## `LoggerProvider[F, Ctx]`

`LoggerProvider[F, Ctx]` is the entry point for creating `Logger[F, Ctx]` instances.

| Member | Result | Description |
|--------|--------|-------------|
| `get(name)` | `F[Logger[F, Ctx]]` | Creates a named logger. Equivalent to `logger(name).get`. |
| `logger(name)` | `LoggerBuilder[F, Ctx]` | Creates a builder for a named logger. |
| `liftTo[G]` | `LoggerProvider[G, Ctx]` | Lifts the provider from effect `F` to effect `G`. |

The `name` identifies the instrumentation scope that emits log records. Use a stable library, module, or fully
qualified class name.

The companion object provides:

- `LoggerProvider[F, Ctx]` to summon an implicit provider
- `LoggerProvider.noop[F, Ctx]` to create a provider whose loggers are no-op

## `LoggerBuilder[F, Ctx]`

`LoggerBuilder[F, Ctx]` adds instrumentation-scope metadata before creating a logger.

| Member | Description |
|--------|-------------|
| `withVersion(version)` | Sets the instrumentation-scope version. |
| `withSchemaUrl(schemaUrl)` | Sets the OpenTelemetry schema URL associated with the instrumentation scope. |
| `get` | Creates `F[Logger[F, Ctx]]`. |
| `liftTo[G]` | Lifts the builder from effect `F` to effect `G`. |

Use `LoggerProvider.get` when the logger does not need a version or schema URL.

`LoggerBuilder.noop[F, Ctx]` creates a builder that returns a no-op logger. It is also the builder returned by
`LoggerProvider.noop[F, Ctx]`.

```scala mdoc:silent
import cats.effect.IO
import org.typelevel.otel4s.AnyValue
import org.typelevel.otel4s.logs.Severity
import org.typelevel.otel4s.oteljava.OtelJava

val program: IO[Unit] =
  OtelJava.autoConfigured[IO]().use { otel4s =>
    otel4s.loggerProvider
      .logger("com.example.logging-bridge")
      .withVersion("1.0.0")
      .get
      .flatMap { logger =>
        logger.logRecordBuilder
          .withSeverity(Severity.error)
          .withSeverityText("ERROR")
          .withBody(AnyValue.string("request failed"))
          .emit
      }
  }
```

## `Logger[F, Ctx]`

`Logger[F, Ctx]` creates records for one instrumentation scope.

| Member | Result | Description |
|--------|--------|-------------|
| `meta` | `InstrumentMeta[F, Ctx]` | Reports whether logging instrumentation is enabled for a record. |
| `currentContext` | `F[Ctx]` | Returns the context that a record emitted now would use. |
| `logRecordBuilder` | `LogRecordBuilder[F, Ctx]` | Creates an empty log record builder. |
| `liftTo[G]` | `Logger[G, Ctx]` | Lifts the logger from effect `F` to effect `G`. |

When `LogRecordBuilder.withContext` is not called, `emit` uses the current context. Use `currentContext` and
`withContext` when the adapter captures a logging event in one context and emits it later.

The companion object provides:

- `Logger[F, Ctx]` to summon an implicit logger
- `Logger.noop[F, Ctx]` to create a no-op logger
- `Logger.Implicits.noop` to supply a no-op implicit logger

## `InstrumentMeta[F, Ctx]`

`Logger.meta` returns logs-specific `InstrumentMeta[F, Ctx]` from `org.typelevel.otel4s.logs.meta`.

| Member | Description |
|--------|-------------|
| `isEnabled(severity, eventName)` | Checks the record using the current context. |
| `isEnabled(context, severity, eventName)` | Checks the record using an explicit context. |
| `liftTo[G]` | Lifts the metadata check from effect `F` to effect `G`. |

Both checks accept `Option[Severity]` and `Option[String]`. They return `F[Boolean]`.

Call `isEnabled` before formatting messages, converting attributes, or rendering exception stack traces. It is a
pipeline-level check: an active OpenTelemetry logs pipeline returns `true`, while a no-op implementation returns
`false`. It does not apply per-logger severity filtering. The source logging framework remains responsible for deciding
whether a record's level is enabled.

## `LogRecordBuilder[F, Ctx]`

`LogRecordBuilder[F, Ctx]` configures and emits one log record. It provides these setters:

| Member | Accepted value | Description |
|--------|----------------|-------------|
| `withTimestamp` | `FiniteDuration` or `Instant` | Sets the time when the event occurred at its source. |
| `withObservedTimestamp` | `FiniteDuration` or `Instant` | Sets the time when the collection system observed the event. |
| `withContext` | `Ctx` | Sets the context used for trace and span correlation. |
| `withSeverity` | `Severity` | Sets the normalized OpenTelemetry severity. |
| `withSeverityText` | `String` | Preserves the source framework's severity text. |
| `withBody` | `AnyValue` | Sets a string or structured log body. |
| `withEventName` | `String` | Identifies the class or type of the event. |
| `withException` | `Throwable` | Adds `exception.type`, `exception.message`, and `exception.stacktrace`. |
| `addAttribute` | `Attribute[A]` | Adds or replaces one attribute. |
| `addAttributes` | `Attribute[_]*` or `immutable.Iterable[Attribute[_]]` | Adds or replaces several attributes. |

Repeated calls to a field setter retain the value from the last call. Adding an attribute whose key already exists
replaces the previous value for that key.

| Member | Result | Description |
|--------|--------|-------------|
| `emit` | `F[Unit]` | Sends the configured record to the processing pipeline. |
| `liftTo[G]` | `LogRecordBuilder[G, Ctx]` | Lifts the builder from effect `F` to effect `G`. |

`LogRecordBuilder.noop[F, Ctx]` creates a builder whose setters return the same no-op builder and whose `emit` returns
`F[Unit]` without producing a record.

## `Severity`

`Severity` represents the OpenTelemetry severity number and display name. Each value exposes `value: Int` and
`name: String`.

| Range | Constructors | Numeric values |
|-------|--------------|----------------|
| Trace | `trace`, `trace2`, `trace3`, `trace4` | 1–4 |
| Debug | `debug`, `debug2`, `debug3`, `debug4` | 5–8 |
| Info | `info`, `info2`, `info3`, `info4` | 9–12 |
| Warn | `warn`, `warn2`, `warn3`, `warn4` | 13–16 |
| Error | `error`, `error2`, `error3`, `error4` | 17–20 |
| Fatal | `fatal`, `fatal2`, `fatal3`, `fatal4` | 21–24 |

The companion object also provides `Hash[Severity]` and `Show[Severity]` instances. `Show` renders the severity name.

## Effect lifting

`LoggerProvider`, `LoggerBuilder`, `Logger`, `InstrumentMeta`, and `LogRecordBuilder` provide `liftTo[G]` methods backed
by `cats.mtl.LiftValue`. The target effect requires a `Monad`; the source effect's `Applicative` comes from the
`LiftValue` instance.

## Related material

- [Bridge Scribe logs into OpenTelemetry](../how-to-logs/bridge-scribe-logs-into-opentelemetry.md)
- [Test logs emitted by your code](../how-to-testkit/test-logs-emitted-by-your-code.md)
- [How otel4s context propagation works](../explanations/how-otel4s-context-propagation-works.md)
- [OpenTelemetry Logs API specification][opentelemetry-logs-api]
- [OpenTelemetry Logs data model][opentelemetry-logs-data-model]

[opentelemetry-logs-api]: https://opentelemetry.io/docs/specs/otel/logs/api/
[opentelemetry-logs-data-model]: https://opentelemetry.io/docs/specs/otel/logs/data-model/
