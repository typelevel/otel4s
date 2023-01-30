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
package java
package metrics

import cats.effect.IO
import io.opentelemetry.sdk.metrics._
import munit.CatsEffectSuite
import org.typelevel.otel4s.testkit._
import org.typelevel.otel4s.testkit.metrics.MetricsSdk

import scala.jdk.CollectionConverters._

class CounterSuite extends CatsEffectSuite {

  test("Counter record a proper data") {
    for {
      sdk <- IO.delay(makeSdk)
      meter <- Metrics
        .forSync[IO](sdk.sdk)
        .meterProvider
        .meter("java.otel.suite")
        .withVersion("1.0")
        .withSchemaUrl("https://localhost:8080")
        .get

      counter <- meter
        .counter("counter")
        .withUnit("unit")
        .withDescription("description")
        .create

      _ <- counter.add(1L, Attribute("string-attribute", "value"))

      metrics <- sdk.metrics
    } yield {
      val resourceAttributes = List(
        Attribute("service.name", "unknown_service:java"),
        Attribute("telemetry.sdk.language", "java"),
        Attribute("telemetry.sdk.name", "opentelemetry"),
        Attribute("telemetry.sdk.version", BuildInfo.openTelemetrySdkVersion)
      )

      val scope = new InstrumentationScope(
        name = "java.otel.suite",
        version = Some("1.0"),
        schemaUrl = Some("https://localhost:8080")
      )

      assertEquals(metrics.map(_.name), List("counter"))
      assertEquals(metrics.map(_.description), List(Some("description")))
      assertEquals(metrics.map(_.unit), List(Some("unit")))
      assertEquals(
        metrics.map(_.resource),
        List(new InstrumentationResource(None, resourceAttributes))
      )
      assertEquals(metrics.map(_.scope), List(scope))
      assertEquals[List[Any], List[Any]](
        metrics.map(_.data.points.map(_.value)),
        List(List(1L))
      )
    }
  }

  private def makeSdk: MetricsSdk[IO] = {
    def customize(builder: SdkMeterProviderBuilder): SdkMeterProviderBuilder =
      builder.registerView(
        InstrumentSelector.builder().setType(InstrumentType.HISTOGRAM).build(),
        View
          .builder()
          .setAggregation(
            Aggregation.explicitBucketHistogram(
              HistogramBuckets.map(Double.box).asJava
            )
          )
          .build()
      )

    MetricsSdk.create[IO](customize)
  }

  private val HistogramBuckets: List[Double] =
    List(0.01, 1, 100)

}
