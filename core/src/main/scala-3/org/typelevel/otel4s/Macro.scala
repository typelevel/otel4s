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

import scala.quoted.*

private[otel4s] object Macro {

  def add[F[_], A](
      backend: Expr[Counter.Backend[F, A]],
      value: Expr[A],
      attributes: Expr[Seq[Attribute[_]]]
  )(using Quotes, Type[F], Type[A]) =
    '{
      if ($backend.isEnabled) $backend.add($value, $attributes*)
      else $backend.unit
    }

  def inc[F[_], A](
      backend: Expr[Counter.Backend[F, A]],
      attributes: Expr[Seq[Attribute[_]]]
  )(using Quotes, Type[F], Type[A]) =
    '{ if ($backend.isEnabled) $backend.inc($attributes*) else $backend.unit }

}
