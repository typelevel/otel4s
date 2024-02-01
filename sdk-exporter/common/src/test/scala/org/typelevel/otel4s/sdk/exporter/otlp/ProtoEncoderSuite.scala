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

package org.typelevel.otel4s.sdk.exporter.otlp

import io.circe.Encoder
import io.circe.Json
import io.circe.syntax._
import io.opentelemetry.proto.common.v1.common.KeyValue
import munit.ScalaCheckSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Prop
import org.scalacheck.Test
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeType
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.scalacheck.Arbitraries._
import scalapb_circe.Printer

class ProtoEncoderSuite extends ScalaCheckSuite {
  import JsonCodecs._

  private implicit val jsonPrinter: Printer =
    new ProtoEncoder.JsonPrinter

  private implicit val keyValueEncoder: ProtoEncoder[KeyValue, KeyValue] =
    kv => kv

  test("encode Attribute[_]") {
    Prop.forAll(Arbitrary.arbitrary[Attribute[_]]) { attribute =>
      val value = attribute.value

      def list[A: Encoder](typeName: String): Json = {
        val list = value.asInstanceOf[List[A]]
        Json.obj("values" := list.map(value => Json.obj(typeName := value)))
      }

      implicit val longEncoder: Encoder[Long] =
        Encoder[String].contramap[Long](_.toString)

      val expected = {
        val v = attribute.key.`type` match {
          case AttributeType.Boolean =>
            Json.obj("boolValue" := value.asInstanceOf[Boolean])
          case AttributeType.Double =>
            Json.obj("doubleValue" := value.asInstanceOf[Double])
          case AttributeType.String =>
            Json.obj("stringValue" := value.asInstanceOf[String])
          case AttributeType.Long =>
            Json.obj("intValue" := value.asInstanceOf[Long])
          case AttributeType.BooleanList =>
            Json.obj("arrayValue" := list[Boolean]("boolValue"))
          case AttributeType.DoubleList =>
            Json.obj("arrayValue" := list[Double]("doubleValue"))
          case AttributeType.StringList =>
            Json.obj("arrayValue" := list[String]("stringValue"))
          case AttributeType.LongList =>
            Json.obj("arrayValue" := list[Long]("intValue"))
        }

        Json.obj("key" := attribute.key.name, "value" := v)
      }

      assertEquals(
        ProtoEncoder.toJson(
          ProtoEncoder.attributeEncoder.encode(attribute)
        ),
        expected
      )
    }
  }

  test("encode Attribute[_] (noSpaces)") {
    assertEquals(
      ProtoEncoder.toJson(Attribute("string", "value")).noSpaces,
      """{"key":"string","value":{"stringValue":"value"}}"""
    )

    assertEquals(
      ProtoEncoder.toJson(Attribute("string_list", List("a", "b"))).noSpaces,
      """{"key":"string_list","value":{"arrayValue":{"values":[{"stringValue":"a"},{"stringValue":"b"}]}}}"""
    )

    assertEquals(
      ProtoEncoder.toJson(Attribute("boolean", true)).noSpaces,
      """{"key":"boolean","value":{"boolValue":true}}"""
    )

    assertEquals(
      ProtoEncoder
        .toJson(Attribute("boolean_list", List(true, false)))
        .noSpaces,
      """{"key":"boolean_list","value":{"arrayValue":{"values":[{"boolValue":true},{"boolValue":false}]}}}"""
    )

    assertEquals(
      ProtoEncoder.toJson(Attribute("int+", 1L)).noSpaces,
      """{"key":"int+","value":{"intValue":"1"}}"""
    )

    assertEquals(
      ProtoEncoder.toJson(Attribute("int-", -1L)).noSpaces,
      """{"key":"int-","value":{"intValue":"-1"}}"""
    )

    assertEquals(
      ProtoEncoder.toJson(Attribute("int_list", List(1L, -1L))).noSpaces,
      """{"key":"int_list","value":{"arrayValue":{"values":[{"intValue":"1"},{"intValue":"-1"}]}}}"""
    )

    assertEquals(
      ProtoEncoder.toJson(Attribute("double+", 1.1)).noSpaces,
      """{"key":"double+","value":{"doubleValue":1.1}}"""
    )

    assertEquals(
      ProtoEncoder.toJson(Attribute("double-", -1.1)).noSpaces,
      """{"key":"double-","value":{"doubleValue":-1.1}}"""
    )

    assertEquals(
      ProtoEncoder.toJson(Attribute("double_list", List(1.1, -1.1))).noSpaces,
      """{"key":"double_list","value":{"arrayValue":{"values":[{"doubleValue":1.1},{"doubleValue":-1.1}]}}}"""
    )
  }

  test("encode TelemetryResource") {
    Prop.forAll(Arbitrary.arbitrary[TelemetryResource]) { resource =>
      val expected = Json
        .obj(
          "attributes" := Encoder[Attributes].apply(resource.attributes)
        )
        .dropEmptyValues

      assertEquals(ProtoEncoder.toJson(resource), expected)
    }
  }

  test("encode InstrumentationScope") {
    Prop.forAll(Arbitrary.arbitrary[InstrumentationScope]) { scope =>
      val expected = Json
        .obj(
          "name" := scope.name,
          "version" := scope.version,
          "attributes" := scope.attributes
        )
        .dropNullValues
        .dropEmptyValues

      assertEquals(ProtoEncoder.toJson(scope), expected)
    }
  }

  test("encode InstrumentationScope (noSpaces)") {
    val attrs = Attributes(Attribute("key", "value"))

    val scope1 = InstrumentationScope("name", None, None, Attributes.empty)

    val scope2 = InstrumentationScope(
      "name",
      Some("version"),
      Some("schema"),
      attrs
    )

    assertEquals(
      ProtoEncoder.toJson(scope1).noSpaces,
      """{"name":"name"}"""
    )

    assertEquals(
      ProtoEncoder.toJson(scope2).noSpaces,
      """{"name":"name","version":"version","attributes":[{"key":"key","value":{"stringValue":"value"}}]}"""
    )
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(100)
      .withMaxSize(100)

}
