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
package metrics

import cats.effect.kernel.Resource

import scala.collection.immutable
import scala.concurrent.duration.*
import scala.quoted.*

private[otel4s] trait HistogramMacro[F[_], A] {
  def backend: Histogram.Backend[F, A]

  /** Records a value with a set of attributes.
    *
    * @param value
    *   the value to record
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  inline def record(
      inline value: A,
      inline attributes: Attribute[_]*
  ): F[Unit] =
    ${ HistogramMacro.record('backend, 'value, 'attributes) }

  /** Records a value with a set of attributes.
    *
    * @param value
    *   the value to record
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  inline def record(
      inline value: A,
      inline attributes: immutable.Iterable[Attribute[_]]
  ): F[Unit] =
    ${ HistogramMacro.record('backend, 'value, 'attributes) }

  /** Records duration of the given effect.
    *
    * @example
    *   {{{
    * val histogram: Histogram[F] = ???
    * val attributeKey = AttributeKey.string("query_name")
    *
    * def findUser(name: String) =
    *   histogram.recordDuration(TimeUnit.MILLISECONDS, Attribute(attributeKey, "find_user")).use { _ =>
    *     db.findUser(name)
    *   }
    *   }}}
    *
    * @param timeUnit
    *   the time unit of the duration measurement
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  inline def recordDuration(
      inline timeUnit: TimeUnit,
      inline attributes: Attribute[_]*
  ): Resource[F, Unit] =
    ${ HistogramMacro.recordDuration('backend, 'timeUnit, 'attributes) }

  /** Records duration of the given effect.
    *
    * @example
    *   {{{
    * val histogram: Histogram[F] = ???
    * val attributeKey = AttributeKey.string("query_name")
    *
    * def findUser(name: String) =
    *   histogram.recordDuration(TimeUnit.MILLISECONDS, Attributes(Attribute(attributeKey, "find_user"))).use { _ =>
    *     db.findUser(name)
    *   }
    *   }}}
    *
    * @param timeUnit
    *   the time unit of the duration measurement
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  inline def recordDuration(
      inline timeUnit: TimeUnit,
      inline attributes: immutable.Iterable[Attribute[_]]
  ): Resource[F, Unit] =
    ${ HistogramMacro.recordDuration('backend, 'timeUnit, 'attributes) }

}

object HistogramMacro {

  def record[F[_], A](
      backend: Expr[Histogram.Backend[F, A]],
      value: Expr[A],
      attributes: Expr[immutable.Iterable[Attribute[_]]]
  )(using Quotes, Type[F], Type[A]) =
    '{
      if ($backend.meta.isEnabled) $backend.record($value, $attributes)
      else $backend.meta.unit
    }

  def recordDuration[F[_], A](
      backend: Expr[Histogram.Backend[F, A]],
      timeUnit: Expr[TimeUnit],
      attributes: Expr[immutable.Iterable[Attribute[_]]]
  )(using Quotes, Type[F], Type[A]) =
    '{
      if ($backend.meta.isEnabled)
        $backend.recordDuration($timeUnit, $attributes)
      else _root_.cats.effect.kernel.Resource.unit
    }

}
