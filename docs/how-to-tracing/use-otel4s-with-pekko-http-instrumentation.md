# Use otel4s with Pekko HTTP instrumentation

Use this page when the standard OpenTelemetry Java agent instruments Pekko HTTP and your handlers run Cats Effect
code instrumented with otel4s.

The Java agent creates spans through OpenTelemetry Java context.
The two bridging helpers from
[Use otel4s with Java-instrumented libraries](use-otel4s-with-java-instrumented-libraries.md) let otel4s continue
those spans and let Java-instrumented clients observe spans created by otel4s.

## 1. Add the Java agent and application dependencies

Add `sbt-javaagent` to `project/plugins.sbt`:

```scala
addSbtPlugin("com.github.sbt" % "sbt-javaagent" % "0.2.0")
```

Enable the plugin and add the dependencies to `build.sbt`:

```scala
lazy val service = project
  .enablePlugins(JavaAgent)
  .settings(
    libraryDependencies ++= Seq(
      "org.typelevel"    %% "otel4s-oteljava"                          % "@VERSION@",
      "org.apache.pekko" %% "pekko-stream"                             % "@PEKKO_STREAM_VERSION@",
      "org.apache.pekko" %% "pekko-http"                               % "@PEKKO_HTTP_VERSION@",
      "io.opentelemetry" % "opentelemetry-exporter-otlp"               % "@OPEN_TELEMETRY_VERSION@" % Runtime,
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % "@OPEN_TELEMETRY_VERSION@" % Runtime,
      "io.opentelemetry.instrumentation" % "opentelemetry-instrumentation-annotations" % "@OPEN_TELEMETRY_INSTRUMENTATION_VERSION@"
    ),
    run / fork := true,
    javaOptions ++= Seq(
      "-Dotel.java.global-autoconfigure.enabled=true",
      "-Dotel.service.name=pekko-otel4s"
    ),
    javaAgents ++= Seq(
      "io.opentelemetry.javaagent" % "opentelemetry-javaagent" % "@OPEN_TELEMETRY_INSTRUMENTATION_VERSION@" % Runtime
    )
  )
```

This guide uses the standard OpenTelemetry Java agent and explicit context bridging.
Do not add `otel4s-oteljava-context-storage` for this setup: `IOLocalContextStorage` does not support the standard
Java agent.

## 2. Read the SDK configured by the agent

The agent owns SDK creation, so use `OtelJava.global`.
Import `otel4s.localContext` to make `Local[IO, Context]` available to the handler code.

```scala mdoc:silent
import cats.effect.IO
import cats.mtl.Local
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.trace.Tracer

def program(implicit tracer: Tracer[IO], local: Local[IO, Context]): IO[Unit] = {
  val _ = (tracer, local)
  IO.unit
}

val run: IO[Unit] =
  OtelJava.global[IO].flatMap { otel4s =>
    import otel4s.localContext
    otel4s.tracerProvider.get("com.example").flatMap { implicit tracer =>
      program
    }
  }
```

## 3. Continue an agent span in Cats Effect code

Pekko HTTP invokes the route handler in Java-instrumented code.
Capture `JContext.current()` at that boundary and use it as the current otel4s context while the `IO` runs.

```scala mdoc:silent
import cats.effect.Async
import cats.effect.std.Random
import cats.effect.syntax.temporal._
import cats.effect.unsafe.implicits.global
import cats.syntax.all._
import io.opentelemetry.context.{Context => JContext}
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.apache.pekko.http.scaladsl.model.StatusCodes.OK
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import org.typelevel.otel4s.Attribute

import scala.concurrent.duration._

def route(implicit tracer: Tracer[IO], local: Local[IO, Context]): Route =
  path("gen-random-name") {
    get {
      complete {
        OK -> generateRandomName(length = 10)
      }
    }
  }

@WithSpan("generate-random-name")
def generateRandomName(length: Int)(implicit
    tracer: Tracer[IO],
    local: Local[IO, Context]
): String =
  withJContext(JContext.current())(generate[IO](length)).unsafeRunSync()

def generate[F[_]: Async: Tracer](length: Int): F[String] =
  Tracer[F].span("generate", Attribute("length", length.toLong)).surround {
    for {
      random <- Random.scalaUtilRandom[F]
      delay  <- random.betweenInt(100, 2000)
      chars  <- random.nextAlphaNumeric.replicateA(length).delayBy(delay.millis)
    } yield chars.mkString
  }

def withJContext[F[_], A](ctx: JContext)(fa: F[A])(implicit
    local: Local[F, Context]
): F[A] =
  Local[F, Context].scope(fa)(Context.wrap(ctx))
```

Calling `/gen-random-name` produces this span structure:

```text
> GET
  > generate-random-name
    > generate { length = 10 }
```

The Pekko server span and `generate-random-name` come from Java instrumentation.
The `generate` span comes from otel4s and uses the Java span as its parent.

## 4. Make Java-instrumented client calls under an otel4s span

For the other direction, activate the current otel4s context before starting a Java-instrumented client call.

```scala mdoc:silent
import cats.effect.Sync
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.HttpRequest
import org.apache.pekko.util.ByteString

import scala.concurrent.Future

def useJContext[F[_]: Sync, A](use: JContext => A)(implicit
    local: Local[F, Context]
): F[A] =
  Sync[F].flatMap(Local[F, Context].ask) { ctx =>
    Sync[F].delay {
      val javaContext = ctx.underlying
      val scope = javaContext.makeCurrent()
      try use(javaContext)
      finally scope.close()
    }
  }

def resolveIP[F[_]: Async: Tracer](implicit
    local: Local[F, Context],
    actorSystem: ActorSystem
): F[String] =
  Tracer[F].span("resolve-ip").surround {
    Async[F].flatMap(Async[F].executionContext) { implicit ec =>
      Async[F].fromFuture {
        useJContext[F, Future[String]] { _ =>
          for {
            response <- Http().singleRequest(
              HttpRequest(uri = "https://checkip.amazonaws.com")
            )
            body <- response.entity.dataBytes
              .runFold(ByteString.empty)(_ ++ _)
          } yield new String(body.toArray)
        }
      }
    }
  }
```

`useJContext` activates the `resolve-ip` span while `Http().singleRequest` starts the request.
The Java agent can then use `resolve-ip` as the parent of its HTTP client span.

The resulting part of the trace has this shape:

```text
> resolve-ip
  > HTTP GET
```

For the complete server lifecycle and an instrumented Pekko HTTP client request, see
[PekkoHttpExample][pekko-http-example].

## What's next

- Use the same bridging patterns with another Java library:
  [Use otel4s with Java-instrumented libraries](use-otel4s-with-java-instrumented-libraries.md)
- Understand why the two context views require bridging:
  [How otel4s context propagation works](../explanations/how-otel4s-context-propagation-works.md)
- Use the otel4s-specific Java agent and shared-context setup:
  [Use the otel4s Java agent](../how-to-jvm-setup/use-the-otel4s-java-agent.md)

[pekko-http-example]: https://github.com/typelevel/otel4s/blob/main/examples/src/main/scala/PekkoHttpExample.scala
