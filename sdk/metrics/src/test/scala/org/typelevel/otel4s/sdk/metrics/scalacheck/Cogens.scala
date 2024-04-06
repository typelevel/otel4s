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
import org.typelevel.otel4s.metrics.BucketBoundaries
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.view.InstrumentSelector

import scala.concurrent.duration.FiniteDuration

trait Cogens extends org.typelevel.otel4s.sdk.scalacheck.Cogens {

  implicit val ciStringCogen: Cogen[CIString] =
    Cogen[String].contramap(_.toString)

  implicit val aggregationTemporalityCogen: Cogen[AggregationTemporality] =
    Cogen[String].contramap(_.toString)

  implicit val boundariesCogen: Cogen[BucketBoundaries] =
    Cogen[Vector[Double]].contramap(_.boundaries)

  implicit val instrumentTypeCogen: Cogen[InstrumentType] =
    Cogen[String].contramap(_.toString)

  implicit val instrumentDescriptorCogen: Cogen[InstrumentDescriptor] =
    Cogen[(CIString, Option[String], Option[String], InstrumentType)]
      .contramap(d => (d.name, d.description, d.unit, d.instrumentType))

  implicit val instrumentSelectorCogen: Cogen[InstrumentSelector] =
    Cogen[
      (
          Option[InstrumentType],
          Option[String],
          Option[String],
          Option[String],
          Option[String],
          Option[String]
      )
    ].contramap { selector =>
      (
        selector.instrumentType,
        selector.instrumentName,
        selector.instrumentUnit,
        selector.meterName,
        selector.meterVersion,
        selector.meterSchemaUrl
      )
    }

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

  implicit val numberPointDataCogen: Cogen[PointData.NumberPoint] =
    Cogen[(TimeWindow, Attributes, Vector[ExemplarData], Either[Long, Double])]
      .contramap { p =>
        val value = p match {
          case long: PointData.LongNumber     => Left(long.value)
          case double: PointData.DoubleNumber => Right(double.value)
        }

        (p.timeWindow, p.attributes, p.exemplars: Vector[ExemplarData], value)
      }

  implicit val histogramPointDataCogen: Cogen[PointData.Histogram] = {
    implicit val statsCogen: Cogen[PointData.Histogram.Stats] =
      Cogen[(Double, Double, Double, Long)].contramap { s =>
        (s.sum, s.min, s.max, s.count)
      }

    Cogen[
      (
          TimeWindow,
          Attributes,
          Vector[ExemplarData],
          Option[PointData.Histogram.Stats],
          BucketBoundaries,
          Vector[Long]
      )
    ].contramap { h =>
      (
        h.timeWindow,
        h.attributes,
        h.exemplars: Vector[ExemplarData],
        h.stats,
        h.boundaries,
        h.counts
      )
    }
  }

  implicit val pointDataCogen: Cogen[PointData] =
    Cogen { (seed, pointData) =>
      pointData match {
        case point: PointData.NumberPoint =>
          numberPointDataCogen.perturb(seed, point)
        case histogram: PointData.Histogram =>
          histogramPointDataCogen.perturb(seed, histogram)
      }
    }
}

object Cogens extends Cogens
