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

package org.typelevel.otel4s.sdk.metrics.data

import cats.Hash
import cats.Show

/** The time period over which the measurements are aggregated.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/data-model/#temporality]]
  */
sealed trait AggregationTemporality

object AggregationTemporality {

  /** Measurements are aggregated since the previous collection.
    */
  case object Delta extends AggregationTemporality

  /** Measurements are aggregated over the lifetime of the instrument.
    */
  case object Cumulative extends AggregationTemporality

  implicit val aggregationTemporalityHash: Hash[AggregationTemporality] =
    Hash.fromUniversalHashCode

  implicit val aggregationTemporalityShow: Show[AggregationTemporality] =
    Show.fromToString
}
