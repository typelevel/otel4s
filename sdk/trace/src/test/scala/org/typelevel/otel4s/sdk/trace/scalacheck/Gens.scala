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

import org.scalacheck.Gen
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.sdk.trace.samplers.SamplingDecision
import org.typelevel.otel4s.sdk.trace.samplers.SamplingResult

trait Gens
    extends org.typelevel.otel4s.sdk.scalacheck.Gens
    with org.typelevel.otel4s.trace.scalacheck.Gens {

  val samplingDecision: Gen[SamplingDecision] =
    Gen.oneOf(
      SamplingDecision.Drop,
      SamplingDecision.RecordOnly,
      SamplingDecision.RecordAndSample
    )

  val samplingResult: Gen[SamplingResult] =
    for {
      decision <- Gens.samplingDecision
      attributes <- Gens.attributes
    } yield SamplingResult(decision, attributes)

  val eventData: Gen[EventData] =
    for {
      name <- Gen.alphaNumStr
      epoch <- Gen.finiteDuration
      attributes <- Gens.attributes
    } yield EventData(name, epoch, attributes)

  val linkData: Gen[LinkData] =
    for {
      spanContext <- Gens.spanContext
      attributes <- Gens.attributes
    } yield LinkData(spanContext, attributes)

  val statusData: Gen[StatusData] =
    for {
      description <- Gen.option(Gen.alphaNumStr)
      data <- Gen.oneOf(
        StatusData.Ok,
        StatusData.Unset,
        StatusData.Error(description)
      )
    } yield data

  val spanData: Gen[SpanData] =
    for {
      name <- Gen.alphaStr
      spanContext <- Gens.spanContext
      parentSpanContext <- Gen.option(Gens.spanContext)
      kind <- Gens.spanKind
      startEpochNanos <- Gen.finiteDuration
      endEpochNanos <- Gen.option(Gen.finiteDuration)
      status <- Gens.statusData
      attributes <- Gens.attributes
      events <- Gen.listOf(Gens.eventData)
      links <- Gen.listOf(Gens.linkData)
      instrumentationScope <- Gens.instrumentationScope
      resource <- Gens.telemetryResource
    } yield SpanData(
      name,
      spanContext,
      parentSpanContext,
      kind,
      startEpochNanos,
      endEpochNanos,
      status,
      attributes,
      events.toVector,
      links.toVector,
      instrumentationScope,
      resource
    )

}

object Gens extends Gens
