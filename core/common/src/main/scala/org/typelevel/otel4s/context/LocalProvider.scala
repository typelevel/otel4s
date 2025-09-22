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
import cats.effect.MonadCancelThrow
import cats.mtl.Local

/** A utility class to simplify the creation of the [[cats.mtl.Local Local]].
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  *
  * @tparam Ctx
  *   the type of the context
  */
@annotation.implicitNotFound("""
Cannot find the `LocalProvider` for effect `${F}` and context `${Ctx}`.
The `LocalProvider` can be derived implicitly:

1) from implicit `IOLocal[${Ctx}]` and `LiftIO[${F}]`:

IOLocal(${Ctx}.root).map { implicit ioLocal =>
  val provider = LocalProvider[IO, ${Ctx}]
}

2) from implicit `Local[${F}, ${Ctx}]`:

implicit val local: Local[${F}, ${Ctx}] = ???
val provider = LocalProvider[${F}, ${Ctx}]

3) from implicit `LiftIO[${F}]`:

val provider = LocalProvider[IO, ${Ctx}]
""")
sealed trait LocalProvider[F[_], Ctx] {

  /** Creates a [[cats.mtl.Local Local]] instance. The method is invoked once per creation of the Otel4s instance.
    */
  def local: F[Local[F, Ctx]]
}

object LocalProvider extends LocalProviderLowPriority {
  private[otel4s] trait Unsealed[F[_], Ctx] extends LocalProvider[F, Ctx]

  def apply[F[_], C](implicit ev: LocalProvider[F, C]): LocalProvider[F, C] = ev

  /** Creates [[LocalProvider]] that derives [[cats.mtl.Local Local]] instance from the given
    * [[cats.effect.IOLocal IOLocal]].
    *
    * @param ioLocal
    *   the [[cats.effect.IOLocal IOLocal]] to use
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam Ctx
    *   the type of the context
    */
  def fromIOLocal[F[_]: MonadCancelThrow: LiftIO, Ctx](ioLocal: IOLocal[Ctx]): LocalProvider[F, Ctx] =
    new LocalProvider[F, Ctx] {
      val local: F[Local[F, Ctx]] = MonadCancelThrow[F].pure(ioLocal.asLocal[F])
      override def toString: String = "LocalProvider.fromIOLocal"
    }

  /** Creates [[LocalProvider]] that returns the given [[cats.mtl.Local Local]] instance.
    *
    * @param l
    *   the [[cats.mtl.Local Local]] to use
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam Ctx
    *   the type of the context
    */
  def fromLocal[F[_]: Applicative, Ctx](l: Local[F, Ctx]): LocalProvider[F, Ctx] =
    new LocalProvider[F, Ctx] {
      val local: F[Local[F, Ctx]] = Applicative[F].pure(l)
      override def toString: String = "LocalProvider.fromLocal"
    }

  /** Creates [[LocalProvider]] that creates [[cats.effect.IOLocal IOLocal]] under the hood to derive the
    * [[cats.mtl.Local Local]] instance.
    *
    * @note
    *   every invocation of the [[LocalProvider.local]] creates new [[cats.effect.IOLocal IOLocal]]. If you want to use
    *   a custom [[cats.effect.IOLocal IOLocal]] (e.g. to share it with other components) use
    *   [[LocalProvider.fromIOLocal]].
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam Ctx
    *   the type of the context
    */
  def fromLiftIO[F[_]: MonadCancelThrow: LiftIO, Ctx: Contextual]: LocalProvider[F, Ctx] =
    new LocalProvider[F, Ctx] {
      def local: F[Local[F, Ctx]] =
        IOLocal(Contextual[Ctx].root)
          .map(ioLocal => ioLocal.asLocal[F])
          .to[F]

      override def toString: String = "LocalProvider.fromLiftIO"
    }

  implicit def liftFromIOLocal[
      F[_]: MonadCancelThrow: LiftIO,
      Ctx
  ](implicit ioLocal: IOLocal[Ctx]): LocalProvider[F, Ctx] =
    LocalProvider.fromIOLocal(ioLocal)

  implicit def liftFromLocal[F[_]: Applicative, Ctx](implicit local: Local[F, Ctx]): LocalProvider[F, Ctx] =
    LocalProvider.fromLocal(local)

}

sealed trait LocalProviderLowPriority { self: LocalProvider.type =>

  implicit def liftFromLiftIO[F[_]: MonadCancelThrow: LiftIO, Ctx: Contextual]: LocalProvider[F, Ctx] =
    LocalProvider.fromLiftIO

}
