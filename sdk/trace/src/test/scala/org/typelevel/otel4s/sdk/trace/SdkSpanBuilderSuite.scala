/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.trace

import cats.effect.IO
import cats.effect.IOLocal
import cats.effect.std.Random
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Test
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.instances.local._
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.exporter.InMemorySpanExporter
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter
import org.typelevel.otel4s.sdk.trace.processor.SimpleSpanProcessor
import org.typelevel.otel4s.sdk.trace.samplers.Sampler
import org.typelevel.otel4s.sdk.trace.scalacheck.Arbitraries._
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind

import scala.concurrent.duration.FiniteDuration

class SdkSpanBuilderSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  test("defaults") {
    PropF.forAllF { (name: String, scope: InstrumentationScope) =>
      for {
        traceScope <- createTraceScope
        inMemory <- InMemorySpanExporter.create[IO](None)
        state <- createState(inMemory)
      } yield {
        val builder = new SdkSpanBuilder(name, scope, state, traceScope)

        assertEquals(builder.name, name)
        assertEquals(builder.parent, SdkSpanBuilder.Parent.Propagate)
        assertEquals(builder.kind, None)
        assertEquals(builder.links, Vector.empty)
        assertEquals(builder.attributes, Vector.empty)
        assertEquals(builder.startTimestamp, None)
      }
    }
  }

  test("create a span with the configured parameters") {
    PropF.forAllF {
      (
          name: String,
          scope: InstrumentationScope,
          parent: Option[SpanContext],
          kind: SpanKind,
          startTimestamp: Option[FiniteDuration],
          links: Vector[LinkData],
          attributes: Attributes
      ) =>
        for {
          traceScope <- createTraceScope
          inMemory <- InMemorySpanExporter.create[IO](None)
          state <- createState(inMemory)
          _ <- {
            val builder: SpanBuilder[IO] =
              new SdkSpanBuilder(name, scope, state, traceScope)

            val withParent =
              parent.foldLeft(builder)(_.withParent(_))

            val withTimestamp =
              startTimestamp.foldLeft(withParent)(_.withStartTimestamp(_))

            val withLinks = links.foldLeft(withTimestamp) { (b, link) =>
              b.addLink(link.spanContext, link.attributes.toSeq: _*)
            }

            val withAttributes =
              withLinks.addAttributes(attributes.toSeq: _*)

            val withKind =
              withAttributes.withSpanKind(kind)

            withKind.build.use(IO.pure)
          }
          spans <- inMemory.finishedSpans
        } yield {
          assertEquals(spans.map(_.spanContext.isValid), List(true))
          assertEquals(spans.map(_.spanContext.isRemote), List(false))
          assertEquals(spans.map(_.spanContext.isSampled), List(true))
          assertEquals(spans.map(_.name), List(name))
          assertEquals(spans.map(_.parentSpanContext), List(parent))
          assertEquals(spans.map(_.kind), List(kind))
          assertEquals(spans.map(_.links), List(links))
          assertEquals(spans.map(_.attributes), List(attributes))
          assertEquals(spans.map(_.instrumentationScope), List(scope))
          assertEquals(spans.map(_.resource), List(state.resource))
        }
    }
  }

  test("create a propagating span when the sampling decision is Drop") {
    PropF.forAllF { (name: String, scope: InstrumentationScope) =>
      for {
        traceScope <- createTraceScope
        inMemory <- InMemorySpanExporter.create[IO](None)
        state <- createState(inMemory, Sampler.AlwaysOff)
        builder = new SdkSpanBuilder(name, scope, state, traceScope)
        span <- builder.build.use(IO.pure)
        spans <- inMemory.finishedSpans
      } yield {
        assertEquals(span.context.isValid, true)
        assertEquals(span.context.isRemote, false)
        assertEquals(span.context.isSampled, false)
        assertEquals(spans, Nil)
      }
    }
  }

  private def createTraceScope: IO[SdkTraceScope[IO]] =
    IOLocal(Context.root).map { implicit ioLocal =>
      SdkTraceScope.fromLocal[IO]
    }

  private def createState(
      exporter: SpanExporter[IO],
      sampler: Sampler = Sampler.AlwaysOn
  ): IO[TracerSharedState[IO]] =
    Random.scalaUtilRandom[IO].map { implicit random =>
      TracerSharedState(
        IdGenerator.random[IO],
        TelemetryResource.default,
        sampler,
        SimpleSpanProcessor(exporter)
      )
    }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(10)
      .withMaxSize(10)

}
