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

import cats.Foldable
import cats.MonadThrow
import cats.Show
import cats.data.NonEmptyVector
import cats.effect.std.Console
import cats.syntax.applicative._
import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.option._
import cats.syntax.show._
import cats.syntax.traverse._
import cats.syntax.vector._
import fs2.Stream
import fs2.text.utf8
import io.circe.Encoder
import io.circe.syntax._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeType
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.exporter.prometheus.PrometheusConverter.convertLabelName
import org.typelevel.otel4s.sdk.exporter.prometheus.PrometheusConverter.convertName
import org.typelevel.otel4s.sdk.exporter.prometheus.PrometheusConverter.convertUnitName
import org.typelevel.otel4s.sdk.exporter.prometheus.PrometheusTextRecord.PrometheusTextPoint
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.data.PointData.DoubleNumber
import org.typelevel.otel4s.sdk.metrics.data.PointData.LongNumber

import scala.collection.immutable.ListMap

trait PrometheusWriter[F[_]] {
  def contentType: String
  def write[G[_]: Foldable](metrics: G[MetricData]): Stream[F, Byte]
}

/** @see
  *   [[https://github.com/prometheus/docs/blob/main/content/docs/instrumenting/exposition_formats.md]]
  */
object PrometheusWriter {

  private val TargetInfoName = "target_info"
  private val TargetInfoDescription = "Target metadata"

  private val ScopeInfoMetricName = "otel_scope_info"
  private val ScopeInfoDescription = "Instrumentation Scope metadata"

  private val ScopeInfoNameLabel = "otel_scope_name"
  private val ScopeInfoVersionLabel = "otel_scope_version"

  private val Counter = "counter"
  private val Gauge = "gauge"
  private val Histogram = "histogram"

  private val PosInf = "+Inf"
  private val NegInf = "-Inf"

  /** The writer's configuration.
    *
    * @see
    *   [[https://opentelemetry.io/docs/specs/otel/metrics/sdk_exporters/prometheus/#configuration]]
    */
  sealed trait Config {

    /** Whether a unit suffix won't be added to the metric name.
      */
    def unitSuffixDisabled: Boolean

    /** Whether a type suffix won't be added to the metric name.
      */
    def typeSuffixDisabled: Boolean

    /** Whether the instrumentation scope attributes won't be added to the `otel_scope_info` metric.
      */
    def scopeInfoDisabled: Boolean

    /** Whether the telemetry resource attributes won't be added to the `target_info` metric.
      */
    def targetInfoDisabled: Boolean

    /** A unit suffix will be added to the metric name.
      *
      * For example, `_seconds` suffix will be added to the histogram metrics.
      */
    def withUnitSuffix: Config

    /** A unit suffix won't be added to the metric name.
      */
    def withoutUnitSuffix: Config

    /** A type suffix will be added to the metric name.
      *
      * For example, `_total` suffix will be added to the counter.
      */
    def withTypeSuffix: Config

    /** A type suffix won't be added to the metric name.
      */
    def withoutTypeSuffix: Config

    /** The instrumentation scope attributes will be added to the `otel_scope_info` metric. The `otel_scope_name` and
      * `otel_scope_version` labels will be added to the instrument metrics:
      *
      * {{{
      * otel_scope_info{otel_scope_name="meter", otel_scope_version="v0.1.0"}
      * counter_total{otel_scope_name="meter", otel_scope_version="v0.1.0"} 12
      * }}}
      *
      * @see
      *   [[https://opentelemetry.io/docs/specs/otel/compatibility/prometheus_and_openmetrics/#instrumentation-scope-1]]
      */
    def withScopeInfo: Config

    /** The instrumentation scope attributes won't be added to the `otel_scope_info` metric.
      */
    def withoutScopeInfo: Config

    /** The telemetry resource attributes will be added to the `target_info` metric:
      * {{{
      * target_info{telemetry_sdk_name="otel4s"} 1
      * }}}
      *
      * @see
      *   [[https://opentelemetry.io/docs/specs/otel/compatibility/prometheus_and_openmetrics/#resource-attributes-1]]
      */
    def withTargetInfo: Config

    /** The telemetry resource attributes won't be added to the `target_info` metric.
      */
    def withoutTargetInfo: Config

    override final def toString: String = Show[PrometheusWriter.Config].show(this)
  }

  object Config {

    /** The default writer configuration:
      *   - A unit suffix (e.g. `_seconds`) will be added to the metric name
      *   - A type suffix (e.g. `_total`) will be added to the metric name
      *   - Instrumentation scope attributes will be added to the `otel_scope_info` metric
      *   - Telemetry resource attributes will be added to the `target_info` metric
      */
    val default: Config = ConfigImpl(
      unitSuffixDisabled = false,
      typeSuffixDisabled = false,
      scopeInfoDisabled = false,
      targetInfoDisabled = false
    )

