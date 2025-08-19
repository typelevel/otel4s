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

import cats.Applicative
import cats.Monad
import cats.~>
import org.typelevel.otel4s.KindTransformer

object InstrumentMeta {

  sealed trait Dynamic[F[_]] {

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
    def mapK[G[_]: Monad](f: F ~> G): InstrumentMeta.Dynamic[G] =
      new Dynamic.MappedK(this)(f)

    /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to `G`.
      */
    def liftTo[G[_]: Monad](implicit kt: KindTransformer[F, G]): InstrumentMeta.Dynamic[G] =
      mapK(kt.liftK)

    @deprecated("use `liftTo` instead", since = "otel4s 0.14.0")
    def mapK[G[_]: Monad](implicit kt: KindTransformer[F, G]): InstrumentMeta.Dynamic[G] = liftTo[G]
  }

  object Dynamic {

    private[otel4s] def from[F[_]: Monad](enabled: F[Boolean]): Dynamic[F] =
      new Dynamic[F] {
        def isEnabled: F[Boolean] = enabled
        def unit: F[Unit] = Monad[F].unit
        def whenEnabled(f: => F[Unit]): F[Unit] = Monad[F].ifM(isEnabled)(f, unit)
      }

    private[otel4s] def enabled[F[_]: Applicative]: Dynamic[F] =
      new Dynamic[F] {
        val isEnabled: F[Boolean] = Applicative[F].pure(true)
        val unit: F[Unit] = Applicative[F].unit
        def whenEnabled(f: => F[Unit]): F[Unit] = f
      }

    private[otel4s] def disabled[F[_]: Applicative]: Dynamic[F] =
      new Dynamic[F] {
        val isEnabled: F[Boolean] = Applicative[F].pure(false)
        val unit: F[Unit] = Applicative[F].unit
        def whenEnabled(f: => F[Unit]): F[Unit] = unit
      }

    private class MappedK[F[_], G[_]: Monad](meta: InstrumentMeta.Dynamic[F])(f: F ~> G) extends Dynamic[G] {
      def isEnabled: G[Boolean] = f(meta.isEnabled)
      def unit: G[Unit] = f(meta.unit)
      def whenEnabled(f: => G[Unit]): G[Unit] = Monad[G].ifM(isEnabled)(f, Monad[G].unit)
    }
  }

  sealed trait Static[F[_]] {

    /** Indicates whether instrumentation is enabled or not.
      */
    def isEnabled: Boolean

    /** A no-op effect.
      */
    def unit: F[Unit]

    /** Modify the context `F` using the transformation `f`. */
    def mapK[G[_]](f: F ~> G): InstrumentMeta.Static[G] =
      new Static.MappedK(this)(f)

    /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to `G`.
      */
    def liftTo[G[_]](implicit kt: KindTransformer[F, G]): InstrumentMeta.Static[G] =
      mapK(kt.liftK)

    @deprecated("use `liftTo` instead", since = "otel4s 0.14.0")
    def mapK[G[_]](implicit kt: KindTransformer[F, G]): InstrumentMeta.Static[G] = liftTo[G]
  }

  object Static {

    private[otel4s] def enabled[F[_]: Applicative]: Static[F] =
      new Static[F] {
        def isEnabled: Boolean = true
        def unit: F[Unit] = Applicative[F].unit
      }

    private[otel4s] def disabled[F[_]: Applicative]: Static[F] =
      new Static[F] {
        def isEnabled: Boolean = false
        def unit: F[Unit] = Applicative[F].unit
      }

    private class MappedK[F[_], G[_]](meta: Static[F])(f: F ~> G) extends Static[G] {
      def isEnabled: Boolean = meta.isEnabled
      def unit: G[Unit] = f(meta.unit)
    }
  }

}
