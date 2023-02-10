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

package org.typelevel.otel4s.java.trace

import cats.effect.IOLocal
import cats.effect.LiftIO
import cats.effect.Sync
import io.opentelemetry.api.{OpenTelemetry => JOpenTelemetry}
import io.opentelemetry.context.{Context => JContext}
import org.typelevel.otel4s.java.trace.instances._
import org.typelevel.otel4s.trace.TracerProvider

trait Traces[F[_]] {
  def tracerProvider: TracerProvider[F]
}

object Traces {

  def ioLocal[F[_]: LiftIO: Sync](jOtel: JOpenTelemetry): F[Traces[F]] =
    IOLocal[TraceScope.Scope](TraceScope.Scope.Root(JContext.root))
      .map { implicit ioLocal: IOLocal[TraceScope.Scope] =>
        val provider = TracerProviderImpl.local(jOtel.getTracerProvider)
        new Traces[F] {
          def tracerProvider: TracerProvider[F] = provider
        }
      }
      .to[F]

}
