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
package trace

import cats.Applicative
import cats.effect.kernel.MonadCancelThrow
import cats.syntax.functor._

trait TracerBuilder[F[_]] {

  /** Assigns a version to the resulting Tracer.
    *
    * @param version
    *   the version of the instrumentation scope
    */
  def withVersion(version: String): TracerBuilder[F]

  /** Assigns an OpenTelemetry schema URL to the resulting Tracer.
    *
    * @param schemaUrl
    *   the URL of the OpenTelemetry schema
    */
  def withSchemaUrl(schemaUrl: String): TracerBuilder[F]

  /** Creates a [[Tracer]] with the given `version` and `schemaUrl` (if any).
    */
  def get: F[Tracer[F]]

  /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to `G`.
    */
  def liftTo[G[_]: MonadCancelThrow](implicit
      F: MonadCancelThrow[F],
      kt: KindTransformer[F, G]
  ): TracerBuilder[G] =
    new TracerBuilder.Lifted(this)

  @deprecated("use `liftTo` instead", since = "otel4s 0.14.0")
  def mapK[G[_]: MonadCancelThrow](implicit
      F: MonadCancelThrow[F],
      kt: KindTransformer[F, G]
  ): TracerBuilder[G] = liftTo[G]
}

object TracerBuilder {

  /** Creates a no-op implementation of the [[TracerBuilder]].
    *
    * A [[Tracer]] has no-op implementation too.
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  def noop[F[_]](implicit F: Applicative[F]): TracerBuilder[F] =
    new TracerBuilder[F] {
      def withVersion(version: String): TracerBuilder[F] = this
      def withSchemaUrl(schemaUrl: String): TracerBuilder[F] = this
      def get: F[Tracer[F]] = F.pure(Tracer.noop)
    }

  private class Lifted[F[_]: MonadCancelThrow, G[_]: MonadCancelThrow](
      builder: TracerBuilder[F]
  )(implicit
      kt: KindTransformer[F, G]
  ) extends TracerBuilder[G] {
    def withVersion(version: String): TracerBuilder[G] =
      new Lifted(builder.withVersion(version))
    def withSchemaUrl(schemaUrl: String): TracerBuilder[G] =
      new Lifted(builder.withSchemaUrl(schemaUrl))
    def get: G[Tracer[G]] =
      kt.liftK(builder.get.map(_.liftTo[G]))
  }
}
