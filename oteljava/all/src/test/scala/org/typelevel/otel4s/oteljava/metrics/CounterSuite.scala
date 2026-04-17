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

package org.typelevel.otel4s
package oteljava
package metrics

import cats.effect.IO
import cats.effect.Resource
import io.opentelemetry.api.common.AttributeKey
import io.opentelemetry.sdk.common.InstrumentationScopeInfo
import io.opentelemetry.sdk.metrics._
import io.opentelemetry.sdk.metrics.data.MetricDataType
import munit.CatsEffectSuite
import org.typelevel.otel4s.oteljava.testkit.metrics.MetricsTestkit

import scala.jdk.CollectionConverters._

class CounterSuite extends CatsEffectSuite {

  test("Counter record a proper data") {
    makeSdk.use { sdk =>
      for {
        meter <- sdk.meterProvider
          .meter("java.otel.suite")
          .withVersion("1.0")
          .withSchemaUrl("https://localhost:8080")
          .get

        counter <- meter
          .counter[Long]("counter")
          .withUnit("unit")
          .withDescription("description")
          .create

        _ <- counter.add(1L, Attribute("string-attribute", "value"))

        metrics <- sdk.collectMetrics
      } yield {
        val scope = InstrumentationScopeInfo
          .builder("java.otel.suite")
          .setVersion("1.0")
          .setSchemaUrl("https://localhost:8080")
          .build()

        assertEquals(metrics.map(_.getName), List("counter"))
        assertEquals(metrics.map(m => Option(m.getDescription).filter(_.nonEmpty)), List(Some("description")))
        assertEquals(metrics.map(m => Option(m.getUnit).filter(_.nonEmpty)), List(Some("unit")))
        assertEquals(
          metrics.map(_.getResource.getAttributes.get(AttributeKey.stringKey("service.name"))),
          List("unknown_service:java")
        )
        assertEquals(
          metrics.map(_.getResource.getAttributes.get(AttributeKey.stringKey("telemetry.sdk.language"))),
          List("java")
        )
        assertEquals(
          metrics.map(_.getResource.getAttributes.get(AttributeKey.stringKey("telemetry.sdk.name"))),
          List("opentelemetry")
        )
        assertEquals(
          metrics.map(_.getResource.getAttributes.get(AttributeKey.stringKey("telemetry.sdk.version"))),
          List(BuildInfo.openTelemetrySdkVersion)
        )
        assertEquals(metrics.map(_.getInstrumentationScopeInfo), List(scope))
        assertEquals(metrics.map(_.getType), List(MetricDataType.LONG_SUM))
        assertEquals[List[Any], List[Any]](
          metrics.map(_.getLongSumData.getPoints.asScala.toList.map(_.getValue)),
          List(List(1L))
        )
      }
    }
  }

  private def makeSdk: Resource[IO, MetricsTestkit[IO]] = {
    def customize(builder: SdkMeterProviderBuilder): SdkMeterProviderBuilder =
      builder.registerView(
        InstrumentSelector.builder().setType(InstrumentType.HISTOGRAM).build(),
        View
          .builder()
          .setAggregation(
            Aggregation.explicitBucketHistogram(
              ExplicitBucketHistogramOptions
                .builder()
                .setBucketBoundaries(HistogramBuckets.map(Double.box).asJava)
                .build(),
            )
          )
          .build()
      )

    MetricsTestkit.inMemory(_.addMeterProviderCustomizer(customize))
  }

  private val HistogramBuckets: List[Double] =
    List(0.01, 1, 100)

}
