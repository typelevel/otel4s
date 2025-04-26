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

package org.typelevel.otel4s.meta

import cats.{Applicative, Monad, ~>}
import org.typelevel.otel4s.KindTransformer

sealed trait InstrumentMeta[F[_]] {

  def isEnabled: F[Boolean]

  /** A no-op effect.
    */
  def unit: F[Unit]

  // todo: define Scala 3 version and use inline transparent
  def whenEnabled(f: => F[Unit]): F[Unit]

  def whenEnabledOrElse[A](whenEnabled: => F[A], whenDisabled: => F[A]): F[A]

  /** Modify the context `F` using the transformation `f`. */
  def mapK[G[_]: Monad](f: F ~> G): InstrumentMeta[G] =
    new InstrumentMeta.MappedK(this)(f)

  /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to `G`.
    */
  def mapK[G[_]: Monad](implicit kt: KindTransformer[F, G]): InstrumentMeta[G] =
    mapK(kt.liftK)
}

object InstrumentMeta {

  sealed trait Dynamic[F[_]] extends InstrumentMeta[F] {
    /** Indicates whether instrumentation is enabled or not.
     */
    def isEnabled: F[Boolean]
  }

  sealed trait Static[F[_]] extends InstrumentMeta[F] {
    /** Indicates whether instrumentation is enabled or not.
     */
    def isEnabledSync: Boolean

    /** Modify the context `F` using the transformation `f`. */
    override def mapK[G[_]: Monad](f: F ~> G): InstrumentMeta.Static[G] =
      new Static.MappedK(this)(f)

    /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to `G`.
     */
    override def mapK[G[_]: Monad](implicit kt: KindTransformer[F, G]): InstrumentMeta.Static[G] =
      mapK(kt.liftK)
  }

  object Static {
    def disabled[F[_]: Applicative]: Static[F] =
      new Static[F] {
        val isEnabledSync: Boolean = false
        val isEnabled: F[Boolean] = Applicative[F].pure(false)
        val unit: F[Unit] = Applicative[F].unit
        def whenEnabled(f: => F[Unit]): F[Unit] = unit
        def whenEnabledOrElse[A](whenEnabled: => F[A], whenDisabled: => F[A]): F[A] = whenDisabled
      }

    private class MappedK[F[_], G[_]: Monad](meta: Static[F])(f: F ~> G) extends Static[G] {
      def isEnabledSync: Boolean = meta.isEnabledSync
      def isEnabled: G[Boolean] = f(meta.isEnabled)
      def unit: G[Unit] = f(meta.unit)
      def whenEnabled(f: => G[Unit]): G[Unit] = Monad[G].ifM(isEnabled)(f, Monad[G].unit)
      def whenEnabledOrElse[A](whenEnabled: => G[A], whenDisabled: => G[A]): G[A] =
        Monad[G].ifM(isEnabled)(whenEnabled, whenDisabled)
    }
  }

  @deprecated("use derived version", "0.12")
  def enabled[F[_]: Monad]: InstrumentMeta[F] =
    new InstrumentMeta[F] {
      val isEnabled: F[Boolean] = Monad[F].pure(true)
      val unit: F[Unit] = Monad[F].unit
      val monad: Monad[F] = Monad[F]
      def whenEnabled(f: => F[Unit]): F[Unit] = monad.ifM(isEnabled)(f, unit)
      def whenEnabledOrElse[A](whenEnabled: => F[A], whenDisabled: => F[A]): F[A] =
        monad.ifM(isEnabled)(whenEnabled, whenDisabled)
    }

  def disabled[F[_]: Applicative]: InstrumentMeta[F] =
    new InstrumentMeta[F] {
      val isEnabled: F[Boolean] = Applicative[F].pure(false)
      val unit: F[Unit] = Applicative[F].unit
      def whenEnabled(f: => F[Unit]): F[Unit] = unit
      def whenEnabledOrElse[A](whenEnabled: => F[A], whenDisabled: => F[A]): F[A] = whenDisabled
    }

  private class MappedK[F[_], G[_]: Monad](meta: InstrumentMeta[F])(f: F ~> G) extends InstrumentMeta[G] {
    def isEnabled: G[Boolean] = f(meta.isEnabled)
    def unit: G[Unit] = f(meta.unit)
    def whenEnabled(f: => G[Unit]): G[Unit] = Monad[G].ifM(isEnabled)(f, Monad[G].unit)
    def whenEnabledOrElse[A](whenEnabled: => G[A], whenDisabled: => G[A]): G[A] =
      Monad[G].ifM(isEnabled)(whenEnabled, whenDisabled)
  }
}
