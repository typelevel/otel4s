# Propagate trace context across service boundaries

Use this page when you want to continue an incoming trace or pass the current trace to another service.

## Prerequisites

- Finish [Set up otel4s in a JVM application](../how-to-jvm-setup/set-up-otel4s-in-a-jvm-application.md).
- Have a `Tracer[F]` available in your application code.
- If you use non-default propagators, configure them in the OpenTelemetry SDK. See the
  [OpenTelemetry Java configuration guide][opentelemetry-java-configuration].

## 1. Continue an incoming trace

Use `joinOrRoot` to extract trace context from an incoming carrier. If extraction fails, otel4s starts a
new root context instead.

```scala mdoc:silent
import cats.effect.IO
import org.typelevel.otel4s.trace.Tracer

def handleIncoming(headers: Map[String, String])(implicit tracer: Tracer[IO]): IO[Unit] =
  Tracer[IO].joinOrRoot(headers) {
    Tracer[IO].span("request.handle").surround(IO.unit)
  }
```

`Map[String, String]` and `Seq[(String, String)]` work out of the box.

## 2. Inject the current trace into an outgoing carrier

Use `propagate` when you need to send the current trace context to another service.
`propagate` does not send anything by itself.
It writes the current trace context into the carrier you provide and returns the updated carrier.

```scala mdoc:silent
import cats.effect.IO
import org.typelevel.otel4s.trace.Tracer

def buildOutgoingHeaders(implicit tracer: Tracer[IO]): IO[Map[String, String]] =
  Tracer[IO].span("request.outgoing").surround {
    Tracer[IO].propagate(Map.empty[String, String])
  }
```

After that, attach the returned carrier to the actual request or message you send downstream.

```scala mdoc:silent
import cats.effect.IO
import org.typelevel.otel4s.trace.Tracer

def send(body: String, headers: Map[String, String]): IO[Unit] =
  IO.println(s"sending $body with headers $headers")

def sendRequest(body: String)(implicit tracer: Tracer[IO]): IO[Unit] =
  Tracer[IO].span("request.outgoing").surround {
    Tracer[IO].propagate(Map.empty[String, String]).flatMap { headers =>
      send(body, headers)
    }
  }
```

## 3. Add support for your carrier type

If your carrier type is not supported out of the box, provide `TextMapGetter` and `TextMapUpdater`.

```scala mdoc:silent
import cats.effect.IO
import org.http4s.{Header, Headers}
import org.typelevel.ci.CIString
import org.typelevel.otel4s.context.propagation.{TextMapGetter, TextMapUpdater}
import org.typelevel.otel4s.trace.Tracer

implicit val headersTextMapUpdater: TextMapUpdater[Headers] =
  new TextMapUpdater[Headers] {
    def updated(headers: Headers, key: String, value: String): Headers =
      headers.put(Header.Raw(CIString(key), value))
  }

implicit val headersTextMapGetter: TextMapGetter[Headers] =
  new TextMapGetter[Headers] {
    def get(headers: Headers, key: String): Option[String] =
      headers.get(CIString(key)).map(_.head.value)

    def keys(headers: Headers): Iterable[String] =
      headers.headers.map(_.name.toString)
  }

def continueFromHttpHeaders(headers: Headers)(implicit tracer: Tracer[IO]): IO[Unit] =
  Tracer[IO].joinOrRoot(headers) {
    Tracer[IO].span("http.handle").surround(IO.unit)
  }

def injectIntoHttpHeaders(implicit tracer: Tracer[IO]): IO[Headers] =
  Tracer[IO].propagate(Headers.empty)
```

## What's next

- Work with Java libraries that depend on OpenTelemetry context:
  [Use otel4s with Java-instrumented libraries](use-otel4s-with-java-instrumented-libraries.md)
- Keep otel4s context in sync with OpenTelemetry Java context:
  [Tracing | Context propagation](../oteljava/tracing-context-propagation.md)
- For baggage, see the existing [Baggage](../instrumentation/baggage.md) page.
- For custom propagators and more background, see the existing
  [Cross-service trace propagation](../instrumentation/tracing-cross-service-propagation.md) page.

[opentelemetry-java-configuration]: https://opentelemetry.io/docs/languages/java/configuration/