    private final case class ConfigImpl(
        unitSuffixDisabled: Boolean,
        typeSuffixDisabled: Boolean,
        scopeInfoDisabled: Boolean,
        targetInfoDisabled: Boolean
    ) extends Config {
      def withUnitSuffix: Config =
        copy(unitSuffixDisabled = false)

      def withoutUnitSuffix: Config =
        copy(unitSuffixDisabled = true)

      def withTypeSuffix: Config =
        copy(typeSuffixDisabled = false)

      def withoutTypeSuffix: Config =
        copy(typeSuffixDisabled = true)

      def withScopeInfo: Config =
        copy(scopeInfoDisabled = false)

      def withoutScopeInfo: Config =
        copy(scopeInfoDisabled = true)

      def withTargetInfo: Config =
        copy(targetInfoDisabled = false)

      def withoutTargetInfo: Config =
        copy(targetInfoDisabled = true)
    }

    implicit val configShow: Show[Config] = Show.show { config =>
      "PrometheusWriter.Config{" +
        s"unitSuffixDisabled=${config.unitSuffixDisabled}, " +
        s"typeSuffixDisabled=${config.typeSuffixDisabled}, " +
        s"scopeInfoDisabled=${config.scopeInfoDisabled}, " +
        s"targetInfoDisabled=${config.targetInfoDisabled}}"
    }

  }

  private final case class MetricGroup(
      prometheusName: String,
      prometheusType: String,
      description: Option[String],
      metrics: NonEmptyVector[MetricData]
  ) {
    def helpLine(metricName: String): Option[String] = description.map { h =>
      s"$metricName ${escapeString(h)}"
    }
  }

  private final case class MetricAggregate(
      groups: Map[String, MetricGroup] = Map.empty,
      scopes: Map[String, InstrumentationScope] = Map.empty,
      resource: Option[TelemetryResource] = None
  ) {
    def tryAddScope(scope: InstrumentationScope, disableScopeInfo: Boolean): MetricAggregate = {
      if (scope.attributes.nonEmpty && !disableScopeInfo) {
        this.copy(scopes = this.scopes + (scope.name -> scope))
      } else {
        this
      }
    }

    def tryUpdateResource(resource: TelemetryResource, disableTargetInfo: Boolean): MetricAggregate = {
      this.resource match {
        case Some(_)                   => this
        case None if disableTargetInfo => this
        case _                         => this.copy(resource = resource.some)
      }
    }

    def addOrUpdateGroup(prometheusName: String, group: MetricGroup): MetricAggregate = {
      this.copy(groups = this.groups + (prometheusName -> group))
    }
  }

  def text[F[_]: MonadThrow: Console](config: Config): PrometheusWriter[F] =
    new TextWriter[F](config)

  /** Writes metrics using the Prometheus text format.
    *
    * @see
    *   [[https://github.com/prometheus/docs/blob/main/content/docs/instrumenting/exposition_formats.md]]
    */
  private final class TextWriter[F[_]: Console](config: Config)(implicit F: MonadThrow[F]) extends PrometheusWriter[F] {
    val contentType: String = "text/plain; version=0.0.4; charset=utf-8"

    def write[G[_]: Foldable](metrics: G[MetricData]): Stream[F, Byte] = {
      Stream
        .foldable(metrics)
        .covary[F]
        .filter(filterMetric)
        .evalScan(MetricAggregate()) { (aggregate, metric) =>
          updateMetricAggregate(aggregate, metric)
        }
        .last
        .unNone
        .flatMap { aggregate =>
          val metricsStream = Stream
            .emits(aggregate.groups.values.toList)
            .evalMap(group => F.fromEither(serialize(group)))
            .through(utf8.encode)
          val scopesStream =
            Stream(aggregate.scopes.values.toVector)
              .map(_.toNev)
              .unNone
              .evalMap(scopes => F.fromEither(serializeScopes(scopes)))
              .through(utf8.encode)
          val targetStream =
            Stream(aggregate.resource)
              .evalMap(resource => F.fromEither(serializeResource(resource)))
              .through(utf8.encode)
          metricsStream ++ scopesStream ++ targetStream
        }
    }

