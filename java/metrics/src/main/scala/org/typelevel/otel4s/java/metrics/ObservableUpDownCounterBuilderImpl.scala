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
package java
package metrics

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.syntax.all._
import io.opentelemetry.api.metrics.{Meter => JMeter}
import org.typelevel.otel4s.metrics._

private[java] case class ObservableUpDownCounterBuilderImpl[F[_]](
    jMeter: JMeter,
    name: String,
    unit: Option[String] = None,
    description: Option[String] = None
)(implicit F: Async[F])
    extends ObservableInstrumentBuilder[F, Long, ObservableUpDownCounter] {

  type Self = ObservableUpDownCounterBuilderImpl[F]

  def withUnit(unit: String): Self = copy(unit = Option(unit))
  def withDescription(description: String): Self =
    copy(description = Option(description))

  def createWithCallback(
      cb: ObservableMeasurement[F, Long] => F[Unit]
  ): Resource[F, ObservableUpDownCounter] =
    Dispatcher.sequential.flatMap(dispatcher =>
      Resource
        .fromAutoCloseable(F.delay {
          val b = jMeter.upDownCounterBuilder(name)
          unit.foreach(b.setUnit)
          description.foreach(b.setDescription)
          b.buildWithCallback { gauge =>
            dispatcher.unsafeRunSync(cb(new ObservableLongImpl(gauge)))
          }

        })
        .as(new ObservableUpDownCounter {})
    )
  def create(
      measurements: F[List[Measurement[Long]]]
  ): Resource[F, ObservableUpDownCounter] =
    Dispatcher.sequential.flatMap(dispatcher =>
      Resource
        .fromAutoCloseable(F.delay {
          val b = jMeter.upDownCounterBuilder(name)
          unit.foreach(b.setUnit)
          description.foreach(b.setDescription)
          b.buildWithCallback { gauge =>
            dispatcher.unsafeRunSync(
              measurements.flatMap(ms =>
                F.delay(
                  ms.foreach(m =>
                    gauge
                      .record(m.value, Conversions.toJAttributes(m.attributes))
                  )
                )
              )
            )
          }
        })
        .as(new ObservableUpDownCounter {})
    )

}
