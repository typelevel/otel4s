# Logs

@:callout(warning)

The logging module is meant for **library authors** who want to **bridge logs** from an existing logging API into OpenTelemetry, 
not for application developers to replace their logging library.

It is **not a replacement** for a full logging API.

@:@

The API mirrors the [OpenTelemetry Logs spec][opentelemetry-logs-spec] and focuses on structured, 
context-aware log records.

## 1. When you should use this module

Use it when your library already relies on a logging facade or implementation, and you want those log events to 
**flow into the same OpenTelemetry pipeline as traces and metrics**. 
The module does not format console output, rotate files, manage log levels of a specific logger, or manage appenders. 
It only allows turning log events into OpenTelemetry `LogRecord`s and exporting them through OTLP.

Library authors should provide an integration through a separate module, for example `logs4cats-otel4s`.
End users, such as application developers, should keep their preferred logger and level management, for example `logback` or `slf4j`.

## 2. Core ideas

You should bridge logs from your logging framework into OpenTelemetry as close to the source as possible. 
That means you implement a small adapter that, for each log event, does the following: build a `LogRecordBuilder`, 
copy the message and attributes, attach the current context so trace and span ids propagate, set severity and observed time, then emit.

There are three main interfaces:
- `LoggerProvider[F, Ctx]` - factory for `Logger`. Usually created during startup by the backend, either `otel4s-oteljava` or `otel4s-sdk`.
- `Logger[F, Ctx]` - use `logRecordBuilder` to create a new empty log record.
- `LogRecordBuilder[F, Ctx]` - set timestamps, severity, attributes, body, and context, then call `emit`.

The `Ctx` parameter refers to the backend-specific context type. In most cases, you don't need to use it directly.

## 3. Tips for library authors

Use `otel4s-core-logs` module, you don't need to use a specific otel4s backend: `otel4s-oteljava` or `otel4s-sdk`. 
That way, users can choose their preferred otel4s backend.

Keep in mind that the logs module **doesn't** manage log files, appenders, or log levels. 
It's the responsibility of a logging library to decide whether the given logger is enabled.  

However, you can use the `logger.meta.isEnabled` to check whether the logging pipeline is active.
If the pipeline is active, `isEnabled` will always return true regardless of the specific logger.
If the implementation is no-op, `isEnabled` will always return false.

### 3.1. Timestamps

Use `withObservedTimestamp` for the time your adapter observed the event. 
If your source event carries the original creation time, set `withTimestamp` to that origin time too. 

### 3.2. Attributes

Use semantic conventions when applicable, for example:
- `code.filepath`, `code.lineno`, `code.function` when provided by your logging framework
- `exception.type`, `exception.message`, `exception.stacktrace` for failures

Prefer stable names and values that are easy to aggregate.

### 3.3. Context propagation

Logs become far more valuable when they carry trace and span ids. 
By default, otel4s uses the current context to propagate trace and span ids, 
check out the [context propagation logic](../tracing-context-propagation.md) for more details.

However, you can also use the `withContext` to inject a specific context into the log record.

### 3.4. Backpressure and performance

The `emit` call sends the record into the processing pipeline. 
By default, the backend configures a batch processor, so the effect is usually non-blocking. 

Avoid heavy string concatenation and unnecessary exception stack traces on the hot path. 
Prefer structured attributes over preformatted strings. 
If your logging facade supports lazy messages, keep that behavior and only build a log record when severity passes the library-level filter.

## 4. Integration example with `scribe`

[Scribe][scribe] is a fast, flexible, and asynchronous Scala logging library that provides rich features like log levels, 
structured logging, and customizable handlers. It is also available for all platforms: JVM, Scala.js, and Scala Native.

