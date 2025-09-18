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
import cats.~>

/** The entry point into a log pipeline.
  *
  * @note
  *   the `Logger` is intended to be used only for bridging logs from other log frameworks into OpenTelemetry and is
  *   '''NOT a replacement''' for logging API.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/api/#logger]]
  */
sealed trait Logger[F[_], Ctx] {

  /** The instrument's metadata. Indicates whether instrumentation is enabled.
    */
  def meta: Logger.Meta[F, Ctx]

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
  private[otel4s] trait Unsealed[F[_], Ctx] extends Logger[F, Ctx]

  sealed trait Meta[F[_], Ctx] {

    /** Indicates whether instrumentation is enabled.
      *
      * @param context
      *   the context to be associated with the log record
      *
      * @param severity
      *   the severity of the log record
      *
      * @param eventName
      *   the event name to be associated with the log record
      *
      * @return
      *   `true` if instrumentation is enabled, `false` otherwise
      *
      * @see
      *   [[https://opentelemetry.io/docs/specs/otel/logs/api/#enabled]]
      */
    def isEnabled(context: Ctx, severity: Option[Severity], eventName: Option[String]): F[Boolean]

    /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to `G`.
      */
    def liftTo[G[_]](implicit kt: KindTransformer[F, G]): Meta[G, Ctx] =
      new Meta.MappedK(this)(kt.liftK)
  }

  object Meta {
    private[otel4s] trait Unsealed[F[_], Ctx] extends Meta[F, Ctx]

    private[otel4s] def enabled[F[_]: Applicative, Ctx]: Meta[F, Ctx] =
      new Const(true)

    private[otel4s] def disabled[F[_]: Applicative, Ctx]: Meta[F, Ctx] =
      new Const(false)

    private final class Const[F[_]: Applicative, Ctx](value: Boolean) extends Meta[F, Ctx] {
      private val enabled: F[Boolean] = Applicative[F].pure(value)
      def isEnabled(context: Ctx, severity: Option[Severity], eventName: Option[String]): F[Boolean] =
        enabled
    }

    private final class MappedK[F[_], G[_], Ctx](meta: Meta[F, Ctx])(f: F ~> G) extends Meta[G, Ctx] {
      def isEnabled(context: Ctx, severity: Option[Severity], eventName: Option[String]): G[Boolean] =
        f(meta.isEnabled(context, severity, eventName))
    }
  }

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
      val meta: Meta[F, Ctx] = Meta.disabled[F, Ctx]
      val logRecordBuilder: LogRecordBuilder[F, Ctx] = LogRecordBuilder.noop[F, Ctx]
    }

  /** Implementation for [[Logger.liftTo]]. */
  private class MappedK[F[_], G[_]: Monad, Ctx](
      logger: Logger[F, Ctx]
  )(implicit kt: KindTransformer[F, G])
      extends Logger[G, Ctx] {
    val meta: Meta[G, Ctx] = logger.meta.liftTo[G]
    def logRecordBuilder: LogRecordBuilder[G, Ctx] = logger.logRecordBuilder.liftTo[G]
  }

  object Implicits {
    implicit def noop[F[_]: Applicative, Ctx]: Logger[F, Ctx] = Logger.noop
  }
}
