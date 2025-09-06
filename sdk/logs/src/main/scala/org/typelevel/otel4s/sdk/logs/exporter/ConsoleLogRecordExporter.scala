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

package org.typelevel.otel4s.sdk.logs.exporter

import cats.Foldable
import cats.Monad
import cats.effect.std.Console
import cats.syntax.foldable._
import org.typelevel.otel4s.sdk.logs.data.LogRecordData

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/** A log record exporter that logs every record using [[cats.effect.std.Console]].
  *
  * @note
  *   use this exporter for debugging purposes because it may affect the performance
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/sdk_exporters/stdout/]]
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
private final class ConsoleLogRecordExporter[F[_]: Monad: Console] extends LogRecordExporter.Unsealed[F] {

  val name: String = "ConsoleLogRecordExporter"

  def exportLogRecords[G[_]: Foldable](logs: G[LogRecordData]): F[Unit] =
    logs.traverse_(span => log(span))

  def flush: F[Unit] = Monad[F].unit

  private def log(data: LogRecordData): F[Unit] = {
    import ConsoleLogRecordExporter.Formatter

    val scope = data.instrumentationScope

    val timestamp =
      Instant
        .ofEpochMilli(data.timestamp.getOrElse(data.observedTimestamp).toMillis)
        .atZone(ZoneOffset.UTC)

    val date = Formatter.format(timestamp)
    val severity = data.severity.map(_.toString).getOrElse("")
    val body = data.body.map(_.toString).getOrElse("")
    val traceId = data.traceContext.map(_.traceId.toHex).getOrElse("")
    val spanId = data.traceContext.map(_.spanId.toHex).getOrElse("")
    val scopeInfo = scope.name + ":" + scope.version.getOrElse("")

    val content = s"$date $severity '$body' : $traceId $spanId [scopeInfo: $scopeInfo] ${data.attributes}"

    Console[F].println(s"ConsoleLogRecordExporter: $content")
  }

}

object ConsoleLogRecordExporter {
  private val Formatter = DateTimeFormatter.ISO_DATE_TIME

  /** Creates a log record exporter that logs every record using [[cats.effect.std.Console]].
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  def apply[F[_]: Monad: Console]: LogRecordExporter[F] =
    new ConsoleLogRecordExporter[F]
}
