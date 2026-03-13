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
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics._
import munit.CatsEffectSuite
import org.typelevel.otel4s.oteljava.testkit.{InstrumentationScopeExpectation, TelemetryResourceExpectation}
import org.typelevel.otel4s.oteljava.testkit.metrics.{MetricExpectation, MetricExpectations, MetricsTestkit}

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

        metrics <- sdk.collectMetrics[MetricData]
      } yield {
        assertExpected(
          metrics,
          List(
            MetricExpectation
              .sum[Long]("counter")
              .withValue(1L, Attributes(Attribute("string-attribute", "value")))
              .withDescription("description")
              .withUnit("unit")
              .withScope(expectedScope)
              .withResource(expectedResource)
          )
        )
      }
    }
  }

  private def assertExpected(metrics: List[MetricData], expected: List[MetricExpectation]): Unit =
    MetricExpectations.checkAll(metrics, expected) match {
      case Right(_) =>
        ()
      case Left(mismatches) =>
        fail(MetricExpectations.format(mismatches))
    }

  private val expectedScope: InstrumentationScopeExpectation =
    InstrumentationScopeExpectation
      .name("java.otel.suite")
      .withVersion("1.0")
      .withSchemaUrl("https://localhost:8080")
      .withAttributesEmpty

  private val expectedResource: TelemetryResourceExpectation =
    TelemetryResourceExpectation.any
      .withAttributesExact(
        Attribute("service.name", "unknown_service:java"),
        Attribute("telemetry.sdk.language", "java"),
        Attribute("telemetry.sdk.name", "opentelemetry"),
        Attribute("telemetry.sdk.version", BuildInfo.openTelemetrySdkVersion)
      )
      .withSchemaUrl(None)

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
