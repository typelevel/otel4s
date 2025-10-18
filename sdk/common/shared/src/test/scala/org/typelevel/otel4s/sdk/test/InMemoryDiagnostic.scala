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

package org.typelevel.otel4s.sdk.test

import cats.Monad
import cats.effect.Concurrent
import cats.effect.std.Queue
import cats.syntax.all._
import org.typelevel.otel4s.sdk.internal.Diagnostic

class InMemoryDiagnostic[F[_]: Monad](queue: Queue[F, InMemoryDiagnostic.Entry]) extends Diagnostic.Unsealed[F] {
  import InMemoryDiagnostic.Entry

  def info(message: => String): F[Unit] =
    queue.offer(Entry.Info(message))

  def error(message: => String): F[Unit] =
    queue.offer(Entry.Error(message, None))

  def error(message: => String, cause: Throwable): F[Unit] =
    queue.offer(Entry.Error(message, Some(cause)))

  def entries: F[List[Entry]] =
    queue.tryTakeN(None)

}

object InMemoryDiagnostic {

  sealed trait Entry
  object Entry {
    final case class Info(message: String) extends Entry
    final case class Error(message: String, cause: Option[Throwable]) extends Entry
  }

  def create[F[_]: Concurrent]: F[InMemoryDiagnostic[F]] =
    Queue.unbounded[F, Entry].map { queue =>
      new InMemoryDiagnostic[F](queue)
    }

}
