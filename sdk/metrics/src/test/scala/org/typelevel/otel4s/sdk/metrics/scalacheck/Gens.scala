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

import org.scalacheck.Gen
import org.typelevel.otel4s.metrics.BucketBoundaries
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import scodec.bits.ByteVector

import scala.concurrent.duration._

trait Gens extends org.typelevel.otel4s.sdk.scalacheck.Gens {

  val aggregationTemporality: Gen[AggregationTemporality] =
    Gen.oneOf(AggregationTemporality.Delta, AggregationTemporality.Cumulative)

  val bucketBoundaries: Gen[BucketBoundaries] =
    for {
      size <- Gen.choose(0, 20)
      b <- Gen.containerOfN[Vector, Double](size, Gen.choose(-100.0, 100.0))
    } yield BucketBoundaries(b.distinct.sorted)

  val timeWindow: Gen[TimeWindow] =
    for {
      start <- Gen.chooseNum(1L, Long.MaxValue - 5)
      end <- Gen.chooseNum(start, Long.MaxValue)
    } yield TimeWindow(start.nanos, end.nanos)

  val traceContext: Gen[ExemplarData.TraceContext] =
    for {
      traceId <- Gen.stringOfN(16, Gen.hexChar)
      spanId <- Gen.stringOfN(8, Gen.hexChar)
    } yield ExemplarData.TraceContext(
      ByteVector.fromValidHex(traceId),
      ByteVector.fromValidHex(spanId)
    )

  val longExemplarData: Gen[ExemplarData.LongExemplar] =
    for {
      attributes <- Gens.attributes
      timestamp <- Gen.finiteDuration
      traceContext <- Gen.option(Gens.traceContext)
      value <- Gen.long
    } yield ExemplarData.long(attributes, timestamp, traceContext, value)

  val doubleExemplarData: Gen[ExemplarData.DoubleExemplar] =
    for {
      attributes <- Gens.attributes
      timestamp <- Gen.finiteDuration
      traceContext <- Gen.option(Gens.traceContext)
      value <- Gen.double
    } yield ExemplarData.double(attributes, timestamp, traceContext, value)

  val exemplarData: Gen[ExemplarData] =
    Gen.oneOf(longExemplarData, doubleExemplarData)

  val longNumberPointData: Gen[PointData.LongNumber] =
    for {
      window <- Gens.timeWindow
      attributes <- Gens.attributes
      exemplars <- Gen.listOf(Gens.longExemplarData)
      value <- Gen.long
    } yield PointData.longNumber(window, attributes, exemplars.toVector, value)

  val doubleNumberPointData: Gen[PointData.DoubleNumber] =
    for {
      window <- Gens.timeWindow
      attributes <- Gens.attributes
      exemplars <- Gen.listOf(Gens.doubleExemplarData)
      value <- Gen.double
    } yield PointData.doubleNumber(
      window,
      attributes,
      exemplars.toVector,
      value
    )

  val histogramPointData: Gen[PointData.Histogram] = {
    val statsGen =
      for {
        sum <- Gen.double
        min <- Gen.double
        max <- Gen.double
        count <- Gen.long
      } yield PointData.Histogram.Stats(sum, min, max, count)

    for {
      window <- Gens.timeWindow
      attributes <- Gens.attributes
      exemplars <- Gen.listOf(Gens.doubleExemplarData)
      stats <- Gen.option(statsGen)
      boundaries <- Gens.bucketBoundaries
      counts <- Gen.listOfN(
        boundaries.length,
        if (stats.isEmpty) Gen.const(0L) else Gen.choose(0L, Long.MaxValue)
      )
    } yield PointData.histogram(
      window,
      attributes,
      exemplars.toVector,
      stats,
      boundaries,
      counts.toVector
    )
  }

  val pointDataNumber: Gen[PointData.NumberPoint] =
    Gen.oneOf(longNumberPointData, doubleNumberPointData)

  val pointData: Gen[PointData] =
    Gen.oneOf(longNumberPointData, doubleNumberPointData, histogramPointData)

}

object Gens extends Gens
