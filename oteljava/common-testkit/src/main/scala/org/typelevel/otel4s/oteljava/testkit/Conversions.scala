/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.oteljava.testkit

import _root_.java.{lang => jl}
import _root_.java.{util => ju}
import cats.effect.Async
import cats.syntax.either._
import io.opentelemetry.api.common.{AttributeType => JAttributeType}
import io.opentelemetry.api.common.{Attributes => JAttributes}
import io.opentelemetry.sdk.common.CompletableResultCode
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes

import scala.jdk.CollectionConverters._

private[oteljava] object Conversions {

  def fromJAttributes(attributes: JAttributes): Attributes = {
    val converted = attributes.asMap().asScala.toList.collect {
      case (attribute, value: String)
          if attribute.getType == JAttributeType.STRING =>
        Attribute(attribute.getKey, value)

      case (attribute, value: jl.Boolean)
          if attribute.getType == JAttributeType.BOOLEAN =>
        Attribute(attribute.getKey, value.booleanValue())

      case (attribute, value: jl.Long)
          if attribute.getType == JAttributeType.LONG =>
        Attribute(attribute.getKey, value.longValue())

      case (attribute, value: jl.Double)
          if attribute.getType == JAttributeType.DOUBLE =>
        Attribute(attribute.getKey, value.doubleValue())

      case (attribute, value: ju.List[String] @unchecked)
          if attribute.getType == JAttributeType.STRING_ARRAY =>
        Attribute(attribute.getKey, value.asScala.toList)

      case (attribute, value: ju.List[jl.Boolean] @unchecked)
          if attribute.getType == JAttributeType.BOOLEAN_ARRAY =>
        Attribute(attribute.getKey, value.asScala.toList.map(_.booleanValue()))

      case (attribute, value: ju.List[jl.Long] @unchecked)
          if attribute.getType == JAttributeType.LONG_ARRAY =>
        Attribute(attribute.getKey, value.asScala.toList.map(_.longValue()))

      case (attribute, value: ju.List[jl.Double] @unchecked)
          if attribute.getType == JAttributeType.DOUBLE_ARRAY =>
        Attribute(attribute.getKey, value.asScala.toList.map(_.doubleValue()))
    }

    converted.to(Attributes)
  }

  def asyncFromCompletableResultCode[F[_]](
      codeF: F[CompletableResultCode],
      msg: => Option[String] = None
  )(implicit F: Async[F]): F[Unit] =
    F.flatMap(codeF)(code =>
      F.async[Unit](cb =>
        F.delay {
          code.whenComplete(() =>
            if (code.isSuccess())
              cb(Either.unit)
            else
              cb(
                Left(
                  new RuntimeException(
                    msg.getOrElse(
                      "OpenTelemetry SDK async operation failed"
                    )
                  )
                )
              )
          )
          None
        }
      )
    )

}
