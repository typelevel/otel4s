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

import scala.reflect.macros.blackbox

private[otel4s] object Macro {

  def add[A](
      c: blackbox.Context
  )(value: c.Expr[A], attributes: c.Expr[Attribute[_]]*): c.universe.Tree = {
    import c.universe._
    val backend = q"${c.prefix}.backend"

    q"if ($backend.isEnabled) $backend.add($value, ..$attributes) else $backend.unit"
  }

  def inc(
      c: blackbox.Context
  )(attributes: c.Expr[Attribute[_]]*): c.universe.Tree = {
    import c.universe._
    val backend = q"${c.prefix}.backend"

    q"if ($backend.isEnabled) $backend.inc(..$attributes) else $backend.unit"
  }

}
