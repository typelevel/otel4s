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
import cats.~>
import org.typelevel.otel4s.KindTransformer
import org.typelevel.otel4s.logs.Severity

/** The instrument's metadata. Indicates whether instrumentation is enabled.
  */
sealed trait InstrumentMeta[F[_], Ctx] {

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
  def liftTo[G[_]](implicit kt: KindTransformer[F, G]): InstrumentMeta[G, Ctx] =
    new InstrumentMeta.MappedK(this)(kt.liftK)
}

object InstrumentMeta {

  private[otel4s] def enabled[F[_]: Applicative, Ctx]: InstrumentMeta[F, Ctx] =
    new Const(true)

  private[otel4s] def disabled[F[_]: Applicative, Ctx]: InstrumentMeta[F, Ctx] =
    new Const(false)

  private final class Const[F[_]: Applicative, Ctx](value: Boolean) extends InstrumentMeta[F, Ctx] {
    private val enabled: F[Boolean] = Applicative[F].pure(value)
    def isEnabled(context: Ctx, severity: Option[Severity], eventName: Option[String]): F[Boolean] =
      enabled
  }

  private final class MappedK[F[_], G[_], Ctx](meta: InstrumentMeta[F, Ctx])(f: F ~> G) extends InstrumentMeta[G, Ctx] {
    def isEnabled(context: Ctx, severity: Option[Severity], eventName: Option[String]): G[Boolean] =
      f(meta.isEnabled(context, severity, eventName))
  }
}
