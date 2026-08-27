# Use semantic attributes

Use generated semantic attributes when a span, metric, or log record represents a concept covered by the OpenTelemetry
semantic conventions.

This guide adds HTTP semantic attributes to a span. The same generated `AttributeKey` values work with metrics and log
records.

## Prerequisites

- [Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md)

## 1. Add the stable attributes module

@:select(build-tool)

@:choice(sbt)

```scala
libraryDependencies +=
  "org.typelevel" %% "otel4s-semconv" % "@VERSION@"
```

@:choice(scala-cli)

```scala
//> using dep "org.typelevel::otel4s-semconv:@VERSION@"
```

@:@

The stable attributes are available under `org.typelevel.otel4s.semconv.attributes`.

## 2. Add generated attributes to telemetry

Import the generated object for the convention you are implementing. Each member is an `AttributeKey[A]`, so applying
it to a value creates a typed `Attribute[A]`.

```scala mdoc:reset:silent
import cats.effect.{IO, IOApp}
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.semconv.attributes.HttpAttributes
import org.typelevel.otel4s.trace.Tracer

object Main extends IOApp.Simple {
  def run: IO[Unit] =
    OtelJava.autoConfigured[IO]().use { otel4s =>
      otel4s.tracerProvider.get("http-server").flatMap { implicit tracer =>
        handleRequest
      }
    }

  def handleRequest(implicit tracer: Tracer[IO]): IO[Unit] =
    Tracer[IO]
      .span(
        "GET /users/{id}",
        HttpAttributes.HttpRequestMethod("GET"),
        HttpAttributes.HttpResponseStatusCode(200L),
      )
      .surround(IO.println("request handled"))
}
```

The generated key fixes the attribute name and value type. The compiler rejects values of the wrong type.

## 3. Follow the semantic requirements for each attribute

Generated keys do not decide whether an attribute is required, recommended, conditionally required, or opt-in. They
also do not validate requirements such as cardinality limits or allowed values.

Read the Scaladoc on the generated key and the corresponding
[OpenTelemetry semantic convention][opentelemetry-semconv] before adding it to instrumentation.

## What's next

- Create an instrument from a generated metric definition:
  [Create metrics from semantic metric specs](create-metrics-from-semantic-metric-specs.md)
- Choose between stable and experimental modules:
  [Semantic conventions and stability](../explanations/semantic-conventions-and-stability.md)
- Look up generated packages and types:
  [Semantic conventions reference](../instrumentation/semantic-conventions.md)

[opentelemetry-semconv]: https://opentelemetry.io/docs/specs/semconv/
