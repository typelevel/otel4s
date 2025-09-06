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

package org.typelevel.otel4s.sdk.exporter.otlp
package metrics

import cats.data.NonEmptyVector
import cats.effect.IO
import cats.syntax.foldable._
import com.comcast.ip4s.IpAddress
import fs2.io.compression._
import io.circe.Decoder
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.http4s.Headers
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.headers.`X-Forwarded-For`
import org.http4s.syntax.literals._
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Test
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeType
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.exporter.RetryPolicy
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.scalacheck.Arbitraries._

import scala.concurrent.duration._

class OtlpMetricExporterSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  import OtlpMetricExporterSuite._

  private implicit val protocolArbitrary: Arbitrary[OtlpProtocol] =
    Arbitrary(
      Gen.oneOf(
        OtlpProtocol.httpJson,
        OtlpProtocol.httpProtobuf,
        OtlpProtocol.grpc
      )
    )

  private implicit val compressionArbitrary: Arbitrary[PayloadCompression] =
    Arbitrary(
      Gen.oneOf(
        PayloadCompression.gzip,
        PayloadCompression.none
      )
    )

  test("represent builder parameters in the name") {
    PropF.forAllF { (protocol: OtlpProtocol, compression: PayloadCompression) =>
      val expected =
        s"OtlpMetricExporter{client=OtlpClient{protocol=$protocol, " +
          "endpoint=https://localhost:4318/api/v1/metrics, " +
          "timeout=5 seconds, " +
          s"compression=$compression, " +
          "headers={X-Forwarded-For: 127.0.0.1}}}"

      OtlpMetricExporter
        .builder[IO]
        .addHeaders(
          Headers(`X-Forwarded-For`(IpAddress.fromString("127.0.0.1")))
        )
        .withEndpoint(uri"https://localhost:4318/api/v1/metrics")
        .withTimeout(5.seconds)
        .withProtocol(protocol)
        .withCompression(compression)
        .build
        .use { exporter =>
          IO(assertEquals(exporter.name, expected))
        }
    }
  }

  test("change endpoint according to the protocol") {
    PropF.forAllF { (protocol: OtlpProtocol) =>
      val endpoint = protocol match {
        case _: OtlpProtocol.Http =>
          "http://localhost:4318/v1/metrics"

        case OtlpProtocol.Grpc =>
          "http://localhost:4317/opentelemetry.proto.collector.metrics.v1.MetricsService/Export"
      }

      val expected =
        s"OtlpMetricExporter{client=OtlpClient{protocol=$protocol, " +
          s"endpoint=$endpoint, " +
          "timeout=10 seconds, " +
          "compression=none, " +
          "headers={}}}"

      OtlpMetricExporter
        .builder[IO]
        .withProtocol(protocol)
        .build
        .use { exporter =>
          IO(assertEquals(exporter.name, expected))
        }
    }
  }

  test("export metrics") {
    PropF.forAllF { (md: MetricData, protocol: OtlpProtocol, compression: PayloadCompression) =>
      val metric = MetricData(
        md.resource,
        md.instrumentationScope,
        md.name,
        md.description,
        md.unit,
        md.data match {
          case sum: MetricPoints.Sum =>
            val monotonic = sum.aggregationTemporality match {
              case AggregationTemporality.Delta      => true
              case AggregationTemporality.Cumulative => false
            }

            MetricPoints.sum(
              adaptNumberPoints(sum.points),
              monotonic,
              sum.aggregationTemporality
            )

          case gauge: MetricPoints.Gauge =>
            MetricPoints.gauge(adaptNumberPoints(gauge.points))

          case histogram: MetricPoints.Histogram =>
            MetricPoints.histogram(
              adaptHistogramPoints(histogram.points),
              AggregationTemporality.Cumulative
            )
        }
      )

      val expectedSeries: Vector[PrometheusSeries] = {
        val const = Map(
          "instance" -> "collector:9464",
          "job" -> "collector",
          "__name__" -> metricName(metric)
        )

        metric.data match {
          case sum: MetricPoints.Sum =>
            numberPointsToSeries(const, sum.points)

          case gauge: MetricPoints.Gauge =>
            numberPointsToSeries(const, gauge.points)

          case histogram: MetricPoints.Histogram =>
            histogramPointsToSeries(const, histogram.points)
        }
      }

      val endpoint = protocol match {
        case _: OtlpProtocol.Http =>
          uri"http://localhost:44318/v1/metrics"

        case OtlpProtocol.Grpc =>
          uri"http://localhost:44317/opentelemetry.proto.collector.metrics.v1.MetricsService/Export"
      }

      OtlpMetricExporter
        .builder[IO]
        .withProtocol(protocol)
        .withCompression(compression)
        .withEndpoint(endpoint)
        .withTimeout(20.seconds)
        .withRetryPolicy(
          RetryPolicy.builder
            .withInitialBackoff(2.second)
            .withMaxBackoff(20.seconds)
            .build
        )
        .build
        .use(exporter => exporter.exportMetrics(List(metric)))
        .flatMap { _ =>
          findMetrics(metric)
            .delayBy(1.second)
            .flatMap { series =>
              val result = series.map(s => s.metric -> s.value).toMap
              val expected = expectedSeries.map(s => s.metric -> s.value).toMap
//IO.println(result) >> IO.println("\n\n") >> IO.println(expected)>> IO.println("\n\n") >>
              IO(assertEquals(result, expected))
            }
        }
    }
  }

  private def findMetrics(metric: MetricData): IO[Vector[PrometheusSeries]] =
    EmberClientBuilder.default[IO].build.use { client =>
      import org.http4s.syntax.literals._
      import org.http4s.circe.CirceEntityCodec._
      import io.circe.generic.auto._

      val query = metricName(metric)

      val url = (uri"http://localhost:49090" / "api" / "v1" / "query")
        .withQueryParam("query", query)

      def loop(attempts: Int): IO[Vector[PrometheusSeries]] =
        client
          .expectOr[PrometheusResponse](url) { response =>
            for {
              body <- response.as[String]
              _ <- IO.println(
                s"Cannot retrieve metrics due. Status ${response.status} body $body"
              )
            } yield new RuntimeException(
              s"Cannot retrieve metrics due. Status ${response.status} body $body"
            )
          }
          .handleErrorWith { error =>
            IO.println(s"Cannot retrieve metrics due to ${error.getMessage}")
              .as(PrometheusResponse("", PrometheusResult("", Vector.empty)))
          }
          .flatMap { response =>
            if (response.data.result.isEmpty && attempts > 0)
              loop(attempts - 1).delayBy(300.millis)
            else if (response.data.result.isEmpty)
              IO.println(
                s"Couldn't find metrics for [$query] after 10 attempts"
              ).as(response.data.result)
            else
              IO.pure(response.data.result)
          }

      loop(10)
    }

  private def metricName(metric: MetricData): String = {
    val suffix = metric.data match {
      case _: MetricPoints.Histogram          => "_bucket"
      case s: MetricPoints.Sum if s.monotonic => "_total"
      case _                                  => ""
    }

    val prefix = if (metric.name.headOption.exists(_.isDigit)) "_" else ""

    // prometheus expands these units
    val unit = metric.unit
      .map {
        case "ms"  => "milliseconds"
        case "s"   => "seconds"
        case "m"   => "minutes"
        case "h"   => "hours"
        case "d"   => "days"
        case other => other
      }
      .foldMap(unit => "_" + unit)

    prefix + metric.name + unit + suffix
  }

  private def numberPointsToSeries(
      const: Map[String, String],
      points: NonEmptyVector[PointData.NumberPoint]
  ): Vector[PrometheusSeries] =
    points.toVector
      .groupBy(_.attributes)
      .map { case (attributes, points) =>
        val attrs =
          attributes.map(a => attributeToPair(a)).toMap

        PrometheusSeries(
          const ++ attrs,
          points.head match {
            case long: PointData.LongNumber =>
              PrometheusValue(long.value.toString)
            case double: PointData.DoubleNumber =>
              PrometheusValue(BigDecimal(double.value).toString)
          }
        )
      }
      .toVector

  private def histogramPointsToSeries(
      const: Map[String, String],
      points: NonEmptyVector[PointData.Histogram]
  ): Vector[PrometheusSeries] =
    points.toVector
      .groupBy(_.attributes)
      .flatMap { case (attributes, points) =>
        val attrs =
          attributes.map(a => attributeToPair(a)).toMap

        points.flatMap { point =>
          val all = point.boundaries.boundaries.zipWithIndex.map { case (boundary, idx) =>
            PrometheusSeries(
              const ++ attrs ++ Map("le" -> boundary.toString),
              PrometheusValue(point.counts.take(idx + 1).sum.toString)
            )
          }

          val inf = PrometheusSeries(
            const ++ attrs ++ Map("le" -> "+Inf"),
            PrometheusValue(point.counts.sum.toString)
          )

          all :+ inf
        }
      }
      .toVector

  private def attributeToPair(attribute: Attribute[_]): (String, String) = {
    // name
    val key = attribute.key.name
    val prefix = if (key.headOption.exists(_.isDigit)) "key_" else ""
    val name = prefix + key

    // value
    def primitive: String = attribute.value.toString

    def seq[A](escape: Boolean): String =
      attribute.value
        .asInstanceOf[Seq[A]]
        .map(v => if (escape) "\"" + v + "\"" else v)
        .mkString("[", ",", "]")

    val value = attribute.key.`type` match {
      case AttributeType.Boolean    => primitive
      case AttributeType.Double     => primitive
      case AttributeType.Long       => primitive
      case AttributeType.String     => primitive
      case AttributeType.BooleanSeq => seq[Boolean](escape = false)
      case AttributeType.DoubleSeq  => seq[Double](escape = false)
      case AttributeType.LongSeq    => seq[Long](escape = false)
      case AttributeType.StringSeq  => seq[String](escape = true)
    }

    // result
    (name, value)
  }

  // it's hard to deal with big numeric values due to various encoding pitfalls
  // so we simplify the numbers
  private def adaptAttributes(attributes: Attributes): Attributes = {
    val adapted = attributes.map { attribute =>
      val name = attribute.key.name
      attribute.key.`type` match {
        case AttributeType.Double    => Attribute(name, 1.1)
        case AttributeType.DoubleSeq => Attribute(name, Seq(1.1))
        case AttributeType.Long      => Attribute(name, 1L)
        case AttributeType.LongSeq   => Attribute(name, Seq(1L))
        case _                       => attribute
      }
    }

    adapted.to(Attributes)
  }

  private def adaptNumberPoints(
      points: NonEmptyVector[PointData.NumberPoint]
  ): NonEmptyVector[PointData.NumberPoint] =
    NonEmptyVector.one(
      points.map {
        case long: PointData.LongNumber =>
          PointData.longNumber(
            long.timeWindow,
            adaptAttributes(long.attributes),
            long.exemplars,
            math.max(math.min(long.value, 100L), -100L)
          )

        case double: PointData.DoubleNumber =>
          PointData.doubleNumber(
            double.timeWindow,
            adaptAttributes(double.attributes),
            double.exemplars,
            math.max(math.min(double.value, 100.0), -100.0)
          )
      }.head
    )

  private def adaptHistogramPoints(
      points: NonEmptyVector[PointData.Histogram]
  ): NonEmptyVector[PointData.Histogram] =
    NonEmptyVector.one(
      points.map { point =>
        PointData.histogram(
          point.timeWindow,
          adaptAttributes(point.attributes),
          point.exemplars,
          point.stats,
          point.boundaries,
          point.counts
        )
      }.head
    )

  override def munitIOTimeout: Duration =
    1.minute

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(10)
      .withMaxSize(10)

}

object OtlpMetricExporterSuite {

  implicit val prometheusValueDecoder: Decoder[PrometheusValue] =
    Decoder.instance { cursor =>
      for {
        // unixTimestamp <- cursor.downN(0).as[Json] // either Long or Double
        value <- cursor.downN(1).as[String]
      } yield PrometheusValue(value)
    }

  final case class PrometheusValue(value: String)

  final case class PrometheusSeries(
      metric: Map[String, String],
      value: PrometheusValue
  )

  final case class PrometheusResult(
      resultType: String,
      result: Vector[PrometheusSeries]
  )

  final case class PrometheusResponse(
      status: String,
      data: PrometheusResult
  )

}
