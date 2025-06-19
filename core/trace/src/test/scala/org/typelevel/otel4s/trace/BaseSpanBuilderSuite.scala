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
import cats.effect.Resource
import munit.CatsEffectSuite
import munit.Location
import munit.TestOptions
import org.typelevel.otel4s.Attribute

import scala.concurrent.duration._

abstract class BaseSpanBuilderSuite extends CatsEffectSuite {

  builderTest("do not allocate attributes when instrument is disabled", enabled = false) { builder =>
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
    val ops = builder
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

    for {
      _ <- builder.meta.isEnabled.assertEquals(false)
      _ <- ops.use_
    } yield assert(!allocated)
  }

  builderTest("macro should work with non-chained calls", enabled = false) { builder =>
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

    var b = builder

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

    val ops = b.build

    for {
      _ <- builder.meta.isEnabled.assertEquals(false)
      _ <- ops.use_
    } yield assert(!allocated)
  }

  // if the optimization is not applied, the compilation will fail with
  // Method too large: org/typelevel/otel4s/...
  // Scala 3 compiler optimizes chained calls out of the box
  // But Scala 2 doesn't, see SpanBuilderMacro#whenEnabled for manual optimization logic
  builderTest("optimize chained calls", enabled = false) { builder =>
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

    val ops = builder
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

    for {
      _ <- builder.meta.isEnabled.assertEquals(false)
      _ <- ops.use_
    } yield assert(!allocated)
  }

  builderTest("should be stack-safe") { builder =>
    val attribute = Attribute("key1", "value")

    val r = 1.to(100_000).foldLeft(builder) { (b, _) =>
      b.addAttribute(attribute)
    }

    for {
      _ <- builder.meta.isEnabled.assertEquals(true)
      _ <- r.build.use_
    } yield ()
  }

  private def builderTest[A](
      options: TestOptions,
      enabled: Boolean = true,
  )(body: SpanBuilder[IO] => IO[A])(implicit loc: Location): Unit =
    test(options)(makeTracerProvider(enabled).use(_.get("tracer").flatMap(t => body(t.spanBuilder("span")))))

  protected def makeTracerProvider(enabled: Boolean): Resource[IO, TracerProvider[IO]]

}
