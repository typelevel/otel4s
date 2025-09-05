/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s

import cats.Hash
import cats.Show
import org.typelevel.scalaccompat.annotation.threadUnsafe3

import java.nio.ByteBuffer
import java.util.Base64

/** Represents `any` type.
  *
  * It can be a primitive value such as a string or integer, or it may contain an arbitrary nested object containing
  * arrays, key-value lists, and primitives.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/data-model/#type-any]]
  *
  * @see
  *   [[https://github.com/open-telemetry/opentelemetry-proto/blob/v1.5.0/opentelemetry/proto/common/v1/common.proto#L28-L40]]
  */
sealed trait AnyValue {

  @threadUnsafe3
  override final lazy val hashCode: Int =
    Hash[AnyValue].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: AnyValue => Hash[AnyValue].eqv(this, other)
      case _               => false
    }

  override final def toString: String =
    Show[AnyValue].show(this)

}

object AnyValue {

  sealed trait StringValue extends AnyValue {
    def value: String
  }

  sealed trait BooleanValue extends AnyValue {
    def value: Boolean
  }

  sealed trait LongValue extends AnyValue {
    def value: Long
  }

  sealed trait DoubleValue extends AnyValue {
    def value: Double
  }

  sealed trait ByteArrayValue extends AnyValue {

    /** Returns read-only ByteBuffer.
      */
    def value: ByteBuffer
  }

  sealed trait SeqValue extends AnyValue {
    def value: Seq[AnyValue]
  }

  sealed trait MapValue extends AnyValue {
    def value: Map[String, AnyValue]
  }

  sealed trait EmptyValue extends AnyValue

  /** Creates an [[AnyValue]] from the given `String`.
    */
  def string(value: String): StringValue =
    StringValueImpl(value)

  /** Creates an [[AnyValue]] from the given `Boolean`.
    */
  def boolean(value: Boolean): BooleanValue =
    BooleanValueImpl(value)

  /** Creates an [[AnyValue]] from the given `Long`.
    */
  def long(value: Long): LongValue =
    LongValueImpl(value)

  /** Creates an [[AnyValue]] from the given `Double`.
    */
  def double(value: Double): DoubleValue =
    DoubleValueImpl(value)

  /** Creates an [[AnyValue]] from the given byte array.
    */
  def bytes(value: Array[Byte]): ByteArrayValue =
    ByteArrayValueImpl(Array.copyOf(value, value.length))

  /** Creates an [[AnyValue]] from the given seq of values.
    */
  def seq(values: Seq[AnyValue]): SeqValue =
    SeqValueImpl(values)

  /** Creates a key-value list (map) of [[AnyValue]] from the given map.
    */
  def map(values: Map[String, AnyValue]): MapValue =
    MapValueImpl(values)

  /** Creates an empty [[AnyValue]]. That is an equivalent of `null`.
    */
  def empty: EmptyValue =
    EmptyValueImpl

  implicit val anyValueHash: Hash[AnyValue] = new Hash[AnyValue] {
    def hash(x: AnyValue): Int = x match {
      case StringValueImpl(value)    => Hash[String].hash(value)
      case BooleanValueImpl(value)   => Hash[Boolean].hash(value)
      case LongValueImpl(value)      => Hash[Long].hash(value)
      case DoubleValueImpl(value)    => Hash[Double].hash(value)
      case ByteArrayValueImpl(value) => java.util.Arrays.hashCode(value)
      case SeqValueImpl(values)      => values.map(hash).hashCode()
      case MapValueImpl(values)      => values.map { case (k, v) => (k, hash(v)) }.hashCode()
      case _: EmptyValue             => System.identityHashCode(EmptyValueImpl)
    }

    def eqv(x: AnyValue, y: AnyValue): Boolean = (x, y) match {
      case (StringValueImpl(a), StringValueImpl(b))   => a == b
      case (BooleanValueImpl(a), BooleanValueImpl(b)) => a == b
      case (LongValueImpl(a), LongValueImpl(b))       => a == b
      case (DoubleValueImpl(a), DoubleValueImpl(b))   => a == b

      case (ByteArrayValueImpl(a), ByteArrayValueImpl(b)) =>
        java.util.Arrays.equals(a, b)

      case (SeqValueImpl(a), SeqValueImpl(b)) =>
        a.size == b.size && a.lazyZip(b).forall { case (x, y) => eqv(x, y) }

      case (MapValueImpl(a), MapValueImpl(b)) =>
        a.size == b.size && a.keys.forall(k => b.contains(k) && eqv(a(k), b(k)))

      case (_: EmptyValue, _: EmptyValue) =>
        true

      case _ =>
        false
    }
  }

  implicit val anyValueShow: Show[AnyValue] = Show.show {
    case _: EmptyValue             => "EmptyValue"
    case StringValueImpl(value)    => s"StringValue($value)"
    case BooleanValueImpl(value)   => s"BooleanValue($value)"
    case LongValueImpl(value)      => s"LongValue($value)"
    case DoubleValueImpl(value)    => s"DoubleValue($value)"
    case ByteArrayValueImpl(value) => s"ByteArrayValue(${Base64.getEncoder.encodeToString(value)})"
    case SeqValueImpl(values)      => s"SeqValue(${values.map(anyValueShow.show).mkString("[", ", ", "]")})"
    case MapValueImpl(values) =>
      s"MapValue(${values.map { case (k, v) => s"$k -> ${anyValueShow.show(v)}" }.mkString("{", ", ", "}")})"
  }

  private[otel4s] final case class StringValueImpl(value: String) extends StringValue
  private[otel4s] final case class BooleanValueImpl(value: Boolean) extends BooleanValue
  private[otel4s] final case class LongValueImpl(value: Long) extends LongValue
  private[otel4s] final case class DoubleValueImpl(value: Double) extends DoubleValue
  private[otel4s] final case class ByteArrayValueImpl(bytes: Array[Byte]) extends ByteArrayValue {
    def value: ByteBuffer = ByteBuffer.wrap(bytes).asReadOnlyBuffer()
  }
  private[otel4s] final case class SeqValueImpl(value: Seq[AnyValue]) extends SeqValue
  private[otel4s] final case class MapValueImpl(value: Map[String, AnyValue]) extends MapValue
  private[otel4s] case object EmptyValueImpl extends EmptyValue

}
