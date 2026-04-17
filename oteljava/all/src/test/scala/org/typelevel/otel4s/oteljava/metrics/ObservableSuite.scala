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
import io.opentelemetry.sdk.metrics.data.MetricData
import munit.CatsEffectSuite
import org.typelevel.otel4s.metrics.Measurement
import org.typelevel.otel4s.oteljava.BuildInfo
import org.typelevel.otel4s.oteljava.testkit.InstrumentationScopeExpectation
import org.typelevel.otel4s.oteljava.testkit.TelemetryResourceExpectation
import org.typelevel.otel4s.oteljava.testkit.metrics.MetricExpectation
import org.typelevel.otel4s.oteljava.testkit.metrics.MetricExpectations
import org.typelevel.otel4s.oteljava.testkit.metrics.MetricsTestkit
import org.typelevel.otel4s.oteljava.testkit.metrics.PointExpectation
import org.typelevel.otel4s.oteljava.testkit.metrics.PointSetExpectation

class ObservableSuite extends CatsEffectSuite {

  test("gauge test") {
    MetricsTestkit.inMemory[IO]().use { sdk =>
      for {
        meter <- sdk.meterProvider
          .meter("java.otel.suite")
          .withVersion("1.0")
          .withSchemaUrl("https://localhost:8080")
          .get

        _ <- meter
          .observableGauge[Double]("gauge")
          .withUnit("unit")
          .withDescription("description")
          .createWithCallback(_.record(42.0, Attribute("foo", "bar")))
          .use(_ =>
            sdk.collectMetrics
              .map(
                assertExpected(
                  _,
                  List(
                    MetricExpectation
                      .gauge[Double]("gauge")
                      .value(42.0, Attributes(Attribute("foo", "bar")))
                      .description("description")
                      .unit("unit")
                      .scope(expectedScope)
                      .resource(expectedResource)
                  )
                )
              )
          )

        _ <- meter
          .observableGauge[Double]("gauge")
          .withUnit("unit")
          .withDescription("description")
          .create(
            IO.pure(
              List(
                Measurement(1336.0, Attribute("1", "2")),
                Measurement(1337.0, Attribute("a", "b"))
              )
            )
          )
          .use(_ =>
            sdk.collectMetrics
              .map(
                assertExpected(
                  _,
                  List(
                    MetricExpectation
                      .gauge[Double]("gauge")
                      .points(
                        PointSetExpectation.contains(
                          PointExpectation.numeric(1336.0).attributesExact(Attribute("1", "2")),
                          PointExpectation.numeric(1337.0).attributesExact(Attribute("a", "b"))
                        )
                      )
                      .description("description")
                      .unit("unit")
                      .scope(expectedScope)
                      .resource(expectedResource)
                  )
                )
              )
          )

      } yield ()
    }
  }

  test("counter test") {
    MetricsTestkit.inMemory[IO]().use { sdk =>
      for {
        meter <- sdk.meterProvider
          .meter("java.otel.suite")
          .withVersion("1.0")
          .withSchemaUrl("https://localhost:8080")
          .get

        _ <- meter
          .observableCounter[Long]("counter")
          .withUnit("unit")
          .withDescription("description")
          .createWithCallback(_.record(1234, Attribute("number", 42L)))
          .use(_ =>
            sdk.collectMetrics
              .map(
                assertExpected(
                  _,
                  List(
                    MetricExpectation
                      .sum[Long]("counter")
                      .value(1234L, Attributes(Attribute("number", 42L)))
                      .description("description")
                      .unit("unit")
                      .scope(expectedScope)
                      .resource(expectedResource)
                  )
                )
              )
          )

        _ <- meter
          .observableCounter[Long]("counter")
          .withUnit("unit")
          .withDescription("description")
          .create(
            IO.pure(
              List(
                Measurement(1336, Attribute("1", "2")),
                Measurement(1337, Attribute("a", "b"))
              )
            )
          )
          .use(_ =>
            sdk.collectMetrics
              .map(
                assertExpected(
                  _,
                  List(
                    MetricExpectation
                      .sum[Long]("counter")
                      .points(
                        PointSetExpectation.contains(
                          PointExpectation.numeric(1336L).attributesExact(Attribute("1", "2")),
                          PointExpectation.numeric(1337L).attributesExact(Attribute("a", "b"))
                        )
                      )
                      .description("description")
                      .unit("unit")
                      .scope(expectedScope)
                      .resource(expectedResource)
                  )
                )
              )
          )

      } yield ()
    }
  }

  test("up down counter test") {
    MetricsTestkit.inMemory[IO]().use { sdk =>
      for {
        meter <- sdk.meterProvider
          .meter("java.otel.suite")
          .withVersion("1.0")
          .withSchemaUrl("https://localhost:8080")
          .get

        _ <- meter
          .observableUpDownCounter[Long]("updowncounter")
          .withUnit("unit")
          .withDescription("description")
          .createWithCallback(
            _.record(1234, Attribute[Boolean]("is_false", true))
          )
          .use(_ =>
            sdk.collectMetrics
              .map(
                assertExpected(
                  _,
                  List(
                    MetricExpectation
                      .sum[Long]("updowncounter")
                      .value(1234L, Attributes(Attribute("is_false", true)))
                      .description("description")
                      .unit("unit")
                      .scope(expectedScope)
                      .resource(expectedResource)
                  )
                )
              )
          )

        _ <- meter
          .observableUpDownCounter[Long]("updowncounter")
          .withUnit("unit")
          .withDescription("description")
          .create(
            IO.pure(
              List(
                Measurement(1336, Attribute("1", "2")),
                Measurement(1336, Attribute("a", "b"))
              )
            )
          )
          .use(_ =>
            sdk.collectMetrics
              .map(
                assertExpected(
                  _,
                  List(
                    MetricExpectation
                      .sum[Long]("updowncounter")
                      .points(
                        PointSetExpectation.contains(
                          PointExpectation.numeric(1336L).attributesExact(Attribute("1", "2")),
                          PointExpectation.numeric(1336L).attributesExact(Attribute("a", "b"))
                        )
                      )
                      .description("description")
                      .unit("unit")
                      .scope(expectedScope)
                      .resource(expectedResource)
                  )
                )
              )
          )

      } yield ()
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
      .version("1.0")
      .schemaUrl("https://localhost:8080")
      .attributesEmpty

  private val expectedResource: TelemetryResourceExpectation =
    TelemetryResourceExpectation.any
      .attributesExact(
        Attribute("service.name", "unknown_service:java"),
        Attribute("telemetry.sdk.language", "java"),
        Attribute("telemetry.sdk.name", "opentelemetry"),
        Attribute("telemetry.sdk.version", BuildInfo.openTelemetrySdkVersion)
      )
      .schemaUrl(None)

}
