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

package org.typelevel.otel4s
package sdk
package exporter.otlp

import io.circe.Encoder
import io.circe.Json
import io.circe.syntax._
import org.typelevel.otel4s.sdk.common.InstrumentationScope

trait JsonCodecs {

  implicit val attributeEncoder: Encoder[Attribute[_]] =
    Encoder.instance { attribute =>
      Json.obj(
        "key" := attribute.key.name,
        "value" := Json.obj(
          attributeTypeName(attribute.key.`type`) := attributeValue(
            attribute.key.`type`,
            attribute.value
          )
        )
      )
    }

  implicit val attributesEncoder: Encoder[Attributes] =
    Encoder[List[Attribute[_]]].contramap(_.toList)

  implicit val resourceEncoder: Encoder[TelemetryResource] =
    Encoder.instance { resource =>
      Json
        .obj(
          "attributes" := resource.attributes
        )
        .dropEmptyValues
    }

  implicit val instrumentationScopeEncoder: Encoder[InstrumentationScope] =
    Encoder.instance { scope =>
      Json
        .obj(
          "name" := scope.name,
          "version" := scope.version,
          "attributes" := scope.attributes
        )
        .dropNullValues
        .dropEmptyValues
    }

  private def attributeValue(
      attributeType: AttributeType[_],
      value: Any
  ): Json = {
    def primitive[A: Encoder]: Json =
      Encoder[A].apply(value.asInstanceOf[A])

    def seq[A: Encoder](attributeType: AttributeType[A]): Json = {
      val typeName = attributeTypeName(attributeType)
      val values = value.asInstanceOf[Seq[A]]
      Json.obj("values" := values.map(value => Json.obj(typeName := value)))
    }

    implicit val longEncoder: Encoder[Long] =
      Encoder[String].contramap(_.toString)

    attributeType match {
      case AttributeType.Boolean    => primitive[Boolean]
      case AttributeType.Double     => primitive[Double]
      case AttributeType.String     => primitive[String]
      case AttributeType.Long       => primitive[Long]
      case AttributeType.BooleanSeq => seq[Boolean](AttributeType.Boolean)
      case AttributeType.DoubleSeq  => seq[Double](AttributeType.Double)
      case AttributeType.StringSeq  => seq[String](AttributeType.String)
      case AttributeType.LongSeq    => seq[Long](AttributeType.Long)
    }
  }

  private def attributeTypeName(attributeType: AttributeType[_]): String =
    attributeType match {
      case AttributeType.Boolean    => "boolValue"
      case AttributeType.Double     => "doubleValue"
      case AttributeType.String     => "stringValue"
      case AttributeType.Long       => "intValue"
      case AttributeType.BooleanSeq => "arrayValue"
      case AttributeType.DoubleSeq  => "arrayValue"
      case AttributeType.StringSeq  => "arrayValue"
      case AttributeType.LongSeq    => "arrayValue"
    }

}

object JsonCodecs extends JsonCodecs
