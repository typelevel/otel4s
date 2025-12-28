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

package org.typelevel.otel4s.sdk.trace

import cats.effect.IO
import cats.effect.Resource
import cats.effect.testkit.TestControl
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.data.LimitedData
import org.typelevel.otel4s.sdk.testkit.trace.TracesTestkit
import org.typelevel.otel4s.sdk.trace.context.propagation.W3CTraceContextPropagator
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.sdk.trace.samplers.Sampler
import org.typelevel.otel4s.trace.BaseTracerSuite
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.StatusCode
import org.typelevel.otel4s.trace.TracerProvider

import scala.concurrent.duration._
import scala.util.control.NoStackTrace

class SdkTracerSuite extends BaseTracerSuite[Context, Context.Key] {
  import BaseTracerSuite.SpanDataWrapper

  type SpanData = org.typelevel.otel4s.sdk.trace.data.SpanData
  type Builder = SdkTracerProvider.Builder[IO]

  sdkTest("propagate instrumentation info") { sdk =>
    val expected = InstrumentationScope
      .builder("tracer")
      .withVersion("1.0")
      .withSchemaUrl("https://localhost:8080")
      .build

    for {
      tracer <- sdk.provider
        .tracer("tracer")
        .withVersion("1.0")
        .withSchemaUrl("https://localhost:8080")
        .get

      _ <- tracer.span("span").use_

      spans <- sdk.finishedSpans
    } yield assertEquals(
      spans.map(_.underlying.instrumentationScope),
      List(expected)
    )
  }

  sdkTest("set error status on abnormal termination (canceled)") { sdk =>
    for {
      tracer <- sdk.provider.get("tracer")
      fiber <- tracer.span("span").surround(IO.canceled).start
      _ <- fiber.joinWith(IO.unit)
      spans <- sdk.finishedSpans
    } yield {
      assertEquals(
        spans.map(_.underlying.status),
        List(StatusData(StatusCode.Error, "canceled"))
      )
      assertEquals(spans.map(_.underlying.events.elements.isEmpty), List(true))
    }
  }

  sdkTest("set error status on abnormal termination (exception)") { sdk =>
    final case class Err(reason: String) extends RuntimeException(reason) with NoStackTrace

    val exception = Err("error")

    def expected(timestamp: FiniteDuration) =
      EventData.fromException(
        timestamp = timestamp,
        exception = exception,
        attributes = LimitedData.attributes(
          SpanLimits.default.maxNumberOfAttributesPerEvent,
          SpanLimits.default.maxAttributeValueLength
        )
      )

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.second) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.span("span").surround(IO.raiseError(exception)).attempt
        spans <- sdk.finishedSpans
      } yield {
        assertEquals(
          spans.map(_.underlying.status),
          List(StatusData.Error(None))
        )
        assertEquals(
          spans.map(_.underlying.events.elements),
          List(Vector(expected(now)))
        )
      }

    }
  }

  sdkTest(
    "keep propagating non-recording spans, but don't record them",
    _.withSampler(Sampler.alwaysOff)
  ) { sdk =>
    for {
      tracer <- sdk.provider.get("tracer")
      _ <- tracer.span("local").use { span1 =>
        for {
          _ <- IO(assertEquals(span1.context.isValid, true))
          _ <- IO(assertEquals(span1.context.isSampled, false))
          _ <- tracer.span("local-2").use { span2 =>
            for {
              _ <- IO(assertEquals(span2.context.isValid, true))
              _ <- IO(assertEquals(span2.context.isSampled, false))
              _ <- IO(
                assertEquals(span2.context.traceId, span1.context.traceId)
              )
            } yield ()
          }
        } yield ()
      }
      spans <- sdk.finishedSpans
    } yield assertEquals(spans, Nil)
  }

  protected def makeSdk(
      configure: Builder => Builder,
      additionalPropagators: List[TextMapPropagator[Context]]
  ): Resource[IO, BaseTracerSuite.Sdk[SpanData]] =
    TracesTestkit
      .builder[IO]
      .addTracerProviderCustomizer(
        configure.andThen(
          _.addTextMapPropagators(W3CTraceContextPropagator.default)
            .addTextMapPropagators(additionalPropagators: _*)
        )
      )
      .build
      .map { traces =>
        new BaseTracerSuite.Sdk[SpanData] {
          def provider: TracerProvider[IO] =
            traces.tracerProvider

          def finishedSpans: IO[List[SpanDataWrapper[SpanData]]] =
            traces.finishedSpans.map(_.map(toWrapper))
        }
      }

  private def toWrapper(sd: SpanData): SpanDataWrapper[SpanData] =
    new SpanDataWrapper[SpanData] {
      def underlying: SpanData = sd
      def name: String = sd.name
      def spanContext: SpanContext = sd.spanContext
      def parentSpanContext: Option[SpanContext] = sd.parentSpanContext
      def attributes: Attributes = sd.attributes.elements
      def startTimestamp: FiniteDuration = sd.startTimestamp
      def endTimestamp: Option[FiniteDuration] = sd.endTimestamp
    }
}
