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

package org.typelevel.otel4s.sdk
package trace.data

import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.sdk.trace.SpanLimits

import java.io.PrintWriter
import java.io.StringWriter

sealed trait EventData {
  def name: String
  def epochNanos: Long
  def attributes: Attributes
  def totalAttributeCount: Int

  final def droppedAttributesCount: Int =
    totalAttributeCount - attributes.size
}

object EventData {

  private object Keys {
    val ExceptionType = AttributeKey.string("exception.type")
    val ExceptionMessage = AttributeKey.string("exception.message")
    val ExceptionStacktrace = AttributeKey.string("exception.stacktrace")
  }

  private val ExceptionEventName = "exception"

  final case class General(
      name: String,
      epochNanos: Long,
      attributes: Attributes,
      totalAttributeCount: Int
  ) extends EventData

  final case class Exception(
      spanLimits: SpanLimits,
      epochNanos: Long,
      exception: Throwable,
      additionalAttributes: Attributes
  ) extends EventData {
    def name: String = ExceptionEventName

    lazy val attributes: Attributes = {
      val builder = List.newBuilder[Attribute[_]]

      builder.addOne(
        Attribute(
          Keys.ExceptionType,
          Option(exception.getClass.getCanonicalName).getOrElse("")
        )
      )

      val message = exception.getMessage
      if (message != null) {
        builder.addOne(Attribute(Keys.ExceptionMessage, message))
      }

      val stringWriter = new StringWriter()

      scala.util.control.Exception.allCatch {
        val printWriter = new PrintWriter(stringWriter)
        exception.printStackTrace(printWriter)
      }

      builder.addOne(Attribute(Keys.ExceptionStacktrace, stringWriter.toString))
      builder.addAll(additionalAttributes.toList)

      /*  todo: apply span limits

         return AttributeUtil.applyAttributesLimit(
             attributesBuilder.build(),
             spanLimits.getMaxNumberOfAttributesPerEvent(),
             spanLimits.getMaxAttributeValueLength());
       */

      println("calc attributes: ")
      Attributes(builder.result(): _*)
    }

    def totalAttributeCount: Int =
      attributes.size
  }

  def general(
      name: String,
      epochNanos: Long,
      attributes: Attributes
  ): EventData =
    General(name, epochNanos, attributes, attributes.size)

  def general(
      name: String,
      epochNanos: Long,
      attributes: Attributes,
      totalAttributeCount: Int
  ): EventData =
    General(name, epochNanos, attributes, totalAttributeCount)

  def exception(
      spanLimits: SpanLimits,
      epochNanos: Long,
      exception: Throwable,
      attributes: Attributes
  ): EventData =
    Exception(spanLimits, epochNanos, exception, attributes)

}
