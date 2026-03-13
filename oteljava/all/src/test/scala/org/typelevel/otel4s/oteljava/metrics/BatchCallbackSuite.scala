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

package org.typelevel.otel4s.oteljava.metrics

import cats.effect.IO
import io.opentelemetry.sdk.metrics.data.MetricData
import munit.CatsEffectSuite
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.BuildInfo
import org.typelevel.otel4s.oteljava.testkit.{InstrumentationScopeExpectation, TelemetryResourceExpectation}
import org.typelevel.otel4s.oteljava.testkit.metrics.{
  MetricExpectation,
  MetricExpectations,
  MetricsTestkit
}

class BatchCallbackSuite extends CatsEffectSuite {

  test("update multiple observers") {
    MetricsTestkit.inMemory[IO]().use { metrics =>
      for {
        meter <- metrics.meterProvider
          .meter("java.otel.suite")
          .withVersion("1.0")
          .withSchemaUrl("https://localhost:8080")
          .get

        metrics <- meter.batchCallback
          .of(
            meter.observableCounter[Long]("long-counter").createObserver,
            meter.observableCounter[Double]("double-counter").createObserver,
            meter
              .observableUpDownCounter[Long]("long-up-down-counter")
              .createObserver,
            meter
              .observableUpDownCounter[Double]("double-up-down-counter")
              .createObserver,
            meter.observableGauge[Long]("long-gauge").createObserver,
            meter.observableGauge[Double]("double-gauge").createObserver
          ) {
            (
                counter1,
                counter2,
                upDownCounter1,
                upDownCounter2,
                gauge1,
                gauge2
            ) =>
              for {
                _ <- counter1.record(1, Attribute("key", "value1"))
                _ <- counter2.record(1.1, Attribute("key", "value2"))
                _ <- upDownCounter1.record(2, Attribute("key", "value3"))
                _ <- upDownCounter2.record(2.1, Attribute("key", "value4"))
                _ <- gauge1.record(3, Attribute("key", "value5"))
                _ <- gauge2.record(3.1, Attribute("key", "value6"))
              } yield ()
          }
          .surround(metrics.collectMetrics[MetricData])
      } yield {
        assertExpected(
          metrics,
          List(
            MetricExpectation
              .sum[Double]("double-counter")
              .withValue(1.1, Attributes(Attribute("key", "value2")))
              .withScope(expectedScope)
              .withResource(expectedResource),
            MetricExpectation
              .gauge[Double]("double-gauge")
              .withValue(3.1, Attributes(Attribute("key", "value6")))
              .withScope(expectedScope)
              .withResource(expectedResource),
            MetricExpectation
              .sum[Double]("double-up-down-counter")
              .withValue(2.1, Attributes(Attribute("key", "value4")))
              .withScope(expectedScope)
              .withResource(expectedResource),
            MetricExpectation
              .sum[Long]("long-counter")
              .withValue(1L, Attributes(Attribute("key", "value1")))
              .withScope(expectedScope)
              .withResource(expectedResource),
            MetricExpectation
              .gauge[Long]("long-gauge")
              .withValue(3L, Attributes(Attribute("key", "value5")))
              .withScope(expectedScope)
              .withResource(expectedResource),
            MetricExpectation
              .sum[Long]("long-up-down-counter")
              .withValue(2L, Attributes(Attribute("key", "value3")))
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
      .withAttributesExact()

  private val expectedResource: TelemetryResourceExpectation =
    TelemetryResourceExpectation.any
      .withAttributesExact(
        Attribute("service.name", "unknown_service:java"),
        Attribute("telemetry.sdk.language", "java"),
        Attribute("telemetry.sdk.name", "opentelemetry"),
        Attribute("telemetry.sdk.version", BuildInfo.openTelemetrySdkVersion)
      )
      .withSchemaUrl(None)

}
