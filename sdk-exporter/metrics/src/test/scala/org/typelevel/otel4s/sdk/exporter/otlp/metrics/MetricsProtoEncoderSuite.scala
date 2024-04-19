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

package org.typelevel.otel4s
package sdk
package exporter.otlp
package metrics

import io.circe.Json
import io.circe.syntax._
import munit._
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.scalacheck.Test
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens

class MetricsProtoEncoderSuite extends ScalaCheckSuite {
  import MetricsJsonCodecs._
  import MetricsProtoEncoder._

  test("encode ExemplarData") {
    Prop.forAll(Gens.exemplarData) { exemplar =>
      val value = exemplar match {
        case exemplar: ExemplarData.LongExemplar =>
          Json.obj("asInt" := exemplar.value)

        case exemplar: ExemplarData.DoubleExemplar =>
          Json.obj("asDouble" := exemplar.value)
      }

      val expected = Json
        .obj(
          "timeUnixNano" := exemplar.timestamp.toNanos.toString,
          "traceId" := exemplar.traceContext.map(_.traceId.toHex),
          "spanId" := exemplar.traceContext.map(_.spanId.toHex),
          "filteredAttributes" := exemplar.filteredAttributes
        )
        .deepMerge(value)
        .dropNullValues
        .dropEmptyValues

      assertEquals(ProtoEncoder.toJson(exemplar), expected)
    }
  }

  test("encode PointData.NumberPoint") {
    Prop.forAll(Gens.pointDataNumber) { point =>
      val value = point match {
        case number: PointData.LongNumber =>
          Json.obj("asInt" := number.value)

        case number: PointData.DoubleNumber =>
          Json.obj("asDouble" := number.value)
      }

      val expected = Json
        .obj(
          "attributes" := point.attributes,
          "startTimeUnixNano" := point.timeWindow.start.toNanos.toString,
          "timeUnixNano" := point.timeWindow.end.toNanos.toString,
          "exemplars" := (point.exemplars: Vector[ExemplarData])
        )
        .deepMerge(value)
        .dropNullValues
        .dropEmptyValues

      assertEquals(ProtoEncoder.toJson(point), expected)
    }
  }

  test("encode PointData.Histogram") {
    Prop.forAll(Gens.histogramPointData) { point =>
      val expected = Json
        .obj(
          "attributes" := point.attributes,
          "startTimeUnixNano" := point.timeWindow.start.toNanos.toString,
          "timeUnixNano" := point.timeWindow.end.toNanos.toString,
          "count" := point.stats.map(_.count.toString),
          "sum" := point.stats.map(_.sum),
          "min" := point.stats.map(_.min),
          "max" := point.stats.map(_.max),
          "bucketCounts" := point.counts.map(_.toString),
          "explicitBounds" := point.boundaries.boundaries,
          "exemplars" := (point.exemplars: Vector[ExemplarData])
        )
        .dropNullValues
        .dropEmptyValues

      assertEquals(ProtoEncoder.toJson(point), expected)
    }
  }

  test("encode MetricData") {
    Prop.forAll(Gens.metricData) { metricData =>
      val expected = Json
        .obj(
          "name" := metricData.name,
          "description" := metricData.description,
          "unit" := metricData.unit
        )
        .dropNullValues
        .dropEmptyValues
        .deepMerge(metricData.data.asJson)

      assertEquals(ProtoEncoder.toJson(metricData), expected)
    }
  }

  test("encode List[MetricData]") {
    Prop.forAll(Gen.listOf(Gens.metricData)) { metrics =>
      val resourceSpans =
        metrics.groupBy(_.resource).map { case (resource, resourceMetrics) =>
          val scopeMetrics: Iterable[Json] =
            resourceMetrics
              .groupBy(_.instrumentationScope)
              .map { case (scope, spans) =>
                Json
                  .obj(
                    "scope" := scope,
                    "metrics" := spans.map(_.asJson),
                    "schemaUrl" := scope.schemaUrl
                  )
                  .dropNullValues
              }

          Json
            .obj(
              "resource" := resource,
              "scopeMetrics" := scopeMetrics,
              "schemaUrl" := resource.schemaUrl
            )
            .dropNullValues
        }

      val expected =
        Json.obj("resourceMetrics" := resourceSpans).dropEmptyValues

      assertEquals(
        ProtoEncoder.toJson(metrics).noSpacesSortKeys,
        expected.noSpacesSortKeys
      )
    }
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(20)
      .withMaxSize(20)

}
