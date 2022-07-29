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

import cats.effect.Resource

import scala.reflect.macros.blackbox

private[otel4s] object TracesMacro {

  def span(c: blackbox.Context)(
      name: c.Expr[String],
      attributes: c.Expr[Attribute[_]]*
  ): c.universe.Tree = {
    import c.universe._
    val meta = q"${c.prefix}.meta"

    q"if ($meta.isEnabled) ${c.prefix}.spanBuilder($name).withAttributes(..$attributes).createAuto else $meta.noopAutoSpan"
  }

  def rootSpan(c: blackbox.Context)(
      name: c.Expr[String],
      attributes: c.Expr[Attribute[_]]*
  ): c.universe.Tree = {
    import c.universe._
    val meta = q"${c.prefix}.meta"

    q"if ($meta.isEnabled) ${c.prefix}.spanBuilder($name).root.withAttributes(..$attributes).createAuto else $meta.noopAutoSpan"
  }

  def resourceSpan[F[_], A](c: blackbox.Context)(
      name: c.Expr[String],
      attributes: c.Expr[Attribute[_]]*
  )(resource: c.Expr[Resource[F, A]]): c.universe.Tree = {
    import c.universe._
    val meta = q"${c.prefix}.meta"

    q"if ($meta.isEnabled) ${c.prefix}.spanBuilder($name).withAttributes(..$attributes).createRes($resource) else $meta.noopResSpan($resource)"
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

  def setAttributes(c: blackbox.Context)(
      attributes: c.Expr[Attribute[_]]*
  ): c.universe.Tree = {
    import c.universe._
    val backend = q"${c.prefix}.backend"
    val meta = q"$backend.meta"

    q"if ($meta.isEnabled) $backend.setAttributes(..$attributes) else $meta.unit"
  }

}
