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

package org.typelevel.otel4s.sdk.metrics.exporter

import cats.effect.IO
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.Test
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens
import org.typelevel.otel4s.sdk.test.InMemoryConsole
import org.typelevel.otel4s.sdk.test.InMemoryConsole._

class ConsoleMetricExporterSuite
    extends CatsEffectSuite
    with ScalaCheckEffectSuite {

  test("print metrics to the console") {
    PropF.forAllF(Gen.listOf(Gens.metricData)) { metrics =>
      InMemoryConsole.create[IO].flatMap { implicit C: InMemoryConsole[IO] =>
        val exporter = ConsoleMetricExporter[IO]

        val expected =
          if (metrics.isEmpty) Nil
          else {
            val first = Entry(
              Op.Println,
              s"ConsoleMetricExporter: received a collection of [${metrics.size}] metrics for export."
            )

            val rest = metrics.map { metric =>
              Entry(Op.Println, s"ConsoleMetricExporter: $metric")
            }

            first :: rest
          }

        for {
          _ <- exporter.exportMetrics(metrics)
          entries <- C.entries
        } yield assertEquals(entries, expected)
      }
    }
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(20)
      .withMaxSize(20)

}
