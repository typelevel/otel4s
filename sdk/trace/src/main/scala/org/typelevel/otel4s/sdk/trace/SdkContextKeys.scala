/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.trace

import cats.effect.SyncIO
import org.typelevel.otel4s.baggage.Baggage
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.trace.SpanContext

/** The keys that can be used by a third-party integrations.
  */
object SdkContextKeys {

  /** The [[org.typelevel.otel4s.trace.SpanContext SpanContext]] is stored under this key in the
    * [[org.typelevel.otel4s.sdk.context.Context Context]].
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

  /** The [[org.typelevel.otel4s.baggage.Baggage Baggage]] is stored under this key in the
    * [[org.typelevel.otel4s.sdk.context.Context Context]].
    *
    * To retrieve the baggage use:
    * {{{
    *   val context: Context = ???
    *   val baggage: Option[Baggage] = context.get(SdkContextKeys.BaggageKey)
    * }}}
    */
  val BaggageKey: Context.Key[Baggage] =
    Context.Key
      .unique[SyncIO, Baggage]("otel4s-trace-baggage-key")
      .unsafeRunSync()

}
