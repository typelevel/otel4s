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

import org.scalacheck.Arbitrary
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor

trait Arbitraries extends org.typelevel.otel4s.sdk.scalacheck.Arbitraries {

  implicit val aggregationTemporalityArb: Arbitrary[AggregationTemporality] =
    Arbitrary(Gens.aggregationTemporality)

  implicit val instrumentDescriptorArbitrary: Arbitrary[InstrumentDescriptor] =
    Arbitrary(Gens.instrumentDescriptor)

  implicit val timeWindowArbitrary: Arbitrary[TimeWindow] =
    Arbitrary(Gens.timeWindow)

  implicit val traceContextArbitrary: Arbitrary[ExemplarData.TraceContext] =
    Arbitrary(Gens.traceContext)

  implicit val exemplarDataArbitrary: Arbitrary[ExemplarData] =
    Arbitrary(Gens.exemplarData)

  implicit val pointDataArbitrary: Arbitrary[PointData] =
    Arbitrary(Gens.pointData)

}

object Arbitraries extends Arbitraries
