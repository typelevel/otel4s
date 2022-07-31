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

package org.typelevel.otel4s.java.trace

import cats.effect.IO
import cats.effect.Resource
import cats.effect.testkit.TestControl
import cats.syntax.functor._
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.sdk.common.InstrumentationScopeInfo
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.testing.time.TestClock
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder
import io.opentelemetry.sdk.trace.SpanLimits
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.data.StatusData
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.internal.data.ExceptionEventData
import munit.CatsEffectSuite
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.trace.Tracer

import java.time.Instant
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.util.control.NoStackTrace

class TracerSuite extends CatsEffectSuite {

  test("propagate instrumentation info") {
    val expected = InstrumentationScopeInfo.create(
      "tracer",
      "1.0",
      "https://localhost:8080"
    )

    for {
      sdk <- makeSdk()
      tracer <- sdk.provider
        .tracer("tracer")
        .withVersion("1.0")
        .withSchemaUrl("https://localhost:8080")
        .get

      _ <- tracer.span("span").use_

      spans <- sdk.finishedSpans
    } yield assertEquals(
      spans.map(_.getInstrumentationScopeInfo),
      List(expected)
    )
  }

  test("propagate traceId and spanId") {
    for {
      sdk <- makeSdk()
      tracer <- sdk.provider.tracer("tracer").get
      _ <- tracer.currentSpanContext.assertEquals(None)
      result <- tracer.span("span").use { span =>
        for {
          _ <- tracer.currentSpanContext.assertEquals(span.context)
          span2 <- tracer.span("span-2").use { span2 =>
            for {
              _ <- tracer.currentSpanContext.assertEquals(span2.context)
            } yield span2
          }
        } yield (span, span2)
      }
      spans <- sdk.finishedSpans
    } yield {
      val (span, span2) = result
      assertEquals(
        span.context.map(_.traceIdHex),
        span2.context.map(_.traceIdHex)
      )
      assertEquals(
        spans.map(_.getTraceId),
        List(span2.context, span.context).flatten.map(_.traceIdHex)
      )
      assertEquals(
        spans.map(_.getSpanId),
        List(span2.context, span.context).flatten.map(_.spanIdHex)
      )
    }
  }

  test("propagate attributes") {
    val attribute = Attribute(AttributeKey.string("string-attribute"), "value")

    for {
      sdk <- makeSdk()
      tracer <- sdk.provider.tracer("tracer").get
      span <- tracer.span("span", attribute).use(IO.pure)
      spans <- sdk.finishedSpans
    } yield {
      assertEquals(
        spans.map(_.getTraceId),
        List(span.context.map(_.traceIdHex)).flatten
      )
      assertEquals(
        spans.map(_.getSpanId),
        List(span.context.map(_.spanIdHex)).flatten
      )
    }
  }

  test("automatically start and stop span") {
    val sleepDuration = 500.millis

    TestControl.executeEmbed {
      for {
        sdk <- makeSdk()
        tracer <- sdk.provider.tracer("tracer").get
        now <- IO.monotonic.delayBy(1.millis) // otherwise returns 0
        _ <- tracer.span("span").surround(IO.sleep(sleepDuration))
        spans <- sdk.finishedSpans
      } yield {
        assertEquals(spans.map(_.getStartEpochNanos), List(now.toNanos))
        assertEquals(
          spans.map(_.getEndEpochNanos),
          List(now.plus(sleepDuration).toNanos)
        )
      }
    }
  }

  test("set error status on abnormal termination (canceled)") {
    for {
      sdk <- makeSdk()
      tracer <- sdk.provider.tracer("tracer").get
      fiber <- tracer.span("span").surround(IO.canceled).start
      _ <- fiber.joinWith(IO.unit)
      spans <- sdk.finishedSpans
    } yield {
      assertEquals(
        spans.map(_.getStatus),
        List(StatusData.create(StatusCode.ERROR, "canceled"))
      )
      assertEquals(spans.map(_.getEvents.isEmpty), List(true))
    }
  }

