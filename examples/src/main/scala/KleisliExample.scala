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

import cats.data.Kleisli
import cats.effect.Async
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import io.opentelemetry.api.GlobalOpenTelemetry
import org.typelevel.otel4s.oteljava.OtelJava
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.context.LocalContext
import org.typelevel.otel4s.trace.Tracer

object KleisliExample extends IOApp.Simple {
  def work[F[_]: Async: Tracer]: F[Unit] =
    Tracer[F].span("work").surround(Async[F].delay(println("I'm working")))

  private def tracerResource[F[_]: Async: LocalContext]
      : Resource[F, Tracer[F]] =
    Resource
      .eval(Async[F].delay(GlobalOpenTelemetry.get))
      .map(OtelJava.local[F])
      .evalMap(_.tracerProvider.get("kleisli-example"))

  def run: IO[Unit] =
    tracerResource[Kleisli[IO, Context, *]]
      .use { implicit tracer: Tracer[Kleisli[IO, Context, *]] =>
        work[Kleisli[IO, Context, *]]
      }
      .run(Context.root)
}
