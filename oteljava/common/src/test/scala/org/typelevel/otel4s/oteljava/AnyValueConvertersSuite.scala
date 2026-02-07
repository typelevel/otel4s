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
package oteljava

import io.opentelemetry.api.common.{Value => JValue}
import io.opentelemetry.api.common.KeyValue
import io.opentelemetry.api.common.ValueType
import munit.Compare
import munit.FunSuite
import org.typelevel.otel4s.oteljava.AnyValueConverters._

import scala.jdk.CollectionConverters._

class AnyValueConvertersSuite extends FunSuite {

  test("proper conversion to and from OpenTelemetry Value") {
    val anyValue: AnyValue = AnyValue.map(
      Map(
        "string" -> AnyValue.string("text"),
        "boolean" -> AnyValue.boolean(true),
        "long" -> AnyValue.long(42L),
        "double" -> AnyValue.double(12.34),
        "bytes" -> AnyValue.bytes(Array[Byte](1, 2, 3)),
        "seq" -> AnyValue.seq(Seq(AnyValue.string("a"), AnyValue.long(1L))),
        "map" -> AnyValue.map(Map("nested" -> AnyValue.double(1.5))),
        "empty" -> AnyValue.empty,
      )
    )

    val jValue: JValue[_] = JValue.of(
      Map[String, JValue[_]](
        "string" -> JValue.of("text"),
        "boolean" -> JValue.of(true),
        "long" -> JValue.of(42L),
        "double" -> JValue.of(12.34),
        "bytes" -> JValue.of(Array[Byte](1, 2, 3)),
        "seq" -> JValue.of(List[JValue[_]](JValue.of("a"), JValue.of(1L)).asJava),
        "map" -> JValue.of(Map[String, JValue[_]]("nested" -> JValue.of(1.5)).asJava),
        "empty" -> JValue.empty(),
      ).asJava
    )

    assertEquals(anyValue.toJava.asInstanceOf[JValue[Any]], jValue.asInstanceOf[JValue[Any]])
    assertEquals(jValue.toScala, anyValue)

    assertEquals(anyValue.toJava.toScala, anyValue)
    assertEquals(jValue.toScala.toJava.asInstanceOf[JValue[Any]], jValue.asInstanceOf[JValue[Any]])
  }

  test("proper conversion for primitive and empty values") {
    val samples = List(
      AnyValue.string("value") -> JValue.of("value"),
      AnyValue.boolean(false) -> JValue.of(false),
      AnyValue.long(Long.MaxValue) -> JValue.of(Long.MaxValue),
      AnyValue.double(Double.MinValue) -> JValue.of(Double.MinValue),
      AnyValue.bytes(Array[Byte](9, 8, 7)) -> JValue.of(Array[Byte](9, 8, 7)),
      AnyValue.empty -> JValue.empty(),
    )

    samples.foreach { case (scalaValue, javaValue) =>
      assertEquals(
        scalaValue.toJava.asInstanceOf[JValue[Any]],
        javaValue.asInstanceOf[JValue[Any]],
        (scalaValue, javaValue)
      )
      assertEquals(javaValue.toScala, scalaValue)
    }
  }

  private implicit val compareJValue: Compare[JValue[Any], JValue[Any]] =
    (left, right) => {
      (left.getType, right.getType) match {
        case (ValueType.STRING, ValueType.STRING)   => left.getValue == right.getValue
        case (ValueType.BOOLEAN, ValueType.BOOLEAN) => left.getValue == right.getValue
        case (ValueType.LONG, ValueType.LONG)       => left.getValue == right.getValue
        case (ValueType.DOUBLE, ValueType.DOUBLE)   => left.getValue == right.getValue
        case (ValueType.BYTES, ValueType.BYTES)     => left.getValue == right.getValue

        case (ValueType.ARRAY, ValueType.ARRAY) =>
          val l = left.getValue.asInstanceOf[java.util.List[JValue[_]]].asScala
          val r = right.getValue.asInstanceOf[java.util.List[JValue[_]]].asScala
          l.diff(r).isEmpty

        case (ValueType.KEY_VALUE_LIST, ValueType.KEY_VALUE_LIST) =>
          val l = left.getValue.asInstanceOf[java.util.List[KeyValue]].asScala
          val r = right.getValue.asInstanceOf[java.util.List[KeyValue]].asScala
          l.diff(r).isEmpty

        case (ValueType.EMPTY, ValueType.EMPTY) =>
          true

        case (_, _) =>
          false
      }
    }
}