    private def updateMetricAggregate(
        aggregate: MetricAggregate,
        metric: MetricData
    ): F[MetricAggregate] = {
      F.fromEither {
        metric.unit
          .filter(_ => !config.unitSuffixDisabled)
          .map(convertUnitName)
          .fold(convertName(metric.name)) {
            _.flatMap(convertName(metric.name, _))
          }
      }.flatMap { prometheusName =>
        val prometheusType = resolvePrometheusType(metric)
        aggregate.groups.get(prometheusName) match {
          case Some(group) if group.prometheusType == prometheusType =>
            val updatedGroup = group.copy(metrics = group.metrics :+ metric)
            aggregate
              .tryAddScope(metric.instrumentationScope, config.scopeInfoDisabled)
              .tryUpdateResource(metric.resource, config.targetInfoDisabled)
              .addOrUpdateGroup(prometheusName, updatedGroup)
              .pure[F]
          case None =>
            val newGroup = MetricGroup(
              prometheusName,
              prometheusType,
              metric.description,
              NonEmptyVector.one(metric)
            )

            aggregate
              .tryAddScope(metric.instrumentationScope, config.scopeInfoDisabled)
              .tryUpdateResource(metric.resource, config.targetInfoDisabled)
              .addOrUpdateGroup(prometheusName, newGroup)
              .pure[F]
          case Some(group) =>
            Console[F]
              .errorln(
                s"Conflicting metric name [$prometheusName]. " +
                  s"Existing metric type [${group.prometheusType}], " +
                  s"dropped metric type [$prometheusType]"
              )
              .as(aggregate)
        }
      }
    }

    private def filterMetric(metric: MetricData) = {
      metric.data match {
        case sum: MetricPoints.Sum if sum.aggregationTemporality == AggregationTemporality.Cumulative => true
        case _: MetricPoints.Gauge                                                                    => true
        case histogram: MetricPoints.Histogram
            if histogram.aggregationTemporality == AggregationTemporality.Cumulative =>
          true
        case _ => false
      }
    }

    private def resolvePrometheusType(metric: MetricData) = {
      metric.data match {
        case sum: MetricPoints.Sum =>
          if (sum.monotonic) {
            Counter
          } else {
            Gauge
          }
        case _: MetricPoints.Gauge     => Gauge
        case _: MetricPoints.Histogram => Histogram
      }
    }

    private def serialize(metricGroup: MetricGroup): Either[Throwable, String] = {
      metricGroup.metrics.head.data match {
        case sum: MetricPoints.Sum =>
          if (sum.monotonic) {
            serializeSums(metricGroup)
          } else {
            serializeGauges(metricGroup)
          }

        case _: MetricPoints.Gauge     => serializeGauges(metricGroup)
        case _: MetricPoints.Histogram => serializeHistograms(metricGroup)
      }
    }

    private def serializeScopes(scopes: NonEmptyVector[InstrumentationScope]): Either[Throwable, String] = {
      val helpLine = s"$ScopeInfoMetricName $ScopeInfoDescription".some
      val typeLine = s"$ScopeInfoMetricName $Gauge"
      scopes
        .traverse { scope =>
          attributesToLabels(scope.attributes)
            .map(_ + (ScopeInfoNameLabel -> scope.name))
            .map(_ ++ scope.version.map(v => ScopeInfoVersionLabel -> v).toMap)
            .map { labels =>
              PrometheusTextPoint(ScopeInfoMetricName, labels, "1")
            }
        }
        .map(PrometheusTextRecord(helpLine, typeLine, _).show)
    }

    private def serializeResource(resource: Option[TelemetryResource]): Either[Throwable, String] = {
      resource
        .filter(_.attributes.nonEmpty)
        .map { res =>
          val helpLine = s"$TargetInfoName $TargetInfoDescription".some
          val typeLine = s"$TargetInfoName $Gauge"
          attributesToLabels(res.attributes)
            .map { labels =>
              val textPoint = PrometheusTextPoint(TargetInfoName, labels, "1")
              PrometheusTextRecord(helpLine, typeLine, NonEmptyVector.one(textPoint)).show
            }
        }
        .orEmpty
    }

    private def serializeSums(metricGroup: MetricGroup): Either[Throwable, String] = {
      val typeSuffix = if (config.typeSuffixDisabled) "" else "_total"
      serializeSumsOrGauges(metricGroup, typeSuffix.some)
    }

    private def serializeGauges(metricGroup: MetricGroup): Either[Throwable, String] = {
      serializeSumsOrGauges(metricGroup)
    }

