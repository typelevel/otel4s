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

import cats.Hash
import cats.Show
import cats.syntax.show._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.sdk.trace.SpanLimits

import java.io.PrintWriter
import java.io.StringWriter

/** Data representation of an event.
  */
sealed trait EventData {

  /** The name of the event.
    */
  def name: String

  /** The epoch time in nanos of the event.
    */
  def epochNanos: Long

  /** The attributes of the event.
    */
  def attributes: Attributes

  /** The total number of attributes that were recorded on the event. This
    * number may be larger than the number of attributes that are attached to
    * this span, if the total number recorded was greater than the configured
    * maximum value.
    *
    * @see
    *   [[SpanLimits.maxNumberOfAttributesPerEvent]]
    */
  def totalAttributeCount: Int

  /** The count of dropped attributes of the event.
    */
  final def droppedAttributesCount: Int =
    totalAttributeCount - attributes.size

  override final def hashCode(): Int =
    Hash[EventData].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: EventData => Hash[EventData].eqv(this, other)
      case _                => false
    }

  override final def toString: String =
    Show[EventData].show(this)
}

object EventData {

  private object Keys {
    val ExceptionType = AttributeKey.string("exception.type")
    val ExceptionMessage = AttributeKey.string("exception.message")
    val ExceptionStacktrace = AttributeKey.string("exception.stacktrace")
  }

  private val ExceptionEventName = "exception"

  private final case class General(
      name: String,
      epochNanos: Long,
      attributes: Attributes,
      totalAttributeCount: Int
  ) extends EventData

  private final case class Exception(
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

  implicit val eventDataHash: Hash[EventData] = {
    implicit val throwableHash: Hash[Throwable] =
      Hash.fromUniversalHashCode

    implicit val generalEventDataHash: Hash[General] =
      Hash.by(g => (g.name, g.epochNanos, g.attributes, g.totalAttributeCount))

    implicit val exceptionEventDataHash: Hash[Exception] =
      Hash.by { e =>
        (e.spanLimits, e.epochNanos, e.exception, e.additionalAttributes)
      }

    new Hash[EventData] {
      def hash(x: EventData): Int =
        x match {
          case g: General   => Hash[General].hash(g)
          case e: Exception => Hash[Exception].hash(e)
        }

      def eqv(x: EventData, y: EventData): Boolean =
        (x, y) match {
          case (x: General, y: General)     => Hash[General].eqv(x, y)
          case (x: Exception, y: Exception) => Hash[Exception].eqv(x, y)
          case _                            => false
        }
    }
  }

  implicit val eventDataShow: Show[EventData] =
    Show.show {
      case General(name, epochNanos, attributes, totalAttributeCount) =>
        show"EventData{name=$name, epochNanos=$epochNanos, attributes=$attributes, totalAttributeCount=$totalAttributeCount}"
      case Exception(limits, epochNanos, exception, additionalAttributes) =>
        show"ExceptionEventData{epochNanos=$epochNanos, exception=${exception.toString}, additionalAttributes=$additionalAttributes, spanLimits=$limits}"
    }
}
