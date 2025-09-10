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

package org.typelevel.otel4s.oteljava.metrics

import cats.effect.Async
import cats.mtl.Ask
import cats.syntax.functor._
import io.opentelemetry.api.{OpenTelemetry => JOpenTelemetry}
import io.opentelemetry.api.GlobalOpenTelemetry
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.context.AskContext
import org.typelevel.otel4s.oteljava.context.Context

/** The configured metrics module.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
sealed trait Metrics[F[_]] {

  /** The [[org.typelevel.otel4s.metrics.MeterProvider MeterProvider]].
    */
  def meterProvider: MeterProvider[F]
}

object Metrics {

  /** Creates a [[org.typelevel.otel4s.oteljava.metrics.Metrics]] from the global Java OpenTelemetry instance.
    *
    * @note
    *   the created module is isolated and exemplars won't be collected. Use `OtelJava` if you need to capture
    *   exemplars.
    */
  def global[F[_]: Async]: F[Metrics[F]] =
    Async[F].delay(GlobalOpenTelemetry.get).map(fromJOpenTelemetry[F])

  /** Creates a [[org.typelevel.otel4s.oteljava.metrics.Metrics]] from a Java OpenTelemetry instance.
    *
    * @note
    *   the created module is isolated and exemplars won't be collected. Use `OtelJava` if you need to capture
    *   exemplars.
    *
    * @param jOtel
    *   A Java OpenTelemetry instance. It is the caller's responsibility to shut this down. Failure to do so may result
    *   in lost metrics and traces.
    *
    * @note
    *   this implementation uses a constant root `Context` via `Ask.const(Context.root)`. That means the module is
    *   isolated: it does not inherit or propagate the surrounding span context. This is useful if you only need logging
    *   (without traces or metrics) and want the module to operate independently. If instead you want interoperability -
    *   i.e. to capture the current span context so that logs, traces, and metrics can all work together - use
    *   `OtelJava.fromJOpenTelemetry`.
    */
  def fromJOpenTelemetry[F[_]: Async](jOtel: JOpenTelemetry): Metrics[F] = {
    implicit val askContext: AskContext[F] = Ask.const(Context.root)
    create(jOtel)
  }

  private[oteljava] def create[F[_]: Async: AskContext](jOtel: JOpenTelemetry): Metrics[F] =
    new Impl(new MeterProviderImpl(jOtel.getMeterProvider))

  private final class Impl[F[_]](val meterProvider: MeterProvider[F]) extends Metrics[F] {
    override def toString: String = s"Metrics{meterProvider=$meterProvider}"
  }

}
