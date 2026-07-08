# Test traces emitted by your code

Use `TracesTestkit` to run code against an in-memory tracing backend and assert the exported spans.

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

## 2. Allocate the tracer once

Create one tracer for the service's instrumentation scope and pass it directly to the code that creates spans.

```scala mdoc:silent
import cats.effect.IO
import org.typelevel.otel4s.trace.Tracer

final class Service(implicit tracer: Tracer[IO]) {
  def handleRequest: IO[Unit] =
    Tracer[IO].span("service.request").surround {
      Tracer[IO].span("service.fetch").surround(IO.unit)
    }
}
```

## 3. Define an assertion helper

The structural expectation API returns mismatches for the exported trace forest. Convert them into the failure type
used by your test framework.

```scala mdoc:silent
import io.opentelemetry.sdk.trace.data.SpanData
import org.typelevel.otel4s.oteljava.testkit.trace.{TraceExpectations, TraceForestExpectation}

def assertTraces(spans: List[SpanData], expected: TraceForestExpectation): IO[Unit] =
  TraceExpectations.check(spans, expected) match {
    case Right(_) =>
      IO.unit
    case Left(mismatches) =>
      IO.raiseError(new AssertionError(TraceExpectations.format(mismatches)))
  }
```

This helper uses `IO.raiseError` so it remains independent of a specific test framework. Replace it with the framework's
failure mechanism when appropriate.

## 4. Run the code with `TracesTestkit`

Create the testkit as a `Resource`, get a tracer from its `tracerProvider`, and pass the tracer directly to the service.
Collect the finished spans before the resource closes.

```scala mdoc:silent
import org.typelevel.otel4s.oteljava.testkit.trace.{
  SpanExpectation,
  TraceExpectation,
  TracesTestkit
}

def test: IO[Unit] =
  TracesTestkit.inMemory[IO]().use { testkit =>
    for {
      tracer <- testkit.tracerProvider.get("service")
      service = new Service()(tracer)
      _ <- service.handleRequest
      spans <- testkit.finishedSpans
      _ <- assertTraces(
        spans,
        TraceForestExpectation.unordered(
          TraceExpectation.unordered(
            SpanExpectation.name("service.request").noParentSpanContext,
            TraceExpectation.leaf(SpanExpectation.name("service.fetch"))
          )
        )
      )
    } yield ()
  }
```

`TraceExpectation` checks the parent-child structure shown in the expectation. Unspecified span fields, such as
timestamps and attributes, are ignored.

## What's next

For flat span matching, attributes, events, links, status, and custom predicates, see
[Testkit | Traces](../oteljava/testkit-traces.md).
