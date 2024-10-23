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

import cats.data.NonEmptyVector
import cats.effect._
import cats.effect.std.Console
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import com.comcast.ip4s._
import fs2.compression.Compression
import fs2.io.net.Network
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exporter._

/** Exports metrics on request (pull based).
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/sdk_exporters/prometheus/]]
  */
private final class PrometheusMetricExporter[F[_]: MonadCancelThrow: Console] private[prometheus] (
    metricProducers: Ref[F, Vector[MetricProducer[F]]],
    val defaultAggregationSelector: AggregationSelector
) extends MetricExporter.Pull[F] { self =>

  def name: String = "PrometheusMetricExporter"

  val aggregationTemporalitySelector: AggregationTemporalitySelector =
    AggregationTemporalitySelector.alwaysCumulative

  val defaultCardinalityLimitSelector: CardinalityLimitSelector =
    CardinalityLimitSelector.default

  val metricReader: MetricReader[F] =
    new MetricReader[F] {
      def aggregationTemporalitySelector: AggregationTemporalitySelector =
        self.aggregationTemporalitySelector

      def defaultAggregationSelector: AggregationSelector =
        self.defaultAggregationSelector

      def defaultCardinalityLimitSelector: CardinalityLimitSelector =
        self.defaultCardinalityLimitSelector

      def register(producers: NonEmptyVector[MetricProducer[F]]): F[Unit] = {
        def warn =
          Console[F].errorln(
            "MetricProducers are already registered at this PrometheusMetricReader"
          )

        metricProducers.flatModify { current =>
          if (current.isEmpty) (producers.toVector, MonadCancelThrow[F].unit)
          else (current, warn)
        }
      }

      def collectAllMetrics: F[Vector[MetricData]] =
        metricProducers.get.flatMap {
          case producers if producers.nonEmpty =>
            producers.flatTraverse(_.produce)

          case _ =>
            Console[F]
              .errorln(
                "The PrometheusMetricReader is running, but producers aren't configured yet. Nothing to export"
              )
              .as(Vector.empty)
        }

      def forceFlush: F[Unit] =
        MonadCancelThrow[F].unit

      override def toString: String =
        "PrometheusMetricReader"
    }
}

object PrometheusMetricExporter {

  private[prometheus] object Defaults {
    val Host: Host = host"localhost"
    val Port: Port = port"9464"
  }

  /** Builder for [[PrometheusMetricExporter]] */
  sealed trait Builder[F[_]] {

    /** Sets the default aggregation selector to use. */
    def withDefaultAggregationSelector(
        selector: AggregationSelector
    ): Builder[F]

    /** Creates a [[PrometheusMetricExporter]] using the configuration of this builder.
      */
    def build: F[MetricExporter.Pull[F]]
  }

  /** Builder for [[PrometheusMetricExporter]] with HTTP server */
  sealed trait HttpServerBuilder[F[_]] {

    /** Sets the host that metrics are served on. */
    def withHost(host: Host): HttpServerBuilder[F]

    /** Sets the port that metrics are served on. */
    def withPort(port: Port): HttpServerBuilder[F]

    /** Sets the default aggregation selector to use. */
    def withDefaultAggregationSelector(
        selector: AggregationSelector
    ): HttpServerBuilder[F]

    /** Sets the writer config, which determines whether to produce metrics with or without unit suffix, type suffix,
      * scope info and target info
      */
    def withWriterConfig(config: PrometheusWriter.Config): HttpServerBuilder[F]

    /** Creates a [[PrometheusMetricExporter]] using the configuration of this builder and starts an HTTP server that
      * will collect metrics and serialize to Prometheus text format on request.
      */
    def build: Resource[F, MetricExporter.Pull[F]]
  }

  /** Creates a [[Builder]] for [[PrometheusMetricExporter]] with the default configuration.
    */
  def builder[F[_]: Concurrent: Console]: Builder[F] =
    new BuilderImpl[F](
      defaultAggregationSelector = AggregationSelector.default
    )

  /** Creates a [[HttpServerBuilder]] for [[PrometheusMetricExporter]] with the default configuration.
    */
  def serverBuilder[F[_]: Async: Network: Compression: Console]: HttpServerBuilder[F] =
    new HttpServerBuilderImpl[F](
      host = Defaults.Host,
      port = Defaults.Port,
      defaultAggregationSelector = AggregationSelector.default,
      writerConfig = PrometheusWriter.Config.default
    )

  private final case class BuilderImpl[F[_]: Concurrent: Console](
      defaultAggregationSelector: AggregationSelector
  ) extends Builder[F] {
    def withDefaultAggregationSelector(
        selector: AggregationSelector
    ): Builder[F] = copy(defaultAggregationSelector = selector)

    def build: F[MetricExporter.Pull[F]] =
      Ref.empty[F, Vector[MetricProducer[F]]].map { ref =>
        new PrometheusMetricExporter[F](
          ref,
          defaultAggregationSelector
        )
      }

  }

  private final case class HttpServerBuilderImpl[F[_]: Async: Network: Compression: Console](
      host: Host,
      port: Port,
      defaultAggregationSelector: AggregationSelector,
      writerConfig: PrometheusWriter.Config
  ) extends HttpServerBuilder[F] {
    def withHost(host: Host): HttpServerBuilder[F] =
      copy(host = host)

    def withPort(port: Port): HttpServerBuilder[F] =
      copy(port = port)

    def withDefaultAggregationSelector(
        selector: AggregationSelector
    ): HttpServerBuilder[F] = copy(defaultAggregationSelector = selector)

    def withWriterConfig(config: PrometheusWriter.Config): HttpServerBuilder[F] =
      copy(writerConfig = config)

    def build: Resource[F, MetricExporter.Pull[F]] =
      Resource.eval(Ref.empty[F, Vector[MetricProducer[F]]]).flatMap { ref =>
        val exporter = new PrometheusMetricExporter[F](
          ref,
          defaultAggregationSelector
        )

        val routes = PrometheusHttpRoutes
          .routes[F](exporter, writerConfig)

        EmberServerBuilder
          .default[F]
          .withHost(host)
          .withPort(port)
          .withHttpApp(Router("metrics" -> routes).orNotFound)
          .build
          .evalTap { _ =>
            val consoleMsg =
              s"PrometheusMetricsExporter: launched Prometheus server at $host:$port/metrics, writer options: $writerConfig"
            Console[F].println(consoleMsg)
          }
          .as(exporter)
      }

  }

}
