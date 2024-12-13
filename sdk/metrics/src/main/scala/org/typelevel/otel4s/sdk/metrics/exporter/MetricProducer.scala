/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.metrics.exporter

import org.typelevel.otel4s.sdk.metrics.data.MetricData

/** Represents an export state of a MetricReader.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/sdk/#metricproducer]]
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
trait MetricProducer[F[_]] {

  /** Produces metrics by collecting them from the SDK. If there are asynchronous instruments involved, their callback
    * functions will be evaluated.
    */
  def produce: F[Vector[MetricData]]
}
