/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.metrics.internal.utils

import cats.effect.Concurrent
import cats.syntax.functor._

private[metrics] trait Adder[F[_], A] {
  def add(a: A): F[Unit]
  def sum(reset: Boolean): F[A]
  def reset: F[Unit]
}

private[metrics] object Adder {

  def create[F[_]: Concurrent, A: Numeric]: F[Adder[F, A]] =
    Concurrent[F].ref(Numeric[A].zero).map { ref =>
      new Adder[F, A] {
        def add(a: A): F[Unit] =
          ref.update(v => Numeric[A].plus(v, a))

        def sum(reset: Boolean): F[A] =
          if (reset) ref.getAndSet(Numeric[A].zero) else ref.get

        def reset: F[Unit] =
          ref.set(Numeric[A].zero)
      }
    }

}
