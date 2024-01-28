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

package org.typelevel.otel4s.sdk.metrics.internal.aggregation

import cats.Monad

import java.util.concurrent.atomic.DoubleAdder
import java.util.concurrent.atomic.LongAdder

private trait Adder[F[_], A] {
  def addLong(a: Long): F[Unit]
  def addDouble(a: Double): F[Unit]
  def sum(reset: Boolean): F[A]
}

private object Adder {
  def makeLong[F[_]: Monad]: F[Adder[F, Long]] =
    Monad[F].pure(
      new Adder[F, Long] {
        private val adder = new LongAdder

        def addLong(a: Long): F[Unit] =
          Monad[F].pure(adder.add(a))

        def addDouble(a: Double): F[Unit] =
          Monad[F].unit

        def sum(reset: Boolean): F[Long] =
          Monad[F].pure(if (reset) adder.sumThenReset() else adder.sum())
      }
    )

  def makeDouble[F[_]: Monad]: F[Adder[F, Double]] =
    Monad[F].pure(
      new Adder[F, Double] {
        private val adder = new DoubleAdder

        def addLong(a: Long): F[Unit] =
          Monad[F].pure(adder.add(a))

        def addDouble(a: Double): F[Unit] =
          Monad[F].unit

        def sum(reset: Boolean): F[Double] =
          Monad[F].pure(if (reset) adder.sumThenReset() else adder.sum())
      }
    )
}
