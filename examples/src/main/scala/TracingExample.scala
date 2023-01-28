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
import cats.effect.IOLocal
import cats.effect.MonadCancelThrow
import cats.effect.Resource
import cats.effect.std.Console
import cats.mtl.Stateful
import cats.syntax.all._
import io.opentelemetry.api.GlobalOpenTelemetry
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.trace.Tracer

trait Work[F[_]] {
  def doWork: F[Unit]
}

object Work {
  def apply[F[_]: MonadCancelThrow: Tracer: Console]: Work[F] =
    new Work[F] {
      def doWork: F[Unit] =
        Tracer[F].span("Work.DoWork").use { span =>
          span.addEvent("Starting the work.") *>
            doWorkInternal *>
            span.addEvent("Finished working.")
        }

      def doWorkInternal =
        Console[F].println("Doin' work")
    }
}

object TracingExample extends IOApp.Simple {

  // This would be a library function
  def stateful[S](a: S): IO[Stateful[IO, S]] =
    IOLocal(a).map { ioLocal =>
      new Stateful[IO, S] {
        override def get: IO[S] =
          ioLocal.get
        override def set(s: S): IO[Unit] =
          ioLocal.set(s)
        override def monad: Monad[IO] =
          Monad[IO]
      }
    }

  def tracerResource: Resource[IO, Tracer[IO]] = {
    import io.opentelemetry.context.{Context => JContext}
    import org.typelevel.otel4s.java.trace.TraceScope.Scope
    Resource.eval(stateful(Scope.Root(JContext.root()): Scope)).flatMap {
      implicit stateful: Stateful[IO, Scope] =>
        Resource
          .eval(IO(GlobalOpenTelemetry.get))
          .evalMap(OtelJava.forSync[IO])
          .evalMap(_.tracerProvider.tracer("Example").get)
    }
  }

  def run: IO[Unit] = {
    tracerResource.use { implicit tracer: Tracer[IO] =>
      Work[IO].doWork
    }
  }
}