Here is an example of how to forward `scribe` log events into OpenTelemetry using `otel4s-core-logs`:
```scala mdoc:silent
import cats.Monad
import cats.mtl.Local
import cats.syntax.all._
import org.typelevel.otel4s.{AnyValue, Attribute, Attributes}
import org.typelevel.otel4s.logs.{LogRecordBuilder, LoggerProvider, Severity}
import org.typelevel.otel4s.logs.{Logger => OtelLogger}
import org.typelevel.otel4s.semconv.attributes.{CodeAttributes, ExceptionAttributes}

import scribe._

import java.io.{PrintWriter, StringWriter}

import scala.concurrent.duration._
import scala.util.chaining._

final class ScriberLoggerSupport[F[_]: Monad, Ctx](
  provider: LoggerProvider[F, Ctx],
  local: Local[F, Ctx]
) extends LoggerSupport[F[Unit]] {
  
  def log(record: => LogRecord): F[Unit] =
    for {
      r <- Monad[F].pure(record)
      // use the library version here
      logger <- provider.logger(r.className).withVersion("0.0.1").get

      // retrieve the current context
      ctx <- local.ask
      
      // Check if logging instrumentation is enabled for the current context.
      // NOTE: this does not check whether an individual logger is enabled.
      // If the OpenTelemetry logging pipeline (backed by OTLP) is active, 
      // `isEnabled` will always return true regardless of the specific logger 
      isEnabled <- logger.meta.isEnabled(ctx, toSeverity(r.level), None)
      
      // if enabled, build and emit the log record
      _ <- if (isEnabled) buildLogRecord(logger, r).emit else Monad[F].unit
    } yield ()

  private def buildLogRecord(
      logger: OtelLogger[F, Ctx], 
      record: LogRecord
  ): LogRecordBuilder[F, Ctx] =
    logger.logRecordBuilder
      // severity
      .pipe { l =>
        toSeverity(record.level).fold(l)(l.withSeverity)
      }
      .withSeverityText(record.level.name)
      // timestmap
      .withTimestamp(record.timeStamp.millis)
      // log message
      .withBody(AnyValue.string(record.logOutput.plainText))
      // thread info
      .pipe { builder =>
        builder.addAttributes(
          if (record.thread.getId != -1) {
            Attributes(
              Attribute("thread.id", record.thread.getId),
              Attribute("thread.name", record.thread.getName),
            )
          } else {
            Attributes(
              Attribute("thread.name", record.thread.getName),
            )
          }
        )
      }
      // code path info
      .pipe { builder =>
        builder.addAttributes(codePathAttributes(record))
      }
      // exception info
      .pipe { builder =>
        record.messages
          .collect  {
            case scribe.throwable.TraceLoggableMessage(throwable) => throwable
          }
          .foldLeft(builder)((b, t) => b.addAttributes(exceptionAttributes(t)))
      }
      // context
      // MDC
      .pipe { builder =>
        if (record.data.nonEmpty) builder.addAttributes(dataAttributes(record.data)) 
        else builder
      }

  private def toSeverity(level: Level): Option[Severity] =
    level match {
      case Level("TRACE", _) => Some(Severity.trace)
      case Level("DEBUG", _) => Some(Severity.debug)
      case Level("INFO", _)  => Some(Severity.info)
      case Level("WARN", _)  => Some(Severity.warn)
      case Level("ERROR", _) => Some(Severity.error)
      case Level("FATAL", _) => Some(Severity.fatal)
      case _                 => None
    }

  private def codePathAttributes(record: LogRecord): Attributes = {
    val builder = Attributes.newBuilder

    builder += Attribute("code.namespace", record.className)
    builder += CodeAttributes.CodeFilePath(record.fileName)
    builder ++= record.line.map(line => CodeAttributes.CodeLineNumber(line.toLong))
    builder ++= record.column.map(col => CodeAttributes.CodeColumnNumber(col.toLong))
    builder ++= record.methodName.map(name => CodeAttributes.CodeFunctionName(name))
    
    builder.result()
  }

  private def exceptionAttributes(exception: Throwable): Attributes = {
    val builder = Attributes.newBuilder

    builder += ExceptionAttributes.ExceptionType(exception.getClass.getName)

    val message = exception.getMessage
    if (message != null) {
      builder += ExceptionAttributes.ExceptionMessage(message)

    }
    
    if (exception.getStackTrace.nonEmpty) {
      val stringWriter = new StringWriter()
      val printWriter = new PrintWriter(stringWriter)

      exception.printStackTrace(printWriter)
      builder += ExceptionAttributes.ExceptionStacktrace(stringWriter.toString)
    }
    
    builder.result()
  }
  
  private def dataAttributes(data: Map[String, () => Any]): Attributes = {
    val builder = Attributes.newBuilder
    data.foreach { case (key, getValue) =>
      getValue() match {
        case v: String  => builder += Attribute(key, v)
        case v: Boolean => builder += Attribute(key, v)
        case v: Byte    => builder += Attribute(key, v.toLong)
        case v: Short   => builder += Attribute(key, v.toLong)
        case v: Int     => builder += Attribute(key, v.toLong)
        case v: Long    => builder += Attribute(key, v)
        case v: Double  => builder += Attribute(key, v)
        case v: Float   => builder += Attribute(key, v.toDouble)
        case _          => 
          // ignore the rest. 
          // alternatively, you can stringify the value:
          // builder += Attribute(key, v.toString)
      }
    }
    builder.result()
  }
}
```

_____


Then, you can use it in your application:
```scala mdoc:silent
import org.typelevel.otel4s.Otel4s

def program[F[_]: Monad](otel4s: Otel4s[F]): F[Unit] = {
  val logger = new ScriberLoggerSupport(otel4s.loggerProvider, otel4s.localContext)

  otel4s.tracerProvider.get("tracer").flatMap { tracer =>
    tracer.spanBuilder("test-span").build.surround {
      logger.error(
        "something went wrong", 
        new RuntimeException("Oops, something went wrong")
      )
    }
  }
}
```

As you can see, the log record is automatically correlated with the current tracing context. 

@:image(grafana-logs-example.png) {
  alt = Grafana Logs Example
}

