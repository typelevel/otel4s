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

package org.typelevel.otel4s.metrics

import cats.Applicative

/** A registry for creating named [[Meter]].
  */
sealed trait MeterProvider[F[_]] {

  /** Creates a named [[Meter]].
    *
    * @example
    *   {{{
    * val meterProvider: MeterProvider[IO] = ???
    * val meter: IO[Meter[IO]] = meterProvider.get("com.service.runtime")
    *   }}}
    *
    * @param name
    *   the name of the instrumentation scope, such as the instrumentation library, package, or fully qualified class
    *   name
    */
  def get(name: String): F[Meter[F]] =
    meter(name).get

  /** Creates a [[MeterBuilder]] for a named [[Meter]] instance.
    *
    * @example
    *   {{{
    * val meterProvider: MeterProvider[IO] = ???
    * val meter: IO[Meter[IO]] = meterProvider
    *   .meter("com.service.runtime")
    *   .withVersion("1.0.0")
    *   .withSchemaUrl("https://opentelemetry.io/schema/v1.1.0")
    *   .get
    *   }}}
    *
    * @param name
    *   the name of the instrumentation scope, such as the instrumentation library, package, or fully qualified class
    *   name
    */
  def meter(name: String): MeterBuilder[F]

}

object MeterProvider {
  private[otel4s] trait Unsealed[F[_]] extends MeterProvider[F]

  def apply[F[_]](implicit ev: MeterProvider[F]): MeterProvider[F] = ev

  /** Creates a no-op implementation of the [[MeterProvider]].
    *
    * A [[MeterBuilder]] has no-op implementation too.
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  def noop[F[_]: Applicative]: MeterProvider[F] =
    new MeterProvider[F] {
      def meter(name: String): MeterBuilder[F] =
        MeterBuilder.noop
      override def toString: String =
        "MeterProvider.Noop"
    }
}
