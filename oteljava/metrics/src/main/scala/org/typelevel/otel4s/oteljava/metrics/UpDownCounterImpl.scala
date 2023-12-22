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

import cats.effect.kernel.Sync
import io.opentelemetry.api.metrics.{LongUpDownCounter => JLongUpDownCounter}
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.metrics._

private[oteljava] class UpDownCounterImpl[F[_]](
    counter: JLongUpDownCounter
)(implicit F: Sync[F])
    extends UpDownCounter[F, Long] {

  val backend: UpDownCounter.Backend[F, Long] =
    new UpDownCounter.LongBackend[F] {
      val meta: InstrumentMeta[F] = InstrumentMeta.enabled

      def add(value: Long, attributes: Attribute[_]*): F[Unit] =
        F.delay(counter.add(value, Conversions.toJAttributes(attributes)))

    }
}
