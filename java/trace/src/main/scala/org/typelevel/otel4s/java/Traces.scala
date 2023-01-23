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

package org.typelevel.otel4s.java

import cats.effect.LiftIO
import cats.effect.Sync
import cats.syntax.functor._
import io.opentelemetry.api.{OpenTelemetry => JOpenTelemetry}
import org.typelevel.otel4s.java.trace.TracerProviderImpl
import org.typelevel.otel4s.trace.TracerProvider

trait Traces[F[_]] {
  def tracerProvider: TracerProvider[F]
}

object Traces {

  def ioLocal[F[_]: LiftIO: Sync](jOtel: JOpenTelemetry): F[Traces[F]] =
    for {
      provider <- TracerProviderImpl.ioLocal(jOtel.getTracerProvider)
    } yield new Traces[F] {
      def tracerProvider: TracerProvider[F] = provider
    }

}
