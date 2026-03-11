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
import cats.Monad
import cats.mtl.LiftValue
import cats.syntax.functor._

/** A builder of the [[Logger]].
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/api/#get-a-logger]]
  */
sealed trait LoggerBuilder[F[_], Ctx] {

  /** Assigns a version to the resulting Logger.
    *
    * @param version
    *   the version of the instrumentation scope
    */
  def withVersion(version: String): LoggerBuilder[F, Ctx]

  /** Assigns an OpenTelemetry schema URL to the resulting Logger.
    *
    * @param schemaUrl
    *   the URL of the OpenTelemetry schema
    */
  def withSchemaUrl(schemaUrl: String): LoggerBuilder[F, Ctx]

  /** Creates a [[Logger]] with the given `version` and `schemaUrl` (if any).
    */
  def get: F[Logger[F, Ctx]]

  /** Modify the context `F` using an implicit [[cats.mtl.LiftValue]] from `F` to `G`.
    */
  def liftTo[G[_]](implicit G: Monad[G], lift: LiftValue[F, G]): LoggerBuilder[G, Ctx] =
    new LoggerBuilder.Lifted(this)
}

object LoggerBuilder {
  private[otel4s] trait Unsealed[F[_], Ctx] extends LoggerBuilder[F, Ctx]

  /** Creates a no-op implementation of the [[LoggerBuilder]].
    *
    * A [[Logger]] has no-op implementation too.
    *
    * @tparam F
    *   the higher-kinded type of polymorphic effect
    */
  def noop[F[_], Ctx](implicit F: Applicative[F]): LoggerBuilder[F, Ctx] =
    new LoggerBuilder[F, Ctx] {
      def withVersion(version: String): LoggerBuilder[F, Ctx] = this
      def withSchemaUrl(schemaUrl: String): LoggerBuilder[F, Ctx] = this
      def get: F[Logger[F, Ctx]] = F.pure(Logger.noop)
    }

  private class Lifted[F[_], G[_], Ctx](
      builder: LoggerBuilder[F, Ctx]
  )(implicit G: Monad[G], lift: LiftValue[F, G])
      extends LoggerBuilder[G, Ctx] {
    private[this] implicit def F: Applicative[F] = lift.applicativeF
    def withVersion(version: String): LoggerBuilder[G, Ctx] =
      new Lifted(builder.withVersion(version))
    def withSchemaUrl(schemaUrl: String): LoggerBuilder[G, Ctx] =
      new Lifted(builder.withSchemaUrl(schemaUrl))
    def get: G[Logger[G, Ctx]] =
      lift(builder.get.map(_.liftTo[G]))
  }
}
