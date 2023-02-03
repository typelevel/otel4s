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

import cats.effect.IO
import cats.effect.IOApp
import cats.effect.MonadCancelThrow
import cats.effect.Resource
import cats.effect.std.Console
import cats.syntax.all._
import io.opentelemetry.api.GlobalOpenTelemetry
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.trace.Tracer

import scala.concurrent.duration._

trait Work[F[_]] {
  def doWork(i: Int): F[Unit]
}

object Work {
  def apply[F[_]: MonadCancelThrow: Tracer: Console]: Work[F] =
    new Work[F] {
      def doWork(i: Int): F[Unit] =
        Tracer[F].span("Work.DoWork").use { span =>
          span.addAttribute(Attribute("number", i.toLong)) *>
            span.addEvent("Starting the work.") *>
            doWorkInternal *>
            span.addEvent("Finished working.")
        }

      def doWorkInternal =
        Console[F].println("Doin' work")
    }
}

object TracingExample extends IOApp.Simple {
  def tracerResource: Resource[IO, Tracer[IO]] =
    Resource
      .eval(IO(GlobalOpenTelemetry.get))
      .evalMap(OtelJava.forSync[IO])
      .evalMap(_.tracerProvider.tracer("Example").get)

  def run: IO[Unit] =
    tracerResource.use { implicit tracer: Tracer[IO] =>
      tracer
        .span("outer")
        .surround(
          IO.both(
            tracer
              .span("left")
              .surround(
                tracer.span("left-worker").use_.replicateA(10)
              ),
            tracer
              .span("right")
              .surround(
                tracer.span("right-worker").use_.replicateA(10)
              )
          )
        )
    }.void
}
