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
import munit.CatsEffectSuite
import org.typelevel.otel4s.metrics.Measurement
import org.typelevel.otel4s.oteljava.testkit.metrics.MetricsTestkit
import org.typelevel.otel4s.oteljava.testkit.metrics.data.Metric

class ObservableSuite extends CatsEffectSuite {

  test("gauge test") {
    MetricsTestkit.inMemory[IO]().use { sdk =>
      for {
        meter <- sdk.meterProvider
          .meter("java.otel.suite")
          .withVersion("1.0")
          .withSchemaUrl("https://localhost:8080")
          .get

        _ <- meter.observableGauge[Double][Double]("gauge")
          .withUnit("unit")
          .withDescription("description")
          .createWithCallback(_.record(42.0, Attribute("foo", "bar")))
          .use(_ =>
            sdk
              .collectMetrics[Metric]
              .map(_.flatMap(_.data.points.map(x => (x.value, x.attributes))))
              .assertEquals(List((42.0, Attributes(Attribute("foo", "bar")))))
          )

        _ <- meter.observableGauge[Double][Double]("gauge")
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
            sdk
              .collectMetrics[Metric]
              .map(
                _.flatMap(
                  _.data.points.map(x => (x.value, x.attributes))
                ).toSet
              )
              .assertEquals(
                Set[(Any, Attributes)](
                  (1336.0, Attributes(Attribute("1", "2"))),
                  (1337.0, Attributes(Attribute("a", "b")))
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

        _ <- meter.observableCounter[Long][Long]("counter")
          .withUnit("unit")
          .withDescription("description")
          .createWithCallback(_.record(1234, Attribute("number", 42L)))
          .use(_ =>
            sdk
              .collectMetrics[Metric]
              .map(_.flatMap(_.data.points.map(x => (x.value, x.attributes))))
              .assertEquals(List((1234, Attributes(Attribute("number", 42L)))))
          )

        _ <- meter.observableCounter[Long][Long]("counter")
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
            sdk
              .collectMetrics[Metric]
              .map(
                _.flatMap(_.data.points.map(x => (x.value, x.attributes))).toSet
              )
              .assertEquals(
                Set[(Any, Attributes)](
                  (1336, Attributes(Attribute("1", "2"))),
                  (1337, Attributes(Attribute("a", "b")))
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

        _ <- meter.observableUpDownCounter[Long][Long]("updowncounter")
          .withUnit("unit")
          .withDescription("description")
          .createWithCallback(
            _.record(1234, Attribute[Boolean]("is_false", true))
          )
          .use(_ =>
            sdk
              .collectMetrics[Metric]
              .map(_.flatMap(_.data.points.map(x => (x.value, x.attributes))))
              .assertEquals(
                List((1234, Attributes(Attribute("is_false", true))))
              )
          )

        _ <- meter.observableUpDownCounter[Long][Long]("updowncounter")
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
            sdk
              .collectMetrics[Metric]
              .map(
                _.flatMap(_.data.points.map(x => (x.value, x.attributes))).toSet
              )
              .assertEquals(
                Set[(Any, Attributes)](
                  (1336, Attributes(Attribute("1", "2"))),
                  (1336, Attributes(Attribute("a", "b")))
                )
              )
          )

      } yield ()
    }
  }

}
