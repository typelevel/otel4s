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

import org.scalacheck.Cogen
import org.typelevel.otel4s.AnyValue
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.logs.Severity
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.TraceContext
import org.typelevel.otel4s.sdk.logs.data.LogRecordData

import scala.concurrent.duration.FiniteDuration

trait Cogens extends org.typelevel.otel4s.sdk.scalacheck.Cogens with org.typelevel.otel4s.logs.scalacheck.Cogens {

  implicit val logRecordDataCogen: Cogen[LogRecordData] =
    Cogen[
      (
          Option[FiniteDuration],
          FiniteDuration,
          Option[TraceContext],
          Option[Severity],
          Option[String],
          Option[AnyValue],
          Option[String],
          Attributes,
          InstrumentationScope,
          TelemetryResource
      )
    ].contramap { logRecordData =>
      (
        logRecordData.timestamp,
        logRecordData.observedTimestamp,
        logRecordData.traceContext,
        logRecordData.severity,
        logRecordData.severityText,
        logRecordData.body,
        logRecordData.eventName,
        logRecordData.attributes.elements,
        logRecordData.instrumentationScope,
        logRecordData.resource
      )
    }

}

object Cogens extends Cogens
