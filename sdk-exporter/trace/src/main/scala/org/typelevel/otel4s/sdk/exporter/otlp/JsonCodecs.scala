package org.typelevel.otel4s
package sdk
package exporter.otlp

import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.typelevel.otel4s.sdk.{Resource => InstrumentationResource}
import org.typelevel.otel4s.sdk.common.InstrumentationScopeInfo
import org.typelevel.otel4s.sdk.trace.data.SpanData

import scala.collection.immutable

private[otlp] object JsonCodecs {

  private def attributeValue1(attribute: Attribute[_]) =
    attribute match {
      case Attribute(key, value)
    }
  private def attributeValue(attributeType: AttributeType[_], value: Any): Json = {
    attributeType match {
      case AttributeType.Boolean =>
        value.asInstanceOf[Boolean].asJson
      case AttributeType.Double =>
        value.asInstanceOf[Double].asJson
      case AttributeType.String =>
        value.asInstanceOf[String].asJson
      case AttributeType.Long =>
        value.asInstanceOf[Long].asJson
      case AttributeType.BooleanList =>
        val list = value.asInstanceOf[List[Boolean]]
        val typeName = attributeTypeName(AttributeType.Boolean)
        Json.obj(
          "values" -> list.map(bool => Json.obj(typeName -> bool.asJson)).asJson
        )
      case AttributeType.DoubleList =>
        val list = value.asInstanceOf[List[Double]]
        val typeName = attributeTypeName(AttributeType.Double)
        Json.obj(
          "values" -> list
            .map(double => Json.obj(typeName -> double.asJson))
            .asJson
        )
      case AttributeType.StringList =>
        val list = value.asInstanceOf[List[String]]
        val typeName = attributeTypeName(AttributeType.String)
        Json.obj(
          "values" -> list
            .map(string => Json.obj(typeName -> string.asJson))
            .asJson
        )
      case AttributeType.LongList =>
        val list = value.asInstanceOf[List[Long]]
        val typeName = attributeTypeName(AttributeType.Long)
        Json.obj(
          "values" -> list.map(long => Json.obj(typeName -> long.asJson)).asJson
        )
    }
  }

  private def attributeTypeName(attributeType: AttributeType[_]): String =
    attributeType match {
      case AttributeType.Boolean     => "boolValue"
      case AttributeType.Double      => "doubleValue"
      case AttributeType.String      => "stringValue"
      case AttributeType.Long        => "intValue"
      case AttributeType.BooleanList => "arrayValue"
      case AttributeType.DoubleList  => "arrayValue"
      case AttributeType.StringList  => "arrayValue"
      case AttributeType.LongList    => "arrayValue"
    }

  implicit val attributeEncoder: Encoder[Attribute[_]] =
    Encoder.instance { attribute =>
      Json.obj(
        "key" := attribute.key.name,
        "value" := Json.obj(
          attributeTypeName(attribute.key.`type`) := attributeValue(attribute.key.`type`, attribute.value)
        )
      )
    }

  implicit val resourceEncoder: Encoder[InstrumentationResource] =
    Encoder.instance { resource =>
      Json.obj(
        "attributes" := resource.attributes
      )
    }

  /*
  "attributes":[{"key":"resource-attr","value":{"stringValue":"resource-attr-val-1"}}]}
   */
  implicit val attributesEncoder: Encoder[Attributes] =
    Encoder[List[Attribute[_]]].contramap(_.toList)

  implicit val scopeInfoEncoder: Encoder[InstrumentationScopeInfo] =
    Encoder.instance { scope =>
      Json.obj(
        "name" := scope.name,
        "version" := scope.version.getOrElse(""),
        "schemaUrl" := scope.schemaUrl.getOrElse(""),
        "attributes" := scope.attributes
      )
    }

  implicit val spanDataEncoder: Encoder[List[SpanData]] =
    Encoder.instance { spans =>
      val resourceState = spans.head.resource

      val resourceSpans =
        spans.groupBy(_.resource).map { case (resource, resourceSpans) =>
          val scopeSpans: Iterable[Json] =
            resourceSpans
              .groupBy(_.instrumentationScopeInfo)
              .map { case (scope, spans) =>
                Json.obj("scope" := scope/*, spans := spans*/)
              }

          Json.obj(
            "resource" := resource,
            "scopeSpans" := scopeSpans
          )
        }

      Json.obj("resourceSpans" := resourceSpans)
    }

}