  test("set error status on abnormal termination (exception)") {
    val exception = new RuntimeException("error") with NoStackTrace

    def expected(epoch: Long) =
      ExceptionEventData.create(
        SpanLimits.getDefault,
        epoch,
        exception,
        Attributes.empty()
      )

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        sdk <- makeSdk(
          _.setClock(TestClock.create(Instant.ofEpochMilli(now.toMillis)))
        )
        tracer <- sdk.provider.tracer("tracer").get
        _ <- tracer.span("span").surround(IO.raiseError(exception)).attempt
        spans <- sdk.finishedSpans
      } yield {
        assertEquals(spans.map(_.getStatus), List(StatusData.error()))
        assertEquals(
          spans.map(_.getEvents.asScala.toList),
          List(List(expected(now.toNanos)))
        )
      }
    }
  }

  test("create root span explicitly") {
    for {
      sdk <- makeSdk()
      tracer <- sdk.provider.tracer("tracer").get
      result <- tracer.span("span").use { span =>
        tracer.rootSpan("root-span").use(IO.pure).tupleRight(span)
      }
      spans <- sdk.finishedSpans
    } yield {
      val (rootSpan, span) = result
      assertNotEquals(
        rootSpan.context.map(_.spanIdHex),
        span.context.map(_.spanIdHex)
      )
      assertEquals(
        spans.map(_.getTraceId),
        List(rootSpan.context, span.context).flatten.map(_.traceIdHex)
      )
      assertEquals(
        spans.map(_.getSpanId),
        List(rootSpan.context, span.context).flatten.map(_.spanIdHex)
      )
    }
  }

  test("create a new root scope") {
    for {
      sdk <- makeSdk()
      tracer <- sdk.provider.tracer("tracer").get
      _ <- tracer.currentSpanContext.assertEquals(None)
      result <- tracer.span("span").use { span =>
        for {
          _ <- tracer.currentSpanContext.assertEquals(span.context)
          span2 <- tracer.rootScope.surround {
            for {
              _ <- tracer.currentSpanContext.assertEquals(None)
              // a new root span should be created
              span2 <- tracer.span("span-2").use { span2 =>
                for {
                  _ <- tracer.currentSpanContext.assertEquals(span2.context)
                } yield span2
              }
            } yield span2
          }
        } yield (span, span2)
      }
      spans <- sdk.finishedSpans
    } yield {
      val (span, span2) = result
      assertNotEquals(
        span.context.map(_.traceIdHex),
        span2.context.map(_.traceIdHex)
      )
      assertEquals(
        spans.map(_.getTraceId),
        List(span2.context, span.context).flatten.map(_.traceIdHex)
      )
      assertEquals(
        spans.map(_.getSpanId),
        List(span2.context, span.context).flatten.map(_.spanIdHex)
      )
    }
  }

  test("create a no-op scope") {
    for {
      sdk <- makeSdk()
      tracer <- sdk.provider.tracer("tracer").get
      _ <- tracer.currentSpanContext.assertEquals(None)
      result <- tracer.span("span").use { span =>
        for {
          _ <- tracer.currentSpanContext.assertEquals(span.context)
          span2 <- tracer.noopScope.surround {
            for {
              _ <- tracer.currentSpanContext.assertEquals(None)
              // a new root span should be created
              span2 <- tracer.span("span-2").use { span2 =>
                for {
                  _ <- tracer.currentSpanContext.assertEquals(None)
                } yield span2
              }
            } yield span2
          }
        } yield (span, span2)
      }
      spans <- sdk.finishedSpans
    } yield {
      val (span, span2) = result
      assertNotEquals(
        span.context.map(_.traceIdHex),
        span2.context.map(_.traceIdHex)
      )
      assertEquals(
        spans.map(_.getTraceId),
        List(span2.context, span.context).flatten.map(_.traceIdHex)
      )
      assertEquals(
        spans.map(_.getSpanId),
        List(span2.context, span.context).flatten.map(_.spanIdHex)
      )
    }
  }

  test("create a tracer with a custom child parent") {

    def expected(now: FiniteDuration) =
      SpanNode(
        "span",
        now,
        now,
        List(
          SpanNode("span-3", now, now, Nil),
          SpanNode("span-2", now, now, Nil)
        )
      )

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        sdk <- makeSdk()
        tracer <- sdk.provider.tracer("tracer").get
        _ <- tracer.currentSpanContext.assertEquals(None)
        _ <- tracer.span("span").use { span =>
          for {
            _ <- tracer.currentSpanContext.assertEquals(span.context)
            _ <- tracer.span("span-2").use { span2 =>
              for {
                _ <- tracer.currentSpanContext.assertEquals(span2.context)
                _ <- tracer.childOf(span).span("span-3").use { span3 =>
                  for {
                    _ <- tracer.currentSpanContext.assertEquals(span3.context)
                  } yield span3
                }
              } yield ()
            }
          } yield ()
        }
        spans <- sdk.finishedSpans
        tree <- IO.pure(SpanNode.fromSpans(spans))
        // _ <- IO.println(SpanNode.render(tree.head))
      } yield assertEquals(tree, List(expected(now)))
    }
  }

  test("trace resource") {
    val attribute = Attribute(AttributeKey.string("string-attribute"), "value")

    val acquireInnerDuration = 25.millis
    val body1Duration = 100.millis
    val body2Duration = 200.millis
    val body3Duration = 50.millis
    val releaseDuration = 300.millis

    def mkRes(tracer: Tracer[IO]): Resource[IO, Unit] =
      Resource
        .make(
          tracer.span("acquire-inner").surround(IO.sleep(acquireInnerDuration))
        )(_ => IO.sleep(releaseDuration))

    def expected(start: FiniteDuration) = {
      val acquireEnd = start + acquireInnerDuration

      val (body1Start, body1End) = (acquireEnd, acquireEnd + body1Duration)
      val (body2Start, body2End) = (body1End, body1End + body2Duration)
      val (body3Start, body3End) = (body2End, body2End + body3Duration)

      val end = body3End + releaseDuration

      SpanNode(
        "resource-span",
        start,
        end,
        List(
          SpanNode(
            "acquire",
            start,
            acquireEnd,
            List(
              SpanNode(
                "acquire-inner",
                start,
                acquireEnd,
                Nil
              )
            )
          ),
          SpanNode(
            "use",
            body1Start,
            body3End,
            List(
              SpanNode("body-1", body1Start, body1End, Nil),
              SpanNode("body-2", body2Start, body2End, Nil),
              SpanNode("body-3", body3Start, body3End, Nil)
            )
          ),
          SpanNode(
            "release",
            body3End,
            end,
            Nil
          )
        )
      )
    }

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        sdk <- makeSdk()
        tracer <- sdk.provider.tracer("tracer").get
        _ <- tracer
          .resourceSpan("resource-span", attribute)(mkRes(tracer))
          .use { _ =>
            for {
              _ <- tracer.span("body-1").surround(IO.sleep(100.millis))
              _ <- tracer.span("body-2").surround(IO.sleep(200.millis))
              _ <- tracer.span("body-3").surround(IO.sleep(50.millis))
            } yield ()
          }
        spans <- sdk.finishedSpans
        tree <- IO.pure(SpanNode.fromSpans(spans))
        // _ <- IO.println(SpanNode.render(tree.head))
      } yield assertEquals(tree, List(expected(now)))
    }
  }

  private def makeSdk(
      customize: SdkTracerProviderBuilder => SdkTracerProviderBuilder = identity
  ): IO[TracerSuite.Sdk] = {
    val exporter = InMemorySpanExporter.create()

    val builder = SdkTracerProvider
      .builder()
      .addSpanProcessor(SimpleSpanProcessor.create(exporter))

    val tracerProvider: SdkTracerProvider =
      customize(builder).build()

    for {
      provider <- TracerProviderImpl.ioLocal[IO](tracerProvider)
    } yield new TracerSuite.Sdk(provider, exporter)
  }

}

object TracerSuite {

  class Sdk(
      val provider: TracerProviderImpl[IO],
      exporter: InMemorySpanExporter
  ) {

    def finishedSpans: IO[List[SpanData]] =
      IO.delay(exporter.getFinishedSpanItems.asScala.toList)

  }

}
