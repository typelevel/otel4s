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

package org.typelevel.otel4s.oteljava.testkit.metrics

import cats.effect.IO
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanContext
import io.opentelemetry.api.trace.TraceFlags
import io.opentelemetry.api.trace.TraceState
import io.opentelemetry.context.{Context => JContext}
import munit.CatsEffectSuite
import org.typelevel.otel4s.oteljava.context.Context

import scala.jdk.CollectionConverters._

class MetricsTestkitSuite extends CatsEffectSuite {
  test("deferred measurements use captured context for exemplars") {
    MetricsTestkit.inMemory[IO]().use { testkit =>
      val spanContext = SpanContext.create(
        "0af7651916cd43dd8448eb211c80319c",
        "b7ad6b7169203331",
        TraceFlags.getSampled,
        TraceState.getDefault
      )
      val context = Context.wrap(Span.wrap(spanContext).storeInContext(JContext.root()))
      val manager = testkit.contextManager

      for {
        meter <- testkit.meterProvider.get("context-manager")
        histogram <- meter.histogram[Double]("duration").create
        restore <- manager.scope(context)(manager.captureScope)
        _ <- restore(histogram.record(1.0))
        ambient <- manager.current
        metrics <- testkit.collectMetrics
      } yield {
        val exemplars = metrics.flatMap(_.getHistogramData.getPoints.asScala).flatMap(_.getExemplars.asScala)
        assertEquals(exemplars.map(_.getSpanContext), List(spanContext))
        assert(!Span.fromContext(ambient.underlying).getSpanContext.isValid)
      }
    }
  }
}
