/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.exporter.prometheus

import cats.MonadThrow
import cats.effect.std.Console
import cats.syntax.functor._
import org.http4s.HttpRoutes
import org.http4s.Response
import org.http4s.dsl.Http4sDsl
import org.http4s.headers.Accept
import org.typelevel.otel4s.sdk.exporter.prometheus.PrometheusMetricExporter.Defaults
import org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter

object PrometheusHttpRoutes {

  /** Creates HTTP routes that will collect metrics and serialize to Prometheus text format on request.
    */
  def routes[F[_]: MonadThrow: Console](
      exporter: MetricExporter.Pull[F],
      withoutUnits: Boolean = Defaults.WithoutUnits,
      withoutTypeSuffixes: Boolean = Defaults.WithoutTypeSuffixes,
      disableScopeInfo: Boolean = Defaults.DisableScopeInfo,
      disableTargetInfo: Boolean = Defaults.DisableTargetInfo
  ): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}

    import dsl._

    val writer = PrometheusWriter.text[F](withoutUnits, withoutTypeSuffixes, disableScopeInfo, disableTargetInfo)

    HttpRoutes.of { case req =>
      if (req.headers.get[Accept].forall(_.values.exists(_.mediaRange.isText))) {
        for {
          metrics <- exporter.metricReader.collectAllMetrics
        } yield Response().withEntity(writer.write(metrics)).withHeaders("Content-Type" -> writer.contentType)
      } else {
        NotAcceptable()
      }
    }
  }

}
