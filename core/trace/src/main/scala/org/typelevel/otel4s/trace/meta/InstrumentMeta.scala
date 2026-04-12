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

package org.typelevel.otel4s.trace.meta

import cats.Applicative
import cats.Monad
import cats.mtl.LiftValue
import cats.~>

/** The instrument's metadata. Indicates whether instrumentation is enabled.
  */
sealed trait InstrumentMeta[F[_]] {

  /** Indicates whether instrumentation is enabled or not.
    */
  def isEnabled: F[Boolean]

  /** A no-op effect.
    */
  def unit: F[Unit]

  /** Modify the context `F` using an implicit [[cats.mtl.LiftValue]] from `F` to `G`.
    */
  def liftTo[G[_]](implicit lift: LiftValue[F, G]): InstrumentMeta[G] =
    new InstrumentMeta.Lifted(this)(lift)

}

object InstrumentMeta {

  private[otel4s] def dynamic[F[_]: Monad](enabled: F[Boolean]): InstrumentMeta[F] =
    new InstrumentMeta[F] {
      def isEnabled: F[Boolean] = enabled
      def unit: F[Unit] = Applicative[F].unit
    }

  private[otel4s] def enabled[F[_]: Applicative]: InstrumentMeta[F] =
    new Const[F](true)

  private[otel4s] def disabled[F[_]: Applicative]: InstrumentMeta[F] =
    new Const[F](false)

  private final class Const[F[_]: Applicative](value: Boolean) extends InstrumentMeta[F] {
    val isEnabled: F[Boolean] = Applicative[F].pure(value)
    val unit: F[Unit] = Applicative[F].unit
  }

  private final class Lifted[F[_], G[_]](meta: InstrumentMeta[F])(f: F ~> G) extends InstrumentMeta[G] {
    def isEnabled: G[Boolean] = f(meta.isEnabled)
    def unit: G[Unit] = f(meta.unit)
  }
}
