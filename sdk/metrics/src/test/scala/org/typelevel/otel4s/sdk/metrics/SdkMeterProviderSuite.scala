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

package org.typelevel.otel4s.sdk.metrics

import cats.effect.IO
import cats.effect.std.Random
import cats.mtl.Ask
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.InMemoryMetricReader
import org.typelevel.otel4s.sdk.metrics.exporter.MetricProducer
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens
import org.typelevel.otel4s.sdk.metrics.view.InstrumentSelector
import org.typelevel.otel4s.sdk.metrics.view.RegisteredView
import org.typelevel.otel4s.sdk.metrics.view.View

class SdkMeterProviderSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private implicit val askContext: AskContext[IO] = Ask.const(Context.root)

  test("empty builder - return noop instance") {
    Random.scalaUtilRandom[IO].flatMap { implicit R: Random[IO] =>
      for {
        provider <- SdkMeterProvider.builder[IO].build
      } yield assertEquals(provider.toString, "MeterProvider.Noop")
    }
  }

  test("reflect parameters in the toString") {
    PropF.forAllF(Gens.telemetryResource) { resource =>
      val reader = new InMemoryMetricReader(
        emptyProducer,
        AggregationTemporalitySelector.alwaysCumulative
      )

      val selector = InstrumentSelector.builder.withInstrumentName("*").build
      val view = View.builder.build
      val rv = RegisteredView(selector, view)

      val expected = {
        val metricReader = reader.toString
        val metricProducer = emptyProducer.toString
        s"SdkMeterProvider{resource=$resource, metricReaders=[$metricReader], metricProducers=[$metricProducer], views=[$rv]}"
      }

      Random.scalaUtilRandom[IO].flatMap { implicit R: Random[IO] =>
        for {
          provider <- SdkMeterProvider
            .builder[IO]
            .withResource(resource)
            .registerMetricReader(reader)
            .registerMetricProducer(emptyProducer)
            .registerView(selector, view)
            .build
        } yield assertEquals(provider.toString, expected)
      }
    }
  }

  private def emptyProducer: MetricProducer[IO] =
    new MetricProducer[IO] {
      def produce: IO[Vector[MetricData]] = IO.pure(Vector.empty)
      override def toString: String = "MetricProducer.Empty"
    }

}
