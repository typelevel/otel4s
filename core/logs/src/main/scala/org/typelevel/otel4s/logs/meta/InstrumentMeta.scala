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

package org.typelevel.otel4s.logs.meta

import cats.Applicative
import cats.Monad
import cats.mtl.Ask
import cats.mtl.LiftValue
import cats.syntax.flatMap._
import org.typelevel.otel4s.logs.Severity

/** The instrument's metadata. Indicates whether instrumentation is enabled.
  */
sealed trait InstrumentMeta[F[_], Ctx] {

  /** Indicates whether instrumentation is enabled.
    *
    * The currently available context will be automatically used, a shortcut for:
    * {{{
    *   Local[IO, Ctx].ask.flatMap(ctx => meta.isEnabled(ctx, severity, eventName))
    *   // or
    *   logger.currentContext.flatMap(ctx => meta.isEnabled(ctx, severity, eventName))
    * }}}
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
  def isEnabled(severity: Option[Severity], eventName: Option[String]): F[Boolean]

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

  /** Modify the context `F` using an implicit [[cats.mtl.LiftValue]] from `F` to `G`.
    */
  def liftTo[G[_]](implicit lift: LiftValue[F, G]): InstrumentMeta[G, Ctx] =
    new InstrumentMeta.Lifted(this)(lift)
}

object InstrumentMeta {

  private[otel4s] def dynamic[F[_], Ctx](
      enabled: (Ctx, Option[Severity], Option[String]) => F[Boolean]
  )(implicit M: Monad[F], A: Ask[F, Ctx]): InstrumentMeta[F, Ctx] =
    new Dynamic(enabled)

  private[otel4s] def enabled[F[_]: Applicative, Ctx]: InstrumentMeta[F, Ctx] =
    new Const(true)

  private[otel4s] def disabled[F[_]: Applicative, Ctx]: InstrumentMeta[F, Ctx] =
    new Const(false)

  private final class Const[F[_]: Applicative, Ctx](value: Boolean) extends InstrumentMeta[F, Ctx] {
    private val enabled: F[Boolean] = Applicative[F].pure(value)
    def isEnabled(context: Ctx, severity: Option[Severity], eventName: Option[String]): F[Boolean] =
      enabled

    def isEnabled(severity: Option[Severity], eventName: Option[String]): F[Boolean] =
      enabled
  }

  private final class Dynamic[F[_], Ctx](
      enabled: (Ctx, Option[Severity], Option[String]) => F[Boolean]
  )(implicit M: Monad[F], A: Ask[F, Ctx])
      extends InstrumentMeta[F, Ctx] {
    def isEnabled(severity: Option[Severity], eventName: Option[String]): F[Boolean] =
      A.ask[Ctx].flatMap(ctx => isEnabled(ctx, severity, eventName))

    def isEnabled(context: Ctx, severity: Option[Severity], eventName: Option[String]): F[Boolean] =
      enabled(context, severity, eventName)
  }

  private final class Lifted[F[_], G[_], Ctx](meta: InstrumentMeta[F, Ctx])(lift: LiftValue[F, G])
      extends InstrumentMeta[G, Ctx] {
    def isEnabled(context: Ctx, severity: Option[Severity], eventName: Option[String]): G[Boolean] =
      lift(meta.isEnabled(context, severity, eventName))

    def isEnabled(severity: Option[Severity], eventName: Option[String]): G[Boolean] =
      lift(meta.isEnabled(severity, eventName))
  }
}
