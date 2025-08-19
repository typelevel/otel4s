/*
 * Copyright 2025 Typelevel
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
package logs

import cats.Applicative
import cats.Functor
import cats.Monad
import cats.syntax.functor._

/** The entry point of the logging API. Provides access to [[Logger]].
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/api/#loggerprovider]]
  */
sealed trait LoggerProvider[F[_], Ctx] {

  /** Creates a named [[Logger]].
    *
    * @example
    *   {{{
    * val loggerProvider: LoggerProvider[IO] = ???
    * val logger: IO[Logger[IO]] = loggerProvider.get("com.service.runtime")
    *   }}}
    *
    * @param name
    *   the name of the instrumentation scope, such as the instrumentation library, package, or fully qualified class
    *   name
    */
  def get(name: String): F[Logger[F, Ctx]] =
    logger(name).get

  /** Creates a [[LoggerBuilder]] for a named [[Logger]] instance.
    *
    * @example
    *   {{{
    * val loggerProvider: LoggerProvider[IO] = ???
    * val logger: IO[Logger[IO]] = loggerProvider
    *   .logger("com.service.runtime")
    *   .withVersion("1.0.0")
    *   .withSchemaUrl("https://opentelemetry.io/schema/v1.1.0")
    *   .get
    *   }}}
    *
    * @param name
    *   the name of the instrumentation scope, such as the instrumentation library, package, or fully qualified class
    *   name
    */
  def logger(name: String): LoggerBuilder[F, Ctx]

  /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to `G`.
    */
  def mapK[G[_]](implicit F: Functor[F], G: Monad[G], kt: KindTransformer[F, G]): LoggerProvider[G, Ctx] =
    new LoggerProvider.MappedK(this)
}

object LoggerProvider {
  private[otel4s] trait Unsealed[F[_], Ctx] extends LoggerProvider[F, Ctx]

  def apply[F[_], Ctx](implicit ev: LoggerProvider[F, Ctx]): LoggerProvider[F, Ctx] = ev

  /** Creates a no-op implementation of the [[LoggerProvider]].
    *
    * A [[LoggerBuilder]] has no-op implementation too.
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  def noop[F[_]: Applicative, Ctx]: LoggerProvider[F, Ctx] =
    new LoggerProvider[F, Ctx] {
      def logger(name: String): LoggerBuilder[F, Ctx] =
        LoggerBuilder.noop
      override def toString: String =
        "LoggerProvider.Noop"
    }

  private class MappedK[F[_]: Functor, G[_]: Monad, Ctx](
      provider: LoggerProvider[F, Ctx]
  )(implicit kt: KindTransformer[F, G])
      extends LoggerProvider[G, Ctx] {
    override def get(name: String): G[Logger[G, Ctx]] =
      kt.liftK(provider.get(name).map(_.mapK[G]))
    def logger(name: String): LoggerBuilder[G, Ctx] =
      provider.logger(name).mapK[G]
  }
}
