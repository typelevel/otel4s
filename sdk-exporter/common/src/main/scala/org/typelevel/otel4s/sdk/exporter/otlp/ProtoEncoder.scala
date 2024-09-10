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

import io.circe.Json
import io.opentelemetry.proto.common.v1.common.{InstrumentationScope => ScopeProto}
import io.opentelemetry.proto.common.v1.common.AnyValue
import io.opentelemetry.proto.common.v1.common.ArrayValue
import io.opentelemetry.proto.common.v1.common.KeyValue
import io.opentelemetry.proto.resource.v1.resource.{Resource => ResourceProto}
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import scalapb.GeneratedMessage
import scalapb_circe.Printer

/** @see
  *   [[https://github.com/open-telemetry/opentelemetry-proto/blob/v1.0.0/opentelemetry/proto/common/v1/common.proto]]
  */
private[otlp] trait ProtoEncoder[-A, +P] {
  def encode(a: A): P
}

private[otlp] object ProtoEncoder {

  type Message[A] = ProtoEncoder[A, GeneratedMessage]

  def encode[A, P](a: A)(implicit ev: ProtoEncoder[A, P]): P =
    ev.encode(a)

  def toByteArray[A, P <: GeneratedMessage](a: A)(implicit
      ev: ProtoEncoder[A, P]
  ): Array[Byte] =
    ev.encode(a).toByteArray

  def toJson[A, P <: GeneratedMessage](a: A)(implicit
      ev: ProtoEncoder[A, P],
      printer: Printer
  ): Json =
    printer.toJson(ev.encode(a))

  // a preconfigured printer, different implementations may override some internal methods
  // see SpansProtoEncoder
  class JsonPrinter
      extends Printer(
        includingDefaultValueFields = false,
        formattingLongAsNumber = false,
        formattingEnumsAsNumber = true
      )

  implicit val attributeEncoder: ProtoEncoder[Attribute[_], KeyValue] = { att =>
    import AnyValue.Value

    def primitive[A](lift: A => Value): Value =
      lift(att.value.asInstanceOf[A])

    def seq[A](lift: A => Value): Value.ArrayValue = {
      val values = att.value.asInstanceOf[Seq[A]]
      Value.ArrayValue(ArrayValue(values.map(value => AnyValue(lift(value)))))
    }

    val value = att.key.`type` match {
      case AttributeType.Boolean    => primitive[Boolean](Value.BoolValue.apply)
      case AttributeType.Double     => primitive[Double](Value.DoubleValue.apply)
      case AttributeType.String     => primitive[String](Value.StringValue.apply)
      case AttributeType.Long       => primitive[Long](Value.IntValue.apply)
      case AttributeType.BooleanSeq => seq[Boolean](Value.BoolValue.apply)
      case AttributeType.DoubleSeq  => seq[Double](Value.DoubleValue.apply)
      case AttributeType.StringSeq  => seq[String](Value.StringValue.apply)
      case AttributeType.LongSeq    => seq[Long](Value.IntValue.apply)
    }

    KeyValue(att.key.name, Some(AnyValue(value)))
  }

  implicit val attributesEncoder: ProtoEncoder[Attributes, Seq[KeyValue]] = { attr =>
    attr.toSeq.map(a => encode[Attribute[_], KeyValue](a))
  }

  implicit val telemetryResourceEncoder: ProtoEncoder[TelemetryResource, ResourceProto] = { resource =>
    ResourceProto(attributes = encode(resource.attributes))
  }

  implicit val instrumentationScopeEncoder: ProtoEncoder[InstrumentationScope, ScopeProto] = { scope =>
    ScopeProto(
      name = scope.name,
      version = scope.version.getOrElse(""),
      attributes = encode(scope.attributes)
    )
  }

}
