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

package org.typelevel.otel4s.context

import cats.Applicative
import cats.effect.IOLocal
import cats.effect.LiftIO
import cats.effect.kernel.MonadCancelThrow
import cats.mtl.Local
import org.typelevel.otel4s.instances.local._

sealed trait ContextCarrier[F[_], Ctx] {
  def local: F[Local[F, Ctx]]
}

object ContextCarrier {

  def apply[F[_], Ctx](implicit
      ev: ContextCarrier[F, Ctx]
  ): ContextCarrier[F, Ctx] = ev

  implicit def fromIOLocal[
      F[_]: MonadCancelThrow: LiftIO,
      Ctx: Contextual
  ]: ContextCarrier[F, Ctx] =
    new ContextCarrier[F, Ctx] {
      def local: F[Local[F, Ctx]] =
        IOLocal(Contextual[Ctx].root)
          .map { implicit ioLocal: IOLocal[Ctx] =>
            localForIOLocal[F, Ctx](implicitly, implicitly, ioLocal)
          }
          .to[F]
    }

  implicit def fromLocal[F[_]: Applicative, Ctx](implicit
      l: Local[F, Ctx]
  ): ContextCarrier[F, Ctx] =
    new ContextCarrier[F, Ctx] {
      def local: F[Local[F, Ctx]] = Applicative[F].pure(l)
    }
}
