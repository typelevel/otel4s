package org.typelevel.otel4s.java.trace

import cats.effect.IO
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

import java.time.Instant
import scala.concurrent.duration._
import scala.jdk.CollectionConverters._
import scala.util.control.NoStackTrace

class TracerSuite extends CatsEffectSuite {

  test("propagate instrumentation info") {
    val expected = InstrumentationScopeInfo.create(
      "java.otel.tracer",
      "1.0",
      "https://localhost:8080"
    )

    for {
      sdk <- makeSdk()
      tracer <- sdk.provider
        .tracer("java.otel.tracer")
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
      tracer <- sdk.provider.tracer("java.otel.tracer").get
      _ <- tracer.traceId.assertEquals(None)
      _ <- tracer.spanId.assertEquals(None)
      (span, span2) <- tracer.span("span").use { span =>
        for {
          _ <- tracer.traceId.assertEquals(Some(span.traceId))
          _ <- tracer.spanId.assertEquals(Some(span.spanId))
          span2 <- tracer.span("span-2").use { span2 =>
            for {
              _ <- tracer.traceId.assertEquals(Some(span2.traceId))
              _ <- tracer.spanId.assertEquals(Some(span2.spanId))
            } yield span2
          }
        } yield (span, span2)
      }
      spans <- sdk.finishedSpans
    } yield {
      assertEquals(span.traceId, span2.traceId)
      assertEquals(spans.map(_.getTraceId), List(span2.traceId, span.traceId))
      assertEquals(spans.map(_.getSpanId), List(span2.spanId, span.spanId))
    }
  }

  test("propagate attributes") {
    val attribute = Attribute(AttributeKey.string("string-attribute"), "value")

    for {
      sdk <- makeSdk()
      tracer <- sdk.provider.tracer("java.otel.tracer").get
      span <- tracer.span("span", attribute).use(IO.pure)
      spans <- sdk.finishedSpans
    } yield {
      assertEquals(spans.map(_.getTraceId), List(span.traceId))
      assertEquals(spans.map(_.getSpanId), List(span.spanId))
    }
  }

  test("automatically start and stop span") {
    val sleepDuration = 500.millis

    TestControl.executeEmbed {
      for {
        sdk <- makeSdk()
        tracer <- sdk.provider.tracer("java.otel.tracer").get
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
      tracer <- sdk.provider.tracer("java.otel.tracer").get
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
        tracer <- sdk.provider.tracer("java.otel.tracer").get
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
      tracer <- sdk.provider.tracer("java.otel.tracer").get
      (span, rootSpan) <- tracer.span("span").use { span =>
        tracer.rootSpan("root-span").use(IO.pure).tupleRight(span)
      }
      spans <- sdk.finishedSpans
    } yield {
      assertNotEquals(rootSpan.spanId, span.spanId)
      assertEquals(spans.map(_.getTraceId), List(span.traceId, rootSpan.traceId))
      assertEquals(spans.map(_.getSpanId), List(span.spanId, rootSpan.spanId))
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
      provider <- TraceProviderImpl.ioLocal[IO](tracerProvider)
    } yield new TracerSuite.Sdk(provider, exporter)
  }

}

object TracerSuite {

  class Sdk(
      val provider: TraceProviderImpl[IO],
      exporter: InMemorySpanExporter
  ) {

    def finishedSpans: IO[List[SpanData]] =
      IO.delay(exporter.getFinishedSpanItems.asScala.toList)

  }
}
