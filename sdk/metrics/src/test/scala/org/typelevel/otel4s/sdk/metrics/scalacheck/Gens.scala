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
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import scodec.bits.ByteVector

import scala.concurrent.duration._

trait Gens extends org.typelevel.otel4s.sdk.scalacheck.Gens {

  val aggregationTemporality: Gen[AggregationTemporality] =
    Gen.oneOf(AggregationTemporality.Delta, AggregationTemporality.Cumulative)

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

}

object Gens extends Gens
