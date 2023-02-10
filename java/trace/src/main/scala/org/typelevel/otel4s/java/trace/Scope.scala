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

import io.opentelemetry.context.{Context => JContext}
import io.opentelemetry.api.trace.{Span => JSpan}
import org.typelevel.otel4s.trace.SpanContext

sealed trait Scope

object Scope {
  val root: Scope = Root(JContext.root)

  private[trace] final case class Root(ctx: JContext) extends Scope
  private[trace] final case class Span(
      ctx: JContext,
      span: JSpan,
      spanContext: SpanContext
  ) extends Scope
  private[trace] case object Noop extends Scope
}
