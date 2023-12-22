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
package oteljava
package metrics

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.syntax.all._
import io.opentelemetry.api.metrics.{Meter => JMeter}
import io.opentelemetry.api.metrics.ObservableLongMeasurement
import org.typelevel.otel4s.metrics._

private[oteljava] case class ObservableCounterBuilderImpl[F[_]](
    jMeter: JMeter,
    name: String,
    unit: Option[String] = None,
    description: Option[String] = None
)(implicit F: Async[F])
    extends ObservableInstrumentBuilder[F, Long, ObservableCounter] {

  type Self = ObservableCounterBuilderImpl[F]

  def withUnit(unit: String): Self = copy(unit = Option(unit))
  def withDescription(description: String): Self =
    copy(description = Option(description))

  private def createInternal(
      cb: ObservableLongMeasurement => F[Unit]
  ): Resource[F, ObservableCounter] =
    Dispatcher.sequential.flatMap(dispatcher =>
      Resource
        .fromAutoCloseable(F.delay {
          val b = jMeter.counterBuilder(name)
          unit.foreach(b.setUnit)
          description.foreach(b.setDescription)
          b.buildWithCallback { olm =>
            dispatcher.unsafeRunSync(cb(olm))
          }
        })
        .as(new ObservableCounter {})
    )

  def createWithCallback(
      cb: ObservableMeasurement[F, Long] => F[Unit]
  ): Resource[F, ObservableCounter] =
    createInternal(olm => cb(new ObservableLongImpl(olm)))

  def create(
      measurements: F[List[Measurement[Long]]]
  ): Resource[F, ObservableCounter] =
    createInternal(olm =>
      measurements.flatMap(ms =>
        F.delay(
          ms.foreach(m =>
            olm.record(m.value, Conversions.toJAttributes(m.attributes))
          )
        )
      )
    )
}
