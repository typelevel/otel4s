/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.sdk.logs.scalacheck

import org.scalacheck.Gen
import org.typelevel.otel4s.sdk.logs.data.LogRecordData

trait Gens extends org.typelevel.otel4s.sdk.scalacheck.Gens with org.typelevel.otel4s.logs.scalacheck.Gens {

  val logRecordData: Gen[LogRecordData] =
    for {
      timestamp <- Gen.option(Gens.timestamp)
      observedTimestamp <- Gens.timestamp
      traceContext <- Gen.option(Gens.traceContext)
      severity <- Gen.option(Gens.severity)
      severityText <- Gen.option(Gens.nonEmptyString)
      body <- Gen.option(Gens.anyValue)
      eventName <- Gen.option(Gens.nonEmptyString)
      attributes <- Gens.limitedAttributes
      instrumentationScope <- Gens.instrumentationScope
      resource <- Gens.telemetryResource
    } yield LogRecordData(
      timestamp = timestamp,
      observedTimestamp = observedTimestamp,
      traceContext = traceContext,
      severity = severity,
      severityText = severityText,
      body = body,
      eventName = eventName,
      attributes = attributes,
      instrumentationScope = instrumentationScope,
      resource = resource
    )

}

object Gens extends Gens
