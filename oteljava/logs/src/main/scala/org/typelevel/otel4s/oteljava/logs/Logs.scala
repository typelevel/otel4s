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
package oteljava.logs

import cats.effect.Sync
import cats.mtl.Ask
import cats.syntax.functor._
import io.opentelemetry.api.{OpenTelemetry => JOpenTelemetry}
import io.opentelemetry.api.GlobalOpenTelemetry
import org.typelevel.otel4s.logs.LoggerProvider
import org.typelevel.otel4s.oteljava.context.AskContext
import org.typelevel.otel4s.oteljava.context.Context

/** The configured logging module.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
sealed trait Logs[F[_]] {

  /** The [[org.typelevel.otel4s.logs.LoggerProvider LoggerProvider]] for bridging logs into OpenTelemetry.
    *
    * @note
    *   the logs bridge API exists to enable bridging logs from other log frameworks (e.g. SLF4J, Log4j, JUL, Logback,
    *   etc) into OpenTelemetry and is '''NOT''' a replacement log API.
    */
  def loggerProvider: LoggerProvider[F, Context]
}

object Logs {

  /** Creates a [[org.typelevel.otel4s.oteljava.logs.Logs]] from the global Java OpenTelemetry instance.
    */
  def global[F[_]: Sync]: F[Logs[F]] =
    Sync[F].delay(GlobalOpenTelemetry.get).map(fromJOpenTelemetry[F])

  /** Creates a [[org.typelevel.otel4s.oteljava.logs.Logs]] from a Java OpenTelemetry instance.
    *
    * @param jOtel
    *   A Java OpenTelemetry instance. It is the caller's responsibility to shut this down. Failure to do so may result
    *   in lost logs.
    *
    * @note
    *   this implementation uses a constant root `Context` via `Ask.const(Context.root)`. That means the module is
    *   isolated: it does not inherit or propagate the surrounding OpenTelemetry span context. This is useful if you
    *   only need logging (without traces or metrics) and want the module to operate independently. If instead you want
    *   interoperability — i.e. to capture the current span context so that logs, traces, and metrics can all work
    *   together — use `OtelJava.fromJOpenTelemetry`.
    */
  def fromJOpenTelemetry[F[_]: Sync](jOtel: JOpenTelemetry): Logs[F] = {
    implicit val askContext: AskContext[F] = Ask.const(Context.root)
    create(jOtel)
  }

  /** Creates a [[org.typelevel.otel4s.oteljava.logs.Logs]] from a Java OpenTelemetry instance using the given context.
    *
    * @param jOtel
    *   A Java OpenTelemetry instance. It is the caller's responsibility to shut this down. Failure to do so may result
    *   in lost logs.
    */
  private[oteljava] def create[F[_]: Sync: AskContext](jOtel: JOpenTelemetry): Logs[F] =
    new Impl(new LoggerProviderImpl[F](jOtel.getLogsBridge))

  private final class Impl[F[_]](val loggerProvider: LoggerProvider[F, Context]) extends Logs[F] {
    override def toString: String = s"Logs{loggerProvider=$loggerProvider}"
  }
}
