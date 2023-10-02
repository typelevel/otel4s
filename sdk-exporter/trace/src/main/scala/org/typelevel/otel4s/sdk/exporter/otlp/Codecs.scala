package org.typelevel.otel4s
package sdk
package exporter.otlp

import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.typelevel.otel4s.sdk.trace.data.SpanData

private[otlp] object Codecs {


  /*
  def attributeValue[A](attributeType: AttributeType[A], value: A): Json = attributeType match {
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
          "values" -> list.map(bool =>
            Json.obj(typeName -> bool.asJson)
          ).asJson
        )
      case AttributeType.DoubleList =>
        val list = value.asInstanceOf[List[Double]]
        val typeName = attributeTypeName(AttributeType.Double)
        Json.obj(
          "values" -> list.map(double =>
            Json.obj(typeName -> double.asJson)
          ).asJson
        )
      case AttributeType.StringList =>
        val list = value.asInstanceOf[List[String]]
        val typeName = attributeTypeName(AttributeType.String)
        Json.obj(
          "values" -> list.map(string =>
            Json.obj(typeName -> string.asJson)
          ).asJson
        )
      case AttributeType.LongList =>
        val list = value.asInstanceOf[List[Long]]
        val typeName = attributeTypeName(AttributeType.Long)
        Json.obj(
          "values" -> list.map(long =>
            Json.obj(typeName -> long.asJson)
          ).asJson
        )
    }

  def attributeTypeName(attributeType: AttributeType[_]): String = attributeType match {
      case AttributeType.Boolean => "boolValue"
      case AttributeType.Double => "doubleValue"
      case AttributeType.String => "stringValue"
      case AttributeType.Long => "intValue"
      case AttributeType.BooleanList => "arrayValue"
      case AttributeType.DoubleList => "arrayValue"
      case AttributeType.StringList => "arrayValue"
      case AttributeType.LongList => "arrayValue"
    }
   */
  private implicit val attributeEncoder: Encoder[Attribute[_]] =
    Encoder.instance { attribute =>
      attribute.key.`type` match {
        case AttributeType.String      =>
          builder.put(key.name, value.asInstanceOf[String])
        case AttributeType.Boolean     =>
          builder.put(key.name, value.asInstanceOf[Boolean])
        case AttributeType.Long        =>
          builder.put(key.name, value.asInstanceOf[Long])
        case AttributeType.Double      =>
          builder.put(key.name, value.asInstanceOf[Double])
        case AttributeType.StringList  =>
          builder.put(key.name, value.asInstanceOf[List[String]]: _*)
        case AttributeType.BooleanList =>
          builder.put(key.name, value.asInstanceOf[List[Boolean]]: _*)
        case AttributeType.LongList    =>
          builder.put(key.name, value.asInstanceOf[List[Long]]: _*)
        case AttributeType.DoubleList  =>
          builder.put(key.name, value.asInstanceOf[List[Double]]: _*)
      }

      Json.obj(
        "key" := attribute.key.name,
        "value" := Json.obj()
      )

      Json.obj(
        "key" -> attribute.key.name.asJson,
        "value" -> Json.obj(
          attributeTypeName(attribute.key.`type`) -> attributeValue(attribute.key.`type`, attribute.value)
        )
      )
    }

  private implicit val resourceEncoder: Encoder[org.typelevel.otel4s.sdk.Resource] =
    Encoder.instance { resource =>
      Json.obj(
        "attributes" -> (
          Attribute("service.name", resourceState.serviceName) :: resourceState.resourceAttributes
          ).map(convertAttribute).asJson,
        "droppedAttributesCount" -> 0.asJson
      )
    }

  private implicit val spanDataEncoder: Encoder[List[SpanData]] =
    Encoder.instance { spans =>
      val resourceState = spans.head.resource


      Json.obj(
        "resourceSpans" -> Json.arr(
          Json.obj("resource")
        )
      )

      /*
      val resourceState = chunk.head.get.resourceState
          Json.obj(
            "resourceSpans" -> Json.arr(
              Json.obj(
                "resource" -> Json.obj(
                  "attributes" -> (
                    Attribute("service.name", resourceState.serviceName) :: resourceState.resourceAttributes
                  ).map(convertAttribute).asJson,
                  "droppedAttributesCount" -> 0.asJson
                ),
                "scopeSpans" -> chunk.toVector.groupBy(_.scopeState).map{
                  case (scopeState, spans) =>
                    Json.obj(
                      "scope" -> Json.obj(
                        "name" -> scopeState.instrumentationScopeName.asJson,
                        "version" -> scopeState.version.getOrElse("").asJson,
                        "schemaUrl" -> scopeState.schemaUrl.getOrElse("").asJson,
                        "attributes" -> List.empty.asJson,
                        "droppedAttributesCount" -> 0.asJson,
                      ),
                      "spans" -> spans.map(convertSpan).asJson
                    )
                }.asJson
              )
            )
          ).deepDropNullValues
       */


    }

}
