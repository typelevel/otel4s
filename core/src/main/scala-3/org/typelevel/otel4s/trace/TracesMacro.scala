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

import scala.concurrent.duration.TimeUnit
import scala.quoted.*

private[otel4s] object TracesMacro {

  def span[F[_]](
      tracer: Expr[Tracer[F]],
      name: Expr[String],
      attributes: Expr[Seq[Attribute[_]]]
  )(using Quotes, Type[F]) =
    '{
      if ($tracer.meta.isEnabled)
        $tracer.spanBuilder($name).withAttributes($attributes*).createAuto
      else $tracer.meta.noopAutoSpan
    }

  def rootSpan[F[_]](
      tracer: Expr[Tracer[F]],
      name: Expr[String],
      attributes: Expr[Seq[Attribute[_]]]
  )(using Quotes, Type[F]) =
    '{
      if ($tracer.meta.isEnabled)
        $tracer.spanBuilder($name).root.withAttributes($attributes*).createAuto
      else $tracer.meta.noopAutoSpan
    }

  def resourceSpan[F[_], A](
      tracer: Expr[Tracer[F]],
      name: Expr[String],
      attributes: Expr[Seq[Attribute[_]]],
      resource: Expr[Resource[F, A]]
  )(using Quotes, Type[F], Type[A]) =
    '{
      if ($tracer.meta.isEnabled)
        $tracer
          .spanBuilder($name)
          .withAttributes($attributes*)
          .createRes($resource)
      else $tracer.meta.noopResSpan($resource)
    }

  def recordException[F[_]](
      span: Expr[Span[F]],
      exception: Expr[Throwable],
      attributes: Expr[Seq[Attribute[_]]]
  )(using Quotes, Type[F]) =
    '{
      if ($span.backend.meta.isEnabled)
        $span.backend.recordException($exception, $attributes*)
      else $span.backend.meta.unit
    }

  def setAttributes[F[_]](
      span: Expr[Span[F]],
      attributes: Expr[Seq[Attribute[_]]]
  )(using Quotes, Type[F]) =
    '{
      if ($span.backend.meta.isEnabled)
        $span.backend.setAttributes($attributes*)
      else $span.backend.meta.unit
    }

}
