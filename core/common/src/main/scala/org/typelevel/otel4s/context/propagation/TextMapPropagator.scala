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

import cats.Monoid
import cats.syntax.foldable._

/** The process of propagating data across process boundaries involves injecting
  * and extracting values in the form of text into carriers that travel in-band.
  *
  * The encoding used for this process is expected to conform to HTTP Header
  * Field semantics, and values are commonly encoded as request headers for
  * RPC/HTTP requests.
  *
  * The carriers used for propagating the data are typically HTTP requests, and
  * the process is often implemented using library-specific request
  * interceptors. On the client side, values are injected into the carriers,
  * while on the server side, values are extracted from them.
  *
  * @tparam Ctx
  *   the context to use to extract or inject data
  */
trait TextMapPropagator[Ctx] {

  /** The collection of propagation fields. */
  def fields: Iterable[String]

  /** Extracts key-value pairs from the given `carrier` and adds them to the
    * given context.
    *
    * @param ctx
    *   the context object to which the key-value pairs are added
    *
    * @param carrier
    *   holds propagation fields
    *
    * @tparam A
    *   the type of the carrier object
    *
    * @return
    *   the new context with stored key-value pairs
    */
  def extract[A: TextMapGetter](ctx: Ctx, carrier: A): Ctx

  /** Injects data from the context into a copy of the given immutable `carrier`
    * for downstream consumers, for example as HTTP headers.
    *
    * This method is an extension to the OpenTelemetry specification to support
    * immutable carrier types.
    *
    * @param ctx
    *   the context containing the value to be injected
    *
    * @param carrier
    *   holds propagation fields
    *
    * @tparam A
    *   the type of the carrier
    *
    * @return
    *   a copy of the carrier, with new fields injected
    */
  def inject[A: TextMapUpdater](ctx: Ctx, carrier: A): A
}

object TextMapPropagator {

  /** Creates a [[TextMapPropagator]] which delegates injection and extraction
    * to the provided propagators.
    *
    * @example
    *   {{{
    * val w3cPropagator: TextMapPropagator[Context] = ???
    * val httpTracePropagator: TextMapPropagator[Context] = ???
    * val textMapPropagator = TextMapPropagator.of(w3cPropagator, httpTracePropagator)
    *   }}}
    *
    * @param propagators
    *   the propagators to use for injection and extraction
    *
    * @tparam Ctx
    *   the context to use to extract or inject data
    */
  def of[Ctx](
      propagators: TextMapPropagator[Ctx]*
  ): TextMapPropagator[Ctx] = {
    // reference stability for noop
    if (propagators.lengthIs == 1) propagators.head
    else propagators.combineAll
  }

  /** Creates a no-op implementation of the [[TextMapPropagator]].
    *
    * All propagation operations are no-op.
    */
  def noop[Ctx]: TextMapPropagator[Ctx] =
    new Noop

  implicit def textMapPropagatorMonoid[Ctx]: Monoid[TextMapPropagator[Ctx]] =
    new Monoid[TextMapPropagator[Ctx]] {
      val empty: TextMapPropagator[Ctx] =
        noop[Ctx]

      def combine(
          x: TextMapPropagator[Ctx],
          y: TextMapPropagator[Ctx]
      ): TextMapPropagator[Ctx] =
        (x, y) match {
          case (that, _: Noop[Ctx]) =>
            that
          case (_: Noop[Ctx], other) =>
            other
          case (that: Multi[Ctx], other: Multi[Ctx]) =>
            multi(that.propagators ++ other.propagators)
          case (that: Multi[Ctx], other) =>
            multi(that.propagators :+ other)
          case (that, other: Multi[Ctx]) =>
            multi(that +: other.propagators)
          case (that, other) =>
            multi(List(that, other))
        }

      private def multi(propagators: List[TextMapPropagator[Ctx]]): Multi[Ctx] =
        Multi(propagators, propagators.flatMap(_.fields).distinct)
    }

  private final class Noop[Ctx] extends TextMapPropagator[Ctx] {
    def fields: Iterable[String] =
      Nil

    def extract[A: TextMapGetter](ctx: Ctx, carrier: A): Ctx =
      ctx

    def inject[A: TextMapUpdater](ctx: Ctx, carrier: A): A =
      carrier

    override def toString: String = "TextMapPropagator.Noop"
  }

  private final case class Multi[Ctx](
      propagators: List[TextMapPropagator[Ctx]],
      fields: List[String]
  ) extends TextMapPropagator[Ctx] {
    def extract[A: TextMapGetter](ctx: Ctx, carrier: A): Ctx =
      propagators.foldLeft(ctx) { (ctx, propagator) =>
        propagator.extract(ctx, carrier)
      }

    def inject[A: TextMapUpdater](ctx: Ctx, carrier: A): A =
      propagators.foldLeft(carrier) { (carrier, propagator) =>
        propagator.inject(ctx, carrier)
      }

    override def toString: String =
      s"TextMapPropagator.Multi(${propagators.map(_.toString).mkString(", ")})"
  }

}
