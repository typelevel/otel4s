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

package org.typelevel.otel4s
package sdk
package trace

import cats.effect.IO
import cats.effect.std.Queue
import cats.effect.testkit.TestControl
import cats.syntax.monoid._
import cats.syntax.traverse._
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import munit.internal.PlatformCompat
import org.scalacheck.Test
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.data.LimitedData
import org.typelevel.otel4s.sdk.internal.Diagnostic
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.sdk.trace.processor.SpanProcessor
import org.typelevel.otel4s.sdk.trace.scalacheck.Arbitraries._
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.StatusCode

import scala.concurrent.duration._

class SdkSpanBackendSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private implicit val noopDiagnostic: Diagnostic[IO] = Diagnostic.noop

  private val spanLimits = Defaults.spanLimits

  // Span.Backend methods

  test(".addAttributes(:Attribute[_]*)") {
    PropF.forAllF { (attributes: Attributes, nextAttributes: Attributes) =>
      val expected = attributes |+| nextAttributes

      for {
        span <- start(
          attributes = attributes,
          spanLimits = SpanLimits.builder
            .withMaxNumberOfAttributes(attributes.size + nextAttributes.size)
            .build
        )
        _ <- assertIO(span.toSpanData.map(_.attributes.elements), attributes)
        _ <- span.addAttributes(nextAttributes)
        _ <- assertIO(span.toSpanData.map(_.attributes.elements), expected)
      } yield ()
    }
  }

  test(".addEvent(:String, :Attribute[_]*)") {
    PropF.forAllF { (name: String, attributes: Attributes) =>
      val event = EventData(
        name,
        Duration.Zero,
        LimitedData
          .attributes(
            spanLimits.maxNumberOfAttributesPerEvent,
            spanLimits.maxAttributeValueLength
          )
          .appendAll(attributes)
      )

      TestControl.executeEmbed {
        for {
          span <- start()
          _ <- assertIO(span.toSpanData.map(_.events.elements), Vector.empty)
          _ <- span.addEvent(name, attributes)
          _ <- assertIO(span.toSpanData.map(_.events.elements), Vector(event))
        } yield ()
      }
    }
  }

  test(".addEvent(:String, :FiniteDuration, :Attribute[_]*)") {
    PropF.forAllF { (name: String, ts: FiniteDuration, attrs: Attributes) =>
      val event = EventData(
        name,
        ts,
        LimitedData
          .attributes(
            spanLimits.maxNumberOfAttributesPerEvent,
            spanLimits.maxAttributeValueLength
          )
          .appendAll(attrs)
      )

      TestControl.executeEmbed {
        for {
          span <- start()
          _ <- assertIO(span.toSpanData.map(_.events.elements), Vector.empty)
          _ <- span.addEvent(name, ts, attrs)
          _ <- assertIO(span.toSpanData.map(_.events.elements), Vector(event))
        } yield ()
      }
    }
  }

  test(".addLink(:SpanContext, :Attribute[_]*)") {
    PropF.forAllF { (spanContext: SpanContext, attrs: Attributes) =>
      val link = LinkData(
        spanContext,
        LimitedData
          .attributes(
            spanLimits.maxNumberOfAttributesPerLink,
            spanLimits.maxAttributeValueLength
          )
          .appendAll(attrs)
      )

      TestControl.executeEmbed {
        for {
          span <- start()
          _ <- assertIO(span.toSpanData.map(_.links.elements), Vector.empty)
          _ <- span.addLink(spanContext, attrs)
          _ <- assertIO(span.toSpanData.map(_.links.elements), Vector(link))
        } yield ()
      }
    }
  }

  test(".updateName(:String)") {
    PropF.forAllF { (name: String, nextName: String) =>
      for {
        span <- start(name = name)
        _ <- assertIO(span.name, name)
        _ <- span.updateName(nextName)
        _ <- assertIO(span.name, nextName)
      } yield ()
    }
  }

  test(".recordException(:Exception, :Attribute[_]*)") {
    PropF.forAllF { (message: String, attributes: Attributes) =>
      val exception = new RuntimeException(message)
      val event = EventData.fromException(
        timestamp = Duration.Zero,
        exception = exception,
        attributes = LimitedData
          .attributes(
            spanLimits.maxNumberOfAttributesPerEvent,
            spanLimits.maxAttributeValueLength
          )
          .appendAll(attributes)
      )

      TestControl.executeEmbed {
        for {
          span <- start()
          _ <- assertIO(span.toSpanData.map(_.events.elements), Vector.empty)
          _ <- span.recordException(exception, attributes)
          _ <- assertIO(span.toSpanData.map(_.events.elements), Vector(event))
        } yield ()
      }
    }
  }

  test(".setStatus(:Status)") {
    PropF.forAllF { (status: StatusCode) =>
      for {
        span <- start()
        _ <- assertIO(span.toSpanData.map(_.status), StatusData.Unset)
        _ <- span.setStatus(status)
        _ <- assertIO(span.toSpanData.map(_.status), StatusData(status))
      } yield ()
    }
  }

  test(".setStatus(:Status, :String)") {
    PropF.forAllF { (status: StatusCode, desc: String) =>
      for {
        span <- start()
        _ <- assertIO(span.toSpanData.map(_.status), StatusData.Unset)
        _ <- span.setStatus(status, desc)
        _ <- assertIO(span.toSpanData.map(_.status), StatusData(status, desc))
      } yield ()
    }
  }

  test(".end - set an automatic end time") {
    TestControl.executeEmbed {
      for {
        span <- start()
        _ <- IO.sleep(100.millis)
        _ <- span.end
        _ <- assertIO(span.toSpanData.map(_.endTimestamp), Some(100.millis))
        _ <- assertIOBoolean(span.hasEnded)
      } yield ()
    }
  }

  test(".end(:FiniteDuration) - set an explicit time") {
    PropF.forAllF { (end: FiniteDuration) =>
      TestControl.executeEmbed {
        for {
          span <- start()
          _ <- span.end(end)
          _ <- assertIO(span.toSpanData.map(_.endTimestamp), Some(end))
          _ <- assertIOBoolean(span.hasEnded)
        } yield ()
      }
    }
  }

  test(".context") {
    PropF.forAllF { (context: SpanContext) =>
      TestControl.executeEmbed {
        for {
          span <- start(context = context)
        } yield assertEquals(span.context, context)
      }
    }
  }

  test(".duration - span not ended - return currently elapsed time") {
    TestControl.executeEmbed {
      for {
        span <- start()
        _ <- IO.sleep(100.millis)
        duration <- span.duration
      } yield assertEquals(duration, 100.millis)
    }
  }

  test(".duration - span ended - return duration") {
    TestControl.executeEmbed {
      for {
        span <- start()
        _ <- IO.sleep(125.millis)
        _ <- span.end
        _ <- IO.sleep(200.millis)
        duration <- span.duration
      } yield assertEquals(duration, 125.millis)
    }
  }

  test(".meta - always enabled") {
    for {
      span <- start()
    } yield assertEquals(span.meta.isEnabled, true)
  }

  // SpanRef methods

  test(".getAttribute(:AttributeKey)") {
    PropF.forAllF { (init: Attributes, extraAttrs: Attributes) =>
      // 'init' and 'extra' may have attributes under the same key. we need only unique keys in extra
      val extra = {
        val initMap = init.toMap
        extraAttrs.filterNot(a => initMap.contains(a.key.name))
      }

      for {
        span <- start(
          attributes = init,
          spanLimits = SpanLimits.builder
            .withMaxNumberOfAttributes(init.size + extraAttrs.size)
            .build
        )

        _ <- assertIO(
          init.toList.traverse(a => span.getAttribute(a.key)),
          init.toList.map(v => Some(v.value))
        )

        _ <- assertIO(
          extra.toList.traverse(a => span.getAttribute(a.key)),
          List.fill(extra.size)(None)
        )

        // add attributes
        _ <- span.addAttributes(extra)

        _ <- assertIO(
          init.toList.traverse(a => span.getAttribute(a.key)),
          init.toList.map(v => Some(v.value))
        )

        _ <- assertIO(
          extra.toList.traverse(a => span.getAttribute(a.key)),
          extra.toList.map(v => Some(v.value))
        )
      } yield ()
    }
  }

  test(".hasEnded - span not ended") {
    for {
      span <- start()
      hasEnded <- span.hasEnded
    } yield assertEquals(hasEnded, false)
  }

  test(".hasEnded - span ended") {
    for {
      span <- start()
      _ <- span.end
      hasEnded <- span.hasEnded
    } yield assertEquals(hasEnded, true)
  }

  test(".kind") {
    PropF.forAllF { (kind: SpanKind) =>
      for {
        span <- start(kind = kind)
      } yield assertEquals(span.kind, kind)
    }
  }

  test(".parentSpanContext") {
    PropF.forAllF { (context: Option[SpanContext]) =>
      for {
        span <- start(parentSpanContext = context)
      } yield assertEquals(span.parentSpanContext, context)
    }
  }

  test(".scopeInfo") {
    PropF.forAllF { (scopeInfo: InstrumentationScope) =>
      for {
        span <- start(scope = scopeInfo)
      } yield assertEquals(span.scopeInfo, scopeInfo)
    }
  }

  test(".toSpanData") {
    PropF.forAllF {
      (
          ctx: SpanContext,
          name: String,
          scope: InstrumentationScope,
          kind: SpanKind,
          parentCtx: Option[SpanContext],
          attributes: Attributes,
          links: Vector[LinkData],
          userStartTimestamp: Option[FiniteDuration]
      ) =>
        def expected(end: Option[FiniteDuration]) =
          SpanData(
            name = name,
            spanContext = ctx,
            parentSpanContext = parentCtx,
            kind = kind,
            startTimestamp = userStartTimestamp.getOrElse(Duration.Zero),
            endTimestamp = end,
            status = StatusData.Unset,
            attributes = LimitedData
              .attributes(
                spanLimits.maxNumberOfAttributes,
                spanLimits.maxAttributeValueLength
              )
              .appendAll(attributes),
            events = LimitedData.vector[EventData](spanLimits.maxNumberOfEvents),
            links = LimitedData.vector[LinkData](spanLimits.maxNumberOfLinks).appendAll(links),
            instrumentationScope = scope,
            resource = Defaults.resource
          )

        TestControl.executeEmbed {
          for {
            spanStorage <- SpanStorage.create[IO]
            span <- SdkSpanBackend.start[IO](
              ctx,
              name,
              scope,
              Defaults.resource,
              kind,
              parentCtx,
              spanLimits,
              Defaults.spanProcessor,
              spanStorage,
              LimitedData
                .attributes(
                  spanLimits.maxNumberOfAttributes,
                  spanLimits.maxAttributeValueLength
                )
                .appendAll(attributes),
              LimitedData.vector[LinkData](spanLimits.maxNumberOfLinks).appendAll(links),
              userStartTimestamp
            )
            _ <- assertIO(span.toSpanData, expected(None))
            _ <- IO.sleep(125.millis)
            _ <- span.end
            _ <- assertIO(span.toSpanData, expected(Some(125.millis)))
          } yield ()
        }
    }
  }

  // Lifecycle

  test("lifecycle: call span processor on start and end") {
    PropF.forAllF { (ctx: SpanContext) =>
      for {
        onStart <- Queue.unbounded[IO, SpanData]
        onEnd <- Queue.unbounded[IO, SpanData]

        span <- start(
          context = ctx,
          spanProcessor = startEndRecorder(onStart, onEnd)
        )

        startedWith <- span.toSpanData
        _ <- assertIO(onStart.tryTakeN(None), List(startedWith))

        _ <- span.end

        endedWith <- span.toSpanData
        _ <- assertIO(onEnd.tryTakeN(None), List(endedWith))

        // calling end for the second time, the processor shouldn't be invoked
        _ <- span.end
        _ <- assertIO(onEnd.tryTakeN(None), Nil)
      } yield ()
    }
  }

  test("lifecycle: ignore modifications once the span has ended") {
    PropF.forAllF { (name: String, code: StatusCode, attributes: Attributes) =>
      def expected(end: Option[FiniteDuration]) =
        SpanData(
          name = Defaults.name,
          spanContext = Defaults.context,
          parentSpanContext = None,
          kind = Defaults.kind,
          startTimestamp = Duration.Zero,
          endTimestamp = end,
          status = StatusData.Unset,
          attributes = LimitedData
            .attributes(
              spanLimits.maxNumberOfAttributes,
              spanLimits.maxAttributeValueLength
            )
            .appendAll(Defaults.attributes),
          events = LimitedData.vector[EventData](spanLimits.maxNumberOfEvents),
          links = LimitedData.vector[LinkData](spanLimits.maxNumberOfLinks),
          instrumentationScope = Defaults.scope,
          resource = Defaults.resource
        )

      TestControl.executeEmbed {
        for {
          span <- start()
          _ <- assertIO(span.toSpanData, expected(None))

          _ <- IO.sleep(125.millis)
          _ <- span.end
          _ <- assertIO(span.toSpanData, expected(Some(125.millis)))

          // should have zero effect
          _ <- IO.sleep(125.millis)
          _ <- span.updateName(name)
          _ <- span.addAttributes(attributes)
          _ <- span.addEvent("event", Nil)
          _ <- span.setStatus(code)
          _ <- span.end

          _ <- assertIO(span.toSpanData, expected(Some(125.millis)))
        } yield ()
      }
    }
  }

  private def startEndRecorder(start: Queue[IO, SpanData], end: Queue[IO, SpanData]): SpanProcessor[IO] =
    new SpanProcessor.Unsealed[IO] {
      val name: String = "InMemorySpanProcessor"

      val onStart: SpanProcessor.OnStart[IO] =
        SpanProcessor.OnStart((_, span) => span.toSpanData.flatMap(d => start.offer(d)))

      val onEnd: SpanProcessor.OnEnd[IO] =
        SpanProcessor.OnEnd(end.offer)

      def forceFlush: IO[Unit] =
        IO.unit
    }

  private def start(
      context: SpanContext = Defaults.context,
      name: String = Defaults.name,
      scope: InstrumentationScope = Defaults.scope,
      resource: TelemetryResource = Defaults.resource,
      kind: SpanKind = Defaults.kind,
      parentSpanContext: Option[SpanContext] = None,
      attributes: Attributes = Defaults.attributes,
      spanLimits: SpanLimits = spanLimits,
      spanProcessor: SpanProcessor[IO] = Defaults.spanProcessor,
      links: Vector[LinkData] = Vector.empty,
      userStartTimestamp: Option[FiniteDuration] = None
  ): IO[SdkSpanBackend[IO]] = SpanStorage.create[IO].flatMap { spanStorage =>
    SdkSpanBackend.start[IO](
      context = context,
      name = name,
      scopeInfo = scope,
      resource = resource,
      kind = kind,
      parentContext = parentSpanContext,
      spanLimits = spanLimits,
      processor = spanProcessor,
      spanStorage = spanStorage,
      attributes = LimitedData
        .attributes(
          spanLimits.maxNumberOfAttributes,
          spanLimits.maxAttributeValueLength
        )
        .appendAll(attributes),
      links = LimitedData.vector[LinkData](spanLimits.maxNumberOfLinks).appendAll(links),
      userStartTimestamp = userStartTimestamp
    )
  }

  private object Defaults {
    val context = SpanContext.invalid
    val name = "span name"
    val scope = InstrumentationScope.builder("otel4s").build
    val resource = TelemetryResource.default
    val kind = SpanKind.Client
    val attributes = Attributes.empty
    val spanLimits = SpanLimits.default
    val spanProcessor = SpanProcessor.noop[IO]
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    if (PlatformCompat.isJVM)
      super.scalaCheckTestParameters
    else
      super.scalaCheckTestParameters
        .withMinSuccessfulTests(10)
        .withMaxSize(10)
}
