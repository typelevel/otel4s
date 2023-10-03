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
package exporter

import cats.Applicative
import cats.kernel.Monoid
import cats.syntax.foldable._
import org.typelevel.otel4s.sdk.trace.data.SpanData

/** An interface that allows different tracing services to export recorded data
  * for sampled spans in their own format. To export data, the exporter MUST be
  * register to the [[SdkTracer]] using a [[SimpleSpanProcessor]] or a
  * [[BatchSpanProcessor]].
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
trait SpanExporter[F[_]] {

  /** Called to export sampled
    * [[org.typelevel.otel4s.sdk.trace.data.SpanData SpanData]].
    *
    * '''Note''': the export operations can be performed simultaneously
    * depending on the type of span processor being used. However, the
    * [[BatchSpanProcessor]] will ensure that only one export can occur at a
    * time.
    *
    * @param spans
    *   the collection of sampled Spans to be exported
    */
  def exportSpans(spans: List[SpanData]): F[Unit]
}

object SpanExporter {

  /** Creates a [[SpanExporter]] which delegates all exports to the exporters in
    * order.
    *
    * Can be used to export to multiple backends using the same
    * [[SpanProcessor]] like a [[SimpleSpanProcessor]] or a
    * [[BatchSpanProcessor]].
    */
  def composite[F[_]: Applicative](
      exporters: List[SpanExporter[F]]
  ): SpanExporter[F] =
    exporters match {
      case Nil         => new Noop
      case head :: Nil => head
      case _           => new Multi[F](exporters)
    }

  /** Creates a no-op implementation of the [[SpanExporter]].
    *
    * All export operations are no-op.
    */
  def noop[F[_]: Applicative]: SpanExporter[F] =
    new Noop

  implicit def spanExporterMonoid[F[_]: Applicative]: Monoid[SpanExporter[F]] =
    new Monoid[SpanExporter[F]] {
      val empty: SpanExporter[F] = noop[F]

      def combine(x: SpanExporter[F], y: SpanExporter[F]): SpanExporter[F] =
        (x, y) match {
          case (_: Noop[F], _: Noop[F]) =>
            empty

          case (that, _: Noop[F]) =>
            that

          case (_: Noop[F], other) =>
            other

          case (that: Multi[F], other: Multi[F]) =>
            new Multi[F](that.exporters ++ other.exporters)

          case (that: Multi[F], other) =>
            new Multi[F](that.exporters :+ other)

          case (that, other: Multi[F]) =>
            new Multi[F](that +: other.exporters)

          case (that, other) =>
            new Multi[F](List(that, other))
        }
    }

  private final class Noop[F[_]: Applicative] extends SpanExporter[F] {
    def exportSpans(spans: List[SpanData]): F[Unit] = Applicative[F].unit
  }

  private final class Multi[F[_]: Applicative](
      private[SpanExporter] val exporters: List[SpanExporter[F]]
  ) extends SpanExporter[F] {
    def exportSpans(spans: List[SpanData]): F[Unit] =
      exporters.traverse_(_.exportSpans(spans))
  }

}
