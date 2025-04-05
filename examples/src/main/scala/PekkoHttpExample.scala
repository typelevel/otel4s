/*
 * Copyright 2022 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import cats.effect.Async
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.effect.Sync
import cats.effect.std.Random
import cats.effect.syntax.temporal._
import cats.mtl.Local
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.opentelemetry.context.{Context => JContext}
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.http.scaladsl.Http
import org.apache.pekko.http.scaladsl.model.HttpRequest
import org.apache.pekko.http.scaladsl.model.StatusCodes.OK
import org.apache.pekko.http.scaladsl.server.Directives._
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.util.ByteString
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.trace.Tracer

import scala.concurrent.Future
import scala.concurrent.duration._

/** This example relies on the OpenTelemetry Java agent. To make it work, add the following settings to your build:
  *
  * add `sbt-javaagent` dependency to the `plugins.sbt`:
  *
  * {{{
  * addSbtPlugin("com.github.sbt" % "sbt-javaagent" % "0.1.8")
  * }}}
  *
  * update definition of a project in the `build.sbt`:
  *
  * {{{
  * .enablePlugins(JavaAgent)
  * .settings(
  *   libraryDependencies ++= Seq(
  *     "org.typelevel"                   %% "otel4s-oteljava"                           % "0.5.0",
  *     "org.apache.pekko"                %% "pekko-stream"                              % "1.1.3",
  *     "org.apache.pekko"                %% "pekko-http"                                % "1.1.0",
  *     "io.opentelemetry.instrumentation" % "opentelemetry-instrumentation-annotations" % "2.14.0",
  *     "io.opentelemetry"                 % "opentelemetry-exporter-otlp"               % "1.49.0" % Runtime,
  *     "io.opentelemetry"                 % "opentelemetry-sdk-extension-autoconfigure" % "1.49.0" % Runtime
  *   )
  *   run / fork := true,
  *   javaOptions += "-Dotel.java.global-autoconfigure.enabled=true",
  *   javaOptions += "-Dotel.service.name=pekko-otel4s",
  *   javaAgents += "io.opentelemetry.javaagent" % "opentelemetry-javaagent" % "2.14.0" % Runtime
  * )
  * }}}
  */
object PekkoHttpExample extends IOApp.Simple {

  def run: IO[Unit] =
    OtelJava.global[IO].flatMap { otelJava =>
      import otelJava.localContext

      otelJava.tracerProvider.get("com.example").flatMap { implicit tracer: Tracer[IO] =>
        createSystem.use { implicit actorSystem: ActorSystem =>
          def bind: Future[Http.ServerBinding] =
            Http().newServerAt("127.0.0.1", 9000).bindFlow(routes)

          Resource
            .make(IO.fromFuture(IO.delay(bind))) { b =>
              IO.fromFuture(IO.delay(b.unbind())).void
            }
            .use(_ => IO.never)
        }
      }
    }

  private def createSystem: Resource[IO, ActorSystem] =
    Resource.make(IO.delay(ActorSystem()))(system => IO.fromFuture(IO.delay(system.terminate())).void)

  private def routes(implicit
      T: Tracer[IO],
      L: Local[IO, Context],
      S: ActorSystem
  ): Route =
    concat(
      path("gen-random-name") {
        get {
          complete {
            OK -> generateRandomName(length = 10)
          }
        }
      },
      path("get-ip") {
        get {
          complete {
            OK -> getIP()
          }
        }
      }
    )

  @WithSpan("generate-random-name")
  private def generateRandomName(
      length: Int
  )(implicit T: Tracer[IO], L: Local[IO, Context]): String =
    withJContext(JContext.current())(generate[IO](length))
      .unsafeRunSync()(runtime)

  @WithSpan("get-ip")
  private def getIP()(implicit
      T: Tracer[IO],
      L: Local[IO, Context],
      A: ActorSystem
  ): String =
    withJContext(JContext.current())(resolveIP[IO]).unsafeRunSync()(runtime)

  private def generate[F[_]: Async: Tracer](length: Int): F[String] =
    Tracer[F].span("generate", Attribute("length", length.toLong)).surround {
      for {
        random <- Random.scalaUtilRandom[F]
        delay <- random.betweenInt(100, 2000)
        chars <- random.nextAlphaNumeric
          .replicateA(length)
          .delayBy(delay.millis)
      } yield chars.mkString
    }

  private def resolveIP[F[_]: Async: Tracer](implicit
      L: Local[F, Context],
      A: ActorSystem
  ): F[String] =
    Tracer[F].span("resolve-ip").surround {
      Async[F].executionContext.flatMap { implicit ec =>
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

  private def withJContext[F[_], A](ctx: JContext)(fa: F[A])(implicit
      L: Local[F, Context]
  ): F[A] =
    Local[F, Context].scope(fa)(Context.wrap(ctx))

  private def useJContext[F[_]: Sync, A](use: JContext => A)(implicit
      L: Local[F, Context]
  ): F[A] =
    Local[F, Context].ask.flatMap { ctx =>
      Sync[F].delay {
        val jContext: JContext = ctx.underlying
        val scope = jContext.makeCurrent()
        try {
          use(jContext)
        } finally {
          scope.close()
        }
      }
    }
}