## 5. Getting a `LoggerProvider`

You can acquire a provider from either backend. 
You typically rely on autoconfiguration or a manual SDK builder that includes the OTLP exporter.

### 5.1. Using `otel4s-oteljava`

The `otel4s-oteljava` uses the [OpenTelemetry Java SDK][opentelemetry-java] under the hood.
Check out the [overview](../oteljava/overview.md) of the backend for more details. 

@:select(build-tool)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %% "otel4s-oteljava" % "@VERSION@", // <1>
  "io.opentelemetry" % "opentelemetry-exporter-otlp" % "@OPEN_TELEMETRY_VERSION@" % Runtime, // <2>
  "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "@OPEN_TELEMETRY_VERSION@" % Runtime // <3>
)
javaOptions += "-Dotel.java.global-autoconfigure.enabled=true" // <4>
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using dep "org.typelevel::otel4s-oteljava:@VERSION@" // <1>
//> using dep "io.opentelemetry:opentelemetry-exporter-otlp:@OPEN_TELEMETRY_VERSION@" // <2>
//> using dep "io.opentelemetry:opentelemetry-sdk-extension-autoconfigure:@OPEN_TELEMETRY_VERSION@" // <3>
//> using javaOpt "-Dotel.java.global-autoconfigure.enabled=true" // <4>
```

@:@

1. Add the `otel4s-oteljava` library
2. Add an OpenTelemetry exporter. Without the exporter, the application will crash
3. Add an OpenTelemetry autoconfigure extension
4. Enable OpenTelemetry SDK [autoconfigure mode][opentelemetry-java-autoconfigure]

_______

Then use `OtelJava.autoConfigured` to autoconfigure the SDK:
```scala mdoc:silent:reset
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.trace.TracerProvider
import org.typelevel.otel4s.logs.LoggerProvider

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] =
    OtelJava
      .autoConfigured[IO]()
      .use { sdk =>
        program(sdk.meterProvider, sdk.tracerProvider, sdk.loggerProvider)
      }

  def program(
      meterProvider: MeterProvider[IO], 
      tracerProvider: TracerProvider[IO],
      loggerProvider: LoggerProvider[IO, Context],
  ): IO[Unit] =
    ???
}
```

The `.autoConfigured(...)` relies on the environment variables and system properties to configure the SDK.
For example, use `export OTEL_SERVICE_NAME=auth-service` to configure the name of the service.
See the full set of the [supported configuration options][opentelemetry-java-autoconfigure].

### 5.2. Using `otel4s-sdk`

The `otel4s-sdk` is an alternative implementation written in Scala and available for all platforms: JVM, Scala Native, and Scala.js.
Check out the [overview](../sdk/overview.md) of the backend for more details.

@:select(build-tool)

@:choice(sbt)

Add settings to the `build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.typelevel" %%% "otel4s-sdk" % "@VERSION@", // <1>
  "org.typelevel" %%% "otel4s-sdk-exporter" % "@VERSION@" // <2>
)
```

@:choice(scala-cli)

Add directives to the `*.scala` file:

```scala
//> using dep "org.typelevel::otel4s-sdk::@VERSION@" // <1>
//> using dep "org.typelevel::otel4s-sdk-exporter::@VERSION@" // <2>
```

@:@

1. Add the `otel4s-sdk` library
2. Add the `otel4s-sdk-exporter` library. Without the exporter, the application will crash

_______

Then use `OpenTelemetrySdk.autoConfigured` to autoconfigure the SDK:
```scala mdoc:silent:reset
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.sdk.OpenTelemetrySdk
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.exporter.otlp.autoconfigure.OtlpExportersAutoConfigure
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.trace.TracerProvider
import org.typelevel.otel4s.logs.LoggerProvider

object TelemetryApp extends IOApp.Simple {

  def run: IO[Unit] =
    OpenTelemetrySdk
      .autoConfigured[IO]( // register OTLP exporters configurer
        _.addExportersConfigurer(OtlpExportersAutoConfigure[IO]) 
      )
      .use { autoConfigured =>
        val sdk = autoConfigured.sdk
        program(sdk.meterProvider, sdk.tracerProvider, sdk.loggerProvider)
      }

  def program(
      meterProvider: MeterProvider[IO], 
      tracerProvider: TracerProvider[IO],
      loggerProvider: LoggerProvider[IO, Context],
  ): IO[Unit] =
    ???
}
```

The `.autoConfigured(...)` relies on the environment variables and system properties to configure the SDK.
For example, use `export OTEL_SERVICE_NAME=auth-service` to configure the name of the service.
See the full set of the [supported configuration options](../sdk/configuration.md).

[scribe]: https://github.com/outr/scribe
[opentelemetry-logs-spec]: https://opentelemetry.io/docs/specs/otel/logs/api/
[opentelemetry-java]: https://github.com/open-telemetry/opentelemetry-java
[opentelemetry-java-autoconfigure]: https://opentelemetry.io/docs/languages/java/configuration/