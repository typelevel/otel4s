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

import cats.Applicative
import org.typelevel.vault.Vault

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
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
trait TextMapPropagator[F[_]] {

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
  def extract[A: TextMapGetter](ctx: Vault, carrier: A): Vault

  /** Injects data from the context into the given mutable `carrier` for
    * downstream consumers, for example as HTTP headers.
    *
    * @param ctx
    *   the context containing the value to be injected
    *
    * @param carrier
    *   holds propagation fields
    *
    * @tparam A
    *   the type of the carrier, which is mutable
    */
  def inject[A: TextMapSetter](ctx: Vault, carrier: A): F[Unit]

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
  def injected[A: TextMapInjector](ctx: Vault, carrier: A): A
}

object TextMapPropagator {

  def apply[F[_]](implicit ev: TextMapPropagator[F]): TextMapPropagator[F] = ev

  def noop[F[_]: Applicative]: TextMapPropagator[F] =
    new TextMapPropagator[F] {
      def extract[A: TextMapGetter](ctx: Vault, carrier: A): Vault =
        ctx

      def inject[A: TextMapSetter](ctx: Vault, carrier: A): F[Unit] =
        Applicative[F].unit

      def injected[A: TextMapInjector](ctx: Vault, carrier: A): A =
        carrier
    }
}
