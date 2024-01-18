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

import org.scalacheck.Cogen
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.sdk.trace.samplers.SamplingDecision
import org.typelevel.otel4s.sdk.trace.samplers.SamplingResult
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.Status

import scala.concurrent.duration.FiniteDuration

trait Cogens
    extends org.typelevel.otel4s.sdk.scalacheck.Cogens
    with org.typelevel.otel4s.trace.scalacheck.Cogens {

  implicit val samplingDecisionCogen: Cogen[SamplingDecision] =
    Cogen[String].contramap(_.toString)

  implicit val samplingResultCogen: Cogen[SamplingResult] =
    Cogen[(SamplingDecision, Attributes)].contramap { result =>
      (result.decision, result.attributes)
    }

  implicit val eventDataCogen: Cogen[EventData] =
    Cogen[(String, FiniteDuration, Attributes)].contramap { data =>
      (data.name, data.timestamp, data.attributes)
    }

  implicit val linkDataCogen: Cogen[LinkData] =
    Cogen[(SpanContext, Attributes)].contramap { data =>
      (data.spanContext, data.attributes)
    }

  implicit val statusDataCogen: Cogen[StatusData] =
    Cogen[(Status, Option[String])].contramap { data =>
      (data.status, data.description)
    }

  implicit val spanDataCogen: Cogen[SpanData] = Cogen[
    (
        String,
        SpanContext,
        Option[SpanContext],
        SpanKind,
        FiniteDuration,
        Option[FiniteDuration],
        StatusData,
        Attributes,
        Vector[EventData],
        Vector[LinkData],
        InstrumentationScope,
        TelemetryResource
    )
  ].contramap { spanData =>
    (
      spanData.name,
      spanData.spanContext,
      spanData.parentSpanContext,
      spanData.kind,
      spanData.startTimestamp,
      spanData.endTimestamp,
      spanData.status,
      spanData.attributes,
      spanData.events,
      spanData.links,
      spanData.instrumentationScope,
      spanData.resource
    )
  }

}

object Cogens extends Cogens
