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
package trace.data

import cats.Hash
import cats.Show
import cats.syntax.show._
import org.typelevel.otel4s.semconv.attributes.ExceptionAttributes

import java.io.PrintWriter
import java.io.StringWriter
import scala.concurrent.duration.FiniteDuration

/** Data representation of an event.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/trace/api/#add-events]]
  */
sealed trait EventData {

  /** The name of the event.
    */
  def name: String

  /** The timestamp of the event.
    */
  def timestamp: FiniteDuration

  /** The attributes of the event.
    */
  def attributes: LimitedData[Attribute[_], Attributes]

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
  private final val ExceptionEventName = "exception"

  /** Creates [[EventData]] with the given arguments.
    *
    * @param name
    *   the name of the event
    *
    * @param timestamp
    *   the timestamp of the event
    *
    * @param attributes
    *   the attributes to associate with the event
    */
  def apply(
      name: String,
      timestamp: FiniteDuration,
      attributes: LimitedData[Attribute[_], Attributes]
  ): EventData =
    Impl(name, timestamp, attributes)

  /** Creates [[EventData]] from the given exception.
    *
    * The name of the even will be `exception`.
    *
    * Exception details (name, message, and stacktrace) will be added to the attributes.
    *
    * @param timestamp
    *   the timestamp of the event
    *
    * @param exception
    *   the exception to associate with the event
    *
    * @param attributes
    *   the attributes to associate with the event
    */
  def fromException(
      timestamp: FiniteDuration,
      exception: Throwable,
      attributes: LimitedData[Attribute[_], Attributes]
  ): EventData = {
    val exceptionAttributes = {
      val builder = Attributes.newBuilder

      builder.addOne(
        ExceptionAttributes.ExceptionType,
        exception.getClass.getName
      )

      val message = exception.getMessage
      if (message != null) {
        builder.addOne(ExceptionAttributes.ExceptionMessage, message)
      }

      if (exception.getStackTrace.nonEmpty) {
        val stringWriter = new StringWriter()
        val printWriter = new PrintWriter(stringWriter)
        exception.printStackTrace(printWriter)

        builder.addOne(
          ExceptionAttributes.ExceptionStacktrace,
          stringWriter.toString
        )
      }

      builder.result()
    }

    Impl(
      ExceptionEventName,
      timestamp,
      attributes.prependAll(exceptionAttributes)
    )
  }

  implicit val eventDataHash: Hash[EventData] =
    Hash.by(data => (data.name, data.timestamp, data.attributes))

  implicit val eventDataShow: Show[EventData] =
    Show.show { data =>
      show"EventData{name=${data.name}, timestamp=${data.timestamp}, attributes=${data.attributes.elements}}"
    }

  private final case class Impl(
      name: String,
      timestamp: FiniteDuration,
      attributes: LimitedData[Attribute[_], Attributes]
  ) extends EventData

}
