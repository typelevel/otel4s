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

package org.typelevel.otel4s.oteljava.trace

import cats.effect.IO
import cats.effect.Resource
import cats.effect.testkit.TestControl
import io.opentelemetry.api.common.{AttributeKey => JAttributeKey}
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.sdk.common.InstrumentationScopeInfo
import io.opentelemetry.sdk.internal.AttributesMap
import io.opentelemetry.sdk.testing.time.TestClock
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder
import io.opentelemetry.sdk.trace.SpanLimits
import io.opentelemetry.sdk.trace.data.ExceptionEventData
import io.opentelemetry.sdk.trace.data.StatusData
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.oteljava.AttributeConverters._
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.context.propagation.PropagatorConverters._
import org.typelevel.otel4s.oteljava.testkit.trace.TracesTestkit
import org.typelevel.otel4s.trace.BaseTracerSuite
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.TracerProvider

import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.util.control.NoStackTrace

class TracerSuite extends BaseTracerSuite[Context, Context.Key] {
  import BaseTracerSuite.SpanDataWrapper

  type SpanData = io.opentelemetry.sdk.trace.data.SpanData
  type Builder = SdkTracerProviderBuilder

  sdkTest("propagate instrumentation info") { sdk =>
    val expected = InstrumentationScopeInfo
      .builder("tracer")
      .setVersion("1.0")
      .setSchemaUrl("https://localhost:8080")
      .build()

    for {
      tracer <- sdk.provider
        .tracer("tracer")
        .withVersion("1.0")
        .withSchemaUrl("https://localhost:8080")
        .get

      _ <- tracer.span("span").use_

      spans <- sdk.finishedSpans
    } yield assertEquals(
      spans.map(_.underlying.getInstrumentationScopeInfo),
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
        spans.map(_.underlying.getStatus),
        List(StatusData.create(StatusCode.ERROR, "canceled"))
      )
      assertEquals(spans.map(_.underlying.getEvents.isEmpty), List(true))
    }
  }

  sdkTest(
    "set error status on abnormal termination (exception)",
    _.setClock(TestClock.create(Instant.ofEpochMilli(1L)))
  ) { sdk =>
    val limits = SpanLimits.getDefault
    val exception = new RuntimeException("error") with NoStackTrace

    val stringWriter = new StringWriter()
    val printWriter = new PrintWriter(stringWriter)
    exception.printStackTrace(printWriter)

    val attributes = AttributesMap.create(
      limits.getMaxNumberOfAttributes.toLong,
      limits.getMaxAttributeValueLength
    )

    attributes.put(JAttributeKey.stringKey("exception.message"), exception.getMessage)
    attributes.put(JAttributeKey.stringKey("exception.stacktrace"), stringWriter.toString)

    def expected(epoch: Long) =
      ExceptionEventData.create(
        epoch,
        exception,
        attributes,
        attributes.size()
      )

    TestControl.executeEmbed {
      for {
        now <- IO.monotonic.delayBy(1.milli) // otherwise returns 0
        tracer <- sdk.provider.get("tracer")
        _ <- tracer.span("span").surround(IO.raiseError(exception)).attempt
        spans <- sdk.finishedSpans
      } yield {
        assertEquals(
          spans.map(_.underlying.getStatus),
          List(StatusData.error())
        )
        assertEquals(
          spans.map(_.underlying.getEvents.asScala.toList),
          List(List(expected(now.toNanos)))
        )
      }
    }
  }

  protected def makeSdk(
      configure: Builder => Builder,
      additionalPropagators: List[TextMapPropagator[Context]]
  ): Resource[IO, BaseTracerSuite.Sdk[SpanData]] =
    TracesTestkit
      .inMemory[IO](
        configure,
        textMapPropagators = W3CTraceContextPropagator
          .getInstance() :: additionalPropagators.map(_.asJava)
      )
      .map { traces =>
        new BaseTracerSuite.Sdk[SpanData] {
          def provider: TracerProvider[IO] =
            traces.tracerProvider

          def finishedSpans: IO[List[SpanDataWrapper[SpanData]]] =
            traces.finishedSpans[SpanData].map(_.map(toWrapper))
        }
      }

  private def toWrapper(sd: SpanData): SpanDataWrapper[SpanData] =
    new SpanDataWrapper[SpanData] {
      def underlying: SpanData = sd

      def name: String = sd.getName

      def spanContext: SpanContext =
        SpanContextConversions.toScala(sd.getSpanContext)

      def parentSpanContext: Option[SpanContext] =
        Option.when(sd.getParentSpanContext.isValid)(
          SpanContextConversions.toScala(sd.getParentSpanContext)
        )

      def attributes: Attributes =
        sd.getAttributes.toScala

      def startTimestamp: FiniteDuration =
        sd.getStartEpochNanos.nanos

      def endTimestamp: Option[FiniteDuration] =
        Option.when(sd.hasEnded)(sd.getEndEpochNanos.nanos)
    }
}
