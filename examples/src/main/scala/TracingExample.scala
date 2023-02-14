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

import cats.Monad
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.effect.std.Console
import cats.syntax.all._
import io.opentelemetry.api.GlobalOpenTelemetry
import org.typelevel.otel4s.Otel4s
import org.typelevel.otel4s.TextMapPropagator
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.Tracer
import org.typelevel.vault.Vault

import scala.concurrent.duration._

trait Work[F[_]] {
  def request(headers: Map[String, String]): F[Unit]
}

object Work {
  def apply[F[_]: Monad: Tracer: TextMapPropagator: Console]: Work[F] =
    new Work[F] {
      def request(headers: Map[String, String]): F[Unit] = {
        val vault =
          implicitly[TextMapPropagator[F]].extract(Vault.empty, headers)
        Tracer[F].childOrContinue(SpanContext.fromContext(vault)) {
          Tracer[F].span("Work.DoWork").use { span =>
            Tracer[F].currentSpanContext
              .flatMap(ctx => Console[F].println("Context is " + ctx)) *>
              span.addEvent("Starting the work.") *>
              doWorkInternal *>
              span.addEvent("Finished working.")
          }
        }
      }

      def doWorkInternal =
        Tracer[F]
          .span("Work.InternalWork")
          .surround(
            Console[F].println("Doin' work")
          )
    }
}

object TracingExample extends IOApp.Simple {
  def globalOtel4s: Resource[IO, Otel4s[IO]] =
    Resource
      .eval(IO(GlobalOpenTelemetry.get))
      .evalMap(OtelJava.forSync[IO])

  def run: IO[Unit] = {
    globalOtel4s.use { (otel4s: Otel4s[IO]) =>
      implicit val textMapProp: TextMapPropagator[IO] =
        otel4s.propagators.textMapPropagator
      otel4s.tracerProvider.tracer("example").get.flatMap {
        implicit tracer: Tracer[IO] =>
          val resource: Resource[IO, Unit] =
            Resource.make(IO.sleep(50.millis))(_ => IO.sleep(100.millis))
          tracer
            .resourceSpan("resource")(resource)
            .surround(
              Work[IO].request(
                Map(
                  "X-B3-TraceId" -> "80f198ee56343ba864fe8b2a57d3eff7",
                  "X-B3-ParentSpanId" -> "05e3ac9a4f6e3b90",
                  "X-B3-SpanId" -> "e457b5a2e4d86bd1",
                  "X-B3-Sampled" -> "1"
                )
              )
            )
      }
    }
  }
}
