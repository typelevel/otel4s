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

import scala.collection.immutable
import scala.quoted.*

private[otel4s] trait CounterMacro[F[_], A] {
  def backend: Counter.Backend[F, A]

  /** Records a value with a set of attributes.
    *
    * @param value
    *   the value to increment a counter with. Must be '''non-negative'''
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  inline def add(inline value: A, inline attributes: Attribute[_]*): F[Unit] =
    ${ CounterMacro.add('backend, 'value, 'attributes) }

  /** Records a value with a set of attributes.
    *
    * @param value
    *   the value to increment a counter with. Must be '''non-negative'''
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  inline def add(
      inline value: A,
      inline attributes: immutable.Iterable[Attribute[_]]
  ): F[Unit] =
    ${ CounterMacro.add('backend, 'value, 'attributes) }

  /** Increments a counter by one.
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  inline def inc(inline attributes: Attribute[_]*): F[Unit] =
    ${ CounterMacro.inc('backend, 'attributes) }

  /** Increments a counter by one.
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  inline def inc(inline attributes: immutable.Iterable[Attribute[_]]): F[Unit] =
    ${ CounterMacro.inc('backend, 'attributes) }

}

object CounterMacro {

  def add[F[_], A](
      backend: Expr[Counter.Backend[F, A]],
      value: Expr[A],
      attributes: Expr[immutable.Iterable[Attribute[_]]]
  )(using Quotes, Type[F], Type[A]) =
    '{
      if ($backend.meta.isEnabled) $backend.add($value, $attributes)
      else $backend.meta.unit
    }

  def inc[F[_], A](
      backend: Expr[Counter.Backend[F, A]],
      attributes: Expr[immutable.Iterable[Attribute[_]]]
  )(using Quotes, Type[F], Type[A]) =
    '{
      if ($backend.meta.isEnabled) $backend.inc($attributes)
      else $backend.meta.unit
    }

}
