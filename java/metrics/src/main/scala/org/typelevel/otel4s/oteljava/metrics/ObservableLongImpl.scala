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

import cats.effect.Sync
import io.opentelemetry.api.metrics.ObservableLongMeasurement
import org.typelevel.otel4s.metrics._

private[oteljava] class ObservableLongImpl[F[_]](
    private val olm: ObservableLongMeasurement
)(implicit F: Sync[F])
    extends ObservableMeasurement[F, Long] {

  def record(value: Long, attributes: Attribute[_]*): F[Unit] =
    F.delay(olm.record(value, Conversions.toJAttributes(attributes)))

}
