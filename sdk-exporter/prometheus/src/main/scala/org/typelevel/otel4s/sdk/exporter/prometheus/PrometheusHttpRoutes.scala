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
import fs2.compression.Compression
import org.http4s.HttpRoutes
import org.http4s.MediaRange
import org.http4s.Method.GET
import org.http4s.Method.HEAD
import org.http4s.Request
import org.http4s.Response
import org.http4s.Status
import org.http4s.headers.Accept
import org.http4s.server.middleware.GZip
import org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter

object PrometheusHttpRoutes {

  /** Creates HTTP routes that will collect metrics and serialize to Prometheus text format on request.
    */
  def routes[F[_]: Compression: Console](
      exporter: MetricExporter.Pull[F],
      writerConfig: PrometheusWriter.Config
  )(implicit F: MonadThrow[F]): HttpRoutes[F] = {
    val writer = PrometheusWriter.text[F](writerConfig)

    val routes: HttpRoutes[F] = HttpRoutes.of {
      case Request(GET, _, _, headers, _, _) =>
        if (
          headers
            .get[Accept]
            .forall(
              _.values
                .exists(v => v.mediaRange.equals(MediaRange.`*/*`) || v.mediaRange.satisfies(MediaRange.`text/*`))
            )
        ) {
          for {
            metrics <- exporter.metricReader.collectAllMetrics
          } yield Response().withEntity(writer.write(metrics)).withHeaders("Content-Type" -> writer.contentType)
        } else {
          F.pure(Response(Status.NotAcceptable))
        }
      case Request(HEAD, _, _, _, _, _) =>
        F.pure(Response())
    }

    GZip(routes)
  }

}
