# Tracing - interop with Java-instrumented libraries

## Glossary

| Name                                   | Description                                                  |
|----------------------------------------|--------------------------------------------------------------|
| [Context][otel4s-context]              | otel4s context that carries tracing information (spans, etc) |
| [Local［F, Context］][cats-mtl-local]    | The context carrier tool within the effect environment       |
| [Java SDK][opentelemetry-java]         | The OpenTelemetry library for Java                           |
| [JContext][opentelemetry-java-context] | Alias for `io.opentelemetry.context.Context`                 |
| [JSpan][opentelemetry-java-span]       | Alias for `io.opentelemetry.api.trace.Span`                  |

## The problem

[OpenTelemetry Java SDK][opentelemetry-java] and otel4s rely on different context manipulation approaches, 
which aren't interoperable out of the box. 
Java SDK utilizes ThreadLocal variables to share tracing information, 
otel4s, on the other hand, uses [Local][cats-mtl-local].

Let's take a look at example below:
```scala mdoc:silent
import cats.effect.IO
import org.typelevel.otel4s.trace.Tracer
import io.opentelemetry.api.trace.{Span => JSpan}

def test(implicit tracer: Tracer[IO]): IO[Unit] =
  tracer.span("test").use { span => // start 'test' span using otel4s
    val jSpanContext = JSpan.current().getSpanContext // get a span from a ThreadLocal var
    IO.println(s"Java ctx: $jSpanContext") >> IO.println(s"Otel4s ctx: ${span.context}")
  }
```

The output will be:
```
Java ctx: {traceId=00000000000000000000000000000000, spanId=0000000000000000, ...}
Otel4s ctx: {traceId=318854a5bd6ac0dd7b0a926f89c97ecb, spanId=925ad3a126cec272, ...}
```

Here we try to get the current `JSpan` within the effect. 
Unfortunately, due to different context manipulation approaches, 
the context operated by otel4s isn't visible to the Java SDK.

To mitigate this limitation, the context must be shared manually.

## Before we start

Since we need to manually modify the context we need direct access to `Local[F, Context]`. 
It can be constructed in the following way:

```scala mdoc:silent
import cats.effect._
import cats.mtl.Local
import cats.syntax.functor._
import org.typelevel.otel4s.instances.local._ // brings Local derived from IOLocal
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.OtelJava
import io.opentelemetry.api.GlobalOpenTelemetry

def createOtel4s[F[_]: Async](implicit L: Local[F, Context]): F[OtelJava[F]] =
  Async[F].delay(GlobalOpenTelemetry.get).map(OtelJava.local[F])

def program[F[_]: Async](otel4s: OtelJava[F])(implicit L: Local[F, Context]): F[Unit] = {
  val _ = (otel4s, L) // both OtelJava and Local[F, Context] are available here
  Async[F].unit
}

val run: IO[Unit] =
  IOLocal(Context.root).flatMap { implicit ioLocal: IOLocal[Context] =>
    createOtel4s[IO].flatMap(otel4s => program(otel4s))
  }
```

## How to use Java SDK context with otel4s

There are several scenarios when you want to run an effect with an explicit Java SDK context. 
For example, when you need to materialize an effect inside [Pekko HTTP][pekko-http] request handler.

To make it work, we can define a utility method:
```scala mdoc:silent:reset
import cats.mtl.Local
import org.typelevel.otel4s.oteljava.context.Context
import io.opentelemetry.context.{Context => JContext}

def withJContext[F[_], A](ctx: JContext)(fa: F[A])(implicit L: Local[F, Context]): F[A] =
  Local[F, Context].scope(fa)(Context.wrap(ctx))
```

1) `Context.wrap(ctx)` - creates otel4s context from the `JContext`  
2) `Local[F, Context].scope` - sets the given context as an active environment for the effect `fa`

_____

