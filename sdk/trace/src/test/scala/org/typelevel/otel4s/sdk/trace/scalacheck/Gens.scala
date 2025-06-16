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
import org.typelevel.otel4s.sdk.data.LimitedData
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.sdk.trace.samplers.SamplingDecision
import org.typelevel.otel4s.sdk.trace.samplers.SamplingResult

trait Gens extends org.typelevel.otel4s.sdk.scalacheck.Gens with org.typelevel.otel4s.trace.scalacheck.Gens {

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
      name <- Gens.nonEmptyString
      epoch <- Gens.timestamp
      attributes <- Gens.limitedAttributes
    } yield EventData(name, epoch, attributes)

  val linkData: Gen[LinkData] =
    for {
      spanContext <- Gens.spanContext
      attributes <- Gens.limitedAttributes
    } yield LinkData(spanContext, attributes)

  val statusData: Gen[StatusData] =
    for {
      description <- Gen.option(Gens.nonEmptyString)
      data <- Gen.oneOf(
        StatusData.Ok,
        StatusData.Unset,
        StatusData.Error(description)
      )
    } yield data

  val limitedEvents: Gen[LimitedData[EventData, Vector[EventData]]] =
    for {
      events <- Gens.nonEmptyVector(Gens.eventData)
      extraEvents <- Gens.nonEmptyVector(Gens.eventData)
    } yield LimitedData
      .vector[EventData](events.length)
      .appendAll((events ++: extraEvents).toVector)

  val limitedLinks: Gen[LimitedData[LinkData, Vector[LinkData]]] =
    for {
      links <- Gens.nonEmptyVector(Gens.linkData)
      extraLinks <- Gens.nonEmptyVector(Gens.linkData)
    } yield LimitedData
      .vector[LinkData](links.length)
      .appendAll((links ++: extraLinks).toVector)

  val spanData: Gen[SpanData] =
    for {
      name <- Gens.nonEmptyString
      spanContext <- Gens.spanContext
      parentSpanContext <- Gen.option(Gens.spanContext)
      kind <- Gens.spanKind
      startTimestamp <- Gens.timestamp
      endTimestamp <- Gen.option(Gens.timestamp)
      status <- Gens.statusData
      attributes <- Gens.limitedAttributes
      events <- Gens.limitedEvents
      links <- Gens.limitedLinks
      instrumentationScope <- Gens.instrumentationScope
      resource <- Gens.telemetryResource
    } yield SpanData(
      name,
      spanContext,
      parentSpanContext,
      kind,
      startTimestamp,
      endTimestamp,
      status,
      attributes,
      events,
      links,
      instrumentationScope,
      resource
    )

}

object Gens extends Gens
