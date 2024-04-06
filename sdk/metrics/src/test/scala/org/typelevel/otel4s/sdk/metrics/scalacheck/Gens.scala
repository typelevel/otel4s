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

import org.scalacheck.Gen
import org.typelevel.ci.CIString
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import scodec.bits.ByteVector

import scala.concurrent.duration._

trait Gens extends org.typelevel.otel4s.sdk.scalacheck.Gens {

  val aggregationTemporality: Gen[AggregationTemporality] =
    Gen.oneOf(AggregationTemporality.Delta, AggregationTemporality.Cumulative)

  val ciString: Gen[CIString] =
    Gens.nonEmptyString.map(CIString(_))

  val instrumentType: Gen[InstrumentType] =
    Gen.oneOf(InstrumentType.values)

  val synchronousInstrumentType: Gen[InstrumentType.Synchronous] =
    Gen.oneOf(InstrumentType.values.collect {
      case tpe: InstrumentType.Synchronous => tpe
    })

  val asynchronousInstrumentType: Gen[InstrumentType.Asynchronous] =
    Gen.oneOf(InstrumentType.values.collect {
      case tpe: InstrumentType.Asynchronous => tpe
    })

  val synchronousInstrumentDescriptor: Gen[InstrumentDescriptor] =
    for {
      tpe <- Gens.synchronousInstrumentType
      name <- Gens.ciString
      description <- Gen.option(Gen.alphaNumStr)
      unit <- Gen.option(Gen.alphaNumStr)
    } yield InstrumentDescriptor.synchronous(name, description, unit, tpe)

  val asynchronousInstrumentDescriptor: Gen[InstrumentDescriptor] =
    for {
      tpe <- Gens.asynchronousInstrumentType
      name <- Gens.ciString
      description <- Gen.option(Gen.alphaNumStr)
      unit <- Gen.option(Gen.alphaNumStr)
    } yield InstrumentDescriptor.asynchronous(name, description, unit, tpe)

  val instrumentDescriptor: Gen[InstrumentDescriptor] =
    Gen.oneOf(synchronousInstrumentDescriptor, asynchronousInstrumentDescriptor)

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