Let's say you use [Pekko HTTP][pekko-http] and want to materialize an `IO` using the current tracing context: 
```scala mdoc:silent:reset
import cats.effect.{Async, IO}
import cats.effect.std.Random
import cats.effect.syntax.temporal._
import cats.effect.unsafe.implicits.global
import cats.mtl.Local
import cats.syntax.all._
import org.apache.pekko.http.scaladsl.model.StatusCodes.OK
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.Tracer
import org.typelevel.otel4s.oteljava.context.Context
import io.opentelemetry.instrumentation.annotations.WithSpan
import io.opentelemetry.context.{Context => JContext}
import scala.concurrent.duration._

def route(implicit T: Tracer[IO], L: Local[IO, Context]): Route = 
  path("gen-random-name") {
    get {
      complete {
        OK -> generateRandomName(length = 10)
      }
    }
  }

@WithSpan("generate-random-name")
def generateRandomName(length: Int)(implicit T: Tracer[IO], L: Local[IO, Context]): String =
  withJContext(JContext.current())(generate[IO](length)).unsafeRunSync()

def generate[F[_]: Async: Tracer](length: Int): F[String] =
  Tracer[F].span("generate", Attribute("length", length.toLong)).surround {
    for {
      random <- Random.scalaUtilRandom[F]
      delay  <- random.betweenInt(100, 2000)
      chars  <- random.nextAlphaNumeric.replicateA(length).delayBy(delay.millis)
    } yield chars.mkString
  }

def withJContext[F[_], A](ctx: JContext)(fa: F[A])(implicit L: Local[F, Context]): F[A] =
  Local[F, Context].scope(fa)(Context.wrap(ctx))
```

When you invoke the `gen-random-name` endpoint, the spans will be structured in the following way:
```
> GET { http.method = GET, http.target = /gen-random-name, ... }
  > generate-random-name 
    > generate { length = 10 } 
```

## How to use otel4s context with Java SDK

To interoperate with Java libraries that rely on the Java SDK context, you need to activate the context manually.
The following utility method allows you to extract the current otel4s context and set it into the ThreadLocal variable:

```scala mdoc:silent:reset
import cats.effect.Sync
import cats.mtl.Local
import cats.syntax.flatMap._
import org.typelevel.otel4s.oteljava.context.Context
import io.opentelemetry.context.{Context => JContext}

def useJContext[F[_]: Sync, A](use: JContext => A)(implicit L: Local[F, Context]): F[A] = 
  Local[F, Context].ask.flatMap { ctx => // <1>
    Sync[F].delay {
      val jContext: JContext = ctx.underlying // <2>
      val scope = jContext.makeCurrent() // <3>
      try {
        use(jContext)
      } finally {
        scope.close()
      }
    }
  }
```

1) `Local[F, Context].ask` - get the current otel4s context  
2) `ctx.underlying` - unwrap otel4s context and get `JContext`  
3) `jContext.makeCurrent()` - activate `JContext` within the current thread

**Note:** we use `Sync[F].delay` to handle the side effects.
Depending on your use case, you may prefer `Sync[F].interruptible` or `Sync[F].blocking`.
 
Now we can run a slightly modified original 'problematic' example:
```scala
tracer.span("test").use { span => // start 'test' span using otel4s
  IO.println(s"Otel4s ctx: ${span.context}") >> useJContext[IO, Unit] { _ =>
    val jSpanContext = JSpan.current().getSpanContext // get a span from the ThreadLocal variable
     println(s"Java ctx: $jSpanContext") 
  }
}
```

The output will be:
```
Java ctx: {traceId=06f5d9112efbe711947ebbded1287a30, spanId=26ed80c398cc039f, ...}
Otel4s ctx: {traceId=06f5d9112efbe711947ebbded1287a30, spanId=26ed80c398cc039f, ...}
```

As we can see, the tracing information is in sync now,
and you can use Java-instrumented libraries within the `useJContext` block. 

## Pekko HTTP example

[PekkoHttpExample][pekko-http-example] is a complete example that shows how to use otel4s
with OpenTelemetry Java SDK instrumented libraries.

[opentelemetry-java]: https://github.com/open-telemetry/opentelemetry-java
[opentelemetry-java-autoconfigure]: https://github.com/open-telemetry/opentelemetry-java/blob/v1.31.0/sdk-extensions/autoconfigure/README.md
[opentelemetry-java-context]: https://github.com/open-telemetry/opentelemetry-java/blob/v1.31.0/context/src/main/java/io/opentelemetry/context/Context.java
[opentelemetry-java-span]: https://github.com/open-telemetry/opentelemetry-java/blob/v1.31.0/api/all/src/main/java/io/opentelemetry/api/trace/Span.java
[otel4s-context]: https://github.com/typelevel/otel4s/blob/main/java/common/src/main/scala/org/typelevel/otel4s/java/context/Context.scala
[cats-mtl-local]: https://typelevel.org/cats-mtl/mtl-classes/local.html
[pekko-http]: https://pekko.apache.org/docs/pekko-http/current
[pekko-http-example]: https://github.com/typelevel/otel4s/blob/main/examples/src/main/scala/PekkoHttpExample.scala
