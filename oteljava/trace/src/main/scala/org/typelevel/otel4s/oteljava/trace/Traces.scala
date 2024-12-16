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

package org.typelevel.otel4s
package oteljava.trace

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.opentelemetry.api.{OpenTelemetry => JOpenTelemetry}
import io.opentelemetry.api.GlobalOpenTelemetry
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.context.LocalContext
import org.typelevel.otel4s.oteljava.context.LocalContextProvider
import org.typelevel.otel4s.oteljava.context.propagation.PropagatorConverters._
import org.typelevel.otel4s.trace.TracerProvider

/** The configured tracing module.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
sealed trait Traces[F[_]] {

  /** The [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]].
    */
  def tracerProvider: TracerProvider[F]

  /** The propagators used by the [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]].
    */
  def propagators: ContextPropagators[Context]

  /** The [[org.typelevel.otel4s.oteljava.context.LocalContext LocalContext]] used by the
    * [[org.typelevel.otel4s.trace.TracerProvider TracerProvider]].
    */
  def localContext: LocalContext[F]

}

object Traces {

  /** Creates a [[org.typelevel.otel4s.oteljava.trace.Traces]] from the global Java OpenTelemetry instance.
    */
  def global[F[_]: Sync: LocalContextProvider]: F[Traces[F]] =
    Sync[F].delay(GlobalOpenTelemetry.get).flatMap(fromJOpenTelemetry[F])

  /** Creates a [[org.typelevel.otel4s.oteljava.trace.Traces]] from a Java OpenTelemetry instance.
    *
    * @param jOtel
    *   A Java OpenTelemetry instance. It is the caller's responsibility to shut this down. Failure to do so may result
    *   in lost metrics and traces.
    */
  def fromJOpenTelemetry[F[_]: Sync: LocalContextProvider](jOtel: JOpenTelemetry): F[Traces[F]] =
    LocalProvider[F, Context].local.map(implicit l => create[F](jOtel))

  /** Creates a [[org.typelevel.otel4s.oteljava.trace.Traces]] from a Java OpenTelemetry instance using the given
    * `Local` instance.
    *
    * @param jOtel
    *   A Java OpenTelemetry instance. It is the caller's responsibility to shut this down. Failure to do so may result
    *   in lost metrics and traces.
    */
  private[oteljava] def create[F[_]: Sync: LocalContext](jOtel: JOpenTelemetry): Traces[F] = {
    val propagators = jOtel.getPropagators.asScala
    val provider = TracerProviderImpl.local(
      jOtel.getTracerProvider,
      propagators,
      TraceScopeImpl.fromLocal[F]
    )
    new Impl(provider, propagators, implicitly)
  }

  private final class Impl[F[_]](
      val tracerProvider: TracerProvider[F],
      val propagators: ContextPropagators[Context],
      val localContext: LocalContext[F]
  ) extends Traces[F] {
    override def toString: String = s"Traces{tracerProvider=$tracerProvider, propagators=$propagators}"
  }
}
