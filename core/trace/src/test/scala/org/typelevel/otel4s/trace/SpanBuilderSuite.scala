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

package org.typelevel.otel4s.trace

import cats.effect.IO
import munit.FunSuite
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.meta.InstrumentMeta

import scala.concurrent.duration._

class SpanBuilderSuite extends FunSuite {

  test("do not allocate attributes when instrument is noop") {
    val tracer = Tracer.noop[IO]

    var allocated = false

    def timestamp: FiniteDuration = {
      allocated = true
      100.millis
    }

    def attribute: Attribute[String] = {
      allocated = true
      Attribute("key", "value")
    }

    def attributes: List[Attribute[String]] = {
      allocated = true
      List(Attribute("key", "value"))
    }

    def spanContext: SpanContext = {
      allocated = true
      SpanContext.invalid
    }

    def finalizationStrategy: SpanFinalizer.Strategy = {
      allocated = true
      SpanFinalizer.Strategy.empty
    }

    def spanKind: SpanKind = {
      allocated = true
      SpanKind.Client
    }

    // test varargs and Iterable overloads
    tracer
      .spanBuilder("span")
      .addAttributes(attribute, attribute)
      .addAttribute(Attribute("k", "v"))
      .addAttributes(attributes)
      .addLink(spanContext, attribute, attribute)
      .addLink(spanContext, attributes)
      .withParent(spanContext)
      .withFinalizationStrategy(finalizationStrategy)
      .withSpanKind(spanKind)
      .withStartTimestamp(timestamp)
      .build

    assert(!allocated)
  }

  test("macro should work with non-chained calls") {
    val tracer = Tracer.noop[IO]

    var allocated = false

    def timestamp: FiniteDuration = {
      allocated = true
      100.millis
    }

    def attribute: Attribute[String] = {
      allocated = true
      Attribute("key", "value")
    }

    def attributes: List[Attribute[String]] = {
      allocated = true
      List(Attribute("key", "value"))
    }

    def spanContext: SpanContext = {
      allocated = true
      SpanContext.invalid
    }

    def finalizationStrategy: SpanFinalizer.Strategy = {
      allocated = true
      SpanFinalizer.Strategy.empty
    }

    def spanKind: SpanKind = {
      allocated = true
      SpanKind.Client
    }

    var b = tracer
      .spanBuilder("span")

    b = b.addAttribute(attribute)
    b = b.addAttributes(attribute, attribute)
    b = b.addAttributes(attributes)
    b = b.addLink(spanContext, attribute, attribute)
    b = b.addLink(spanContext, attributes)
    b = b.withParent(spanContext)
    b = b.withFinalizationStrategy(finalizationStrategy)
    b = b.withSpanKind(spanKind)
    b = b.withStartTimestamp(timestamp)
    b = b.addAttribute(attribute)
    b = b.addAttributes(attribute, attribute)
    b = b.addAttributes(attributes)
    b = b.addLink(spanContext, attribute, attribute)
    b = b.addLink(spanContext, attributes)

    b = b
      .withParent(spanContext)
      .withFinalizationStrategy(finalizationStrategy)
      .withSpanKind(spanKind)
      .withStartTimestamp(timestamp)
      .addAttribute(attribute)
      .addAttributes(attribute, attribute)
      .addAttributes(attributes)
      .addLink(spanContext, attribute, attribute)
      .addLink(spanContext, attributes)

    b = b.withParent(spanContext)
    b = b.withFinalizationStrategy(finalizationStrategy)
    b = b.withSpanKind(spanKind)
    b = b.withStartTimestamp(timestamp)
    b = b.addAttribute(attribute)
    b = b.addAttributes(attribute, attribute)
    b = b.addAttributes(attributes)
    b = b.addLink(spanContext, attribute, attribute)
    b = b.addLink(spanContext, attributes)
    b = b.withParent(spanContext)
    b = b.withFinalizationStrategy(finalizationStrategy)
    b = b.withSpanKind(spanKind)
    b = b.withStartTimestamp(timestamp)
    b.build

    assert(!allocated)
  }

