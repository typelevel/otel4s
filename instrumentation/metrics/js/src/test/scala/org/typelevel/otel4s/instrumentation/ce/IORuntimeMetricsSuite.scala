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

package org.typelevel.otel4s.instrumentation.ce

import cats.Show
import cats.effect.IO
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Prop
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.scalacheck.Arbitraries._
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.testkit.metrics.MetricsTestkit

class IORuntimeMetricsSuite extends CatsEffectSuite with ScalaCheckEffectSuite {
  import IORuntimeMetrics.Config.CpuStarvationConfig

  test("register metrics using default config") {
    MetricsTestkit.inMemory[IO]().use { testkit =>
      implicit val meterProvider: MeterProvider[IO] = testkit.meterProvider

      val expected = cpuStarvationMetrics

      for {
        metrics <- IORuntimeMetrics
          .register[IO](munitIORuntime.metrics, IORuntimeMetrics.Config.default)
          .surround(testkit.collectMetrics)
      } yield assertEquals(metrics.map(toMetric).sortBy(_.name), expected.sortBy(_.name))
    }
  }

  test("register metrics according to the config") {
    PropF.forAllF { (config: IORuntimeMetrics.Config) =>
      MetricsTestkit.inMemory[IO]().use { testkit =>
        implicit val meterProvider: MeterProvider[IO] = testkit.meterProvider

        val expected = List(
          config.cpuStarvation.enabled -> cpuStarvationMetrics,
        ).collect { case (true, metrics) => metrics }.flatten

        for {
          metrics <- IORuntimeMetrics
            .register[IO](munitIORuntime.metrics, config)
            .surround(testkit.collectMetrics)
        } yield assertEquals(metrics.map(toMetric).sortBy(_.name), expected.sortBy(_.name))
      }
    }
  }

  test("Show[IORuntimeMetrics.Config]") {
    Prop.forAll { (config: IORuntimeMetrics.Config) =>
      val cpuStarvation = config.cpuStarvation

      val expected = "IORuntimeMetrics.Config{" +
        s"cpuStarvation=CpuStarvationConfig{enabled=${cpuStarvation.enabled}, attributes=${cpuStarvation.attributes}}" +
        "}"

      assertEquals(Show[IORuntimeMetrics.Config].show(config), expected)
      assertEquals(config.toString, expected)
    }
  }

  private case class Metric(name: String, description: Option[String], unit: Option[String])

  private def toMetric(metric: MetricData): Metric =
    Metric(metric.name, metric.description, metric.unit)

  private val cpuStarvationMetrics = List(
    Metric(
      "cats.effect.runtime.cpu.starvation.count",
      Some("The number of CPU starvation events."),
      None
    ),
    Metric(
      "cats.effect.runtime.cpu.starvation.clock.drift.current",
      Some("The current CPU drift in milliseconds."),
      Some("ms")
    ),
    Metric(
      "cats.effect.runtime.cpu.starvation.clock.drift.max",
      Some("The max CPU drift in milliseconds."),
      Some("ms")
    )
  )

  private implicit val cpuStarvationArbitrary: Arbitrary[CpuStarvationConfig] =
    Arbitrary(
      for {
        enabled <- Arbitrary.arbitrary[Boolean]
        attributes <- Arbitrary.arbitrary[Attributes]
      } yield if (enabled) CpuStarvationConfig.enabled(attributes) else CpuStarvationConfig.disabled
    )

  private implicit val configArbitrary: Arbitrary[IORuntimeMetrics.Config] =
    Arbitrary(
      for {
        cpuStarvation <- Arbitrary.arbitrary[CpuStarvationConfig]
      } yield IORuntimeMetrics.Config(cpuStarvation)
    )

}
