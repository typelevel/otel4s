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

package org.typelevel.otel4s.sdk.trace
package processor

import cats.MonadThrow
import cats.effect.std.Console
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter
import org.typelevel.otel4s.trace.SpanContext

/** An implementation of the [[SpanProcessor]] that passes ended [[data.SpanData SpanData]] directly to the configured
  * exporter.
  *
  * @note
  *   this processor exports spans individually upon completion, resulting in a single span per export request.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/trace/sdk/#simple-processor]]
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
private final class SimpleSpanProcessor[F[_]: MonadThrow: Console] private (
    exporter: SpanExporter[F],
    exportOnlySampled: Boolean
) extends SpanProcessor[F] {

  val name: String =
    s"SimpleSpanProcessor{exporter=${exporter.name}, exportOnlySampled=$exportOnlySampled}"

  val isStartRequired: Boolean = false
  val isEndRequired: Boolean = true

  def onStart(parentContext: Option[SpanContext], span: SpanRef[F]): F[Unit] =
    MonadThrow[F].unit

  def onEnd(span: SpanData): F[Unit] = {
    val canExport = !exportOnlySampled || span.spanContext.isSampled
    doExport(span).whenA(canExport)
  }

  private def doExport(span: SpanData): F[Unit] =
    exporter.exportSpans(List(span)).handleErrorWith { e =>
      Console[F].errorln(
        s"SimpleSpanProcessor: the export has failed: ${e.getMessage}\n${e.getStackTrace.mkString("\n")}\n"
      )
    }

  def forceFlush: F[Unit] =
    MonadThrow[F].unit
}

object SimpleSpanProcessor {

  /** Creates a [[SimpleSpanProcessor]] that passes only '''sampled''' ended spans to the given `exporter`.
    *
    * @param exporter
    *   the [[exporter.SpanExporter SpanExporter]] to use
    */
  def apply[F[_]: MonadThrow: Console](exporter: SpanExporter[F]): SpanProcessor[F] =
    apply(exporter, exportOnlySampled = true)

  /** Creates a [[SimpleSpanProcessor]] that passes ended spans to the given `exporter`.
    *
    * @param exporter
    *   the [[exporter.SpanExporter SpanExporter]] to use
    *
    * @param exportOnlySampled
    *   whether to export only sampled spans
    */
  def apply[F[_]: MonadThrow: Console](exporter: SpanExporter[F], exportOnlySampled: Boolean): SpanProcessor[F] =
    new SimpleSpanProcessor[F](exporter, exportOnlySampled)

}
