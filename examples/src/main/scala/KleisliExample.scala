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
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.Resource
import cats.effect.Sync
import cats.mtl.Local
import io.opentelemetry.api.GlobalOpenTelemetry
import org.typelevel.otel4s.java.trace.Traces
import org.typelevel.otel4s.trace.Tracer
import org.typelevel.vault.Vault

object KleisliExample extends IOApp.Simple {
  def work[F[_]: Sync: Tracer] =
    Tracer[F].span("work").surround(Sync[F].delay(println("I'm working")))

  def tracerResource[F[_]](implicit
      F: Sync[F],
      L: Local[F, Vault]
  ): Resource[F, Tracer[F]] =
    Resource
      .eval(Sync[F].delay(GlobalOpenTelemetry.get))
      .map(Traces.local[F])
      .evalMap(_.tracerProvider.get("kleisli-example"))

  def run: IO[Unit] =
    tracerResource[Kleisli[IO, Vault, *]]
      .use { implicit tracer: Tracer[Kleisli[IO, Vault, *]] =>
        work[Kleisli[IO, Vault, *]]
      }
      .run(Vault.empty)
}
