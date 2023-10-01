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
  */
trait TextMapPropagator[Ctx] {

  /** The list of propagation fields. */
  def fields: List[String]

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
  def injected[A: TextMapUpdater](ctx: Ctx, carrier: A): A
}

object TextMapPropagator {

  /** Creates a [[TextMapPropagator]] which delegates injection and extraction
    * to the provided propagators.
    */
  def composite[Ctx](
      propagators: List[TextMapPropagator[Ctx]]
  ): TextMapPropagator[Ctx] =
    propagators match {
      case Nil         => new Noop[Ctx]
      case head :: Nil => head
      case _           => new Multi(propagators)
    }

  /** Creates a no-op implementation of the [[TextMapPropagator]].
    *
    * All propagation operations are no-op.
    */
  def noop[Ctx]: TextMapPropagator[Ctx] =
    new Noop

  private final class Noop[Ctx] extends TextMapPropagator[Ctx] {
    def fields: List[String] =
      Nil

    def extract[A: TextMapGetter](ctx: Ctx, carrier: A): Ctx =
      ctx

    def injected[A: TextMapUpdater](ctx: Ctx, carrier: A): A =
      carrier
  }

  private final class Multi[Ctx](
      propagators: List[TextMapPropagator[Ctx]]
  ) extends TextMapPropagator[Ctx] {
    val fields: List[String] =
      propagators.flatMap(_.fields)

    def extract[A: TextMapGetter](ctx: Ctx, carrier: A): Ctx =
      propagators.foldLeft(ctx) { (ctx, propagator) =>
        propagator.extract(ctx, carrier)
      }

    def injected[A: TextMapUpdater](ctx: Ctx, carrier: A): A =
      propagators.foldLeft(carrier) { (carrier, propagator) =>
        propagator.injected(ctx, carrier)
      }
  }
}
