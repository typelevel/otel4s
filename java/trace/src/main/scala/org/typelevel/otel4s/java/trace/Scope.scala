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
