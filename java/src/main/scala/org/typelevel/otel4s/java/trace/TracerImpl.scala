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

package org.typelevel.otel4s.java.trace

import cats.effect.Sync
import io.opentelemetry.api.trace.{Tracer => JTracer}
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.Tracer

private[trace] class TracerImpl[F[_]: Sync](
    jTracer: JTracer,
    scope: TraceScope[F]
) extends Tracer[F] {

  val meta: Tracer.Meta[F] =
    Tracer.Meta.enabled

  def spanBuilder(name: String): SpanBuilder[F] =
    new SpanBuilderImpl[F](jTracer, name, scope)

  def childOf(s: Span[F]): Tracer[F] =
    new TracerImpl(jTracer, scope) {
      override def spanBuilder(name: String): SpanBuilder[F] =
        s.backend.child(name)
    }
}