  // if the optimization is not applied, the compilation will fail with
  // Method too large: org/typelevel/otel4s/...
  // Scala 3 compiler optimizes chained calls out of the box
  // But Scala 2 doesn't, see SpanBuilderMacro#whenEnabled for manual optimization logic
  test("optimize chained calls") {
    val tracer = Tracer.noop[IO]

    var allocated = false

    def timestamp: FiniteDuration = {
      allocated = true
      100.millis
    }

    def attribute: Attribute[String] = {
      allocated = true
      Attribute("key", "value")
    }

    def attributes: List[Attribute[String]] = {
      allocated = true
      List(Attribute("key", "value"))
    }

    def spanContext: SpanContext = {
      allocated = true
      SpanContext.invalid
    }

    def finalizationStrategy: SpanFinalizer.Strategy = {
      allocated = true
      SpanFinalizer.Strategy.empty
    }

    def spanKind: SpanKind = {
      allocated = true
      SpanKind.Client
    }

    tracer
      .spanBuilder("span")
      .addAttribute(attribute)
      .addAttributes(attribute, attribute)
      .addAttributes(attributes)
      .addLink(spanContext, attribute, attribute)
      .addLink(spanContext, attributes)
      .withParent(spanContext)
      .withFinalizationStrategy(finalizationStrategy)
      .withSpanKind(spanKind)
      .withStartTimestamp(timestamp)
      .addAttribute(attribute)
      .addAttributes(attribute, attribute)
      .addAttributes(attributes)
      .addLink(spanContext, attribute, attribute)
      .addLink(spanContext, attributes)
      .withParent(spanContext)
      .withFinalizationStrategy(finalizationStrategy)
      .withSpanKind(spanKind)
      .withStartTimestamp(timestamp)
      .addAttribute(attribute)
      .addAttributes(attribute, attribute)
      .addAttributes(attributes)
      .addLink(spanContext, attribute, attribute)
      .addLink(spanContext, attributes)
      .withParent(spanContext)
      .withFinalizationStrategy(finalizationStrategy)
      .withSpanKind(spanKind)
      .withStartTimestamp(timestamp)
      .addAttribute(attribute)
      .addAttributes(attribute, attribute)
      .addAttributes(attributes)
      .addLink(spanContext, attribute, attribute)
      .addLink(spanContext, attributes)
      .withParent(spanContext)
      .withFinalizationStrategy(finalizationStrategy)
      .withSpanKind(spanKind)
      .withStartTimestamp(timestamp)
      .build

    assert(!allocated)
  }

  test("store changes") {
    val builder =
      InMemoryBuilder(InstrumentMeta.enabled, SpanBuilder.State.init)

    val attribute1 = Attribute("key1", "value")
    val attribute2 = Attribute("key2", 1L)
    val attribute3 = Attribute("key3", false)
    val ctx = SpanContext.invalid
    val kind = SpanKind.Client
    val timestamp = Duration.Zero
    val finalizer = SpanFinalizer.Strategy.reportAbnormal

    var b = builder
      .addAttribute(attribute1)
      .addLink(ctx, attribute1)

    b = b.addAttributes(attribute2, attribute3)
    b = b.withFinalizationStrategy(finalizer)

    val result =
      b.withSpanKind(kind)
        .withStartTimestamp(timestamp)
        .withParent(ctx)
        .asInstanceOf[InMemoryBuilder]

    val expected = SpanBuilder.State.init
      .addAttributes(Seq(attribute1, attribute2, attribute3))
      .withSpanKind(kind)
      .withFinalizationStrategy(finalizer)
      .addLink(ctx, Seq(attribute1))
      .withStartTimestamp(timestamp)
      .withParent(SpanBuilder.Parent.explicit(ctx))

    assertEquals(result.state, expected)
    assertEquals(result.modifications, 4) // we have 4 separate statements
  }

  test("addAttributes: eliminate empty varargs calls") {
    val builder = InMemoryBuilder(InstrumentMeta.enabled, SpanBuilder.State.init)
    val result = builder.addAttributes().asInstanceOf[InMemoryBuilder]

    assertEquals(result.modifications, 0)
  }

  case class InMemoryBuilder(
      meta: InstrumentMeta[IO],
      state: SpanBuilder.State,
      modifications: Int = 0
  ) extends SpanBuilder[IO] {
    def modifyState(
        f: SpanBuilder.State => SpanBuilder.State
    ): SpanBuilder[IO] =
      copy(state = f(state), modifications = modifications + 1)

    def build: SpanOps[IO] =
      ???
  }

}
