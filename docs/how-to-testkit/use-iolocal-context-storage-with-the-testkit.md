# Use IOLocalContextStorage with the testkit

Use `IOLocalTestContextStorage` when application code uses `IOLocalContextStorage` and a test runs that code with an
OpenTelemetry Java testkit.

The testkit depends on the OpenTelemetry Java SDK testing module, which installs a test-specific `ContextStorageProvider`.
Using `IOLocalContextStorage.localProvider` directly with that provider produces an error like:

```text
java.lang.IllegalStateException: IOLocalContextStorage is not configured for use as the ContextStorageProvider.
The current ContextStorage is: io.opentelemetry.sdk.testing.context.SettableContextStorageProvider$SettableContextStorage
```

## 1. Add the context-storage testkit

This page assumes the test already depends on `otel4s-oteljava-testkit` and the application is configured to use
`IOLocalContextStorage`. See
[Keep otel4s context in sync with OpenTelemetry Java](../how-to-jvm-setup/keep-otel4s-context-in-sync-with-opentelemetry-java.md)
for the application setup.

@:select(build-tool)

@:choice(sbt)

Add the context-storage testkit to the test scope in `build.sbt`:

```scala
libraryDependencies +=
  "org.typelevel" %% "otel4s-oteljava-context-storage-testkit" % "@VERSION@" % Test
```

@:choice(scala-cli)

Add the test dependency to the test source file:

```scala
//> using test.dep "org.typelevel::otel4s-oteljava-context-storage-testkit:@VERSION@"
```

@:@

## 2. Provide the test local context

Define `IOLocalTestContextStorage.localProvider[IO]` as the `LocalContextProvider` used to create the testkit.

```scala mdoc:silent
import cats.effect.IO
import org.typelevel.otel4s.oteljava.context.LocalContextProvider
import org.typelevel.otel4s.oteljava.testkit.OtelJavaTestkit
import org.typelevel.otel4s.oteljava.testkit.context.IOLocalTestContextStorage

def test: IO[Unit] = {
  implicit val localContextProvider: LocalContextProvider[IO] =
    IOLocalTestContextStorage.localProvider[IO]

  OtelJavaTestkit.inMemory[IO]().use { testkit =>
    testkit.tracerProvider.get("service").flatMap { tracer =>
      tracer.span("test").surround(IO.unit)
    }
  }
}
```

The same provider works with `MetricsTestkit`, `TracesTestkit`, and `LogsTestkit`.

## What's next

- [Test metrics emitted by your code](test-metrics-emitted-by-your-code.md)
- [Test traces emitted by your code](test-traces-emitted-by-your-code.md)
- [Test logs emitted by your code](test-logs-emitted-by-your-code.md)
