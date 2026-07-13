# Testkit

The `otel4s-oteljava-testkit` module provides in-memory metric, trace, and log exporters for the OpenTelemetry Java
backend. It runs instrumentation against the real backend and exposes exported telemetry as OpenTelemetry Java SDK
models.

The testkit is framework-independent, so it can be used with munit, weaver, ScalaTest, or another test framework.

## Choose a testkit

Use a signal-specific testkit when a test covers one signal. Use `OtelJavaTestkit` when a test needs two or more signals.

| Testkit             | Providers                                        | Exported telemetry                                  |
|---------------------|--------------------------------------------------|-----------------------------------------------------|
| `MetricsTestkit`    | `meterProvider`                                  | `collectMetrics`: `List[MetricData]`                |
| `TracesTestkit`     | `tracerProvider`                                 | `finishedSpans`: `List[SpanData]`                   |
| `LogsTestkit`       | `loggerProvider`                                 | `finishedLogs`: `List[LogRecordData]`               |
| `OtelJavaTestkit`   | `meterProvider`, `tracerProvider`, `loggerProvider` | Metrics, spans, and logs from the methods above   |

## How-to guides

- [Test metrics emitted by your code](../how-to-testkit/test-metrics-emitted-by-your-code.md)
- [Test traces emitted by your code](../how-to-testkit/test-traces-emitted-by-your-code.md)
- [Test logs emitted by your code](../how-to-testkit/test-logs-emitted-by-your-code.md)
- [Use IOLocalContextStorage with the testkit](../how-to-testkit/use-iolocal-context-storage-with-the-testkit.md)

## Expectation APIs

The expectation APIs match OpenTelemetry Java SDK models. Expectations are partial by default: fields that are not
specified are ignored.

| Signal  | SDK model       | Main expectation APIs                                                                   | API reference                         |
|---------|-----------------|-----------------------------------------------------------------------------------------|---------------------------------------|
| Metrics | `MetricData`    | `MetricExpectation`, `PointExpectation`, `PointSetExpectation`, `MetricExpectations`    | [Metrics testkit reference](testkit-metrics.md) |
| Traces  | `SpanData`      | `SpanExpectation`, `TraceExpectation`, `TraceForestExpectation`, `SpanExpectations`, `TraceExpectations` | [Traces testkit reference](testkit-traces.md) |
| Logs    | `LogRecordData` | `LogRecordExpectation`, `LogRecordExpectations`                                         | [Logs testkit reference](testkit-logs.md)       |

Each top-level expectation API has a `format` method for rendering structured mismatches:

- `MetricExpectations.format(...)`
- `SpanExpectations.format(...)`
- `TraceExpectations.format(...)`
- `LogRecordExpectations.format(...)`

## Use raw SDK models when needed

The in-memory testkits always expose the underlying `MetricData`, `SpanData`, and `LogRecordData` values. Direct model
assertions are useful when:

- diagnosing a failing test by inspecting the complete exported payload
- checking a field that the expectation API does not expose
- prototyping a matcher before adding a reusable expectation

Expectation-based and direct model assertions can be used in the same test.

## Context storage in tests

The OpenTelemetry Java SDK testing dependency installs a test-specific `ContextStorageProvider`. When production code
uses `IOLocalContextStorage`, provide `IOLocalTestContextStorage.localProvider` in tests instead. See
[Use IOLocalContextStorage with the testkit](../how-to-testkit/use-iolocal-context-storage-with-the-testkit.md).
