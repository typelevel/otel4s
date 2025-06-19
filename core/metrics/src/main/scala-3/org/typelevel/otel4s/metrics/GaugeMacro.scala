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

private[otel4s] trait GaugeMacro[F[_], A] {
  def backend: Gauge.Backend[F, A]

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
    ${ GaugeMacro.record('backend, 'value, 'attributes) }

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
    ${ GaugeMacro.record('backend, 'value, 'attributes) }

}

object GaugeMacro {

  def record[F[_], A](
      backend: Expr[Gauge.Backend[F, A]],
      value: Expr[A],
      attributes: Expr[immutable.Iterable[Attribute[_]]]
  )(using Quotes, Type[F], Type[A]) =
    '{ $backend.meta.whenEnabled($backend.record($value, $attributes)) }

}
