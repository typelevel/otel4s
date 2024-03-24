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

package org.typelevel.otel4s.sdk.metrics.data

import cats.Hash
import cats.Show
import cats.syntax.foldable._
import org.typelevel.otel4s.Attributes
import scodec.bits.ByteVector

import scala.concurrent.duration.FiniteDuration

/** An exemplar is a recorded value that associates
  * [[ExemplarData.TraceContext]] (extracted from the
  * [[org.typelevel.otel4s.sdk.context.Context Context]]) to a metric event.
  *
  * It allows linking trace details with metrics.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/data-model/#exemplars]]
  */
sealed trait ExemplarData {
  type Value

  /** A set of filtered attributes that provide additional insight about the
    * measurement.
    */
  def filteredAttributes: Attributes

  /** The time of the observation.
    */
  def timestamp: FiniteDuration

  /** The trace associated with a recording.
    */
  def traceContext: Option[ExemplarData.TraceContext]

  /** The recorded value.
    */
  def value: Value

  override final def hashCode(): Int =
    Hash[ExemplarData].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: ExemplarData => Hash[ExemplarData].eqv(this, other)
      case _                   => false
    }

  override final def toString: String =
    Show[ExemplarData].show(this)
}

object ExemplarData {

  sealed trait LongExemplar extends ExemplarData { type Value = Long }

  sealed trait DoubleExemplar extends ExemplarData { type Value = Double }

  /** The trace information.
    */
  sealed trait TraceContext {
    def traceId: ByteVector
    def spanId: ByteVector

    override final def hashCode(): Int =
      Hash[TraceContext].hash(this)

    override final def equals(obj: Any): Boolean =
      obj match {
        case other: TraceContext => Hash[TraceContext].eqv(this, other)
        case _                   => false
      }

    override final def toString: String =
      Show[TraceContext].show(this)
  }

  object TraceContext {

    /** Creates a [[TraceContext]] with the given `traceId` and `spanId`.
      */
    def apply(traceId: ByteVector, spanId: ByteVector): TraceContext =
      Impl(traceId, spanId)

    implicit val traceContextShow: Show[TraceContext] =
      Show.show { c =>
        s"TraceContext{traceId=${c.traceId.toHex}, spanId=${c.spanId.toHex}}"
      }

    implicit val traceContextHash: Hash[TraceContext] = {
      implicit val byteVectorHash: Hash[ByteVector] = Hash.fromUniversalHashCode
      Hash.by(c => (c.traceId, c.spanId))
    }

    private final case class Impl(
        traceId: ByteVector,
        spanId: ByteVector
    ) extends TraceContext
  }

  /** Creates a [[LongExemplar]] with the given values.
    */
  def long(
      filteredAttributes: Attributes,
      timestamp: FiniteDuration,
      traceContext: Option[TraceContext],
      value: Long
  ): LongExemplar =
    LongExemplarImpl(filteredAttributes, timestamp, traceContext, value)

  /** Creates a [[DoubleExemplar]] with the given values.
    */
  def double(
      filteredAttributes: Attributes,
      timestamp: FiniteDuration,
      traceContext: Option[TraceContext],
      value: Double
  ): DoubleExemplar =
    DoubleExemplarImpl(filteredAttributes, timestamp, traceContext, value)

  implicit val exemplarDataHash: Hash[ExemplarData] = {
    implicit val valueHash: Hash[ExemplarData#Value] =
      Hash.fromUniversalHashCode

    Hash.by { e =>
      (
        e.filteredAttributes,
        e.timestamp,
        e.traceContext,
        e.value: ExemplarData#Value
      )
    }
  }

  implicit val exemplarDataShow: Show[ExemplarData] = {
    Show.show { e =>
      val prefix = e match {
        case _: LongExemplar   => "ExemplarData.Long"
        case _: DoubleExemplar => "ExemplarData.Double"
      }

      val traceContext = e.traceContext.foldMap(c => s", traceContext=$c")

      s"$prefix{filteredAttributes=${e.filteredAttributes}, timestamp=${e.timestamp}$traceContext, value=${e.value}}"
    }
  }

  private final case class LongExemplarImpl(
      filteredAttributes: Attributes,
      timestamp: FiniteDuration,
      traceContext: Option[TraceContext],
      value: Long
  ) extends LongExemplar

  private final case class DoubleExemplarImpl(
      filteredAttributes: Attributes,
      timestamp: FiniteDuration,
      traceContext: Option[TraceContext],
      value: Double
  ) extends DoubleExemplar

}
