package org.typelevel.otel4s.sdk.trace

import cats.effect.SyncIO
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.trace.SpanContext

/** The keys that may be used by a third-party integrations.
  */
object SdkContextKeys {

  /** The [[SpanContext]] is stored under this key in the [[Context]].
    *
    * To retrieve the context use:
    * {{{
    *   val context: Context = ???
    *   val spanContext: Option[SpanContext] = context.get(SdkContextKeys.SpanContextKey)
    * }}}
    */
  val SpanContextKey: Context.Key[SpanContext] =
    Context.Key
      .unique[SyncIO, SpanContext]("otel4s-trace-span-context-key")
      .unsafeRunSync()

}
