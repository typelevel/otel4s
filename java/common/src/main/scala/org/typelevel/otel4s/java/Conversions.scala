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
package java

import _root_.java.lang.{Iterable => JIterable}
import cats.effect.kernel.Async
import cats.effect.kernel.Sync
import cats.syntax.either._
import io.opentelemetry.api.common.{Attributes => JAttributes}
import io.opentelemetry.context.{Context => JContext}
import io.opentelemetry.context.propagation.{TextMapGetter => JTextMapGetter}
import io.opentelemetry.context.propagation.{
  TextMapPropagator => JTextMapPropagator
}
import io.opentelemetry.context.propagation.{TextMapSetter => JTextMapSetter}
import io.opentelemetry.sdk.common.CompletableResultCode

import scala.collection.mutable
import scala.jdk.CollectionConverters._

private[java] object Conversions {

  def toJAttributes(attributes: Seq[Attribute[_]]): JAttributes = {
    val builder = JAttributes.builder

    attributes.foreach { case Attribute(key, value) =>
      key.`type` match {
        case AttributeType.String =>
          builder.put(key.name, value.asInstanceOf[String])
        case AttributeType.Boolean =>
          builder.put(key.name, value.asInstanceOf[Boolean])
        case AttributeType.Long =>
          builder.put(key.name, value.asInstanceOf[Long])
        case AttributeType.Double =>
          builder.put(key.name, value.asInstanceOf[Double])
        case AttributeType.StringList =>
          builder.put(key.name, value.asInstanceOf[List[String]]: _*)
        case AttributeType.BooleanList =>
          builder.put(key.name, value.asInstanceOf[List[Boolean]]: _*)
        case AttributeType.LongList =>
          builder.put(key.name, value.asInstanceOf[List[Long]]: _*)
        case AttributeType.DoubleList =>
          builder.put(key.name, value.asInstanceOf[List[Double]]: _*)
      }
    }

    builder.build()
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

  def fromJTextMapPropagator[F[_]: Sync](
      jPropagator: JTextMapPropagator
  ): TextMapPropagator.Aux[F, JContext] =
    new TextMapPropagator[F] {
      type Context = JContext

      def extract(ctx: Context, carrier: mutable.Map[String, String]): Context =
        jPropagator.extract(ctx, carrier, textMapGetter)

      def inject(ctx: Context, carrier: mutable.Map[String, String]): F[Unit] =
        Sync[F].delay(jPropagator.inject(ctx, carrier, textMapSetter))
    }

  val textMapGetter: JTextMapGetter[mutable.Map[String, String]] =
    new JTextMapGetter[mutable.Map[String, String]] {
      def get(carrier: mutable.Map[String, String], key: String) =
        carrier.get(key).orNull
      def keys(carrier: mutable.Map[String, String]): JIterable[String] =
        carrier.keys.asJava
    }

  val textMapSetter: JTextMapSetter[mutable.Map[String, String]] =
    new JTextMapSetter[mutable.Map[String, String]] {
      def set(
          carrier: mutable.Map[String, String],
          key: String,
          value: String
      ) =
        carrier.update(key, value)
    }
}
