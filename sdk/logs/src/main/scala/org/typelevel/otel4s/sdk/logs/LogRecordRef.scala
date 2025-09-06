/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.sdk.logs

import cats.effect.Concurrent
import cats.effect.Ref
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.logs.data.LogRecordData

/** See [[https://opentelemetry.io/docs/specs/otel/logs/sdk/#additional-logrecord-interfaces]]
  */
sealed trait LogRecordRef[F[_]] {

  /** Returns an immutable instance of the [[data.LogRecordData LogRecordData]], for use in export.
    */
  def toLogRecordData: F[LogRecordData]

  /** Modifies the underlying [[data.LogRecordData LogRecordData]].
    */
  def modify(f: LogRecordData => LogRecordData): F[Unit]

}

object LogRecordRef {

  private[sdk] def create[F[_]: Concurrent](init: LogRecordData): F[LogRecordRef[F]] =
    for {
      ref <- Ref.of(init)
    } yield new Impl(ref)

  private class Impl[F[_]](ref: Ref[F, LogRecordData]) extends LogRecordRef[F] {
    def toLogRecordData: F[LogRecordData] = ref.get
    def modify(f: LogRecordData => LogRecordData): F[Unit] = ref.modify(r => (f(r), ()))
  }

}
