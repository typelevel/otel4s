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
import munit.CatsEffectSuite
import org.typelevel.otel4s.metrics.Measurement
import org.typelevel.otel4s.testkit.metrics.MetricsSdk

class ObservableSuite extends CatsEffectSuite {

  test("gauge test") {
    for {
      sdk <- IO.delay(MetricsSdk.create[IO]())
      meter <- Metrics
        .forAsync[IO](sdk.sdk)
        .meterProvider
        .meter("java.otel.suite")
        .withVersion("1.0")
        .withSchemaUrl("https://localhost:8080")
        .get

      _ <- meter
        .observableGauge("gauge")
        .withUnit("unit")
        .withDescription("description")
        .createWithCallback(_.record(42.0, Attribute("foo", "bar")))
        .use(_ =>
          sdk.metrics
            .map(_.flatMap(_.data.points.map(x => (x.value, x.attributes))))
            .assertEquals(List((42.0, List(Attribute("foo", "bar")))))
        )

      _ <- meter
        .observableGauge("gauge")
        .withUnit("unit")
        .withDescription("description")
        .create(
          IO.pure(
            List(
              Measurement(1336.0, List(Attribute("1", "2"))),
              Measurement(1337.0, List(Attribute("a", "b")))
            )
          )
        )
        .use(_ =>
          sdk.metrics
            .map(
              _.flatMap(_.data.points.map(x => (x.value, x.attributes)))
            )
            .assertEquals(
              List(
                (1336.0, List(Attribute("1", "2"))),
                (1337.0, List(Attribute("a", "b")))
              )
            )
        )

    } yield ()
  }

  test("counter test") {
    for {
      sdk <- IO.delay(MetricsSdk.create[IO]())
      meter <- Metrics
        .forAsync[IO](sdk.sdk)
        .meterProvider
        .meter("java.otel.suite")
        .withVersion("1.0")
        .withSchemaUrl("https://localhost:8080")
        .get

      _ <- meter
        .observableCounter("counter")
        .withUnit("unit")
        .withDescription("description")
        .createWithCallback(_.record(1234, Attribute("number", 42L)))
        .use(_ =>
          sdk.metrics
            .map(_.flatMap(_.data.points.map(x => (x.value, x.attributes))))
            .assertEquals(List((1234, List(Attribute("number", 42L)))))
        )

      _ <- meter
        .observableCounter("counter")
        .withUnit("unit")
        .withDescription("description")
        .create(
          IO.pure(
            List(
              Measurement(1336, List(Attribute("1", "2"))),
              Measurement(1337, List(Attribute("a", "b")))
            )
          )
        )
        .use(_ =>
          sdk.metrics
            .map(
              _.flatMap(_.data.points.map(x => (x.value, x.attributes)))
            )
            .assertEquals(
              List(
                (1336, List(Attribute("1", "2"))),
                (1337, List(Attribute("a", "b")))
              )
            )
        )

    } yield ()
  }

  test("up down counter test") {
    for {
      sdk <- IO.delay(MetricsSdk.create[IO]())
      meter <- Metrics
        .forAsync[IO](sdk.sdk)
        .meterProvider
        .meter("java.otel.suite")
        .withVersion("1.0")
        .withSchemaUrl("https://localhost:8080")
        .get

      _ <- meter
        .observableUpDownCounter("updowncounter")
        .withUnit("unit")
        .withDescription("description")
        .createWithCallback(
          _.record(1234, Attribute[Boolean]("is_false", true))
        )
        .use(_ =>
          sdk.metrics
            .map(_.flatMap(_.data.points.map(x => (x.value, x.attributes))))
            .assertEquals(List((1234, List(Attribute("is_false", true)))))
        )

      _ <- meter
        .observableUpDownCounter("updowncounter")
        .withUnit("unit")
        .withDescription("description")
        .create(
          IO.pure(
            List(
              Measurement(1336, List(Attribute("1", "2"))),
              Measurement(1336, List(Attribute("a", "b")))
            )
          )
        )
        .use(_ =>
          sdk.metrics
            .map(
              _.flatMap(_.data.points.map(x => (x.value, x.attributes)))
            )
            .assertEquals(
              List(
                (1336, List(Attribute("1", "2"))),
                (1336, List(Attribute("a", "b")))
              )
            )
        )

    } yield ()
  }

}
