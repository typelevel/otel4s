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

import cats.Applicative
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.IOLocal
import cats.effect.MonadCancelThrow
import cats.effect.Resource
import cats.effect.std.Console
import cats.mtl.Local
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
  def local[E](a: E): IO[Local[IO, E]] =
    IOLocal(a).map { ioLocal =>
      new Local[IO, E] {
        override def local[A](fa: IO[A])(f: E => E): IO[A] =
          ioLocal.get.flatMap(prev =>
            ioLocal
              .set(f(prev))
              .bracket(Function.const(fa))(Function.const(ioLocal.set(prev)))
          )
        override val applicative: Applicative[IO] =
          Applicative[IO]
        override def ask[E2 >: E]: IO[E2] =
          ioLocal.get
      }
    }

  def tracerResource: Resource[IO, Tracer[IO]] = {
    import io.opentelemetry.context.{Context => JContext}
    import org.typelevel.otel4s.java.trace.TraceScope.Scope
    Resource.eval(local(Scope.Root(JContext.root()): Scope)).flatMap {
      implicit local: Local[IO, Scope] =>
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
