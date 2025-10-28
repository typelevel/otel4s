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
package processor

import cats.MonadThrow
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter

/** An implementation of the [[LogRecordProcessor]] that passes [[data.LogRecordData LogRecordData]] directly to the
  * configured exporter.
  *
  * @note
  *   this processor exports logs individually upon completion, resulting in a single log per export request.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/sdk/#simple-processor]]
  *
  * @tparam F
  *   the higher-kinded type of polymorphic effect
  */
private final class SimpleLogRecordProcessor[F[_]: MonadThrow: Diagnostic] private (
    exporter: LogRecordExporter[F]
) extends LogRecordProcessor.Unsealed[F] {

  val name: String =
    s"SimpleLogRecordProcessor{exporter=${exporter.name}}"

  def onEmit(context: Context, logRecordRef: LogRecordRef[F]): F[Unit] =
    logRecordRef.toLogRecordData
      .flatMap(logRecord => exporter.exportLogRecords(List(logRecord)))
      .handleErrorWith { e =>
        Diagnostic[F].error(s"SimpleLogRecordProcessor: the export has failed: ${e.getMessage}", e)
      }

  def forceFlush: F[Unit] =
    MonadThrow[F].unit
}

object SimpleLogRecordProcessor {

  /** Creates a [[SimpleLogRecordProcessor]] that passes log records to the given `exporter`.
    *
    * @param exporter
    *   the [[exporter.LogRecordExporter LogRecordExporter]] to use
    */
  def apply[F[_]: MonadThrow: Diagnostic](exporter: LogRecordExporter[F]): LogRecordProcessor[F] =
    new SimpleLogRecordProcessor[F](exporter)

}
