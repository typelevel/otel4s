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
import scala.concurrent.duration.TimeUnit

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
  def record(value: A, attributes: Attribute[_]*): F[Unit] =
    macro HistogramMacro.record[A]

  /** Records a value with a set of attributes.
    *
    * @param value
    *   the value to record
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  def record(value: A, attributes: immutable.Iterable[Attribute[_]]): F[Unit] =
    macro HistogramMacro.recordColl[A]

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
    *    }
    *   }}}
    *
    * @param timeUnit
    *   the time unit of the duration measurement
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  def recordDuration(
      timeUnit: TimeUnit,
      attributes: Attribute[_]*
  ): Resource[F, Unit] =
    macro HistogramMacro.recordDuration

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
    *    }
    *   }}}
    *
    * @param timeUnit
    *   the time unit of the duration measurement
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  def recordDuration(
      timeUnit: TimeUnit,
      attributes: immutable.Iterable[Attribute[_]]
  ): Resource[F, Unit] =
    macro HistogramMacro.recordDurationColl

  /** Records duration of the given effect.
    *
    * @example
    *   {{{
    * val histogram: Histogram[F] = ???
    * val attributeKey = AttributeKey.string("query_name")
    *
    * def attributes(queryName: String)(ec: Resource.ExitCase): Attributes = {
    *  val ecAttributes = ec match {
    *    case Resource.ExitCase.Succeeded  => Attributes.empty
    *    case Resource.ExitCase.Errored(e) => Attributes(Attribute("error.type", e.getClass.getName))
    *    case Resource.ExitCase.Canceled   => Attributes(Attribute("error.type", "canceled"))
    *  }
    *
    *  ecAttributes :+ Attribute(attributeKey, queryName)
    * }
    *
    * def findUser(name: String) =
    *   histogram.recordDuration(TimeUnit.MILLISECONDS, attributes("find_user")).use { _ =>
    *     db.findUser(name)
    *    }
    *   }}}
    *
    * @param timeUnit
    *   the time unit of the duration measurement
    *
    * @param attributes
    *   the function to build attributes to associate with the value
    */
  def recordDuration(
      timeUnit: TimeUnit,
      attributes: Resource.ExitCase => immutable.Iterable[Attribute[_]]
  ): Resource[F, Unit] =
    macro HistogramMacro.recordDurationFuncColl

}

object HistogramMacro {
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

  def recordDuration(c: blackbox.Context)(
      timeUnit: c.Expr[TimeUnit],
      attributes: c.Expr[Attribute[_]]*
  ): c.universe.Tree = {
    import c.universe._
    recordDurationColl(c)(timeUnit, c.Expr(q"_root_.scala.Seq(..$attributes)"))
  }

  def recordDurationColl(c: blackbox.Context)(
      timeUnit: c.Expr[TimeUnit],
      attributes: c.Expr[immutable.Iterable[Attribute[_]]]
  ): c.universe.Tree = {
    import c.universe._
    val backend = q"${c.prefix}.backend"
    val meta = q"$backend.meta"

    q"""
       _root_.cats.effect.kernel.Resource.eval($meta.isEnabled).flatMap { isEnabled =>
         if (isEnabled) $backend.recordDuration($timeUnit, _ => $attributes)
         else _root_.cats.effect.kernel.Resource.unit
       }
     """
  }

  def recordDurationFuncColl(c: blackbox.Context)(
      timeUnit: c.Expr[TimeUnit],
      attributes: c.Expr[Resource.ExitCase => immutable.Iterable[Attribute[_]]]
  ): c.universe.Tree = {
    import c.universe._
    val backend = q"${c.prefix}.backend"
    val meta = q"$backend.meta"

    q"""
       _root_.cats.effect.kernel.Resource.eval($meta.isEnabled).flatMap { isEnabled =>
         if (isEnabled) $backend.recordDuration($timeUnit, $attributes)
         else _root_.cats.effect.kernel.Resource.unit
       }
     """
  }

}
