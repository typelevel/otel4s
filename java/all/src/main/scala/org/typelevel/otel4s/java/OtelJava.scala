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

import cats.effect.Async
import cats.effect.IOLocal
import cats.effect.LiftIO
import cats.effect.Resource
import cats.effect.Sync
import cats.mtl.Local
import cats.syntax.all._
import io.opentelemetry.api.{
  GlobalOpenTelemetry,
  OpenTelemetry => JOpenTelemetry
}
import io.opentelemetry.sdk.{OpenTelemetrySdk => JOpenTelemetrySdk}
import org.typelevel.otel4s.ContextPropagators
import org.typelevel.otel4s.Otel4s
import org.typelevel.otel4s.java.Conversions.asyncFromCompletableResultCode
import org.typelevel.otel4s.java.instances._
import org.typelevel.otel4s.java.metrics.Metrics
import org.typelevel.otel4s.java.trace.Traces
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.trace.TracerProvider
import org.typelevel.vault.Vault

object OtelJava {

  /** Creates an [[org.typelevel.otel4s.Otel4s]] from a Java OpenTelemetry
    * instance.
    *
    * @param jOtel
    *   A Java OpenTelemetry instance. It is the caller's responsibility to shut
    *   this down. Failure to do so may result in lost metrics and traces.
    *
    * @return
    *   An effect of an [[org.typelevel.otel4s.Otel4s]] resource.
    */
  def forAsync[F[_]: LiftIO: Async](jOtel: JOpenTelemetry): F[Otel4s[F]] =
    IOLocal(Vault.empty)
      .map { implicit ioLocal: IOLocal[Vault] =>
        local[F](jOtel)
      }
      .to[F]

  def local[F[_]](
      jOtel: JOpenTelemetry
  )(implicit F: Async[F], L: Local[F, Vault]): Otel4s[F] = {
    val contextPropagators = new ContextPropagatorsImpl[F](
      jOtel.getPropagators,
      ContextConversions.toJContext,
      ContextConversions.fromJContext
    )

    val metrics = Metrics.forAsync(jOtel)
    val traces = Traces.local(jOtel, contextPropagators)
    new Otel4s[F] {
      def propagators: ContextPropagators[F] = contextPropagators
      def meterProvider: MeterProvider[F] = metrics.meterProvider
      def tracerProvider: TracerProvider[F] = traces.tracerProvider

      override def toString: String = jOtel.toString()
    }
  }

  /** Lifts the acquisition of a Java OpenTelemetrySdk instance to a Resource.
    *
    * @param acquire
    *   OpenTelemetrySdk resource
    *
    * @return
    *   An [[org.typelevel.otel4s.Otel4s]] resource.
    */
  def resource[F[_]: LiftIO: Async](
      acquire: F[JOpenTelemetrySdk]
  ): Resource[F, Otel4s[F]] =
    Resource
      .make(acquire)(sdk =>
        asyncFromCompletableResultCode(Sync[F].delay(sdk.shutdown()))
      )
      .evalMap(forAsync[F])

  /** Creates an [[org.typelevel.otel4s.Otel4s]] from the global Java
    * OpenTelemetry instance.
    */
  def global[F[_]: LiftIO: Async]: F[Otel4s[F]] =
    Sync[F].delay(GlobalOpenTelemetry.get).flatMap(forAsync[F])
}
