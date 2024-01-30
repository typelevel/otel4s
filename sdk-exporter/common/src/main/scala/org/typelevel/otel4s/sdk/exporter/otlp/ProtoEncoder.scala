package org.typelevel.otel4s
package sdk
package exporter.otlp

import io.circe.Json
import io.opentelemetry.proto.common.v1.common.{
  InstrumentationScope => ScopeProto
}
import io.opentelemetry.proto.common.v1.common.AnyValue
import io.opentelemetry.proto.common.v1.common.ArrayValue
import io.opentelemetry.proto.common.v1.common.KeyValue
import io.opentelemetry.proto.resource.v1.resource.{Resource => ResourceProto}
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import scalapb.GeneratedMessage
import scalapb_circe.Printer

/** @see
  *   [[https://github.com/open-telemetry/opentelemetry-proto/blob/v1.0.0/opentelemetry/proto/metrics/v1/metrics.proto]]
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
  // see TraceProtoEncoder
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

    def list[A](lift: A => Value): Value.ArrayValue = {
      val list = att.value.asInstanceOf[List[A]]
      Value.ArrayValue(ArrayValue(list.map(value => AnyValue(lift(value)))))
    }

    val value = att.key.`type` match {
      case AttributeType.Boolean     => primitive[Boolean](Value.BoolValue(_))
      case AttributeType.Double      => primitive[Double](Value.DoubleValue(_))
      case AttributeType.String      => primitive[String](Value.StringValue(_))
      case AttributeType.Long        => primitive[Long](Value.IntValue(_))
      case AttributeType.BooleanList => list[Boolean](Value.BoolValue(_))
      case AttributeType.DoubleList  => list[Double](Value.DoubleValue(_))
      case AttributeType.StringList  => list[String](Value.StringValue(_))
      case AttributeType.LongList    => list[Long](Value.IntValue(_))
    }

    KeyValue(att.key.name, Some(AnyValue(value)))
  }

  implicit val attributesEncoder: ProtoEncoder[Attributes, Seq[KeyValue]] = {
    attr => attr.toSeq.map(a => encode[Attribute[_], KeyValue](a))
  }

  implicit val telemetryResourceEncoder
      : ProtoEncoder[TelemetryResource, ResourceProto] = { resource =>
    ResourceProto(attributes = encode(resource.attributes))
  }

  implicit val instrumentationScopeEncoder
      : ProtoEncoder[InstrumentationScope, ScopeProto] = { scope =>
    ScopeProto(
      name = scope.name,
      version = scope.version.getOrElse(""),
      attributes = encode(scope.attributes)
    )
  }

}
