/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.trace.scalacheck

import org.scalacheck.Arbitrary
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.sdk.trace.samplers.SamplingDecision
import org.typelevel.otel4s.sdk.trace.samplers.SamplingResult

trait Arbitraries
    extends org.typelevel.otel4s.sdk.scalacheck.Arbitraries
    with org.typelevel.otel4s.trace.scalacheck.Arbitraries {

  implicit val samplingDecisionArbitrary: Arbitrary[SamplingDecision] =
    Arbitrary(Gens.samplingDecision)

  implicit val samplingResultArbitrary: Arbitrary[SamplingResult] =
    Arbitrary(Gens.samplingResult)

  implicit val eventDataArbitrary: Arbitrary[EventData] =
    Arbitrary(Gens.eventData)

  implicit val linkDataArbitrary: Arbitrary[LinkData] =
    Arbitrary(Gens.linkData)

  implicit val statusDataArbitrary: Arbitrary[StatusData] =
    Arbitrary(Gens.statusData)

  implicit val spanDataArbitrary: Arbitrary[SpanData] =
    Arbitrary(Gens.spanData)

}

object Arbitraries extends Arbitraries
