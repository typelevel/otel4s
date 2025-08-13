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

package org.typelevel.otel4s.sdk.context

import cats.Hash
import cats.Show
import scodec.bits.ByteVector

/** The trace information.
  *
  * [[TraceContext]] is a minimal version of SpanContext. That way, `sdk-metrics` and `sdk-logs` do not need to depend
  * on the `core-trace` or `sdk-trace`.
  */
sealed trait TraceContext {

  /** The trace identifier - a 16-byte array that uniquely identifies a trace.
    */
  def traceId: ByteVector

  /** The span identifier - an 8-byte array that uniquely identifies a span within a trace.
    */
  def spanId: ByteVector

  /** Returns whether this trace is sampled.
    */
  def isSampled: Boolean

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

  /** Provides a way to extract [[TraceContext]] from a [[Context]].
    */
  sealed trait Lookup {

    /** Retrieves a [[TraceContext]] from the given context.
      *
      * @param context
      *   the context to extract trace information from
      */
    def get(context: Context): Option[TraceContext]
  }

  object Lookup {
    private[otel4s] trait Unsealed extends Lookup

    /** Returns a no-op [[Lookup]] implementation that always returns `None`.
      */
    def noop: Lookup = Noop

    private[otel4s] object Noop extends Lookup {
      def get(context: Context): Option[TraceContext] = None
    }
  }

  /** Creates a [[TraceContext]] with the given `traceId`, `spanId`, and a sampling flag.
    *
    * @param traceId
    *   the trace identifier
    *
    * @param spanId
    *   the span identifier
    *
    * @param sampled
    *   whether the trace is sampled
    */
  def apply(
      traceId: ByteVector,
      spanId: ByteVector,
      sampled: Boolean
  ): TraceContext =
    Impl(traceId, spanId, sampled)

  implicit val traceContextShow: Show[TraceContext] =
    Show.show { c =>
      s"TraceContext{traceId=${c.traceId.toHex}, spanId=${c.spanId.toHex}, isSampled=${c.isSampled}}"
    }

  implicit val traceContextHash: Hash[TraceContext] = {
    implicit val byteVectorHash: Hash[ByteVector] = Hash.fromUniversalHashCode
    Hash.by(c => (c.traceId, c.spanId, c.isSampled))
  }

  private final case class Impl(
      traceId: ByteVector,
      spanId: ByteVector,
      isSampled: Boolean
  ) extends TraceContext
}
