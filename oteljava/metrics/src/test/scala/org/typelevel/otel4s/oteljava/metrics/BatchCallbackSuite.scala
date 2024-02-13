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
import munit.CatsEffectSuite
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.testkit.metrics.MetricsSdk

class BatchCallbackSuite extends CatsEffectSuite {

  test("update multiple observers") {
    for {
      sdk <- IO.delay(MetricsSdk.create[IO]())
      meter <- Metrics
        .forAsync[IO](sdk.sdk)
        .meterProvider
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
        .surround(sdk.metrics)
    } yield {
      assertEquals(
        metrics.view
          .map(r => (r.name, r.data.points.map(v => (v.value, v.attributes))))
          .toMap,
        Map(
          "double-counter" -> List(1.1 -> List(Attribute("key", "value2"))),
          "double-gauge" -> List(3.1 -> List(Attribute("key", "value6"))),
          "double-up-down-counter" -> List(
            2.1 -> List(Attribute("key", "value4"))
          ),
          "long-counter" -> List(1 -> List(Attribute("key", "value1"))),
          "long-gauge" -> List(3 -> List(Attribute("key", "value5"))),
          "long-up-down-counter" -> List(2 -> List(Attribute("key", "value3")))
        )
      )
    }
  }

}
