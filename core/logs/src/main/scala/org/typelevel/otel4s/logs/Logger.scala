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

package org.typelevel.otel4s
package logs

import cats.Applicative
import cats.Monad
import org.typelevel.otel4s.meta.InstrumentMeta

/** The entry point into a log pipeline.
  *
  * @note
  *   the `Logger` is intended to be used only for bridging logs from other log frameworks into OpenTelemetry and is
  *   '''NOT a replacement''' for logging API.
  */
trait Logger[F[_], Ctx] {

  /** The instrument's metadata. Indicates whether instrumentation is enabled.
    */
  def meta: InstrumentMeta.Dynamic[F]

  /** Returns a [[LogRecordBuilder]] to emit a log record.
    *
    * Construct the log record using [[LogRecordBuilder]], then emit the record via [[LogRecordBuilder.emit]].
    *
    * @note
    *   this method is intended for use in appenders that bridge logs from existing logging frameworks, it is '''NOT a
    *   replacement''' for a full-featured application logging framework and should not be used directly by application
    *   developers
    */
  def logRecordBuilder: LogRecordBuilder[F, Ctx]

  /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to `G`.
    */
  def liftTo[G[_]: Monad](implicit kt: KindTransformer[F, G]): Logger[G, Ctx] =
    new Logger.MappedK(this)
}

object Logger {
  def apply[F[_], Ctx](implicit ev: Logger[F, Ctx]): Logger[F, Ctx] = ev

  /** Creates a no-op implementation of the [[Logger]].
    *
    * All logging operations are no-op.
    *
    * @tparam F
    *   the higher-kinded type of polymorphic effect
    */
  def noop[F[_]: Applicative, Ctx]: Logger[F, Ctx] =
    new Logger[F, Ctx] {
      val meta: InstrumentMeta.Dynamic[F] = InstrumentMeta.Dynamic.disabled
      val logRecordBuilder: LogRecordBuilder[F, Ctx] = LogRecordBuilder.noop[F, Ctx]
    }

  /** Implementation for [[Logger.liftTo]]. */
  private class MappedK[F[_], G[_]: Monad, Ctx](
      logger: Logger[F, Ctx]
  )(implicit kt: KindTransformer[F, G])
      extends Logger[G, Ctx] {
    val meta: InstrumentMeta.Dynamic[G] = logger.meta.liftTo[G]
    def logRecordBuilder: LogRecordBuilder[G, Ctx] = logger.logRecordBuilder.liftTo[G]
  }

  object Implicits {
    implicit def noop[F[_]: Applicative, Ctx]: Logger[F, Ctx] = Logger.noop
  }
}
