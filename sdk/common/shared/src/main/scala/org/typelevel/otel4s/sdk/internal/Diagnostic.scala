/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.internal

import cats.Applicative
import cats.effect.std.Console

/** A minimalistic abstraction for logging diagnostic messages.
  *
  * Used internally by the library, not intended for end users.
  */
@annotation.implicitNotFound(
  "Cannot find Diagnostic[${F}]. The instance can be derived automatically from cats.effect.std.Console[${F}]."
)
sealed trait Diagnostic[F[_]] {

  def info(message: => String): F[Unit]

  def error(message: => String): F[Unit]

  def error(message: => String, cause: Throwable): F[Unit]

}

object Diagnostic {
  private[sdk] trait Unsealed[F[_]] extends Diagnostic[F]

  def apply[F[_]](implicit ev: Diagnostic[F]): Diagnostic[F] = ev

  /** A no-op instance that ignores all messages. */
  def noop[F[_]: Applicative]: Diagnostic[F] =
    new Diagnostic[F] {
      def info(message: => String): F[Unit] = Applicative[F].unit
      def error(message: => String): F[Unit] = Applicative[F].unit
      def error(message: => String, cause: Throwable): F[Unit] = Applicative[F].unit
    }

  /** An instance backed by Cats Effect Console. */
  def console[F[_]: Console]: Diagnostic[F] =
    new Diagnostic[F] {
      def info(message: => String): F[Unit] =
        Console[F].println(message)

      def error(message: => String): F[Unit] =
        Console[F].errorln(message)

      def error(message: => String, cause: Throwable): F[Unit] =
        Console[F].errorln(s"$message\n${cause.getStackTrace.mkString("\n")}\n")
    }

  implicit def fromConsole[F[_]: Console]: Diagnostic[F] =
    console[F]

}