    private def serializeHistograms(metricGroup: MetricGroup): Either[Throwable, String] = {
      val typeLine = s"${metricGroup.prometheusName} $Histogram"
      metricGroup.metrics
        .flatTraverse { metric =>
          val scopeLabels = prepareScopeLabels(metric.instrumentationScope)
          NonEmptyVector
            .fromVectorUnsafe(metric.data.points.collect { case point: PointData.Histogram => point })
            .flatTraverse { point =>
              val buckets = point.boundaries.boundaries.toNev
                .map2(point.counts.toNev) { case (boundaries, counts) =>
                  val boundariesWithInf = boundaries.map(formatDouble) :+ PosInf
                  val countsWithInf = if (counts.length > boundaries.length) {
                    counts
                  } else {
                    counts :+ 0L
                  }

                  boundariesWithInf.zipWith(countsWithInf)((_, _))
                }
                .getOrElse(NonEmptyVector.of((PosInf, point.counts.sum)))

              attributesToLabels(point.attributes).map(_ ++ scopeLabels).map { labels =>
                buckets.tail
                  .foldLeft((NonEmptyVector.one(buckets.head), buckets.head._2)) {
                    case ((res, countSoFar), (boundary, count)) =>
                      val cumulativeCount = countSoFar + count
                      (res :+ (boundary, cumulativeCount), cumulativeCount)
                  }
                  ._1
                  .map { case (boundary, cumulativeCount) =>
                    PrometheusTextPoint(
                      s"${metricGroup.prometheusName}_bucket",
                      labels + ("le" -> boundary),
                      cumulativeCount.toString
                    )
                  } ++ point.stats.map { stats =>
                  Vector(
                    PrometheusTextPoint(s"${metricGroup.prometheusName}_count", labels, stats.count.toString),
                    PrometheusTextPoint(s"${metricGroup.prometheusName}_sum", labels, formatDouble(stats.sum))
                  )
                }.orEmpty
              }
            }
        }
        .map(PrometheusTextRecord(metricGroup.helpLine(metricGroup.prometheusName), typeLine, _).show)
    }

    private def serializeSumsOrGauges(
        metricGroup: MetricGroup,
        typeSuffix: Option[String] = None
    ): Either[Throwable, String] = {
      val suffix = typeSuffix.orEmpty
      val typeLine = s"${metricGroup.prometheusName}$suffix ${metricGroup.prometheusType}"
      metricGroup.metrics
        .flatTraverse { metric =>
          val scopeLabels = prepareScopeLabels(metric.instrumentationScope)
          metric.data.points.traverse { point =>
            attributesToLabels(point.attributes).map(_ ++ scopeLabels).map { labels =>
              PrometheusTextPoint(
                s"${metricGroup.prometheusName}$suffix",
                labels,
                serializePointValue(point)
              )
            }
          }
        }
        .map(PrometheusTextRecord(metricGroup.helpLine(s"${metricGroup.prometheusName}$suffix"), typeLine, _).show)
    }

    private def prepareScopeLabels(scope: InstrumentationScope): ListMap[String, String] = {
      if (config.scopeInfoDisabled) {
        ListMap.empty
      } else {
        ListMap(ScopeInfoNameLabel -> scope.name) ++ scope.version.map(v => ScopeInfoVersionLabel -> v).toMap
      }
    }

    private def attributesToLabels(attributes: Attributes): Either[Throwable, ListMap[String, String]] = {
      attributes
        .map(attributeToPair)
        .toList
        .sortBy(_._1)
        .foldLeft(ListMap.empty[String, List[String]].asRight[Throwable]) { case (acc, (name, value)) =>
          acc.flatMap { map =>
            convertLabelName(name).map { convertedName =>
              map + map
                .get(convertedName)
                .map(v => convertedName -> (v :+ value))
                .getOrElse(convertedName -> (value :: Nil))
            }
          }
        }
        .map(_.map({ case (k, v) => k -> v.mkString(";") }))
    }

    private def attributeToPair(attribute: Attribute[_]): (String, String) = {
      val key = attribute.key.name

      def primitive: String = attribute.value.toString

      def seq[A: Encoder]: String =
        attribute.value.asInstanceOf[Seq[A]].toList.asJson.noSpaces

      val value = attribute.key.`type` match {
        case AttributeType.Boolean    => primitive
        case AttributeType.Double     => primitive
        case AttributeType.Long       => primitive
        case AttributeType.String     => primitive
        case AttributeType.BooleanSeq => seq[Boolean]
        case AttributeType.DoubleSeq  => seq[Boolean]
        case AttributeType.LongSeq    => seq[Long]
        case AttributeType.StringSeq  => seq[String]
      }

      (key, escapeString(value))
    }

    private def serializePointValue(point: PointData): String = {
      point match {
        case long: LongNumber => long.value.toString
        case double: DoubleNumber =>
          if (double.value == Double.PositiveInfinity) {
            PosInf
          } else if (double.value == Double.NegativeInfinity) {
            NegInf
          } else {
            formatDouble(double.value)
          }
        case _ => ""
      }
    }

    private def formatDouble(double: Double): String = {
      val long = double.toLong
      if (double == long) long.toString else double.toString
    }

  }

  private def escapeString(s: String): String = {
    s.replace("""\""", """\\""").replace("\"", """\"""").replace("\n", """\n""")
  }

}
