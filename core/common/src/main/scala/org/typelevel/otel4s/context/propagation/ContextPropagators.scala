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

package org.typelevel.otel4s.context.propagation

/** A container of the registered propagators for every supported format.
  *
  * @tparam Ctx
  *   the type of the Context
  */
sealed trait ContextPropagators[Ctx] {

  /** Returns a text map propagator to extract or inject data.
    */
  def textMapPropagator: TextMapPropagator[Ctx]
}

object ContextPropagators {

  /** Creates a [[ContextPropagators]] which can be used to extract and inject context in text payloads with the given
    * [[TextMapPropagator]].
    *
    * If multiple text map propagators are passed, the combined (composite) TextMapPropagator instance will be created.
    *
    * It's a shortcut for:
    * {{{
    * ContextPropagators.of(TextMapPropagator.of(w3cPropagator, httpTracePropagator))
    * }}}
    *
    * @example
    *   {{{
    * val w3cPropagator: TextMapPropagator[Context] = ???
    * val httpTracePropagator: TextMapPropagator[Context] = ???
    * val contextPropagators = ContextPropagators.of(w3cPropagator, httpTracePropagator)
    *   }}}
    *
    * @see
    *   [[TextMapPropagator.of]]
    *
    * @param textMapPropagators
    *   the propagators to use for injection and extraction
    *
    * @tparam Ctx
    *   the context to use to extract or inject data
    */
  def of[Ctx](
      textMapPropagators: TextMapPropagator[Ctx]*
  ): ContextPropagators[Ctx] =
    new Default(TextMapPropagator.of(textMapPropagators: _*))

  /** Creates a no-op implementation of the [[ContextPropagators]].
    *
    * A [[TextMapPropagator]] has no-op implementation too.
    */
  def noop[Ctx]: ContextPropagators[Ctx] =
    new Noop

  private final class Noop[Ctx] extends ContextPropagators[Ctx] {
    val textMapPropagator: TextMapPropagator[Ctx] =
      TextMapPropagator.noop

    override def toString: String =
      "ContextPropagators.Noop"
  }

  private final class Default[Ctx](
      val textMapPropagator: TextMapPropagator[Ctx]
  ) extends ContextPropagators[Ctx] {
    override def toString: String =
      s"ContextPropagators.Default{textMapPropagator=${textMapPropagator.toString}}"
  }

}
