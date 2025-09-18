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

package org.typelevel.otel4s.metrics.meta

import cats.Applicative
import cats.Monad
import cats.~>
import org.typelevel.otel4s.KindTransformer

sealed trait InstrumentMeta[F[_]] extends org.typelevel.otel4s.meta.InstrumentMeta.Dynamic.Unsealed[F] {

  /** Indicates whether instrumentation is enabled or not.
    */
  def isEnabled: F[Boolean]

  /** A no-op effect.
    */
  def unit: F[Unit]

  /** Runs `f` only when the instrument is enabled. A shortcut for `Monad[F].ifM(isEnabled)(f, unit)`.
    */
  def whenEnabled(f: => F[Unit]): F[Unit]

  /** Modify the context `F` using the transformation `f`. */
  override def mapK[G[_]: Monad](f: F ~> G): InstrumentMeta[G] =
    new InstrumentMeta.MappedK(this)(f)

  /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to `G`.
    */
  override def liftTo[G[_]: Monad](implicit kt: KindTransformer[F, G]): InstrumentMeta[G] =
    new InstrumentMeta.MappedK(this)(kt.liftK)

}

object InstrumentMeta {

  private[otel4s] def from[F[_]: Monad](enabled: F[Boolean]): InstrumentMeta[F] =
    new InstrumentMeta[F] {
      def isEnabled: F[Boolean] = enabled
      def unit: F[Unit] = Monad[F].unit
      def whenEnabled(f: => F[Unit]): F[Unit] = Monad[F].ifM(isEnabled)(f, unit)
    }

  private[otel4s] def enabled[F[_]: Applicative]: InstrumentMeta[F] =
    new InstrumentMeta[F] {
      val isEnabled: F[Boolean] = Applicative[F].pure(true)
      val unit: F[Unit] = Applicative[F].unit
      def whenEnabled(f: => F[Unit]): F[Unit] = f
    }

  private[otel4s] def disabled[F[_]: Applicative]: InstrumentMeta[F] =
    new InstrumentMeta[F] {
      val isEnabled: F[Boolean] = Applicative[F].pure(false)
      val unit: F[Unit] = Applicative[F].unit
      def whenEnabled(f: => F[Unit]): F[Unit] = unit
    }

  private class MappedK[F[_], G[_]: Monad](meta: InstrumentMeta[F])(f: F ~> G) extends InstrumentMeta[G] {
    def isEnabled: G[Boolean] = f(meta.isEnabled)
    def unit: G[Unit] = f(meta.unit)
    def whenEnabled(f: => G[Unit]): G[Unit] = Monad[G].ifM(isEnabled)(f, Monad[G].unit)
  }
}
