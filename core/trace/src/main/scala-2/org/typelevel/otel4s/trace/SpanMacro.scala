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
package trace

import scala.concurrent.duration.FiniteDuration

private[otel4s] trait SpanMacro[F[_]] {
  self: Span[F] =>

  /** Adds an attribute to the span. If the span previously contained a mapping
    * for the key, the old value is replaced by the specified value.
    *
    * @param attribute
    *   the attribute to add to the span
    */
  def addAttribute[A](attribute: Attribute[A]): F[Unit] =
    macro SpanMacro.addAttribute[A]

  /** Adds attributes to the span. If the span previously contained a mapping
    * for any of the keys, the old values are replaced by the specified values.
    *
    * @param attributes
    *   the set of attributes to add to the span
    */
  def addAttributes(attributes: Attribute[_]*): F[Unit] =
    macro SpanMacro.addAttributes

  /** Adds an event to the span with the given attributes. The timestamp of the
    * event will be the current time.
    *
    * @param name
    *   the name of the event
    *
    * @param attributes
    *   the set of attributes to associate with the event
    */
  def addEvent(name: String, attributes: Attribute[_]*): F[Unit] =
    macro SpanMacro.addEvent

  /** Adds an event to the span with the given attributes and timestamp.
    *
    * '''Note''': the timestamp should be based on `Clock[F].realTime`. Using
    * `Clock[F].monotonic` may lead to an incorrect data.
    *
    * @param name
    *   the name of the event
    *
    * @param timestamp
    *   the explicit event timestamp since epoch
    *
    * @param attributes
    *   the set of attributes to associate with the event
    */
  def addEvent(
      name: String,
      timestamp: FiniteDuration,
      attributes: Attribute[_]*
  ): F[Unit] =
    macro SpanMacro.addEventWithTimestamp

  /** Records information about the `Throwable` to the span.
    *
    * @param exception
    *   the `Throwable` to record
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  def recordException(
      exception: Throwable,
      attributes: Attribute[_]*
  ): F[Unit] =
    macro SpanMacro.recordException

  /** Sets the status to the span.
    *
    * Only the value of the last call will be recorded, and implementations are
    * free to ignore previous calls.
    *
    * @param status
    *   the [[Status]] to set
    */
  def setStatus(status: Status): F[Unit] =
    macro SpanMacro.setStatus

  /** Sets the status to the span.
    *
    * Only the value of the last call will be recorded, and implementations are
    * free to ignore previous calls.
    *
    * @param status
    *   the [[Status]] to set
    *
    * @param description
    *   the description of the [[Status]]
    */
  def setStatus(status: Status, description: String): F[Unit] =
    macro SpanMacro.setStatusWithDescription

}

object SpanMacro {
  import scala.reflect.macros.blackbox

  def addAttribute[A](c: blackbox.Context)(
      attribute: c.Expr[Attribute[A]]
  ): c.universe.Tree = {
    import c.universe._

    val backend = q"${c.prefix}.backend"
    val meta = q"$backend.meta"

    q"if ($meta.isEnabled) $backend.addAttributes($attribute) else $meta.unit"
  }

  def addAttributes(c: blackbox.Context)(
      attributes: c.Expr[Attribute[_]]*
  ): c.universe.Tree = {
    import c.universe._

    val backend = q"${c.prefix}.backend"
    val meta = q"$backend.meta"

    q"if ($meta.isEnabled) $backend.addAttributes(..$attributes) else $meta.unit"
  }

  def addEvent(c: blackbox.Context)(
      name: c.Expr[String],
      attributes: c.Expr[Attribute[_]]*
  ): c.universe.Tree = {
    import c.universe._

    val backend = q"${c.prefix}.backend"
    val meta = q"$backend.meta"

    q"if ($meta.isEnabled) $backend.addEvent($name, ..$attributes) else $meta.unit"
  }

  def addEventWithTimestamp(c: blackbox.Context)(
      name: c.Expr[String],
      timestamp: c.Expr[FiniteDuration],
      attributes: c.Expr[Attribute[_]]*
  ): c.universe.Tree = {
    import c.universe._

    val backend = q"${c.prefix}.backend"
    val meta = q"$backend.meta"

    q"if ($meta.isEnabled) $backend.addEvent($name, $timestamp, ..$attributes) else $meta.unit"
  }

  def recordException(c: blackbox.Context)(
      exception: c.Expr[Throwable],
      attributes: c.Expr[Attribute[_]]*
  ): c.universe.Tree = {
    import c.universe._

    val backend = q"${c.prefix}.backend"
    val meta = q"$backend.meta"

    q"if ($meta.isEnabled) $backend.recordException($exception, ..$attributes) else $meta.unit"
  }

  def setStatus(c: blackbox.Context)(
      status: c.Expr[Status]
  ): c.universe.Tree = {
    import c.universe._

    val backend = q"${c.prefix}.backend"
    val meta = q"$backend.meta"

    q"if ($meta.isEnabled) $backend.setStatus($status) else $meta.unit"
  }

  def setStatusWithDescription(c: blackbox.Context)(
      status: c.Expr[Status],
      description: c.Expr[String]
  ): c.universe.Tree = {
    import c.universe._

    val backend = q"${c.prefix}.backend"
    val meta = q"$backend.meta"

    q"if ($meta.isEnabled) $backend.setStatus($status, $description) else $meta.unit"
  }

}
