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
  def record(value: A, attributes: Attribute[_]*): F[Unit] =
    macro GaugeMacro.record[A]

  /** Records a value with a set of attributes.
    *
    * @param value
    *   the value to record
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  def record(value: A, attributes: immutable.Iterable[Attribute[_]]): F[Unit] =
    macro GaugeMacro.recordColl[A]

}

object GaugeMacro {
  import scala.reflect.macros.blackbox

  def record[A](c: blackbox.Context)(
      value: c.Expr[A],
      attributes: c.Expr[Attribute[_]]*
  ): c.universe.Tree = {
    import c.universe._
    recordColl(c)(value, c.Expr(q"_root_.scala.Seq(..$attributes)"))
  }

  def recordColl[A](c: blackbox.Context)(
      value: c.Expr[A],
      attributes: c.Expr[immutable.Iterable[Attribute[_]]]
  ): c.universe.Tree = {
    import c.universe._
    val backend = q"${c.prefix}.backend"
    val meta = q"$backend.meta"

    q"$meta.whenEnabled($backend.record($value, $attributes))"
  }

}
