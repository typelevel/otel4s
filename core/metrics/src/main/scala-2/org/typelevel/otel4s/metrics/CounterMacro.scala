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
  def add(value: A, attributes: Attribute[_]*): F[Unit] =
    macro CounterMacro.add[A]

  /** Records a value with a set of attributes.
    *
    * @param value
    *   the value to increment a counter with. Must be '''non-negative'''
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  def add(value: A, attributes: immutable.Iterable[Attribute[_]]): F[Unit] =
    macro CounterMacro.addColl[A]

  /** Increments a counter by one.
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  def inc(attributes: Attribute[_]*): F[Unit] =
    macro CounterMacro.inc

  /** Increments a counter by one.
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  def inc(attributes: immutable.Iterable[Attribute[_]]): F[Unit] =
    macro CounterMacro.incColl

}

object CounterMacro {
  import scala.reflect.macros.blackbox

  def add[A](c: blackbox.Context)(
      value: c.Expr[A],
      attributes: c.Expr[Attribute[_]]*
  ): c.universe.Tree = {
    import c.universe._
    addColl(c)(value, c.Expr(q"_root_.scala.Seq(..$attributes)"))
  }

  def addColl[A](c: blackbox.Context)(
      value: c.Expr[A],
      attributes: c.Expr[immutable.Iterable[Attribute[_]]]
  ): c.universe.Tree = {
    import c.universe._
    val backend = q"${c.prefix}.backend"
    val meta = q"$backend.meta"

    q"$meta.ifEnabled($backend.add($value, $attributes))"
  }

  def inc(c: blackbox.Context)(
      attributes: c.Expr[Attribute[_]]*
  ): c.universe.Tree = {
    import c.universe._
    incColl(c)(c.Expr(q"_root_.scala.Seq(..$attributes)"))
  }

  def incColl(c: blackbox.Context)(
      attributes: c.Expr[immutable.Iterable[Attribute[_]]]
  ): c.universe.Tree = {
    import c.universe._
    val backend = q"${c.prefix}.backend"
    val meta = q"$backend.meta"

    q"$meta.ifEnabled($backend.inc($attributes))"
  }

}
