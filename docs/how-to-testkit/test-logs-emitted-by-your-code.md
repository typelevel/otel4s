# Test logs emitted by your code

Use `LogsTestkit` to run a logging bridge against an in-memory logs backend and assert the exported log records.

## 1. Add the testkit dependency

@:select(build-tool)

@:choice(sbt)

Add the testkit to the test scope in `build.sbt`:

```scala
libraryDependencies +=
  "org.typelevel" %% "otel4s-oteljava-testkit" % "@VERSION@" % Test
```

@:choice(scala-cli)

Add the test dependency to the test source file:

```scala
//> using test.dep "org.typelevel::otel4s-oteljava-testkit:@VERSION@"
```

@:@

## 2. Allocate the logger once

Create one logger for the bridge's instrumentation scope and pass it directly to the bridge that emits OpenTelemetry
log records.

```scala mdoc:silent
import cats.effect.IO
import org.typelevel.otel4s.{AnyValue, Attribute}
import org.typelevel.otel4s.logs.{Logger, Severity}

final class ServiceLogBridge(logger: Logger[IO, _]) {
  def requestFailed: IO[Unit] =
    logger.logRecordBuilder
      .withSeverity(Severity.error)
      .withSeverityText("ERROR")
      .withBody(AnyValue.string("request failed"))
      .addAttributes(
        Attribute("http.route", "/users"),
        Attribute("error.type", "timeout")
      )
      .emit
}
```

`Logger` is intended for adapters that bridge an existing logging framework into OpenTelemetry. It does not replace
the logging API used by application code.

## 3. Define an assertion helper

The expectation API returns structured mismatches. Convert them into the failure type used by your test framework.

```scala mdoc:silent
import io.opentelemetry.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.oteljava.testkit.logs.{LogRecordExpectation, LogRecordExpectations}

def assertLogs(records: List[LogRecordData], expected: LogRecordExpectation*): IO[Unit] =
  LogRecordExpectations.checkAll(records, expected: _*) match {
    case Right(_) =>
      IO.unit
    case Left(mismatches) =>
      IO.raiseError(new AssertionError(LogRecordExpectations.format(mismatches)))
  }
```

This helper uses `IO.raiseError` so it remains independent of a specific test framework. Replace it with the framework's
failure mechanism when appropriate.

## 4. Run the bridge with `LogsTestkit`

Create the testkit as a `Resource`, get a logger from its `loggerProvider`, and pass the logger directly to the bridge.
Collect the finished log records before the resource closes.

```scala mdoc:silent
import org.typelevel.otel4s.oteljava.testkit.logs.LogsTestkit

def test: IO[Unit] =
  LogsTestkit.inMemory[IO]().use { testkit =>
    for {
      logger <- testkit.loggerProvider.logger("service").withVersion("1.0.0").get
      bridge = new ServiceLogBridge(logger)
      _ <- bridge.requestFailed
      records <- testkit.finishedLogs
      _ <- assertLogs(
        records,
        LogRecordExpectation
          .message("request failed")
          .severity(Severity.error)
          .severityText("ERROR")
          .attributesSubset(
            Attribute("http.route", "/users"),
            Attribute("error.type", "timeout")
          )
      )
    } yield ()
  }
```

`LogRecordExpectation` ignores unspecified fields, such as timestamps, trace correlation, and resource attributes.

## What's next

For structured bodies, trace correlation, scope and resource matching, timestamps, and custom predicates, see
[Testkit | Logs](../oteljava/testkit-logs.md).
