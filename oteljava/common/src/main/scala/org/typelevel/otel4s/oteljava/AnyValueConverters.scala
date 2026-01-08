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

package org.typelevel.otel4s.oteljava

import io.opentelemetry.api.common.{Value => JValue}
import io.opentelemetry.api.common.{ValueType => JValueType}
import io.opentelemetry.api.common.KeyValue
import org.typelevel.otel4s.AnyValue

import java.{util => ju}
import java.nio.ByteBuffer
import scala.jdk.CollectionConverters._

object AnyValueConverters {

  implicit final class AnyValueToJava(private val value: AnyValue) extends AnyVal {

    /** Converts Scala `AnyValue` to Java `Value`. */
    def toJava: JValue[_] = Explicit.toJava(value)
  }

  implicit final class ValueToScala(private val value: JValue[_]) extends AnyVal {

    /** Converts a Java `Value` to a Scala `AnyValue`. */
    def toScala: AnyValue = Explicit.toScala(value)
  }

  private object Explicit {

    def toJava(value: AnyValue): JValue[_] =
      value match {
        case string: AnyValue.StringValue =>
          JValue.of(string.value)

        case boolean: AnyValue.BooleanValue =>
          JValue.of(boolean.value)

        case long: AnyValue.LongValue =>
          JValue.of(long.value)

        case double: AnyValue.DoubleValue =>
          JValue.of(double.value)

        case AnyValue.ByteArrayValueImpl(bytes) =>
          JValue.of(bytes)

        case seq: AnyValue.SeqValue =>
          JValue.of(seq.value.map(toJava).asJava)

        case map: AnyValue.MapValue =>
          JValue.of(
            map.value.map { case (key, value) => (key, toJava(value)) }.asJava
          )

        case _: AnyValue.EmptyValue =>
          JValue.empty()
      }

    def toScala(value: JValue[_]): AnyValue =
      value.getType match {
        case JValueType.STRING =>
          AnyValue.string(value.getValue.asInstanceOf[String])

        case JValueType.BOOLEAN =>
          AnyValue.boolean(value.getValue.asInstanceOf[Boolean])

        case JValueType.LONG =>
          AnyValue.long(value.getValue.asInstanceOf[Long])

        case JValueType.DOUBLE =>
          AnyValue.double(value.getValue.asInstanceOf[Double])

        case JValueType.ARRAY =>
          AnyValue.seq(value.getValue.asInstanceOf[ju.List[JValue[_]]].asScala.toSeq.map(toScala))

        case JValueType.KEY_VALUE_LIST =>
          AnyValue.map(
            value.getValue.asInstanceOf[ju.List[KeyValue]].asScala.map(kv => (kv.getKey, toScala(kv.getValue))).toMap
          )

        case JValueType.BYTES =>
          val byteBuffer = value.getValue.asInstanceOf[ByteBuffer]
          val byteArray = new Array[Byte](byteBuffer.remaining)
          byteBuffer.get(byteArray)
          AnyValue.bytes(byteArray)

        case JValueType.EMPTY =>
          AnyValue.empty
      }

  }

}
