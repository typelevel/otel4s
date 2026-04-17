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
import cats.mtl.LiftValue

/** The instrument's metadata. Indicates whether instrumentation is enabled.
  */
sealed trait InstrumentMeta[F[_]] {

  /** Indicates whether instrumentation is enabled or not.
    */
  def isEnabled: F[Boolean]

  /** A no-op effect.
    */
  def unit: F[Unit]

  /** Runs `f` only when the instrument is enabled. A shortcut for `Monad[F].ifM(isEnabled)(f, unit)`.
    */
  def whenEnabled(f: => F[Unit]): F[Unit]

  /** Modify the context `F` using an implicit [[cats.mtl.LiftValue]] from `F` to `G`.
    */
  def liftTo[G[_]](implicit G: Monad[G], lift: LiftValue[F, G]): InstrumentMeta[G] =
    new InstrumentMeta.Lifted(this)(lift)

}

object InstrumentMeta {

  private[otel4s] def dynamic[F[_]: Monad](enabled: F[Boolean]): InstrumentMeta[F] =
    new Dynamic(enabled)

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

  private final class Dynamic[F[_]: Monad](enabled: F[Boolean]) extends InstrumentMeta[F] {
    def isEnabled: F[Boolean] = enabled
    def unit: F[Unit] = Monad[F].unit
    def whenEnabled(f: => F[Unit]): F[Unit] = Monad[F].ifM(isEnabled)(f, unit)
  }

  private class Lifted[F[_], G[_]: Monad](meta: InstrumentMeta[F])(lift: LiftValue[F, G]) extends InstrumentMeta[G] {
    def isEnabled: G[Boolean] = lift(meta.isEnabled)
    def unit: G[Unit] = lift(meta.unit)
    def whenEnabled(f: => G[Unit]): G[Unit] = Monad[G].ifM(isEnabled)(f, Monad[G].unit)
  }
}
