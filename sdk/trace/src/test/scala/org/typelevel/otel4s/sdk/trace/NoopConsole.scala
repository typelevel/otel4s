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

package org.typelevel.otel4s.sdk.trace

import cats.Show
import cats.effect.Sync
import cats.effect.std.Console

import java.nio.charset.Charset

class NoopConsole[F[_]: Sync] extends Console[F] {
  def readLineWithCharset(charset: Charset): F[String] =
    Sync[F].delay(sys.error("not implemented"))
  def print[A](a: A)(implicit S: Show[A]): F[Unit] = Sync[F].unit
  def println[A](a: A)(implicit S: Show[A]): F[Unit] = Sync[F].unit
  def error[A](a: A)(implicit S: Show[A]): F[Unit] = Sync[F].unit
  def errorln[A](a: A)(implicit S: Show[A]): F[Unit] = Sync[F].unit
}
