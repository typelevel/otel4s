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

package org.typelevel.otel4s.sdk.metrics
package scalacheck

import org.scalacheck.Cogen
import org.typelevel.ci.CIString
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor

import scala.concurrent.duration.FiniteDuration

trait Cogens extends org.typelevel.otel4s.sdk.scalacheck.Cogens {

  implicit val ciStringCogen: Cogen[CIString] =
    Cogen[String].contramap(_.toString)

  implicit val aggregationTemporalityCogen: Cogen[AggregationTemporality] =
    Cogen[String].contramap(_.toString)

  implicit val instrumentTypeCogen: Cogen[InstrumentType] =
    Cogen[String].contramap(_.toString)

  implicit val instrumentDescriptorCogen: Cogen[InstrumentDescriptor] =
    Cogen[(CIString, Option[String], Option[String], InstrumentType)]
      .contramap(d => (d.name, d.description, d.unit, d.instrumentType))

  implicit val timeWindowCogen: Cogen[TimeWindow] =
    Cogen[(FiniteDuration, FiniteDuration)].contramap(w => (w.start, w.end))

  implicit val traceContextCogen: Cogen[ExemplarData.TraceContext] =
    Cogen[(String, String)].contramap(c => (c.traceId.toHex, c.spanId.toHex))

  implicit val exemplarDataCogen: Cogen[ExemplarData] =
    Cogen[
      (
          Attributes,
          FiniteDuration,
          Option[ExemplarData.TraceContext],
          Either[Long, Double]
      )
    ].contramap { e =>
      val value = e match {
        case long: ExemplarData.LongExemplar     => Left(long.value)
        case double: ExemplarData.DoubleExemplar => Right(double.value)
      }

      (e.filteredAttributes, e.timestamp, e.traceContext, value)
    }

}

object Cogens extends Cogens
