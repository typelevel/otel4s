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

package org.typelevel.otel4s.sdk.metrics.scalacheck

import org.scalacheck.Cogen
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow

import scala.concurrent.duration.FiniteDuration

trait Cogens extends org.typelevel.otel4s.sdk.scalacheck.Cogens {

  implicit val aggregationTemporalityCogen: Cogen[AggregationTemporality] =
    Cogen[String].contramap(_.toString)

  implicit val timeWindowCogen: Cogen[TimeWindow] =
    Cogen[(FiniteDuration, FiniteDuration)].contramap(w => (w.start, w.end))

}

object Cogens extends Cogens
