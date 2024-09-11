/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.sdk.autoconfigure

import org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter

/** A combined auto configure.
  */
sealed trait ExportersAutoConfigure[F[_]] {

  /** Configures [[org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter MetricExporter]].
    */
  def metricExporterAutoConfigure: AutoConfigure.Named[F, MetricExporter[F]]

  /** Configures [[org.typelevel.otel4s.sdk.trace.exporter.SpanExporter SpanExporter]].
    */
  def spanExporterAutoConfigure: AutoConfigure.Named[F, SpanExporter[F]]
}

object ExportersAutoConfigure {

  /** Creates an [[ExportersAutoConfigure]] using the given values.
    */
  def apply[F[_]](
      metricExporterAutoConfigure: AutoConfigure.Named[F, MetricExporter[F]],
      spanExporterAutoConfigure: AutoConfigure.Named[F, SpanExporter[F]]
  ): ExportersAutoConfigure[F] =
    Impl(metricExporterAutoConfigure, spanExporterAutoConfigure)

  private final case class Impl[F[_]](
      metricExporterAutoConfigure: AutoConfigure.Named[F, MetricExporter[F]],
      spanExporterAutoConfigure: AutoConfigure.Named[F, SpanExporter[F]]
  ) extends ExportersAutoConfigure[F]

}
