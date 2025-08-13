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

package org.typelevel.otel4s.sdk.trace.exporter

import cats.Applicative
import cats.Foldable
import cats.effect.std.Console
import cats.syntax.foldable._
import org.typelevel.otel4s.sdk.trace.data.SpanData

/** Creates a span exporter that logs every span using [[cats.effect.std.Console]].
  *
  * @note
  *   use this exporter for debugging purposes because it may affect the performance
  */
private final class ConsoleSpanExporter[F[_]: Applicative: Console] extends SpanExporter.Unsealed[F] {

  val name: String = "ConsoleSpanExporter"

  def exportSpans[G[_]: Foldable](spans: G[SpanData]): F[Unit] = {
    def log(span: SpanData): F[Unit] = {
      val scope = span.instrumentationScope

      val content = s"'${span.name}' : " +
        s"${span.spanContext.traceIdHex} ${span.spanContext.spanIdHex} ${span.kind} " +
        s"[tracer: ${scope.name}:${scope.version.getOrElse("")}] " +
        s"${span.attributes}"

      Console[F].println(s"ConsoleSpanExporter: $content")
    }

    spans.traverse_(span => log(span))
  }

  def flush: F[Unit] = Applicative[F].unit

}

object ConsoleSpanExporter {

  def apply[F[_]: Applicative: Console]: SpanExporter[F] =
    new ConsoleSpanExporter[F]

}
