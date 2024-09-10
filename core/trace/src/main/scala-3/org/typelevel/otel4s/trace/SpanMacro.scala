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

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration
import scala.quoted.*

private[otel4s] trait SpanMacro[F[_]] {
  self: Span[F] =>

  /** Adds an attribute to the span. If the span previously contained a mapping for the key, the old value is replaced
    * by the specified value.
    *
    * @param attribute
    *   the attribute to add to the span
    */
  inline def addAttribute[A](inline attribute: Attribute[A]): F[Unit] =
    ${ SpanMacro.addAttribute('self, 'attribute) }

  /** Adds attributes to the span. If the span previously contained a mapping for any of the keys, the old values are
    * replaced by the specified values.
    *
    * @param attributes
    *   the set of attributes to add to the span
    */
  inline def addAttributes(inline attributes: Attribute[_]*): F[Unit] =
    ${ SpanMacro.addAttributes('self, 'attributes) }

  /** Adds attributes to the span. If the span previously contained a mapping for any of the keys, the old values are
    * replaced by the specified values.
    *
    * @param attributes
    *   the set of attributes to add to the span
    */
  inline def addAttributes(
      inline attributes: immutable.Iterable[Attribute[_]]
  ): F[Unit] =
    ${ SpanMacro.addAttributes('self, 'attributes) }

  /** Adds an event to the span with the given attributes. The timestamp of the event will be the current time.
    *
    * @param name
    *   the name of the event
    *
    * @param attributes
    *   the set of attributes to associate with the event
    */
  inline def addEvent(
      inline name: String,
      inline attributes: Attribute[_]*
  ): F[Unit] =
    ${ SpanMacro.addEvent('self, 'name, 'attributes) }

  /** Adds an event to the span with the given attributes. The timestamp of the event will be the current time.
    *
    * @param name
    *   the name of the event
    *
    * @param attributes
    *   the set of attributes to associate with the event
    */
  inline def addEvent(
      inline name: String,
      inline attributes: immutable.Iterable[Attribute[_]]
  ): F[Unit] =
    ${ SpanMacro.addEvent('self, 'name, 'attributes) }

  /** Adds an event to the span with the given attributes and timestamp.
    *
    * '''Note''': the timestamp should be based on `Clock[F].realTime`. Using `Clock[F].monotonic` may lead to an
    * incorrect data.
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
  inline def addEvent(
      inline name: String,
      inline timestamp: FiniteDuration,
      inline attributes: Attribute[_]*
  ): F[Unit] =
    ${ SpanMacro.addEvent('self, 'name, 'timestamp, 'attributes) }

  /** Adds an event to the span with the given attributes and timestamp.
    *
    * '''Note''': the timestamp should be based on `Clock[F].realTime`. Using `Clock[F].monotonic` may lead to an
    * incorrect data.
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
  inline def addEvent(
      inline name: String,
      inline timestamp: FiniteDuration,
      inline attributes: immutable.Iterable[Attribute[_]]
  ): F[Unit] =
    ${ SpanMacro.addEvent('self, 'name, 'timestamp, 'attributes) }

  /** Adds a link to the span.
    *
    * Links are used to link spans in different traces. Used (for example) in batching operations, where a single batch
    * handler processes multiple requests from different traces or the same trace.
    *
    * @param spanContext
    *   the context of the linked span
    *
    * @param attributes
    *   the set of attributes to associated with the link
    */
  inline def addLink(
      spanContext: SpanContext,
      attributes: Attribute[_]*
  ): F[Unit] =
    ${ SpanMacro.addLink('self, 'spanContext, 'attributes) }

  /** Adds a link to the span.
    *
    * Links are used to link spans in different traces. Used (for example) in batching operations, where a single batch
    * handler processes multiple requests from different traces or the same trace.
    *
    * @param spanContext
    *   the context of the linked span
    *
    * @param attributes
    *   the set of attributes to associated with the link
    */
  inline def addLink(
      spanContext: SpanContext,
      attributes: immutable.Iterable[Attribute[_]]
  ): F[Unit] =
    ${ SpanMacro.addLink('self, 'spanContext, 'attributes) }

  /** Records information about the `Throwable` to the span.
    *
    * @param exception
    *   the `Throwable` to record
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  inline def recordException(
      inline exception: Throwable,
      inline attributes: Attribute[_]*
  ): F[Unit] =
    ${ SpanMacro.recordException('self, 'exception, 'attributes) }

  /** Records information about the `Throwable` to the span.
    *
    * @param exception
    *   the `Throwable` to record
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  inline def recordException(
      inline exception: Throwable,
      inline attributes: immutable.Iterable[Attribute[_]]
  ): F[Unit] =
    ${ SpanMacro.recordException('self, 'exception, 'attributes) }

  /** Sets the status to the span.
    *
    * Only the value of the last call will be recorded, and implementations are free to ignore previous calls.
    *
    * @param status
    *   the [[StatusCode]] to set
    */
  inline def setStatus(inline status: StatusCode): F[Unit] =
    ${ SpanMacro.setStatus('self, 'status) }

  /** Sets the status to the span.
    *
    * Only the value of the last call will be recorded, and implementations are free to ignore previous calls.
    *
    * @param status
    *   the [[StatusCode]] to set
    *
    * @param description
    *   the description of the [[StatusCode]]
    */
  inline def setStatus(
      inline status: StatusCode,
      inline description: String
  ): F[Unit] =
    ${ SpanMacro.setStatus('self, 'status, 'description) }

}

object SpanMacro {

  def addAttribute[F[_], A](
      span: Expr[Span[F]],
      attribute: Expr[Attribute[A]]
  )(using Quotes, Type[F], Type[A]) =
    '{
      if ($span.backend.meta.isEnabled)
        $span.backend.addAttributes(List($attribute))
      else $span.backend.meta.unit
    }

  def addAttributes[F[_]](
      span: Expr[Span[F]],
      attributes: Expr[immutable.Iterable[Attribute[_]]]
  )(using Quotes, Type[F]) =
    '{
      if ($span.backend.meta.isEnabled)
        $span.backend.addAttributes($attributes)
      else $span.backend.meta.unit
    }

  def addEvent[F[_]](
      span: Expr[Span[F]],
      name: Expr[String],
      attributes: Expr[immutable.Iterable[Attribute[_]]]
  )(using Quotes, Type[F]) =
    '{
      if ($span.backend.meta.isEnabled)
        $span.backend.addEvent($name, $attributes)
      else $span.backend.meta.unit
    }

  def addEvent[F[_]](
      span: Expr[Span[F]],
      name: Expr[String],
      timestamp: Expr[FiniteDuration],
      attributes: Expr[immutable.Iterable[Attribute[_]]]
  )(using Quotes, Type[F]) =
    '{
      if ($span.backend.meta.isEnabled)
        $span.backend.addEvent($name, $timestamp, $attributes)
      else $span.backend.meta.unit
    }

  def addLink[F[_]](
      span: Expr[Span[F]],
      spanContext: Expr[SpanContext],
      attributes: Expr[immutable.Iterable[Attribute[_]]]
  )(using Quotes, Type[F]) =
    '{
      if ($span.backend.meta.isEnabled)
        $span.backend.addLink($spanContext, $attributes)
      else $span.backend.meta.unit
    }

  def recordException[F[_]](
      span: Expr[Span[F]],
      exception: Expr[Throwable],
      attributes: Expr[immutable.Iterable[Attribute[_]]]
  )(using Quotes, Type[F]) =
    '{
      if ($span.backend.meta.isEnabled)
        $span.backend.recordException($exception, $attributes)
      else $span.backend.meta.unit
    }

  def setStatus[F[_]](
      span: Expr[Span[F]],
      status: Expr[StatusCode]
  )(using Quotes, Type[F]) =
    '{
      if ($span.backend.meta.isEnabled)
        $span.backend.setStatus($status)
      else $span.backend.meta.unit
    }

  def setStatus[F[_]](
      span: Expr[Span[F]],
      status: Expr[StatusCode],
      description: Expr[String]
  )(using Quotes, Type[F]) =
    '{
      if ($span.backend.meta.isEnabled)
        $span.backend.setStatus($status, $description)
      else $span.backend.meta.unit
    }

}
